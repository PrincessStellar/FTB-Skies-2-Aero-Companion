package dev.ftb.mods.ftbskies2aerocompanion.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public record RemoveOverworldOresBiomeModifier(HolderSet<Biome> biomes) implements BiomeModifier {
    public static final MapCodec<RemoveOverworldOresBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(RemoveOverworldOresBiomeModifier::biomes)
    ).apply(instance, RemoveOverworldOresBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.REMOVE || !biomes.contains(biome)) {
            return;
        }
        BiomeGenerationSettingsBuilder generation = builder.getGenerationSettings();
        for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
            generation.getFeatures(step).removeIf(RemoveOverworldOresBiomeModifier::isOreFeature);
        }
    }

    private static boolean isOreFeature(Holder<PlacedFeature> placed) {
        ConfiguredFeature<?, ?> configured = placed.value().feature().value();
        if (configured.feature() != Feature.ORE && configured.feature() != Feature.SCATTERED_ORE) {
            return false;
        }
        if (!(configured.config() instanceof OreConfiguration ore)) {
            return false;
        }
        return ore.targetStates.stream()
                .anyMatch(target -> BuiltInRegistries.BLOCK.getKey(target.state.getBlock()).getPath().contains("ore"));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModBiomeModifierSerializers.REMOVE_OVERWORLD_ORES.get();
    }
}
