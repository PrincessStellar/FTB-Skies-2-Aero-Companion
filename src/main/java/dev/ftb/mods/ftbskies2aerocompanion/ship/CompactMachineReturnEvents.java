package dev.ftb.mods.ftbskies2aerocompanion.ship;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Optional;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class CompactMachineReturnEvents {

    private CompactMachineReturnEvents() {
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        ShipHomeData data = ShipHomeData.get(server);
        Optional<ShipBinding> binding = data.getCompactReturn(player.getUUID());
        if (binding.isEmpty() || !event.getTo().equals(binding.get().shipDimension())) {
            return;
        }
        data.clearCompactReturn(player.getUUID());
        Optional<ShipBindings.Resolved> resolved = ShipBindings.resolveAnchor(server, binding.get());
        if (resolved.isEmpty()) {
            return;
        }
        ServerLevel level = server.getLevel(binding.get().shipDimension());
        if (level == null) {
            return;
        }
        ShipBindings.Resolved r = resolved.get();
        Vec3 pos = r.worldPos();
        player.teleportTo(level, pos.x, pos.y, pos.z, r.yaw(), r.pitch());
    }
}
