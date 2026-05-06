package dev.ftb.mods.ftbskies2aerocompanion.ship;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import dev.ftb.mods.ftbessentials.util.TeleportPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class ShipBindings {
    private ShipBindings() {}

    public static Optional<AbstractContraptionEntity> findShip(Player player) {
        Entity root = player.getRootVehicle();
        if (root != player && root instanceof AbstractContraptionEntity ship) {
            return Optional.of(ship);
        }
        AABB search = player.getBoundingBox().inflate(0.5).expandTowards(0, -2, 0);
        return player.level().getEntitiesOfClass(AbstractContraptionEntity.class, search).stream().findFirst();
    }

    public static ShipBinding capture(AbstractContraptionEntity ship, Vec3 worldPos, float yaw, float pitch) {
        Vec3 local = ship.toLocalVector(worldPos, 0f);
        return new ShipBinding(ship.getUUID(), ship.level().dimension(), local, yaw, pitch, ship.position());
    }

    /**
     * Resolve the binding to a current world-space position, force-loading the contraption's last-known
     * chunk so the entity actually loads, then force-loading the destination chunk so the player lands
     * in loaded space. Returns empty when the contraption is gone or in a dead state.
     */
    public static Optional<Vec3> resolveWorldPos(MinecraftServer server, ShipBinding binding) {
        if (server == null) return Optional.empty();
        ServerLevel level = server.getLevel(binding.shipDimension());
        if (level == null) return Optional.empty();

        ensureChunkLoaded(level, BlockPos.containing(binding.lastKnownPos()));

        Entity entity = level.getEntity(binding.shipUuid());
        if (!(entity instanceof AbstractContraptionEntity ship) || !ship.isAlive()) {
            return Optional.empty();
        }

        Vec3 world = ship.toGlobalVector(binding.localOffset(), 1.0f);
        ensureChunkLoaded(level, BlockPos.containing(world));
        return Optional.of(world);
    }

    public static Optional<TeleportPos> resolve(MinecraftServer server, ShipBinding binding) {
        return resolveWorldPos(server, binding).map(world -> {
            ServerLevel level = server.getLevel(binding.shipDimension());
            return new TeleportPos(level.dimension(), BlockPos.containing(world), binding.yaw(), binding.pitch());
        });
    }

    public static TeleportPos worldSpawnFallback(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        ensureChunkLoaded(overworld, spawn);
        return new TeleportPos(overworld.dimension(), spawn, 0f, 0f);
    }

    public static void ensureChunkLoaded(ServerLevel level, BlockPos pos) {
        level.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }
}
