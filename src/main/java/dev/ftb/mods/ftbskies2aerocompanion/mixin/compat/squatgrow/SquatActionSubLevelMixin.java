package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.squatgrow;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "dev.wuffs.squatgrow.SquatAction", remap = false)
public abstract class SquatActionSubLevelMixin {

    @ModifyExpressionValue(
            method = "grow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;blockPosition()Lnet/minecraft/core/BlockPos;")
    )
    private static BlockPos ftbskies2aero$subLevelOrigin(BlockPos original, @Local(argsOnly = true) ServerPlayer player) {
        try {
            if (Sable.HELPER.getContaining(player) != null) {
                return original;
            }
            SubLevel sub = Sable.HELPER.getTrackingOrVehicleSubLevel(player);
            if (sub == null) {
                return original;
            }
            Pose3dc pose = sub.logicalPose();
            if (pose == null) {
                return original;
            }
            Vec3 local = pose.transformPositionInverse(player.position());
            return BlockPos.containing(local);
        } catch (Throwable ignored) {
            return original;
        }
    }
}
