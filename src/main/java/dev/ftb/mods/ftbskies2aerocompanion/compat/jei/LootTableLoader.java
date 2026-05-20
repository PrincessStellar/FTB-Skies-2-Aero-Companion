package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

import dev.ftb.mods.ftbskies2aerocompanion.loot.VoidFishingDropResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;

import java.util.List;

public final class LootTableLoader {
    private static volatile List<VoidFishingDrop> syncedDrops;

    private LootTableLoader() {}

    public static List<VoidFishingDrop> load() {
        List<VoidFishingDrop> cached = syncedDrops;
        if (cached != null) {
            return cached;
        }
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            return VoidFishingDropResolver.resolve(VoidFishingDropResolver.serverResolver(server));
        }
        return VoidFishingDropResolver.resolve(VoidFishingDropResolver.jarResolver());
    }

    public static void applySyncedDrops(List<VoidFishingDrop> drops) {
        syncedDrops = drops;
        JEIPlugin.refreshVoidFishing();
    }

    public static void clearSyncedDrops() {
        syncedDrops = null;
    }
}
