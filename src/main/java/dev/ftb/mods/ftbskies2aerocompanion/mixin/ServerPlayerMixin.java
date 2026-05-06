package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipBinding;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipBindings;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipHomeData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Redirects bed-respawn directly to a ship-bound location when the player has a ship binding,
 * so the player materialises on the ship in a single step instead of vanilla teleporting them
 * to the (now empty) bed pos and a follow-up handler porting them again.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$shipAwareRespawn(boolean isAlive, DimensionTransition.PostDimensionTransition post,
                                                CallbackInfoReturnable<DimensionTransition> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        MinecraftServer server = self.getServer();
        if (server == null) return;

        ShipHomeData data = ShipHomeData.get(server);
        Optional<ShipBinding> bindingOpt = data.getBed(self.getUUID());
        if (bindingOpt.isEmpty()) return;
        ShipBinding binding = bindingOpt.get();

        Optional<Vec3> shipWorld = ShipBindings.resolveWorldPos(server, binding);
        if (shipWorld.isPresent()) {
            ServerLevel shipLevel = server.getLevel(binding.shipDimension());
            cir.setReturnValue(new DimensionTransition(
                    shipLevel, shipWorld.get(), Vec3.ZERO, binding.yaw(), binding.pitch(), post));
            return;
        }

        cir.setReturnValue(DimensionTransition.missingRespawnBlock(server.overworld(), self, post));
    }
}
