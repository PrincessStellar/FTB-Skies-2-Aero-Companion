package dev.ftb.mods.ftbskies2aerocompanion.compat.sa;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BiomeSurfaceResolver {
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState SANDSTONE = Blocks.SANDSTONE.defaultBlockState();
    private static final BlockState RED_SAND = Blocks.RED_SAND.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
    private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
    private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    private static final BlockState MYCELIUM = Blocks.MYCELIUM.defaultBlockState();
    private static final BlockState PODZOL = Blocks.PODZOL.defaultBlockState();
    private static final BlockState COARSE_DIRT = Blocks.COARSE_DIRT.defaultBlockState();

    private BiomeSurfaceResolver() {}

    public static BlockState topBlock(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_BADLANDS)) return RED_SAND;
        if (biome.is(BiomeTags.HAS_DESERT_PYRAMID)) return SAND;
        if (biome.is(BiomeTags.IS_BEACH)) return SAND;
        if (biome.is(BiomeTags.IS_MOUNTAIN) && biome.value().getBaseTemperature() < 0.15f) return SNOW_BLOCK;
        if (biome.value().getBaseTemperature() < 0.15f) return SNOW_BLOCK;
        if (biome.unwrapKey().map(k -> k.location().getPath().contains("mushroom")).orElse(false)) return MYCELIUM;
        if (biome.unwrapKey().map(k -> {
            String p = k.location().getPath();
            return p.contains("taiga") || p.contains("podzol") || p.contains("old_growth");
        }).orElse(false)) return PODZOL;
        if (biome.unwrapKey().map(k -> k.location().getPath().contains("savanna")).orElse(false)) return GRASS;
        return GRASS;
    }

    public static BlockState subSurfaceBlock(Holder<Biome> biome) {
        BlockState top = topBlock(biome);
        if (top == SAND) return SANDSTONE;
        if (top == RED_SAND) return TERRACOTTA;
        if (top == SNOW_BLOCK) return DIRT;
        if (top == MYCELIUM) return DIRT;
        if (top == PODZOL) return DIRT;
        return DIRT;
    }
}
