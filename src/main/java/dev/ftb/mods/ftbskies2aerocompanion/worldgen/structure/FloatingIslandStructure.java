package dev.ftb.mods.ftbskies2aerocompanion.worldgen.structure;

import com.mojang.serialization.MapCodec;
import dev.ftb.mods.ftbskies2aerocompanion.basebuffer.TeamBaseGrid;
import dev.ftb.mods.ftbskies2aerocompanion.compat.sa.IslandBiomeSelector;
import dev.ftb.mods.ftbskies2aerocompanion.worldgen.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class FloatingIslandStructure extends Structure {
    public static final MapCodec<FloatingIslandStructure> CODEC = simpleCodec(FloatingIslandStructure::new);

    private static final int MIN_RADIUS = 77;
    private static final int MAX_RADIUS = 230;
    private static final int MIN_Y = 110;
    private static final int MAX_Y = 200;

    public FloatingIslandStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        if (TeamBaseGrid.isWithinBaseExclusion(centerX, centerZ)) {
            return Optional.empty();
        }

        long worldSeed = context.seed();
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(worldSeed));
        random.setLargeFeatureSeed(worldSeed, chunkPos.x, chunkPos.z);

        int radius = MIN_RADIUS + random.nextInt(MAX_RADIUS - MIN_RADIUS + 1);
        int yRange = MAX_Y - MIN_Y + 1;
        int centerY = MIN_Y + random.nextInt(yRange);
        BlockPos center = new BlockPos(centerX, centerY, centerZ);

        long seedTag = ((long) centerX * 341873128712341L) ^ ((long) centerZ * 132897987541L) ^ worldSeed;

        Optional<Holder<Biome>> biomeOpt = IslandBiomeSelector.pick(context.registryAccess(), seedTag);
        if (biomeOpt.isEmpty()) {
            return Optional.empty();
        }
        ResourceKey<Biome> biomeKey = biomeOpt.get().unwrapKey().orElse(null);
        if (biomeKey == null) {
            return Optional.empty();
        }

        return Optional.of(new GenerationStub(center, builder ->
                builder.addPiece(new FloatingIslandPiece(center, radius, seedTag, biomeKey))
        ));
    }

    @Override
    public StructureType<?> type() {
        return ModFeatures.FLOATING_ISLAND_STRUCTURE.get();
    }
}
