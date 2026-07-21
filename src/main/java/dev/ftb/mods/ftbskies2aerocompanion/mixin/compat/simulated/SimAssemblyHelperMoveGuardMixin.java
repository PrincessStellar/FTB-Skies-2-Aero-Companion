package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.simulated;

import dev.ftb.mods.ftbskies2aerocompanion.compat.sable.SubLevelMoveGuard;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.simulated_team.simulated.util.SimAssemblyHelper", remap = false)
public abstract class SimAssemblyHelperMoveGuardMixin {

    @Inject(method = "disassembleSubLevel", at = @At("HEAD"))
    private static void ftbskies2aero$beginSubLevelDisassemble(Level level, SubLevel subLevel, BlockPos worldPos, BlockPos assemblerPos, Rotation rotation, boolean flag, CallbackInfo ci) {
        SubLevelMoveGuard.enter();
    }

    @Inject(method = "disassembleSubLevel", at = @At("RETURN"))
    private static void ftbskies2aero$endSubLevelDisassemble(Level level, SubLevel subLevel, BlockPos worldPos, BlockPos assemblerPos, Rotation rotation, boolean flag, CallbackInfo ci) {
        SubLevelMoveGuard.exit();
    }
}
