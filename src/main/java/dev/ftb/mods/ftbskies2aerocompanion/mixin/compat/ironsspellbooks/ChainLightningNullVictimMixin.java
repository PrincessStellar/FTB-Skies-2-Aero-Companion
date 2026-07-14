package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.ironsspellbooks;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "io.redspace.ironsspellbooks.entity.spells.ChainLightning", remap = false)
public abstract class ChainLightningNullVictimMixin {

    @Shadow
    Entity initialVictim;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = true)
    private void ftbskies2aero$guardNullVictim(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide && initialVictim == null) {
            self.discard();
            ci.cancel();
        }
    }
}
