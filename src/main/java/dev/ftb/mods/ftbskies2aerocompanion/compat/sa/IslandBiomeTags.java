package dev.ftb.mods.ftbskies2aerocompanion.compat.sa;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class IslandBiomeTags {
    public static final TagKey<Biome> HAS_FLOATING_ISLAND_BIOME = TagKey.create(
            Registries.BIOME,
            FTBSkies2AeroCompanion.id("has_floating_island_biome")
    );

    private IslandBiomeTags() {}
}
