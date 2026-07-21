package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.bitsnbobs;

import dev.ftb.mods.ftbskies2aerocompanion.compat.sable.SubLevelMoveGuard;
import dev.ryanhcode.sable.Sable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bits N' Bobs cogwheel chains dupe when an airship carrying them is assembled/disassembled.
 * {@code CogwheelChain.checkIntegrity} locates each cogwheel by a fixed {@code localPos} offset
 * (and axis) captured in the ship's build frame; Sable can assemble the ship at a different
 * rotation, so those offsets no longer resolve and {@code lazyTick} calls {@code destroyChain},
 * which refunds the spanning chain items and tears the connection down. The chain re-forms from
 * the moved blocks, so each cycle drops another set of refund chains.
 *
 * <p>Suppress {@code destroyChain} only for that spurious teardown: while a sub-level move is in
 * progress (the removal on both ends of the move), and when {@code lazyTick}'s integrity check
 * trips while the block sits on a sub-level. A genuine player break routes through {@code destroy}
 * rather than {@code lazyTick}, so breaking a cogwheel chain still refunds normally, on the ground
 * or on a ship.
 */
@Mixin(targets = "com.kipti.bnb.content.cogwheel_chain.block.CogwheelChainBlockEntity", remap = false)
public abstract class CogwheelChainBlockEntityMixin {

    @Unique
    private static final ThreadLocal<int[]> ftbskies2aero$lazyTickDepth = ThreadLocal.withInitial(() -> new int[1]);

    @Inject(method = "lazyTick", at = @At("HEAD"), remap = false)
    private void ftbskies2aero$beginLazyTick(CallbackInfo ci) {
        ftbskies2aero$lazyTickDepth.get()[0]++;
    }

    @Inject(method = "lazyTick", at = @At("RETURN"), remap = false)
    private void ftbskies2aero$endLazyTick(CallbackInfo ci) {
        int[] depth = ftbskies2aero$lazyTickDepth.get();
        if (depth[0] > 0) {
            depth[0]--;
        }
    }

    @Inject(method = "destroyChain", at = @At("HEAD"), cancellable = true, remap = false)
    private void ftbskies2aero$suppressSpuriousChainRefund(boolean drop, CallbackInfoReturnable<ItemStack> cir) {
        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        try {
            if (SubLevelMoveGuard.isActive()) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
            if (ftbskies2aero$lazyTickDepth.get()[0] > 0 && Sable.HELPER.getContaining(self) != null) {
                cir.setReturnValue(ItemStack.EMPTY);
            }
        } catch (Throwable ignored) {
        }
    }
}
