package dev.ftb.mods.ftbskies2aerocompanion.compat.mi;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.definition.FluidDefinition;
import mekanism.common.registries.MekanismFluids;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public final class MIMekSteam {
    public static final ResourceLocation MI_STEAM_ID = ResourceLocation.fromNamespaceAndPath("modern_industrialization", "steam");
    public static final ResourceLocation MEK_STEAM_ID = ResourceLocation.fromNamespaceAndPath("mekanism", "steam");

    private MIMekSteam() {}

    public static Fluid mekSteamFluid() {
        return MekanismFluids.STEAM.get();
    }

    public static Fluid swapIfMiSteam(FluidDefinition def) {
        if (def == MIFluids.STEAM) {
            return mekSteamFluid();
        }
        return def.asFluid();
    }

    public static ResourceLocation swapId(ResourceLocation key) {
        return MI_STEAM_ID.equals(key) ? MEK_STEAM_ID : key;
    }
}
