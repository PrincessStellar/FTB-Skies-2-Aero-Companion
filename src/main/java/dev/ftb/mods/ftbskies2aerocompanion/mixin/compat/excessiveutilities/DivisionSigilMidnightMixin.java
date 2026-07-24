package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.excessiveutilities;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "dev.aaronhowser.mods.excessive_utilities.handler.division_sigil.DivisionSigilActivation", remap = false)
public abstract class DivisionSigilMidnightMixin {

    @ModifyExpressionValue(
            method = "checkActivationTime",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getDayTime()J")
    )
    private long ftbskies2aero$wrapDayTime(long original) {
        return original % 24000L;
    }
}
