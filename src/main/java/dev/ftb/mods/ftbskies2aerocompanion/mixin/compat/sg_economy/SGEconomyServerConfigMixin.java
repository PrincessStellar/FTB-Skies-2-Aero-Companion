package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sg_economy;

import net.neoforged.fml.event.config.ModConfigEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.sirgrantd.sg_economy.config.ServerConfig", remap = false)
public class SGEconomyServerConfigMixin {

    @Inject(
            method = "onLoad(Lnet/neoforged/fml/event/config/ModConfigEvent;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            expect = 0
    )
    private static void ftbskies2$skipOnUnload(ModConfigEvent event, CallbackInfo ci) {
        if (event instanceof ModConfigEvent.Unloading) {
            ci.cancel();
        }
    }
}
