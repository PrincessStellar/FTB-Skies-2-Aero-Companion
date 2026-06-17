package dev.ftb.mods.ftbskies2aerocompanion.worldgen;

import com.mojang.serialization.MapCodec;
import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModBiomeModifierSerializers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, FTBSkies2AeroCompanion.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<RemoveOverworldOresBiomeModifier>> REMOVE_OVERWORLD_ORES =
            SERIALIZERS.register("remove_overworld_ores", () -> RemoveOverworldOresBiomeModifier.CODEC);

    private ModBiomeModifierSerializers() {
    }

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
