package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.craftingstationjei;

import dev.ftb.mods.ftbskies2aerocompanion.compat.sable.SubLevelMoveGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.leclowndu93150.craftingstationjei.block.CraftingStationBlock", remap = false)
public abstract class CraftingStationDropGuardMixin {

    @Redirect(
            method = "onRemove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/Containers;dropContents(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/Container;)V"
            )
    )
    private void ftbskies2aero$skipDropDuringSubLevelMove(Level level, BlockPos pos, Container container) {
        if (SubLevelMoveGuard.isActive()) {
            return;
        }
        Containers.dropContents(level, pos, container);
    }
}
