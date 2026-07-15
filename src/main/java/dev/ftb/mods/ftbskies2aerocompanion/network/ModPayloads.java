package dev.ftb.mods.ftbskies2aerocompanion.network;

import dev.ftb.mods.ftbskies2aerocompanion.compat.jei.VoidFishingDrop;
import dev.ftb.mods.ftbskies2aerocompanion.loot.VoidFishingDropResolver;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

public final class ModPayloads {
    private ModPayloads() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(
                SyncVoidFishingDropsPayload.TYPE,
                SyncVoidFishingDropsPayload.STREAM_CODEC,
                ClientPayloadHandler::handleSyncVoidFishingDrops
        );
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        List<VoidFishingDrop> drops = VoidFishingDropResolver.resolveAll(server);
        if (drops.isEmpty()) return;
        PacketDistributor.sendToPlayer(player, new SyncVoidFishingDropsPayload(drops));
    }
}
