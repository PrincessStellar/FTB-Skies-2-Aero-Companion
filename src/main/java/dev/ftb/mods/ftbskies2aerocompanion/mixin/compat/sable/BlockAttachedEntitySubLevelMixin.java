package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import dev.ryanhcode.sable.Sable;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Item frames (and other block-attached entities) holding items can be duped on an airship by
 * changing the contraption's state. A ship assembly/disassembly relocates the frame and its
 * support block, but the move can leave the frame's support check momentarily failing, and
 * {@code BlockAttachedEntity.tick} then discards the frame and drops its held item, while the
 * relocated copy keeps it. With a bundle in the frame, one drop is a stack's worth.
 *
 * <p>While a block-attached entity sits on a sub-level, treat its periodic survival check as
 * passing, so it does not auto-drop its item from a transient support mismatch during a move. A
 * genuine player break drops normally, and behaviour off-ship is unchanged.
 */
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
