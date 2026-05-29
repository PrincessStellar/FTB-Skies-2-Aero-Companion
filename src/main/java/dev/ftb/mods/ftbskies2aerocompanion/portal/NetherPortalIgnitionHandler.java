package dev.ftb.mods.ftbskies2aerocompanion.portal;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class NetherPortalIgnitionHandler {

    private static final String IRREGULAR_IMPLEMENTS = "irregular_implements";
    private static final ResourceLocation BLAZE_FIRE_ID =
            ResourceLocation.fromNamespaceAndPath(IRREGULAR_IMPLEMENTS, "blaze_fire");

    private NetherPortalIgnitionHandler() {}

    @SubscribeEvent
    public static void onPortalSpawn(BlockEvent.PortalSpawnEvent event) {
        if (!ModList.get().isLoaded(IRREGULAR_IMPLEMENTS)) {
            return;
        }
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock());
        if (!BLAZE_FIRE_ID.equals(blockId)) {
            event.setCanceled(true);
        }
    }
}
