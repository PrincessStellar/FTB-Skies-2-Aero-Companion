package dev.ftb.mods.ftbskies2aerocompanion.ship;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.Optional;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class ShipHomeEvents {
    private ShipHomeEvents() {}

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ShipHomeData.setActiveServer(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ShipHomeData.setActiveServer(null);
    }

    @SubscribeEvent
    public static void onSetSpawn(PlayerSetSpawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ShipHomeData data = ShipHomeData.get(server);
        BlockPos newSpawn = event.getNewSpawn();
        if (newSpawn == null) {
            data.clearBed(player.getUUID());
            return;
        }
        Optional<AbstractContraptionEntity> ship = ShipBindings.findShip(player);
        if (ship.isEmpty()) {
            data.clearBed(player.getUUID());
            return;
        }
        Vec3 worldPos = newSpawn.getCenter();
        ShipBinding binding = ShipBindings.capture(ship.get(), worldPos, player.getYRot(), player.getXRot());
        data.setBed(player.getUUID(), binding);
    }
}
