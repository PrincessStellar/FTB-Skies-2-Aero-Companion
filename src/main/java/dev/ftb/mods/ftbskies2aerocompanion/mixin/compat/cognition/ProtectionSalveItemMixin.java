package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.cognition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.PrintStream;

@Mixin(targets = "com.cyanogen.cognition.item.ProtectionSalveItem", remap = false)
public abstract class ProtectionSalveItemMixin {

    @Redirect(
            method = "handleEntity",
            at = @At(value = "INVOKE", target = "Ljava/io/PrintStream;println(Ljava/lang/Object;)V"),
            require = 0
    )
    private static void ftbskies2aero$suppressDebugEntityDump(PrintStream stream, Object message) {
    }
}
