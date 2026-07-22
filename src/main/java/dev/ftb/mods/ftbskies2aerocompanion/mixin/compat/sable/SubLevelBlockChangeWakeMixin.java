package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import dev.ryanhcode.sable.SableCommonEvents;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removing a block on an airship leaves a solid, invisible "ghost" collision (reported with Simple
 * Tombs graves opened by key on a parked ship at spawn). Sable queues the sub-level's physics
 * collider change from the block-change hook but only applies it during a physics tick, and a
 * stationary ship is asleep and never ticks, so the collider keeps the removed block. Opening the
 * grave while standing on the ship happened to wake it; opening from an adjacent block with the key
 * did not.
 *
 * <p>Wake the sub-level's physics at the changed position from the block-change hook itself, so a
 * parked ship processes the queued collider update immediately and clears the ghost. Only fires for
 * blocks that are actually on a sub-level.
 */
@Mixin(value = SableCommonEvents.class, remap = false)
public abstract class SubLevelBlockChangeWakeMixin {

    @Inject(method = "handleBlockChange", at = @At("RETURN"))
    private static void ftbskies2aero$wakePhysicsForBlockChange(ServerLevel level, LevelChunk chunk, int x, int y, int z, BlockState oldState, BlockState newState, CallbackInfo ci) {
        try {
            ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
            if (container == null || container.getPlot(chunk.getPos()) == null) {
                return;
            }
            SubLevelPhysicsSystem physics = container.physicsSystem();
            if (physics != null) {
                physics.wakeUpObjectsAt(x, y, z);
            }
        } catch (Throwable ignored) {
        }
    }
}
