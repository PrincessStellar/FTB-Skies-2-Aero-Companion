package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import dev.ftb.mods.ftbskies2aerocompanion.compat.arscaelum.GeoreRituals;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.hollingsworth.arsnouveau.api.registry.RitualRegistry", remap = false)
public class RitualRegistryGeoreMixin {

    @Inject(method = "getRitual", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ftbskies2aero$freshGeoreRitual(ResourceLocation id, CallbackInfoReturnable<AbstractRitual> cir) {
        AbstractRitual fresh = GeoreRituals.createFresh(id);
        if (fresh != null) {
            cir.setReturnValue(fresh);
        }
    }
}
