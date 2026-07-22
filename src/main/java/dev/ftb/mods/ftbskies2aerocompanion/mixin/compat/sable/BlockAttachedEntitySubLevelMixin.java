package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntitySubLevelMixin {

    @Shadow
    protected BlockPos pos;

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/BlockAttachedEntity;survives()Z")
    )
    private boolean ftbskies2aero$survivesOnSubLevel(BlockAttachedEntity self) {
        try {
            if (!self.level().isClientSide && Sable.HELPER.getContaining(self) != null) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return self.survives();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$acceptSubLevelAttachPos(CompoundTag tag, CallbackInfo ci) {
        try {
            BlockAttachedEntity self = (BlockAttachedEntity) (Object) this;
            if (self.level() == null || self.level().isClientSide || Sable.HELPER.getContaining(self) == null) {
                return;
            }
            this.pos = new BlockPos(tag.getInt("TileX"), tag.getInt("TileY"), tag.getInt("TileZ"));
            ci.cancel();
        } catch (Throwable ignored) {
        }
    }
}
