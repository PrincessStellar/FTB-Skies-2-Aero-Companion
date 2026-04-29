package dev.ftb.mods.ftbmodpackcompanion;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(FTBModpackCompanion.MOD_ID)
public class FTBModpackCompanion {
    public static final String MOD_ID = "ftbmodpackcompanion";

    private static final Logger LOGGER = LoggerFactory.getLogger(FTBModpackCompanion.class);

    public FTBModpackCompanion(IEventBus eventBus, ModContainer container) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            eventBus.<FMLClientSetupEvent>addListener(event -> clientSetup(event, eventBus));
        }
    }

    private void clientSetup(FMLClientSetupEvent event, IEventBus eventBus) {
        // Client init
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
