package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.titanium;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(targets = "com.hrznstudio.titanium.network.locator.instance.TileEntityLocatorInstance", remap = false)
public abstract class TileEntityLocatorSubLevelMixin {

    @Shadow
    private BlockPos blockPos;

    @Inject(method = "locale", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$subLevelLocale(Player player, CallbackInfoReturnable<Optional<?>> cir) {
        if (blockPos == null) {
            return;
        }
        SubLevel subLevel = player.level().isClientSide()
                ? Sable.HELPER.getContainingClient(player)
                : Sable.HELPER.getContaining(player);
        if (subLevel == null) {
            return;
        }
        BlockEntity blockEntity = subLevel.getLevel().getBlockEntity(blockPos);
        if (blockEntity != null) {
            cir.setReturnValue(Optional.of(blockEntity));
        }
    }
}
