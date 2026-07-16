package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.create;

import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import net.createmod.catnip.data.Couple;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mixin(value = RedstoneLinkNetworkHandler.class, remap = false)
public abstract class RedstoneLinkSubLevelNetworkMixin {

    @Shadow
    @Final
    static Map<LevelAccessor, Map<Couple<RedstoneLinkNetworkHandler.Frequency>, Set<IRedstoneLinkable>>> connections;

    @Inject(method = "onLoadWorld", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$keepLiveNetworks(LevelAccessor level, CallbackInfo ci) {
        if (connections.containsKey(level)) {
            ci.cancel();
        }
    }

    @Inject(method = "networksIn", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$prepareOnDemand(LevelAccessor level, CallbackInfoReturnable<Map<Couple<RedstoneLinkNetworkHandler.Frequency>, Set<IRedstoneLinkable>>> cir) {
        if (!connections.containsKey(level)) {
            Map<Couple<RedstoneLinkNetworkHandler.Frequency>, Set<IRedstoneLinkable>> prepared = new HashMap<>();
            connections.put(level, prepared);
            cir.setReturnValue(prepared);
        }
    }
}
