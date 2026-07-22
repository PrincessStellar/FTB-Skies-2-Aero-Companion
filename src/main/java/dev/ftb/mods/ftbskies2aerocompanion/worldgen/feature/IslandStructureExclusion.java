package dev.ftb.mods.ftbskies2aerocompanion.worldgen.feature;

import dev.ftb.mods.ftbskies2aerocompanion.basebuffer.BaseExclusionConfig;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;

public final class IslandStructureExclusion {
    private static final ResourceLocation ISLAND_SET_ID =
            ResourceLocation.fromNamespaceAndPath("ftbskies2aerocompanion", "floating_island");

    private static final Set<StructurePlacement> TRACKED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static volatile RegistryAccess registryAccess;
    private static volatile RandomSpreadStructurePlacement islandPlacement;
    private static volatile int islandSpacing;
    private static volatile boolean ready;

    private IslandStructureExclusion() {
    }

    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        init(event.getServer());
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        clear();
    }

    private static synchronized void init(MinecraftServer server) {
        clear();
        try {
            if (!BaseExclusionConfig.STRUCTURE_EXCLUSION_ENABLED.get()) {
                return;
            }
            RegistryAccess access = server.registryAccess();
            var setRegistry = access.registryOrThrow(Registries.STRUCTURE_SET);
            StructureSet islandSet = setRegistry.get(ISLAND_SET_ID);
            if (islandSet == null || !(islandSet.placement() instanceof RandomSpreadStructurePlacement rsp)) {
                return;
            }
            for (String id : BaseExclusionConfig.STRUCTURE_EXCLUSION_SETS.get()) {
                ResourceLocation rl = ResourceLocation.tryParse(id);
                if (rl == null) {
                    continue;
                }
                StructureSet set = setRegistry.get(rl);
                if (set != null) {
                    TRACKED.add(set.placement());
                }
            }
            if (TRACKED.isEmpty()) {
                return;
            }
            islandPlacement = rsp;
            islandSpacing = rsp.spacing();
            registryAccess = access;
            ready = true;
        } catch (Throwable t) {
            clear();
        }
    }

    public static synchronized void clear() {
        ready = false;
        registryAccess = null;
        islandPlacement = null;
        islandSpacing = 0;
        TRACKED.clear();
    }

    public static boolean tracks(StructurePlacement placement) {
        return ready && TRACKED.contains(placement);
    }

    public static boolean isChunkNearIsland(long worldSeed, int chunkX, int chunkZ) {
        RandomSpreadStructurePlacement placement = islandPlacement;
        RegistryAccess access = registryAccess;
        int spacing = islandSpacing;
        if (!ready || placement == null || access == null || spacing <= 0) {
            return false;
        }
        int margin = BaseExclusionConfig.STRUCTURE_EXCLUSION_MARGIN.get();
        int blockX = (chunkX << 4) + 8;
        int blockZ = (chunkZ << 4) + 8;
        int cellBlocks = spacing * 16;
        int reachBlocks = IslandPlacementResolver.MAX_RADIUS + margin;
        int originCellX = Math.floorDiv(chunkX, spacing);
        int originCellZ = Math.floorDiv(chunkZ, spacing);
        int cellRadius = Math.floorDiv(reachBlocks, cellBlocks) + 1;
        for (int cellX = originCellX - cellRadius; cellX <= originCellX + cellRadius; cellX++) {
            for (int cellZ = originCellZ - cellRadius; cellZ <= originCellZ + cellRadius; cellZ++) {
                ChunkPos candidate = placement.getPotentialStructureChunk(worldSeed, cellX * spacing, cellZ * spacing);
                Optional<IslandPlacementResolver.LogicalIsland> islandOpt =
                        IslandPlacementResolver.resolve(worldSeed, candidate, access);
                if (islandOpt.isEmpty()) {
                    continue;
                }
                IslandPlacementResolver.LogicalIsland island = islandOpt.get();
                long limit = island.radius() + margin;
                long dx = island.center().getX() - blockX;
                long dz = island.center().getZ() - blockZ;
                if (dx * dx + dz * dz <= limit * limit) {
                    return true;
                }
            }
        }
        return false;
    }
}
