package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A bad Sable/Simulated assembly can leave a chunk saved with a block entity whose type does not
 * match the block at that position (e.g. a {@code simulated:throttle_lever} block entity where the
 * block is now {@code minecraft:oak_log}). On chunk load the block entity constructor's
 * {@code validateBlockState} throws {@code IllegalStateException} and crashes the server, and the
 * crash re-fires every time that chunk loads (for example via {@code /home}).
 *
 * <p>Skip a block entity that fails to load instead of propagating the exception. The mismatched
 * block entity is dropped and the block stays, so the chunk loads and the base is reachable again.
 */
@Mixin(BlockEntity.class)
public abstract class InvalidBlockEntityLoadGuardMixin {

    @Unique
    private static final Logger ftbskies2aero$LOGGER = LogUtils.getLogger();

    @WrapMethod(method = "loadStatic")
    private static BlockEntity ftbskies2aero$skipInvalidBlockEntity(BlockPos pos, BlockState state, CompoundTag tag, HolderLookup.Provider registries, Operation<BlockEntity> original) {
        try {
            return original.call(pos, state, tag, registries);
        } catch (Throwable t) {
            ftbskies2aero$LOGGER.warn("Dropping invalid block entity at {} (block {}) on load: {}", pos, state, t.toString());
            return null;
        }
    }
}
