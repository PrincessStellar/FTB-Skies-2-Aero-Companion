package dev.ftb.mods.ftbskies2aerocompanion.ship;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.core.BlockPos;
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

import java.util.Optional;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class ShipHomeEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger("ShipHome");

    private ShipHomeEvents() {}

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ShipHomeData.setActiveServer(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ShipHomeData.setActiveServer(null);
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
        if (binding.isEmpty()) return;
        Optional<ShipBindings.Resolved> resolved = ShipBindings.resolveAnchor(server, binding.get());
        if (resolved.isEmpty()) {
            ServerLevel overworld = server.overworld();
            BlockPos spawn = overworld.getSharedSpawnPos();
            LOGGER.warn("[respawnEvent] FALLBACK INVOKED — anchor uuid={} could not be resolved, sending player to world spawn {} (note: in this pack, world spawn is the FTB Team Bases lobby)",
                    binding.get().shipUuid(), spawn);
            event.setDimensionTransition(new DimensionTransition(
                    overworld, Vec3.atCenterOf(spawn), Vec3.ZERO, 0f, 0f, DimensionTransition.DO_NOTHING));
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
