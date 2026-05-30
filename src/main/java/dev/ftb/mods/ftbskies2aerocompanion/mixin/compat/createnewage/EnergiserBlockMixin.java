package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.createnewage;

import dev.ftb.mods.ftbskies2aerocompanion.compat.createnewage.CnaAddonConfig;
import org.antarcticgardens.cna.content.energising.EnergiserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EnergiserBlock.class, remap = false)
public class EnergiserBlockMixin {
    @Inject(method = "getStrength(I)I", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private static void aero$strength(int tier, CallbackInfoReturnable<Integer> cir) {
        if (tier >= CnaAddonConfig.TIER_NETHERITE) {
            cir.setReturnValue(CnaAddonConfig.speedFor(tier));
        }
    }

    @Inject(method = "getCapacity(I)J", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private static void aero$capacity(int tier, CallbackInfoReturnable<Long> cir) {
        if (tier >= CnaAddonConfig.TIER_NETHERITE) {
            cir.setReturnValue(CnaAddonConfig.capacityFor(tier));
        }
    }
}
