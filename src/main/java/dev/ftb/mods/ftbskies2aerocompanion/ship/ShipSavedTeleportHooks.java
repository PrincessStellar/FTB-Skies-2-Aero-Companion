package dev.ftb.mods.ftbskies2aerocompanion.ship;

import dev.architectury.event.CompoundEventResult;
import dev.ftb.mods.ftbessentials.api.TeleportDestination;
import dev.ftb.mods.ftbessentials.api.event.SavedTeleportEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class ShipSavedTeleportHooks implements SavedTeleportEvent {
    private static final Logger LOG = LoggerFactory.getLogger("ShipHome");

    private ShipSavedTeleportHooks() {}

    public static void register() {
        ShipSavedTeleportHooks hooks = new ShipSavedTeleportHooks();
        SavedTeleportEvent.ADDED.register(hooks);
        SavedTeleportEvent.DELETED.register(hooks);
        SavedTeleportEvent.PRE_TELEPORT.register(ShipSavedTeleportHooks::onPreTeleport);
    }

    @Override
    public void onAdded(String name, TeleportDestination dest, ServerPlayer player, @Nullable UUID owningPlayer) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ShipHomeData data = ShipHomeData.get(server);
        String key = name.toLowerCase(Locale.ROOT);
        Optional<ShipBinding> binding = ShipBindings.captureForPlayer(player, player.position(), player.getYRot(), player.getXRot());

        if (owningPlayer != null) {
            if (binding.isPresent()) {
                data.setHome(owningPlayer, key, binding.get());
                LOG.debug("[setHome] {} name={} captured anchor={} localOffset={}",
                        player.getName().getString(), key, binding.get().shipUuid(), binding.get().localOffset());
            } else {
                data.clearHome(owningPlayer, key);
                LOG.debug("[setHome] {} name={} no sub-level/contraption — vanilla pos only",
                        player.getName().getString(), key);
            }
        } else {
            if (binding.isPresent()) {
                data.setWarp(key, binding.get());
                LOG.debug("[setWarp] {} name={} captured anchor={} localOffset={}",
                        player.getName().getString(), key, binding.get().shipUuid(), binding.get().localOffset());
            } else {
                data.clearWarp(key);
                LOG.debug("[setWarp] {} name={} no sub-level/contraption — vanilla pos only",
                        player.getName().getString(), key);
            }
        }
    }

    @Override
    public void onDeleted(String name, TeleportDestination destination, @Nullable UUID owningPlayer) {
        ShipHomeData data = ShipHomeData.current();
        if (data == null) return;
        String key = name.toLowerCase(Locale.ROOT);
        if (owningPlayer != null) {
            data.clearHome(owningPlayer, key);
        } else {
            data.clearWarp(key);
        }
    }

    private static CompoundEventResult<TeleportDestination.Outcome> onPreTeleport(String name, ServerPlayer player,
                                                                                 TeleportDestination dest, @Nullable UUID owningPlayer) {
        MinecraftServer server = player.getServer();
        if (server == null) return CompoundEventResult.pass();
        ShipHomeData data = ShipHomeData.get(server);
        String key = name.toLowerCase(Locale.ROOT);
        Optional<ShipBinding> binding = owningPlayer != null ? data.getHome(owningPlayer, key) : data.getWarp(key);
        LOG.debug("[teleport] {} name={} owner={} hasBinding={}",
                player.getName().getString(), key, owningPlayer, binding.isPresent());
        if (binding.isEmpty()) {
            return CompoundEventResult.pass();
        }

        ShipBinding b = binding.get();
        Optional<ShipBindings.Resolved> resolved = ShipBindings.resolveAnchor(server, b);
        if (resolved.isPresent()) {
            ShipBindings.Resolved r = resolved.get();
            LOG.debug("[teleport] anchor uuid={} resolved world={} yaw={}", b.shipUuid(), r.worldPos(), r.yaw());
            TeleportDestination shipDest = new TeleportDestination(
                    b.shipDimension(), BlockPos.containing(r.worldPos()),
                    Optional.of(r.yaw()), Optional.of(r.pitch()), null);
            return CompoundEventResult.interruptTrue(dest.success(shipDest));
        }

        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        LOG.warn("[teleport] anchor uuid={} unresolved — diverting to world spawn {}", b.shipUuid(), spawn);
        TeleportDestination spawnDest = new TeleportDestination(
                overworld.dimension(), spawn, Optional.empty(), Optional.empty(), null);
        return CompoundEventResult.interruptTrue(dest.success(spawnDest));
    }
}
