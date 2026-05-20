package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

import dev.ftb.mods.ftbskies2aerocompanion.loot.VoidFishingDropResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;

import java.util.List;

/**
 * Client-side entry point that produces the void-fishing drops list for JEI.
 *
 * <p>Priority order:
 * <ol>
 *   <li>{@link #syncedDrops}, populated by an S2C payload on player login (multiplayer source of truth).</li>
 *   <li>The integrated server's live reloadable loot tables (singleplayer source of truth).</li>
 *   <li>The static JSON shipped in the mod jar (JEI-startup fallback before any world has been entered).</li>
 * </ol>
 */
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

    /**
     * Called when the server-pushed payload arrives. Stores the authoritative list and asks JEI to refresh.
     */
    public static void applySyncedDrops(List<VoidFishingDrop> drops) {
        syncedDrops = drops;
        JEIPlugin.refreshVoidFishing();
    }

    public static void clearSyncedDrops() {
        syncedDrops = null;
    }
}
