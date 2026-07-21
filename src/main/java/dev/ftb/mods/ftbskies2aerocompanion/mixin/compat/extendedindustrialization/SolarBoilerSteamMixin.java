package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.extendedindustrialization;

import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import dev.ftb.mods.ftbskies2aerocompanion.compat.mi.MIMekSteam;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Extended Industrialization's Solar Boilers produce {@code modern_industrialization:steam}, which
 * the rest of the pack's MI machines can't use (they run on {@code mekanism:steam}). The boiler locks
 * its output slot to MI steam and hands MI steam's {@link FluidVariant} to its
 * {@code SteamProductionComponent}. Redirect both through {@link MIMekSteam} so the boiler outputs
 * Mekanism steam, matching the companion's other MI steam overrides.
 */
@Mixin(targets = "net.swedz.extended_industrialization.machines.blockentity.SolarBoilerMachineBlockEntity", remap = false)
public class SolarBoilerSteamMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Laztech/modern_industrialization/definition/FluidDefinition;asFluid()Lnet/minecraft/world/level/material/Fluid;"
            )
    )
    private static Fluid ftbskies$lockSlotToMekSteam(FluidDefinition def) {
        return MIMekSteam.swapIfMiSteam(def);
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Laztech/modern_industrialization/definition/FluidDefinition;variant()Laztech/modern_industrialization/thirdparty/fabrictransfer/api/fluid/FluidVariant;"
            )
    )
    private static FluidVariant ftbskies$produceMekSteam(FluidDefinition def) {
        return MIMekSteam.swapVariantIfMiSteam(def);
    }
}
