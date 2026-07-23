package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.ryanhcode.sable.sublevel.storage.region.SubLevelStorageFile", remap = false)
public abstract class SubLevelStorageFileFsyncMixin {

    @Inject(method = "flush", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$honorSyncChunkWrites(CallbackInfo ci) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && !server.forceSynchronousWrites()) {
            ci.cancel();
        }
    }
}
