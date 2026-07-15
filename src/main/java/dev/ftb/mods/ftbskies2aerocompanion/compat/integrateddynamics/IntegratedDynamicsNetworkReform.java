package dev.ftb.mods.ftbskies2aerocompanion.compat.integrateddynamics;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Reforms IntegratedDynamics cable networks after a bulk relocation that places cables
 * without the per-block neighbour updates a normal placement triggers (Sable sub-level
 * assembly, vanilla structure placement). Without this the cables keep stale connection
 * data and never link into a single network, so connected devices can't see each other
 * until the player forces an update by placing/breaking a cable.
 *
 * <p>Recompute every relocated cable's connections, then form each connected cluster's
 * network exactly once — only init a cable that isn't already part of a network, so the
 * first cable of a cluster builds the whole thing and the rest just join it (calling
 * {@code initNetwork} per cable would build N overlapping networks and split the parts).
 *
 * <p>Structure placement (structure blocks and off-thread worldgen) goes through the
 * deferred path: record the placed region, then reform on the server thread once the
 * region is loaded and its block entities have had a few ticks to initialise — the same
 * conditions that make a manual cable place/break fix it.
 */
public final class IntegratedDynamicsNetworkReform {

    private static final int SETTLE_TICKS = 3;

    private static final class Deferred {
        private final ResourceKey<Level> dimension;
        private final BoundingBox box;
        private int settledTicks;

        private Deferred(ResourceKey<Level> dimension, BoundingBox box) {
            this.dimension = dimension;
            this.box = box;
        }
    }

    private static final Queue<Deferred> PENDING = new ConcurrentLinkedQueue<>();

    private IntegratedDynamicsNetworkReform() {
    }

    public static void reform(ServerLevel level, Iterable<BlockPos> positions) {
        List<BlockPos> cables = new ArrayList<>();
        for (BlockPos pos : positions) {
            if (CableHelpers.getCable(level, pos, null).isPresent()) {
                cables.add(pos.immutable());
            }
        }
        if (cables.isEmpty()) {
            return;
        }

        for (BlockPos pos : cables) {
            CableHelpers.getCable(level, pos, null).ifPresent(cable -> cable.updateConnections());
        }
        for (BlockPos pos : cables) {
            boolean alreadyNetworked = NetworkHelpers.getNetworkCarrier(level, pos, null)
                    .map(carrier -> carrier.getNetwork() != null)
                    .orElse(false);
            if (!alreadyNetworked) {
                NetworkHelpers.initNetwork(level, pos, null);
            }
        }
    }

    public static void reform(ServerLevel level, BoundingBox box) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())
                .forEach(pos -> positions.add(pos.immutable()));
        reform(level, positions);
    }

    public static void recordDeferred(ServerLevel level, BoundingBox box) {
        PENDING.add(new Deferred(level.dimension(), box));
    }

    public static void drainDeferred(MinecraftServer server) {
        if (PENDING.isEmpty()) {
            return;
        }
        Iterator<Deferred> iterator = PENDING.iterator();
        while (iterator.hasNext()) {
            Deferred deferred = iterator.next();
            ServerLevel level = server.getLevel(deferred.dimension);
            if (level == null) {
                iterator.remove();
                continue;
            }
            BoundingBox box = deferred.box;
            if (!level.hasChunksAt(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                continue;
            }
            if (++deferred.settledTicks < SETTLE_TICKS) {
                continue;
            }
            reform(level, box);
            iterator.remove();
        }
    }
}
