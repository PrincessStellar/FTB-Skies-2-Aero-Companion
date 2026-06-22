package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.create;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.simibubi.create.foundation.blockEntity.SmartBlockEntity", remap = false)
public abstract class SmartBlockEntityVoidGuardMixin {
    private static final Logger FTBSKIES2AERO$LOGGER = LogUtils.getLogger();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$skipOrphanedSubLevelTick(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return;
        }
        BlockState worldState = level.getBlockState(self.getBlockPos());
        if (!self.getType().isValid(worldState)) {
            FTBSKIES2AERO$LOGGER.warn(
                    "Dropping orphaned Create block entity {} at {} (world block is {}) to avoid a validateBlockState crash; likely a Sable sub-level teardown left it behind",
                    self.getType(), self.getBlockPos(), worldState);
            self.setRemoved();
            ci.cancel();
        }
    }
}
