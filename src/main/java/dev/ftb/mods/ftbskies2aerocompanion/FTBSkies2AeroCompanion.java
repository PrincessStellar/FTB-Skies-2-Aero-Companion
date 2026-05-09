package dev.ftb.mods.ftbskies2aerocompanion;

import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.CreateIntegration;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshTier;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModAeroRecipes;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModBlockEntities;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModBlocks;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.client.AeroScoopBlockEntityRenderer;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import dev.ftb.mods.ftbskies2aerocompanion.voidconversion.ModRecipes;
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
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(FTBSkies2AeroCompanion.MOD_ID)
public class FTBSkies2AeroCompanion {
    public static final String MOD_ID = "ftbskies2aerocompanion";

    private static final Logger LOGGER = LoggerFactory.getLogger(FTBSkies2AeroCompanion.class);

    public FTBSkies2AeroCompanion(IEventBus eventBus, ModContainer container) {
        ModItems.register(eventBus);
        ModBlocks.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModRecipes.register(eventBus);
        ModAeroRecipes.register(eventBus);

        eventBus.addListener(FTBSkies2AeroCompanion::onBuildCreativeTabs);
        eventBus.addListener(FTBSkies2AeroCompanion::onRegisterCapabilities);
        eventBus.addListener(FTBSkies2AeroCompanion::onCommonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            eventBus.<FMLClientSetupEvent>addListener(event -> clientSetup(event, eventBus));
            eventBus.addListener(FTBSkies2AeroCompanion::onRegisterRenderers);
        }
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.AIR_FILTER_BE.get(), AeroScoopBlockEntityRenderer::new);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CreateIntegration::register);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.AIR_FILTER_BE.get(),
                (be, side) -> be.getOutputHandlerExternal()
        );
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
            event.accept(new ItemStack(ModItems.AIR_FILTER_ITEM.get()));
            for (MeshTier tier : MeshTier.values()) {
                event.accept(new ItemStack(ModItems.MESHES.get(tier).get()));
            }
            event.accept(new ItemStack(ModItems.IRON_DUST.get()));
            event.accept(new ItemStack(ModItems.GOLD_DUST.get()));
            event.accept(new ItemStack(ModItems.DIAMOND_DUST.get()));
            event.accept(new ItemStack(ModItems.BLAZING_DUST.get()));
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
