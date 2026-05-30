package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.createnewage;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.compat.createnewage.CnaAddon;
import dev.ftb.mods.ftbskies2aerocompanion.compat.createnewage.CnaAddonConfig;
import net.minecraft.resources.ResourceLocation;
import org.antarcticgardens.cna.content.electricity.wire.WireType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(value = WireType.class, remap = false)
public class WireTypeMixin {
    @Shadow
    @Final
    @Mutable
    private static WireType[] $VALUES;

    @Inject(method = "<clinit>", at = @At("TAIL"), require = 0, expect = 0)
    private static void aero$addWires(CallbackInfo ci) {
        WireType[] old = $VALUES;
        int base = old.length;
        String[] names = {
                "OVERCHARGED_COPPER",
                "OVERCHARGED_NETHERITE",
                "OVERCHARGED_PLATINUM",
                "OVERCHARGED_TITANIUM"
        };
        WireType[] expanded = Arrays.copyOf(old, base + names.length);
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            expanded[base + i] = WireTypeInvoker.ftbskies2aero$new(name, base + i, 0, () -> CnaAddon.wireDropFor(name));
        }
        $VALUES = expanded;
    }

    @Inject(method = "getConductivity", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void aero$conductivity(CallbackInfoReturnable<Long> cir) {
        WireType self = (WireType) (Object) this;
        if (CnaAddon.isAeroWire(self.name())) {
            cir.setReturnValue(CnaAddonConfig.conductivityFor(self.name()));
        }
    }

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void aero$texture(CallbackInfoReturnable<ResourceLocation> cir) {
        WireType self = (WireType) (Object) this;
        if (CnaAddon.isAeroWire(self.name())) {
            cir.setReturnValue(ResourceLocation.fromNamespaceAndPath(
                    FTBSkies2AeroCompanion.MOD_ID, "textures/wire/" + self.name().toLowerCase() + ".png"));
        }
    }
}
