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
