package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.cognition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.cyanogen.cognition.item.ProtectionSalveItem", remap = false)
public abstract class ProtectionSalveItemMixin {

    @Inject(
            method = "handleEntity",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void ftbskies2aero$skipDebugEntityDump(CallbackInfo ci) {
        ci.cancel();
    }
}
