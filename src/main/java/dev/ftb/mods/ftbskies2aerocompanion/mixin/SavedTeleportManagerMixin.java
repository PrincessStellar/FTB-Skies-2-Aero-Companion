package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import dev.ftb.mods.ftbessentials.util.SavedTeleportManager;
import dev.ftb.mods.ftbessentials.util.TeleportPos;
import dev.ftb.mods.ftbessentials.util.WarmupCooldownTeleporter;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipBinding;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipBindings;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipHomeData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

/**
 * Makes FTB Essentials' {@code /sethome} and {@code /home} ship-aware. When a player sets a home while
 * standing on (or piloting) a Create contraption, we capture a ship-local offset alongside the home name.
 * On teleport the destination is resolved lazily from the contraption's current world transform, so the
 * home tracks the moving ship. Falls back to the server's world spawn if the contraption is gone.
 *
 * <p>The mixin only acts on instances of {@link SavedTeleportManager.HomeManager}; warps and other
 * subclasses are passed through untouched.
 */
@Mixin(SavedTeleportManager.class)
public abstract class SavedTeleportManagerMixin {

    @Inject(method = "addDestination", at = @At("HEAD"))
    private void ftbskies2aero$captureBinding(String name, TeleportPos pos, ServerPlayer player, CallbackInfo ci) {
        UUID owner = ftbskies2aero$ownerUuid();
        if (owner == null) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ShipHomeData data = ShipHomeData.get(server);
        Optional<AbstractContraptionEntity> ship = ShipBindings.findShip(player);
        if (ship.isEmpty()) {
            data.clearHome(owner, name);
            return;
        }
        ShipBinding binding = ShipBindings.capture(ship.get(), player.position(), player.getYRot(), player.getXRot());
        data.setHome(owner, name, binding);
    }

    @Inject(method = "deleteDestination", at = @At("HEAD"))
    private void ftbskies2aero$clearBinding(String name, CallbackInfoReturnable<Boolean> cir) {
        UUID owner = ftbskies2aero$ownerUuid();
        if (owner == null) return;
        ShipHomeData data = ShipHomeData.current();
        if (data != null) {
            data.clearHome(owner, name);
        }
    }

    @Inject(method = "teleportTo", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$divertTeleport(String name, ServerPlayer player, WarmupCooldownTeleporter teleporter, CallbackInfoReturnable<TeleportPos.TeleportResult> cir) {
        UUID owner = ftbskies2aero$ownerUuid();
        if (owner == null) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ShipHomeData data = ShipHomeData.get(server);
        Optional<ShipBinding> binding = data.getHome(owner, name);
        if (binding.isEmpty()) return;
        TeleportPos.TeleportResult result = teleporter.teleport(player, p -> {
            MinecraftServer s = p.getServer();
            return ShipBindings.resolve(s, binding.get()).orElseGet(() -> ShipBindings.worldSpawnFallback(s));
        });
        cir.setReturnValue(result);
    }

    @Unique
    private UUID ftbskies2aero$ownerUuid() {
        Object self = this;
        if (!(self instanceof SavedTeleportManager.HomeManager hm)) return null;
        return ((HomeManagerAccessor) hm).ftbskies2aero$getPlayerData().getUuid();
    }
}
