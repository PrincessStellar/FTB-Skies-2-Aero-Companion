package dev.ftb.mods.ftbskies2aerocompanion.worldgen.feature;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbskies2aerocompanion.basebuffer.BaseExclusionConfig;
import dev.ftb.mods.ftbskies2aerocompanion.compat.sa.IslandBiomeSelector;
import dev.ftb.mods.ftbskies2aerocompanion.compat.sa.SkyAIslandBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.sathrek.sky_archipelago.config.SkyIslandConfig;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class IslandPlacementResolver {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int MIN_RADIUS = 77;
    public static final int MAX_RADIUS = 230;

    public static final int MAX_COPY_REACH = 1024;

    public record LogicalIsland(ChunkPos candidateChunk, BlockPos center, int radius, long seedTag,
                                ResourceKey<Biome> biomeKey, int reach) {
        public int paintExtent() {
            return Math.max(radius, reach) + SkyAIslandBuilder.REACH_SAFETY_MARGIN;
        }
    }

    private record CacheKey(long worldSeed, long chunkPacked, int configFingerprint) {
    }

    private static final int MAX_CACHE_ENTRIES = 4096;
    private static final ConcurrentMap<CacheKey, Optional<LogicalIsland>> CACHE = new ConcurrentHashMap<>();

    private IslandPlacementResolver() {
    }

    public static Optional<LogicalIsland> resolve(long worldSeed, ChunkPos candidateChunk, RegistryAccess registryAccess) {
        CacheKey key = new CacheKey(worldSeed, candidateChunk.toLong(), configFingerprint());
        Optional<LogicalIsland> cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        Optional<LogicalIsland> resolved = resolveUncached(worldSeed, candidateChunk, registryAccess);
        if (CACHE.size() >= MAX_CACHE_ENTRIES) {
            CACHE.clear();
        }
        CACHE.put(key, resolved);
        return resolved;
    }

    private static Optional<LogicalIsland> resolveUncached(long worldSeed, ChunkPos chunkPos, RegistryAccess registryAccess) {
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(worldSeed));
        random.setLargeFeatureSeed(worldSeed, chunkPos.x, chunkPos.z);

        int radius = MIN_RADIUS + random.nextInt(MAX_RADIUS - MIN_RADIUS + 1);
        int minY = BaseExclusionConfig.ISLAND_MIN_Y.get();
        int maxY = Math.max(minY, BaseExclusionConfig.ISLAND_MAX_Y.get());
        int centerY = minY + random.nextInt(maxY - minY + 1);

        double spawnChance = BaseExclusionConfig.ISLAND_SPAWN_CHANCE.get();
        if (spawnChance < 1.0 && random.nextFloat() >= spawnChance) {
            return Optional.empty();
        }
        BlockPos center = new BlockPos(centerX, centerY, centerZ);

        long seedTag = (centerX * 341873128712341L) ^ (centerZ * 132897987541L) ^ worldSeed;

        Optional<Holder<Biome>> biomeOpt = IslandBiomeSelector.pick(registryAccess, seedTag);
        if (biomeOpt.isEmpty()) {
            return Optional.empty();
        }
        ResourceKey<Biome> biomeKey = biomeOpt.get().unwrapKey().orElse(null);
        if (biomeKey == null) {
            return Optional.empty();
        }

        int reach = SkyAIslandBuilder.resolveHorizontalReach(worldSeed, seedTag, radius);
        if (reach < 0) {
            return Optional.empty();
        }
        LogicalIsland island = new LogicalIsland(chunkPos, center, radius, seedTag, biomeKey, reach);
        if (island.paintExtent() > MAX_COPY_REACH) {
            LOGGER.warn("Rejecting island at {}: paintExtent {} exceeds MAX_COPY_REACH {}. "
                    + "Raise MAX_COPY_REACH if island sizing config has changed.", center, island.paintExtent(), MAX_COPY_REACH);
            return Optional.empty();
        }

        if (SkyAIslandBuilder.isIslandExcludedByBaseZones(worldSeed, seedTag, radius, centerX, centerZ)) {
            return Optional.empty();
        }

        return Optional.of(island);
    }

    private static int configFingerprint() {
        int result = BaseExclusionConfig.EXCLUSION_RADIUS.get();
        result = 31 * result + BaseExclusionConfig.BASE_SIZE_REGIONS.get();
        result = 31 * result + BaseExclusionConfig.BASE_SEPARATION_REGIONS.get();
        result = 31 * result + BaseExclusionConfig.MAX_REGION_X.get();
        result = 31 * result + Double.hashCode(BaseExclusionConfig.ISLAND_SPAWN_CHANCE.get());
        result = 31 * result + BaseExclusionConfig.ISLAND_MIN_Y.get();
        result = 31 * result + BaseExclusionConfig.ISLAND_MAX_Y.get();
        result = 31 * result + SkyIslandConfig.current().hashCode();
        return result;
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        CACHE.clear();
        SkyAIslandBuilder.clearCaches();
    }
}
