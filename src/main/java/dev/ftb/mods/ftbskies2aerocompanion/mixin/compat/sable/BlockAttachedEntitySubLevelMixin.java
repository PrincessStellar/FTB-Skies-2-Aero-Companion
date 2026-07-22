package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import dev.ryanhcode.sable.Sable;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntitySubLevelMixin {

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
}
