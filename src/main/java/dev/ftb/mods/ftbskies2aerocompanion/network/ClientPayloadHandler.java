package dev.ftb.mods.ftbskies2aerocompanion.network;

import dev.ftb.mods.ftbskies2aerocompanion.compat.jei.LootTableLoader;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void handleSyncVoidFishingDrops(SyncVoidFishingDropsPayload payload, IPayloadContext context) {
        if (!ModList.get().isLoaded("jei")) return;
        context.enqueueWork(() -> LootTableLoader.applySyncedDrops(payload.drops()));
    }
}
