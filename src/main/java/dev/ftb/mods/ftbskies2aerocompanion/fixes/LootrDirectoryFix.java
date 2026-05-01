package dev.ftb.mods.ftbskies2aerocompanion.fixes;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Workaround for Lootr 1.21.1 not creating world/data/lootr/ before atomic-writing its TickingData.
@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class LootrDirectoryFix {
    private static final Logger LOGGER = LoggerFactory.getLogger(LootrDirectoryFix.class);

    private LootrDirectoryFix() {}

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        Path dir = event.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve("lootr");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            LOGGER.warn("Failed to ensure Lootr data directory exists at {}", dir, e);
        }
    }
}
