package dev.ftb.mods.ftbskies2aerocompanion.ship;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.HoldingSubLevel;
import dev.ryanhcode.sable.sublevel.storage.holding.SubLevelHoldingChunkMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ShipBindings {
    private static final Logger LOGGER = LoggerFactory.getLogger("ShipBindings");

    private ShipBindings() {}

    public static Optional<ShipBinding> captureForPlayer(Player player, Vec3 worldPos, float yaw, float pitch) {
        SubLevel sub = Sable.HELPER.getTrackingSubLevel(player);
        if (sub != null) {
            Pose3dc pose = sub.logicalPose();
            Vec3 local = pose.transformPositionInverse(worldPos);
            float subYawNow = (float) yawFromOrientation(pose.orientation());
            float storedYaw = yaw - subYawNow;
            LOGGER.debug("[capture] sable subLevel uuid={} local={} worldPos={} subYawNow={} storedDeltaYaw={}",
                    sub.getUniqueId(), local, worldPos, subYawNow, storedYaw);
            return Optional.of(new ShipBinding(sub.getUniqueId(), player.level().dimension(),
                    local, storedYaw, pitch, worldPos, false));
        }
        Entity root = player.getRootVehicle();
        if (root != player && root instanceof AbstractContraptionEntity contraption) {
            Vec3 local = contraption.toLocalVector(worldPos, 0f);
            LOGGER.debug("[capture] AbstractContraption fallback uuid={} local={}", contraption.getUUID(), local);
            return Optional.of(new ShipBinding(contraption.getUUID(), contraption.level().dimension(),
                    local, yaw, pitch, worldPos, false));
        }
        LOGGER.debug("[capture] player not on a sub-level or contraption — no binding");
        return Optional.empty();
    }

    public record Resolved(Vec3 worldPos, float yaw, float pitch) {}

    public static Optional<Vec3> resolveWorldPos(MinecraftServer server, ShipBinding binding) {
        return resolveAnchor(server, binding).map(Resolved::worldPos);
    }

    public static Optional<Resolved> resolveAnchor(MinecraftServer server, ShipBinding binding) {
        if (server == null) return Optional.empty();
        ServerLevel level = server.getLevel(binding.shipDimension());
        if (level == null) {
            LOGGER.warn("[resolveAnchor] dimension {} not loaded", binding.shipDimension().location());
            return Optional.empty();
        }

        BlockPos lastKnownBlock = BlockPos.containing(binding.lastKnownPos());
        BlockPos storageBlock = BlockPos.containing(binding.localOffset());
        ensureChunkLoaded(level, lastKnownBlock);
        ensureChunkLoaded(level, storageBlock);
        ensureChunkLoaded(level, storageBlock.east(16));
        ensureChunkLoaded(level, storageBlock.west(16));
        ensureChunkLoaded(level, storageBlock.north(16));
        ensureChunkLoaded(level, storageBlock.south(16));

        SubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container != null) {
            java.util.List<? extends SubLevel> all = container.getAllSubLevels();
            LOGGER.debug("[resolveAnchor] container has {} loaded sub-levels: {}",
                    all.size(),
                    all.stream().map(s -> s.getUniqueId() + "@" + s.logicalPose().position()).toList());
        }
        SubLevel sub = container == null ? null : container.getSubLevel(binding.shipUuid());
        if (sub == null && container != null) {
            for (SubLevel candidate : container.getAllSubLevels()) {
                if (binding.shipUuid().equals(candidate.getUniqueId())) {
                    sub = candidate;
                    LOGGER.debug("[resolveAnchor] subLevel found via getAllSubLevels iteration (map miss)");
                    break;
                }
            }
        }
        if (sub != null && !sub.isRemoved()) {
            Pose3dc pose = sub.logicalPose();
            Vec3 world = pose.transformPosition(binding.localOffset());
            float subYawNow = (float) yawFromOrientation(pose.orientation());
            float resolvedYaw = binding.yaw() + subYawNow;
            ensureChunkLoaded(level, BlockPos.containing(world));
            LOGGER.debug("[resolveAnchor] sable subLevel uuid={} world={} subYawNow={} resolvedYaw={}",
                    binding.shipUuid(), world, subYawNow, resolvedYaw);
            return Optional.of(new Resolved(world, resolvedYaw, binding.pitch()));
        }

        if (container instanceof ServerSubLevelContainer serverContainer) {
            SubLevelHoldingChunkMap chunkMap = serverContainer.getHoldingChunkMap();
            HoldingSubLevel holding = chunkMap == null ? null : chunkMap.getHoldingSubLevel(binding.shipUuid());
            if (holding != null) {
                Pose3dc pose = holding.data().pose();
                Vec3 world = pose.transformPosition(binding.localOffset());
                float subYawNow = (float) yawFromOrientation(pose.orientation());
                float resolvedYaw = binding.yaw() + subYawNow;
                ensureChunkLoaded(level, BlockPos.containing(world));
                LOGGER.debug("[resolveAnchor] sable HoldingSubLevel (storage record) uuid={} world={} subYawNow={} resolvedYaw={}",
                        binding.shipUuid(), world, subYawNow, resolvedYaw);
                return Optional.of(new Resolved(world, resolvedYaw, binding.pitch()));
            }
        }

        Entity anchor = level.getEntity(binding.shipUuid());
        if (anchor instanceof AbstractContraptionEntity contraption && contraption.isAlive()) {
            Vec3 world = contraption.toGlobalVector(binding.localOffset(), 1.0f);
            ensureChunkLoaded(level, BlockPos.containing(world));
            LOGGER.debug("[resolveAnchor] AbstractContraption uuid={} world={}", binding.shipUuid(), world);
            return Optional.of(new Resolved(world, binding.yaw(), binding.pitch()));
        }

        Vec3 lastKnown = binding.lastKnownPos();
        if (lastKnown != null && !Vec3.ZERO.equals(lastKnown)) {
            float yaw = binding.grounded() ? binding.yaw() : 0f;
            ensureChunkLoaded(level, BlockPos.containing(lastKnown));
            LOGGER.debug("[resolveAnchor] uuid={} resolved via lastKnownPos fallback grounded={} world={} yaw={}",
                    binding.shipUuid(), binding.grounded(), lastKnown, yaw);
            return Optional.of(new Resolved(lastKnown, yaw, binding.pitch()));
        }

        LOGGER.warn("[resolveAnchor] anchor uuid={} unresolved (no active sub-level, holding record, contraption, or last-known position)",
                binding.shipUuid());
        return Optional.empty();
    }

    public static void onSubLevelDisassembled(MinecraftServer server, SubLevel subLevel) {
        if (server == null || subLevel == null) return;
        UUID id = subLevel.getUniqueId();
        Pose3dc pose;
        try {
            pose = subLevel.logicalPose();
        } catch (Throwable t) {
            LOGGER.error("[disassemble] failed to read pose for sub-level {}", id, t);
            return;
        }
        float subYawNow = (float) yawFromOrientation(pose.orientation());
        ShipHomeData data = ShipHomeData.get(server);
        int updated = data.groundBindingsForShip(id, b -> {
            Vec3 world = pose.transformPosition(b.localOffset());
            float absYaw = b.yaw() + subYawNow;
            return b.groundedAt(world, absYaw);
        });
        if (updated > 0) {
            LOGGER.debug("[disassemble] grounded {} ship-home binding(s) for sub-level {}", updated, id);
        }
    }

    public static void ensureChunkLoaded(ServerLevel level, BlockPos pos) {
        level.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    private static double yawFromOrientation(Quaterniondc orientation) {
        Vector3d forward = new Vector3d(0, 0, 1);
        orientation.transform(forward);
        return Math.toDegrees(Math.atan2(-forward.x, forward.z));
    }

    public static void dumpDiagnostics(String tag, Player player) {
        Vec3 pos = player.position();
        Entity root = player.getRootVehicle();
        SubLevel tracking = Sable.HELPER.getTrackingSubLevel(player);
        LOGGER.debug("[{}] DIAG player={} pos={} pose={} rootVehicle={}{} trackingSubLevel={}",
                tag, player.getName().getString(), pos, player.getPose(),
                root.getClass().getSimpleName(), root == player ? " (self)" : "",
                tracking == null ? "null" : tracking.getUniqueId() + "@" + tracking.logicalPose().position());

        BlockPos foot = player.blockPosition();
        BlockState footState = player.level().getBlockState(foot);
        BlockState belowState = player.level().getBlockState(foot.below());
        LOGGER.debug("[{}] DIAG block@foot({})={} block@below={}", tag, foot, footState, belowState);

        BlockEntity beBelow = player.level().getBlockEntity(foot.below());
        LOGGER.debug("[{}] DIAG be@below={}", tag, beBelow == null ? "null" : beBelow.getClass().getName());

        AABB search = new AABB(pos, pos).inflate(64);
        Map<String, Integer> classCounts = new HashMap<>();
        for (Entity e : player.level().getEntities((Entity) null, search)) {
            classCounts.merge(e.getClass().getName(), 1, Integer::sum);
        }
        LOGGER.debug("[{}] DIAG entity classes within 64 blocks: {}", tag, classCounts);
    }
}
