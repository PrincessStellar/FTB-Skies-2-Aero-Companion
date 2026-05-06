package dev.ftb.mods.ftbskies2aerocompanion;

import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(FTBSkies2AeroCompanion.MOD_ID)
public class FTBSkies2AeroCompanion {
    public static final String MOD_ID = "ftbskies2aerocompanion";

    private static final Logger LOGGER = LoggerFactory.getLogger(FTBSkies2AeroCompanion.class);

    public FTBSkies2AeroCompanion(IEventBus eventBus, ModContainer container) {
        ModItems.register(eventBus);
        eventBus.addListener(FTBSkies2AeroCompanion::onBuildCreativeTabs);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            eventBus.<FMLClientSetupEvent>addListener(event -> clientSetup(event, eventBus));
        }
    }

    private void clientSetup(FMLClientSetupEvent event, IEventBus eventBus) {
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.VOID_FISHING_ROD.get(),
                id("cast"),
                (ClampedItemPropertyFunction) (stack, level, entity, seed) -> {
                    if (entity == null) {
                        return 0.0F;
                    }
                    boolean mainHand = entity.getMainHandItem() == stack;
                    boolean offHand = entity.getOffhandItem() == stack;
                    // If the main hand also holds a fishing rod, the off-hand rod is ignored —
                    // matches vanilla's behaviour so a player dual-wielding rods still has only one bobber.
                    if (entity.getMainHandItem().getItem() instanceof FishingRodItem) {
                        offHand = false;
                    }
                    Player player = entity instanceof Player p ? p : null;
                    return (mainHand || offHand) && player != null && player.fishing != null ? 1.0F : 0.0F;
                }
        ));
    }

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(new ItemStack(ModItems.VOID_FISHING_ROD.get()));
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
