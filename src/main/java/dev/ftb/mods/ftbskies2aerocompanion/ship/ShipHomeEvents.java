package dev.ftb.mods.ftbskies2aerocompanion.ship;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class ShipHomeEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger("ShipHome");

    private static final Map<UUID, DimensionTransition> VANILLA_RESPAWN = new ConcurrentHashMap<>();

    private ShipHomeEvents() {}

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ShipHomeData.setActiveServer(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ShipHomeData.setActiveServer(null);
        VANILLA_RESPAWN.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void captureVanillaRespawn(PlayerRespawnPositionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getRespawnPosition() == null) return;
        DimensionTransition vanilla = event.getDimensionTransition();
        if (vanilla == null) return;
        VANILLA_RESPAWN.put(player.getUUID(), vanilla);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRespawnPos(PlayerRespawnPositionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ShipHomeData data = ShipHomeData.get(server);
        Optional<ShipBinding> binding = data.getBed(player.getUUID());
        LOGGER.debug("[respawnEvent] player={} hasBinding={} currentTarget={}",
                player.getName().getString(), binding.isPresent(),
                event.getDimensionTransition() == null ? "null"
                        : event.getDimensionTransition().newLevel().dimension().location()
                          + "@" + event.getDimensionTransition().pos());
        if (binding.isEmpty()) {
            DimensionTransition vanilla = VANILLA_RESPAWN.remove(player.getUUID());
            if (vanilla != null) {
                LOGGER.debug("[respawnEvent] restoring vanilla bed/anchor respawn for {} -> {}@{} (overriding lobby override)",
                        player.getName().getString(), vanilla.newLevel().dimension().location(), vanilla.pos());
                event.setDimensionTransition(vanilla);
            }
            return;
        }
        DimensionTransition vanilla = VANILLA_RESPAWN.remove(player.getUUID());
        Optional<ShipBindings.Resolved> resolved = ShipBindings.resolveAnchor(server, binding.get());
        if (resolved.isEmpty()) {
            if (vanilla != null) {
                LOGGER.debug("[respawnEvent] anchor uuid={} unresolved — restoring vanilla bed/anchor respawn -> {}@{}",
                        binding.get().shipUuid(), vanilla.newLevel().dimension().location(), vanilla.pos());
                event.setDimensionTransition(vanilla);
            } else {
                LOGGER.warn("[respawnEvent] anchor uuid={} unresolved and no vanilla respawn captured — leaving current respawn target unchanged",
                        binding.get().shipUuid());
            }
            return;
        }
        ServerLevel shipLevel = server.getLevel(binding.get().shipDimension());
        if (shipLevel == null) return;
        ShipBindings.Resolved r = resolved.get();
        LOGGER.debug("[respawnEvent] OVERRIDING to ship pos={} yaw={}", r.worldPos(), r.yaw());
        event.setDimensionTransition(new DimensionTransition(
                shipLevel, r.worldPos(), Vec3.ZERO, r.yaw(), r.pitch(), DimensionTransition.DO_NOTHING));
    }
}
