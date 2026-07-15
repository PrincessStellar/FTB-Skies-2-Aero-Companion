package dev.ftb.mods.ftbskies2aerocompanion.compat.sa;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.sathrek.sky_archipelago.config.SkyIslandConfig;
import org.sathrek.sky_archipelago.config.SkyIslandSettings;
import org.sathrek.sky_archipelago.worldgen.generator.field.IslandField;
import org.sathrek.sky_archipelago.worldgen.generator.field.TerrainColumn;
import org.sathrek.sky_archipelago.worldgen.generator.terrain.SkyIslandColumnMaterialPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class SkyAIslandBuilder {
    private static final ConcurrentMap<Long, IslandField> FIELDS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Long, IslandField.IslandPreview> SOURCES = new ConcurrentHashMap<>();

    private SkyAIslandBuilder() {}

    public static boolean buildSlice(WorldGenLevel level, RandomSource random, BlockPos center, int targetRadius,
                                     long seedTag, ResourceKey<Biome> biomeKey, BoundingBox writeBox) {
        SkyIslandSettings settings = SkyIslandConfig.current();
        long layoutSeed = level.getSeed();
        IslandField field = FIELDS.computeIfAbsent(layoutSeed, IslandField::new);

        long sourceKey = layoutSeed * 1099511628211L + seedTag;
        IslandField.IslandPreview source = SOURCES.get(sourceKey);
        if (source == null) {
            source = findSourceIsland(field, settings, seedTag, targetRadius);
            if (source == null) return false;
            if (SOURCES.size() > 1024) SOURCES.clear();
            SOURCES.put(sourceKey, source);
        }

        int dy = center.getY() - source.y();
        int bandBottom = source.y() - source.hangDepth();
        int bandTop = source.y() + source.plateauHeight();

        Holder<Biome> skyBiome = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biomeKey);
        BlockState topBlockState = BiomeSurfaceResolver.topBlock(skyBiome);
        BlockState subSurfaceState = BiomeSurfaceResolver.subSurfaceBlock(skyBiome);

        int scanRadius = targetRadius + 50;
        int minX = Math.max(center.getX() - scanRadius, writeBox.minX());
        int maxX = Math.min(center.getX() + scanRadius, writeBox.maxX());
        int minZ = Math.max(center.getZ() - scanRadius, writeBox.minZ());
        int maxZ = Math.min(center.getZ() + scanRadius, writeBox.maxZ());
        if (minX > maxX || minZ > maxZ) return false;

        int worldMinY = level.getMinBuildHeight();
        int worldMaxY = level.getMaxBuildHeight();
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();

        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        for (int worldX = minX; worldX <= maxX; worldX++) {
            for (int worldZ = minZ; worldZ <= maxZ; worldZ++) {
                int srcX = source.x() + (worldX - center.getX());
                int srcZ = source.z() + (worldZ - center.getZ());
                List<TerrainColumn> rawSegments = field.sampleSolidSegments(srcX, srcZ, settings);
                if (rawSegments.isEmpty()) continue;

                List<TerrainColumn> segments = new ArrayList<>(rawSegments.size());
                for (TerrainColumn column : rawSegments) {
                    if (!column.intersectsInclusive(bandBottom, bandTop)) continue;
                    segments.add(new TerrainColumn(column.bottomY() + dy, column.topY() + dy));
                }
                if (segments.isEmpty()) continue;

                SkyIslandColumnMaterialPlan plan = SkyIslandColumnMaterialPlan.create(
                        segments, worldMinY, worldMaxY, settings, worldX, worldZ, layoutSeed);
                int highestSolid = plan.highestSolidY();

                ChunkAccess chunk = level.getChunk(worldX >> 4, worldZ >> 4);
                int localX = worldX & 15;
                int localZ = worldZ & 15;
                Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
                Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
                Heightmap motionBlocking = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING);
                Heightmap motionNoLeaves = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);

                for (SkyIslandColumnMaterialPlan.MaterialRange range : plan.materialRanges()) {
                    for (int y = range.bottomY(); y <= range.topY(); y++) {
                        if (y < writeBox.minY() || y > writeBox.maxY()) continue;
                        BlockState plannedState = plan.plannedStateAt(y, stone, deepslate, water, bedrock);
                        if (plannedState == null || plannedState == water) continue;
                        BlockState toPlace = plannedState;
                        if (plannedState == stone) {
                            if (y == highestSolid) {
                                toPlace = topBlockState;
                            } else if (y >= highestSolid - 2) {
                                toPlace = subSurfaceState;
                            }
                        }
                        m.set(worldX, y, worldZ);
                        level.setBlock(m, toPlace, 2);
                        worldSurface.update(localX, y, localZ, toPlace);
                        oceanFloor.update(localX, y, localZ, toPlace);
                        motionBlocking.update(localX, y, localZ, toPlace);
                        motionNoLeaves.update(localX, y, localZ, toPlace);
                        overrideBiomeQuart(chunk, worldX, y, worldZ, skyBiome);
                        placedAny = true;
                    }
                }
            }
        }
        return placedAny;
    }

    private static IslandField.IslandPreview findSourceIsland(IslandField field, SkyIslandSettings settings,
                                                              long seedTag, int targetRadius) {
        long hx = Math.floorMod(seedTag * 0x9E3779B97F4A7C15L, 100000L);
        long hz = Math.floorMod((seedTag ^ 0x2545F4914F6CDD1DL) * 0xBF58476D1CE4E5B9L, 100000L);
        int searchX = 100000 + (int) hx;
        int searchZ = 100000 + (int) hz;
        int maxRadius = targetRadius + 40;

        for (int searchRadius = 256; searchRadius <= 1024; searchRadius *= 2) {
            List<IslandField.IslandPreview> previews = field.collectIslandPreviewsInRadius(searchX, searchZ, searchRadius, settings);
            IslandField.IslandPreview best = null;
            int bestDiff = Integer.MAX_VALUE;
            for (IslandField.IslandPreview preview : previews) {
                if (preview.radius() > maxRadius) continue;
                int diff = Math.abs(preview.radius() - targetRadius);
                if (diff < bestDiff) {
                    bestDiff = diff;
                    best = preview;
                }
            }
            if (best != null) return best;
        }
        return null;
    }

    private static void overrideBiomeQuart(ChunkAccess chunk, int worldX, int worldY, int worldZ, Holder<Biome> biome) {
        int sectionIndex = chunk.getSectionIndex(worldY);
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) return;
        LevelChunkSection section = chunk.getSection(sectionIndex);
        if (section == null) return;
        int qx = QuartPos.fromBlock(worldX) & 3;
        int qy = QuartPos.fromBlock(worldY) & 3;
        int qz = QuartPos.fromBlock(worldZ) & 3;
        try {
            PalettedContainerRO<Holder<Biome>> ro = section.getBiomes();
            if (ro instanceof PalettedContainer<Holder<Biome>> rw) {
                rw.set(qx, qy, qz, biome);
            }
        } catch (Exception ignored) {
        }
    }
}
