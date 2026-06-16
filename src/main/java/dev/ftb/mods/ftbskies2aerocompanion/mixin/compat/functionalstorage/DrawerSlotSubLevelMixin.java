package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.functionalstorage;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(targets = "com.buuz135.functionalstorage.block.Drawer", remap = false)
public abstract class DrawerSlotSubLevelMixin {

    @Shadow
    public abstract Collection<VoxelShape> getHitShapes(BlockState state);

    @ModifyExpressionValue(
            method = {"useItemOn", "useWithoutItem"},
            at = @At(value = "INVOKE", target = "Lcom/buuz135/functionalstorage/block/Drawer;getHit(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;)I")
    )
    private int ftbskies2aero$slotFromSubLevelHit(int slot,
                                                  @Local(argsOnly = true) BlockState state,
                                                  @Local(argsOnly = true) BlockPos pos,
                                                  @Local(argsOnly = true) Player player,
                                                  @Local(argsOnly = true) BlockHitResult hit) {
        if (Sable.HELPER.getContaining(player) == null) {
            return slot;
        }
        Vec3 rel = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        int index = 0;
        for (VoxelShape shape : getHitShapes(state)) {
            for (AABB box : shape.toAabbs()) {
                if (box.inflate(0.01).contains(rel)) {
                    return index;
                }
            }
            index++;
        }
        return slot;
    }
}
