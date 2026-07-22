package dev.ftb.mods.ftbskies2aerocompanion.worldgen.feature;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbskies2aerocompanion.compat.sa.SkyAIslandBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.Optional;

public class FloatingIslandTerrainFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation STRUCTURE_SET_ID =
            ResourceLocation.fromNamespaceAndPath("ftbskies2aerocompanion", "floating_island");

    public FloatingIslandTerrainFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkPos chunkPos = new ChunkPos(context.origin());
        long worldSeed = level.getSeed();

        StructureSet structureSet = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET)
                .get(STRUCTURE_SET_ID);
        if (structureSet == null) {
            return false;
        }
        StructurePlacement placement = structureSet.placement();
        if (!(placement instanceof RandomSpreadStructurePlacement randomSpread)) {
            return false;
        }

        int spacing = randomSpread.spacing();
        int cellBlocks = spacing * 16;
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        int chunkMaxX = chunkPos.getMaxBlockX();
        int chunkMaxZ = chunkPos.getMaxBlockZ();
        int minCellX = Math.floorDiv(chunkMinX - IslandPlacementResolver.MAX_COPY_REACH, cellBlocks);
        int maxCellX = Math.floorDiv(chunkMaxX + IslandPlacementResolver.MAX_COPY_REACH, cellBlocks);
        int minCellZ = Math.floorDiv(chunkMinZ - IslandPlacementResolver.MAX_COPY_REACH, cellBlocks);
        int maxCellZ = Math.floorDiv(chunkMaxZ + IslandPlacementResolver.MAX_COPY_REACH, cellBlocks);

        BoundingBox chunkBox = new BoundingBox(
                chunkMinX, level.getMinBuildHeight(), chunkMinZ,
                chunkMaxX, level.getMaxBuildHeight() - 1, chunkMaxZ);

        boolean paintedAny = false;
        for (int cellX = minCellX; cellX <= maxCellX; ++cellX) {
            for (int cellZ = minCellZ; cellZ <= maxCellZ; ++cellZ) {
                ChunkPos candidate = randomSpread.getPotentialStructureChunk(worldSeed, cellX * spacing, cellZ * spacing);
                Optional<IslandPlacementResolver.LogicalIsland> islandOpt =
                        IslandPlacementResolver.resolve(worldSeed, candidate, level.registryAccess());
                if (islandOpt.isEmpty()) {
                    continue;
                }
                IslandPlacementResolver.LogicalIsland island = islandOpt.get();

                int extent = island.paintExtent();
                BlockPos center = island.center();
                if (center.getX() + extent < chunkMinX || center.getX() - extent > chunkMaxX
                        || center.getZ() + extent < chunkMinZ || center.getZ() - extent > chunkMaxZ) {
                    continue;
                }

                paintedAny |= SkyAIslandBuilder.buildSlice(level, context.random(), center,
                        island.radius(), island.seedTag(), island.biomeKey(), chunkBox);
            }
        }
        return paintedAny;
    }
}
