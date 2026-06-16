package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.compactmachines;

import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipBindings;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipHomeData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.compactmods.machines.room.RoomHelper", remap = false)
public abstract class RoomHelperCaptureMixin {

    @Inject(method = "teleportPlayerIntoRoom", at = @At("HEAD"))
    private static void ftbskies2aero$captureShipReturn(MinecraftServer server, ServerPlayer player,
                                                        @Coerce Object roomInstance, @Coerce Object entryPoint,
                                                        CallbackInfoReturnable<?> cir) {
        if (server == null) {
            return;
        }
        ShipHomeData data = ShipHomeData.get(server);
        ShipBindings.captureForPlayer(player, player.position(), player.getYRot(), player.getXRot())
                .ifPresent(binding -> data.setCompactReturn(player.getUUID(), binding));
    }
}
