package dev.ftb.mods.ftbskies2aerocompanion.compat.sa;

import net.minecraft.util.RandomSource;
import org.sathrek.sky_archipelago.config.SkyIslandSettings;
import org.sathrek.sky_archipelago.worldgen.generator.field.IslandShapeArchetype;

public final class IslandArchetypeSelector {
    private IslandArchetypeSelector() {}

    public static IslandShapeArchetype pick(SkyIslandSettings settings, long seedTag) {
        try {
            double totalWeight = 0.0;
            for (IslandShapeArchetype archetype : IslandShapeArchetype.values()) {
                if (settings.isArchetypeEnabled(archetype)) {
                    totalWeight += Math.max(0.0, settings.archetypeWeight(archetype));
                }
            }
            if (totalWeight <= 0.0) {
                return IslandShapeArchetype.CLASSIC;
            }

            RandomSource random = RandomSource.create(seedTag ^ 0x5C0FF15L);
            double roll = random.nextDouble() * totalWeight;
            for (IslandShapeArchetype archetype : IslandShapeArchetype.values()) {
                if (!settings.isArchetypeEnabled(archetype)) {
                    continue;
                }
                roll -= Math.max(0.0, settings.archetypeWeight(archetype));
                if (roll < 0.0) {
                    return archetype;
                }
            }
            return IslandShapeArchetype.CLASSIC;
        } catch (Throwable ignored) {
            return IslandShapeArchetype.CLASSIC;
        }
    }
}
