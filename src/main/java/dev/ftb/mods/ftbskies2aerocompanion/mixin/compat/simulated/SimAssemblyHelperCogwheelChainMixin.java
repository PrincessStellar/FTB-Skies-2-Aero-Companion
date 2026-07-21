package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.simulated;

import dev.ftb.mods.ftbskies2aerocompanion.compat.bitsnbobs.CogwheelChainMoveGuard;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.simulated_team.simulated.util.SimAssemblyHelper", remap = false)
public abstract class SimAssemblyHelperCogwheelChainMixin {

    @Inject(method = "disassembleSubLevel", at = @At("HEAD"))
    private static void ftbskies2aero$beginCogwheelChainDisassemble(Level level, SubLevel subLevel, BlockPos worldPos, BlockPos assemblerPos, Rotation rotation, boolean flag, CallbackInfo ci) {
        CogwheelChainMoveGuard.enter();
    }

    @Inject(method = "disassembleSubLevel", at = @At("RETURN"))
    private static void ftbskies2aero$endCogwheelChainDisassemble(Level level, SubLevel subLevel, BlockPos worldPos, BlockPos assemblerPos, Rotation rotation, boolean flag, CallbackInfo ci) {
        CogwheelChainMoveGuard.exit();
    }
}
