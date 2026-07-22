package dev.ftb.mods.ftbskies2aerocompanion.compat.sa;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import dev.ftb.mods.ftbskies2aerocompanion.basebuffer.BaseExclusionConfig;
import dev.ftb.mods.ftbskies2aerocompanion.basebuffer.TeamBaseGrid;
import org.sathrek.sky_archipelago.config.ClusterSpacingMode;
import org.sathrek.sky_archipelago.config.SkyIslandConfig;
import org.sathrek.sky_archipelago.config.SkyIslandSettings;
import org.sathrek.sky_archipelago.worldgen.generator.field.IslandField;
import org.sathrek.sky_archipelago.worldgen.generator.field.TerrainColumn;
import org.sathrek.sky_archipelago.worldgen.generator.field.internal.IslandClusterSampler;
import org.sathrek.sky_archipelago.worldgen.generator.field.internal.IslandColumnResolver;
import org.sathrek.sky_archipelago.worldgen.generator.field.internal.IslandDescriptorFactory;
import org.sathrek.sky_archipelago.worldgen.generator.field.internal.IslandNoise;
import org.sathrek.sky_archipelago.worldgen.generator.terrain.SkyIslandColumnMaterialPlan;

import java.util.Arrays;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public final class SkyAIslandBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ConcurrentMap<Long, IslandField> FIELDS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<SourceKey, ResolvedSource> SOURCES = new ConcurrentHashMap<>();

    private record SourceKey(long worldSeed, long seedTag, int settingsHash) {
    }

    public static void clearCaches() {
        FIELDS.clear();
        SOURCES.clear();
    }

    private record ResolvedSource(IslandField.IslandPreview preview, int unionReach, long[] memberSeeds,
                                  List<MemberFootprint> memberFootprints) {
    }

    private record MemberFootprint(int offsetX, int offsetZ, int reach) {
    }

    private static final int DESCRIPTOR_SEED_SALT = 337;

    private record ReconstructedCluster(IslandField.ClusterDescriptor cluster,
                                        IslandField.IslandDescriptor anchor,
                                        java.util.List<IslandField.IslandDescriptor> members) {
    }

    private static int effectiveClusterSpacing(SkyIslandSettings settings, long layoutSeed) {
        if (settings.terrain().spacing().clusterSpacingMode() == ClusterSpacingMode.CONSISTENT) {
            return settings.terrain().spacing().clusterSpacing();
        }
        int min = settings.terrain().spacing().minClusterSpacing();
        int max = settings.terrain().spacing().maxClusterSpacing();
        int span = Math.max(0, max - min);
        if (span == 0) {
            return min;
        }
        long mixed = layoutSeed ^ -7046029254386353131L;
        int offset = (int) Math.floorMod(mixed, (long) span + 1L);
        return min + offset;
    }

    private static final int CLUSTER_ACTIVE_SALT = 101;

    private static ReconstructedCluster reconstructActiveCluster(IslandNoise noise, IslandClusterSampler sampler,
                                                                 IslandDescriptorFactory factory, int cellX, int cellZ,
                                                                 long layoutSeed, int spacing, SkyIslandSettings settings) {
        if (noise.sample01(cellX, cellZ, CLUSTER_ACTIVE_SALT) >= settings.terrain().islandDensity()) {
            return null;
        }
        IslandField.ClusterDescriptor cluster = sampler.sampleClusterDescriptor(cellX, cellZ, layoutSeed, spacing, settings, DESCRIPTOR_SEED_SALT);
        IslandField.IslandDescriptor anchor = factory.createAnchorDescriptor(cluster, settings);
        ArrayList<IslandField.IslandDescriptor> members = new ArrayList<>(1 + cluster.satelliteCount() + cluster.spireCount());
        members.add(anchor);
        if (settings.advanced().clusterCompanionIslandsEnabled()) {
            for (int i = 0; i < cluster.satelliteCount(); i++) {
                members.add(factory.createSatelliteDescriptor(cluster, settings, i));
            }
            for (int i = 0; i < cluster.spireCount(); i++) {
                members.add(factory.createSpireDescriptor(cluster, settings, i));
            }
        }
        return new ReconstructedCluster(cluster, anchor, List.copyOf(members));
    }

    private static boolean previewMatchesDescriptor(IslandField.IslandPreview preview, IslandField.IslandDescriptor d) {
        return d.centerX() == preview.x()
                && d.centerY() == preview.y()
                && d.centerZ() == preview.z()
                && d.maxRadius() == preview.radius()
                && d.plateauHeight() == preview.plateauHeight()
                && d.hangDepth() == preview.hangDepth()
                && d.archetype() == preview.archetype()
                && d.family() == preview.family()
                && IslandColumnResolver.horizontalReach(d) == preview.horizontalReach();
    }

    private static boolean isMemberSeed(long[] sortedMemberSeeds, long seed) {
        return Arrays.binarySearch(sortedMemberSeeds, seed) >= 0;
    }

    public static final int REACH_SAFETY_MARGIN = 8;

    private static final int VERTICAL_SAFETY_MARGIN = 80;

    private SkyAIslandBuilder() {
    }

    private static ResolvedSource resolveSourceIsland(long worldSeed, long seedTag, int targetRadius, SkyIslandSettings settings) {
        IslandField field = FIELDS.computeIfAbsent(worldSeed, IslandField::new);
        SourceKey sourceKey = new SourceKey(worldSeed, seedTag, settings.hashCode());
        ResolvedSource resolved = SOURCES.get(sourceKey);
        if (resolved == null) {
            resolved = findSourceIsland(field, settings, seedTag, targetRadius);
            if (resolved == null) {
                return null;
            }
            if (SOURCES.size() > 1024) {
                SOURCES.clear();
            }
            SOURCES.put(sourceKey, resolved);
        }
        return resolved;
    }

    public static int resolveHorizontalReach(long worldSeed, long seedTag, int targetRadius) {
        ResolvedSource resolved = resolveSourceIsland(worldSeed, seedTag, targetRadius, SkyIslandConfig.current());
        return resolved == null ? -1 : resolved.unionReach();
    }

    public static boolean buildSlice(WorldGenLevel level, RandomSource random, BlockPos center, int targetRadius, long seedTag, ResourceKey<Biome> biomeKey, BoundingBox writeBox) {
        SkyIslandSettings settings = SkyIslandConfig.current();
        long layoutSeed = level.getSeed();
        IslandField field = FIELDS.computeIfAbsent(layoutSeed, IslandField::new);
        ResolvedSource resolved = resolveSourceIsland(layoutSeed, seedTag, targetRadius, settings);
        if (resolved == null) {
            return false;
        }
        IslandField.IslandPreview source = resolved.preview();
        int dy = center.getY() - source.y();
        int verticalReach = Math.max(source.hangDepth() + source.plateauHeight(),
                settings.terrain().maxIslandThicknessBlocks()) + VERTICAL_SAFETY_MARGIN;
        int bandBottom = source.y() - verticalReach;
        int bandTop = source.y() + verticalReach;
        Holder.Reference<Biome> skyBiome = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biomeKey);
        BlockState topBlockState = BiomeSurfaceResolver.topBlock(skyBiome);
        BlockState subSurfaceState = BiomeSurfaceResolver.subSurfaceBlock(skyBiome);
        int scanRadius = Math.max(targetRadius, resolved.unionReach()) + REACH_SAFETY_MARGIN;
        int desiredMinX = center.getX() - scanRadius;
        int desiredMaxX = center.getX() + scanRadius;
        int desiredMinZ = center.getZ() - scanRadius;
        int desiredMaxZ = center.getZ() + scanRadius;
        int minX = Math.max(desiredMinX, writeBox.minX());
        int maxX = Math.min(desiredMaxX, writeBox.maxX());
        int minZ = Math.max(desiredMinZ, writeBox.minZ());
        int maxZ = Math.min(desiredMaxZ, writeBox.maxZ());
        if (minX > maxX || minZ > maxZ) {
            return false;
        }
        int worldMinY = level.getMinBuildHeight();
        int worldMaxY = level.getMaxBuildHeight();
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        boolean placedAny = false;
        for (int worldX = minX; worldX <= maxX; ++worldX) {
            for (int worldZ = minZ; worldZ <= maxZ; ++worldZ) {
                int srcX = source.x() + (worldX - center.getX());
                int srcZ = source.z() + (worldZ - center.getZ());
                List<TerrainColumn> rawSegments = field.sampleSolidSegments(srcX, srcZ, settings);
                if (rawSegments.isEmpty()) continue;
                IslandField.IslandDescriptor dominant = field.sampleDominantIslandDescriptor(srcX, srcZ, settings);
                if (dominant == null || !isMemberSeed(resolved.memberSeeds(), dominant.seed())) continue;
                ArrayList<TerrainColumn> segments = new ArrayList<>(rawSegments.size());
                for (TerrainColumn column : rawSegments) {
                    if (!column.intersectsInclusive(bandBottom, bandTop)) continue;
                    segments.add(new TerrainColumn(column.bottomY() + dy, column.topY() + dy));
                }
                if (segments.isEmpty()) continue;
                SkyIslandColumnMaterialPlan plan = SkyIslandColumnMaterialPlan.create(segments, worldMinY, worldMaxY, settings, worldX, worldZ, layoutSeed);
                int highestSolid = plan.highestSolidY();
                ChunkAccess chunk = level.getChunk(worldX >> 4, worldZ >> 4);
                int localX = worldX & 0xF;
                int localZ = worldZ & 0xF;
                Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
                Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
                Heightmap motionBlocking = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING);
                Heightmap motionNoLeaves = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
                for (SkyIslandColumnMaterialPlan.MaterialRange range : plan.materialRanges()) {
                    for (int y = range.bottomY(); y <= range.topY(); ++y) {
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

    private static final int MAX_CLUSTER_SEARCH_RADIUS = 1024;

    private static ResolvedSource findSourceIsland(IslandField field, SkyIslandSettings settings, long seedTag, int targetRadius) {
        long hx = Math.floorMod(seedTag * -7046029254386353131L, 100000L);
        long hz = Math.floorMod((seedTag ^ 2685821657736338717L) * -4658895280553007687L, 100000L);
        int searchX = 100000 + (int) hx;
        int searchZ = 100000 + (int) hz;

        long layoutSeed = field.layoutSeed();
        int spacing = effectiveClusterSpacing(settings, layoutSeed);
        IslandNoise noise = new IslandNoise(layoutSeed);
        IslandClusterSampler sampler = new IslandClusterSampler(noise);
        IslandDescriptorFactory factory = new IslandDescriptorFactory(noise, sampler);

        int originCellX = Math.floorDiv(searchX, spacing);
        int originCellZ = Math.floorDiv(searchZ, spacing);
        int maxRing = Math.floorDiv(MAX_CLUSTER_SEARCH_RADIUS, spacing) + 1;
        int maxRadius = targetRadius + 40;

        IslandField.IslandDescriptor chosen = null;
        ReconstructedCluster chosenCluster = null;
        int bestDiff = Integer.MAX_VALUE;
        for (int ring = 0; ring <= maxRing && chosen == null; ring++) {
            for (int cellX = originCellX - ring; cellX <= originCellX + ring; cellX++) {
                for (int cellZ = originCellZ - ring; cellZ <= originCellZ + ring; cellZ++) {
                    if (Math.max(Math.abs(cellX - originCellX), Math.abs(cellZ - originCellZ)) != ring) {
                        continue;
                    }
                    ReconstructedCluster rc = reconstructActiveCluster(noise, sampler, factory, cellX, cellZ, layoutSeed, spacing, settings);
                    if (rc == null) {
                        continue;
                    }
                    for (IslandField.IslandDescriptor m : rc.members()) {
                        if (m.maxRadius() > maxRadius) {
                            continue;
                        }
                        int diff = Math.abs(m.maxRadius() - targetRadius);
                        if (diff < bestDiff) {
                            bestDiff = diff;
                            chosen = m;
                            chosenCluster = rc;
                        }
                    }
                }
            }
        }
        if (chosen == null) {
            return null;
        }

        boolean fieldAgrees = false;
        for (IslandField.IslandPreview p : field.collectIslandPreviewsInRadius(
                chosenCluster.anchor().centerX(), chosenCluster.anchor().centerZ(), 1, settings)) {
            if (previewMatchesDescriptor(p, chosenCluster.anchor())) {
                fieldAgrees = true;
                break;
            }
        }
        if (!fieldAgrees) {
            LOGGER.warn(
                    "Reconstructed cluster at cell ({},{}) not confirmed by field previews at ({},{}), rejecting placement.",
                    chosenCluster.cluster().cellX(), chosenCluster.cluster().cellZ(),
                    chosenCluster.anchor().centerX(), chosenCluster.anchor().centerZ());
            return null;
        }

        int unionReach = 0;
        long[] memberSeeds = new long[chosenCluster.members().size()];
        ArrayList<MemberFootprint> footprints = new ArrayList<>(chosenCluster.members().size());
        for (int i = 0; i < memberSeeds.length; i++) {
            IslandField.IslandDescriptor m = chosenCluster.members().get(i);
            memberSeeds[i] = m.seed();
            int offX = m.centerX() - chosen.centerX();
            int offZ = m.centerZ() - chosen.centerZ();
            int reach = IslandColumnResolver.horizontalReach(m);
            footprints.add(new MemberFootprint(offX, offZ, reach));
            int needed = Math.max(Math.abs(offX), Math.abs(offZ)) + reach;
            if (needed > unionReach) {
                unionReach = needed;
            }
        }
        Arrays.sort(memberSeeds);

        IslandField.IslandPreview sourcePreview = new IslandField.IslandPreview(
                chosen.archetype(), chosen.family(), chosenCluster.cluster().heightBand(),
                chosen.centerX(), chosen.centerY(), chosen.centerZ(), chosen.maxRadius(),
                chosen.plateauHeight(), chosen.hangDepth(), IslandColumnResolver.horizontalReach(chosen));
        return new ResolvedSource(sourcePreview, unionReach, memberSeeds, List.copyOf(footprints));
    }

    public static boolean isIslandExcludedByBaseZones(long worldSeed, long seedTag, int targetRadius,
                                                      int worldCenterX, int worldCenterZ) {
        ResolvedSource resolved = resolveSourceIsland(worldSeed, seedTag, targetRadius, SkyIslandConfig.current());
        if (resolved == null) {
            return false;
        }
        int exclusionRadius = BaseExclusionConfig.EXCLUSION_RADIUS.get();
        for (MemberFootprint m : resolved.memberFootprints()) {
            if (TeamBaseGrid.isWithinBaseExclusion(worldCenterX + m.offsetX(), worldCenterZ + m.offsetZ(),
                    exclusionRadius + m.reach() + REACH_SAFETY_MARGIN)) {
                return true;
            }
        }
        return false;
    }

    private static void overrideBiomeQuart(ChunkAccess chunk, int worldX, int worldY, int worldZ, Holder<Biome> biome) {
        int sectionIndex = chunk.getSectionIndex(worldY);
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) {
            return;
        }
        LevelChunkSection section = chunk.getSection(sectionIndex);
        if (section == null) {
            return;
        }
        int qx = QuartPos.fromBlock(worldX) & 3;
        int qy = QuartPos.fromBlock(worldY) & 3;
        int qz = QuartPos.fromBlock(worldZ) & 3;
        try {
            PalettedContainerRO<Holder<Biome>> ro = section.getBiomes();
            if (ro instanceof PalettedContainer) {
                PalettedContainer<Holder<Biome>> rw = (PalettedContainer<Holder<Biome>>) ro;
                rw.set(qx, qy, qz, biome);
            }
        } catch (Exception ignored) {
        }
    }
}
