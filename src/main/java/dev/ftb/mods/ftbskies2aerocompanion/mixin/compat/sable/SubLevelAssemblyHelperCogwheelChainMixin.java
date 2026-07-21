package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import dev.ftb.mods.ftbskies2aerocompanion.compat.bitsnbobs.CogwheelChainMoveGuard;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SubLevelAssemblyHelper.class, remap = false)
public abstract class SubLevelAssemblyHelperCogwheelChainMixin {

    @Inject(method = "moveBlocks", at = @At("HEAD"))
    private static void ftbskies2aero$beginCogwheelChainMove(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci) {
        CogwheelChainMoveGuard.enter();
    }

    @Inject(method = "moveBlocks", at = @At("RETURN"))
    private static void ftbskies2aero$endCogwheelChainMove(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci) {
        CogwheelChainMoveGuard.exit();
    }
}
