package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import dev.ftb.mods.ftbessentials.util.SavedTeleportManager;
import dev.ftb.mods.ftbessentials.util.TeleportPos;
import dev.ftb.mods.ftbessentials.util.WarmupCooldownTeleporter;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipBinding;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipBindings;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipHomeData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(SavedTeleportManager.class)
public abstract class SavedTeleportManagerMixin {
    @Unique
    private static final Logger ftbskies2aero$LOG = LoggerFactory.getLogger("ShipHome");

    @Inject(method = "addDestination", at = @At("HEAD"))
    private void ftbskies2aero$captureBinding(String name, TeleportPos pos, ServerPlayer player, CallbackInfo ci) {
        UUID owner = ftbskies2aero$ownerUuid();
        if (owner == null) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ShipHomeData data = ShipHomeData.get(server);
        ShipBindings.dumpDiagnostics("setHome", player);
        ShipBindings.dumpDiagnostics("setHome", player);
        Optional<ShipBinding> binding = ShipBindings.captureForPlayer(player, player.position(),
                player.getYRot(), player.getXRot());
        if (binding.isEmpty()) {
            data.clearHome(owner, name);
            ftbskies2aero$LOG.debug("[setHome] {} name={} no sub-level/contraption — vanilla pos only",
                    player.getName().getString(), name);
            return;
        }
        data.setHome(owner, name, binding.get());
        ftbskies2aero$LOG.debug("[setHome] {} name={} captured anchor={} localOffset={}",
                player.getName().getString(), name, binding.get().shipUuid(), binding.get().localOffset());
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
        ftbskies2aero$LOG.debug("[home] {} name={} hasBinding={}",
                player.getName().getString(), name, binding.isPresent());
        if (binding.isEmpty()) return;
        ShipBinding b = binding.get();
        Optional<ShipBindings.Resolved> resolved = ShipBindings.resolveAnchor(server, b);
        ftbskies2aero$LOG.debug("[home] anchor uuid={} resolved={}",
                b.shipUuid(), resolved.map(r -> r.worldPos() + " yaw=" + r.yaw()).orElse("none"));
        TeleportPos.TeleportResult result = teleporter.teleport(player, p -> {
            MinecraftServer s = p.getServer();
            return ShipBindings.resolve(s, b).orElseGet(() -> ShipBindings.worldSpawnFallback(s));
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
