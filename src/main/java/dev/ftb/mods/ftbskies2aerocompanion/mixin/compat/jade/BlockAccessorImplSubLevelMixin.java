package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.jade;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Jade refuses to serve block-entity tooltip data when the target is out of interaction
 * range: its server handler bails on
 * {@code targetPos.distSqr(player.blockPosition()) > (blockInteractionRange + 21)^2}.
 * Blocks Sable has moved into a sub-level live at the plot's far-offset coordinates, so
 * that raw distance is enormous and Jade drops the request — block-entity blocks
 * (Variable Store, cable parts) show nothing while block-entity-less blocks still work
 * because they need no server data.
 *
 * <p>When the target is inside a sub-level, report the distance as in-range; Sable
 * already validates real interaction reach via {@code Player.canInteractWithBlock}.
 */
@Mixin(targets = "snownee.jade.impl.BlockAccessorImpl", remap = false)
public abstract class BlockAccessorImplSubLevelMixin {

    @Redirect(
            method = "lambda$handleRequest$0",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;distSqr(Lnet/minecraft/core/Vec3i;)D"),
            require = 0, expect = 0)
    private static double ftbskies2aero$subLevelDistSqr(BlockPos targetPos, Vec3i playerPos, @Local(argsOnly = true) ServerPlayer player) {
        if (Sable.HELPER.getContaining(player.serverLevel(), targetPos) != null) {
            return 0.0D;
        }
        return targetPos.distSqr(playerPos);
    }
}
