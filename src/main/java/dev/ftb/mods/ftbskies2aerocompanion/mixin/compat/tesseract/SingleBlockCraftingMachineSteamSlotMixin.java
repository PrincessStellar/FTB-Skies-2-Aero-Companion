package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.tesseract;

import dev.ftb.mods.ftbskies2aerocompanion.compat.mi.MIMekSteam;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;

@Mixin(targets = "net.swedz.tesseract.neoforge.compat.mi.machine.builder.SingleBlockCraftingMachineBuilder", remap = false)
public class SingleBlockCraftingMachineSteamSlotMixin {

    @ModifyArg(
            method = "lambda$internalRegister$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/swedz/tesseract/neoforge/compat/mi/machine/builder/slots/MachineSlotConfiguration$Builder;fluidInput(IILjava/util/function/Supplier;I)Lnet/swedz/tesseract/neoforge/compat/mi/machine/builder/slots/MachineSlotConfiguration$Builder;"
            ),
            index = 2
    )
    private Supplier<Fluid> ftbskies2aero$lockSteamSlotToMekSteam(Supplier<Fluid> original) {
        return () -> MIMekSteam.swapIfMiSteamFluid(original.get());
    }
}
