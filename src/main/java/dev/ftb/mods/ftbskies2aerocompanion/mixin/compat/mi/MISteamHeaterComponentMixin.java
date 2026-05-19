package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.mi;

import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.machines.components.SteamHeaterComponent;
import dev.ftb.mods.ftbskies2aerocompanion.compat.mi.MIMekSteam;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SteamHeaterComponent.class, remap = false)
public class MISteamHeaterComponentMixin {

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Laztech/modern_industrialization/definition/FluidDefinition;asFluid()Lnet/minecraft/world/level/material/Fluid;"
            )
    )
    private Fluid ftbskies$produceMekSteam(FluidDefinition def) {
        return MIMekSteam.swapIfMiSteam(def);
    }
}
