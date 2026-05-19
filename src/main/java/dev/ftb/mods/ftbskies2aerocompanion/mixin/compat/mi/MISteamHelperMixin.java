package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.mi;

import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.machines.helper.SteamHelper;
import dev.ftb.mods.ftbskies2aerocompanion.compat.mi.MIMekSteam;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SteamHelper.class, remap = false)
public class MISteamHelperMixin {

    @Redirect(
            method = "consumeSteamEu",
            at = @At(
                    value = "INVOKE",
                    target = "Laztech/modern_industrialization/definition/FluidDefinition;asFluid()Lnet/minecraft/world/level/material/Fluid;"
            )
    )
    private static Fluid ftbskies$swapToMekSteam(FluidDefinition def) {
        return MIMekSteam.swapIfMiSteam(def);
    }
}
