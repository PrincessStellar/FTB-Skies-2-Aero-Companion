package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.createnewage;

import org.antarcticgardens.cna.content.electricity.wire.IRegistrateIsAFuckingShitNeverUseIt;
import org.antarcticgardens.cna.content.electricity.wire.WireType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = WireType.class, remap = false)
public interface WireTypeInvoker {
    @Invoker("<init>")
    static WireType ftbskies2aero$new(String name, int ordinal, int conductivity, IRegistrateIsAFuckingShitNeverUseIt dropProvider) {
        throw new AssertionError();
    }
}
