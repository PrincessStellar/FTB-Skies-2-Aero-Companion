package dev.ftb.mods.ftbskies2aerocompanion;

import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.CreateIntegration;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModAeroRecipes;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModBlockEntities;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModBlocks;
import dev.ftb.mods.ftbskies2aerocompanion.basebuffer.BaseExclusionConfig;
import dev.ftb.mods.ftbskies2aerocompanion.bucket.ModBucketComponents;
import dev.ftb.mods.ftbskies2aerocompanion.bucket.WoodenBucketFluidHandler;
import dev.ftb.mods.ftbskies2aerocompanion.client.ClientBootstrap;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import dev.ftb.mods.ftbskies2aerocompanion.network.ModPayloads;
import dev.ftb.mods.ftbskies2aerocompanion.skybound_anchor.ModSkyboundAnchorBlocks;
import dev.ftb.mods.ftbskies2aerocompanion.skybound_anchor.SkyboundAnchorConfig;
import dev.ftb.mods.ftbskies2aerocompanion.worldgen.registry.ModFeatures;
import dev.ftb.mods.ftbskies2aerocompanion.voidconversion.ModRecipes;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(FTBSkies2AeroCompanion.MOD_ID)
public class FTBSkies2AeroCompanion {
    public static final String MOD_ID = "ftbskies2aerocompanion";

    public FTBSkies2AeroCompanion(IEventBus eventBus, ModContainer container) {
        ModItems.register(eventBus);
        ModBlocks.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModSkyboundAnchorBlocks.register(eventBus);
        ModRecipes.register(eventBus);
        ModAeroRecipes.register(eventBus);
        ModBucketComponents.register(eventBus);
        ModCreativeTabs.register(eventBus);
        ModFeatures.register(eventBus);

        container.registerConfig(ModConfig.Type.SERVER, SkyboundAnchorConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, BaseExclusionConfig.SPEC, "ftbskies2aerocompanion-base-exclusion.toml");

        eventBus.addListener(FTBSkies2AeroCompanion::onRegisterCapabilities);
        eventBus.addListener(FTBSkies2AeroCompanion::onCommonSetup);
        eventBus.addListener(ModPayloads::register);

        NeoForge.EVENT_BUS.addListener(ModPayloads::onPlayerLoggedIn);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientBootstrap.init(eventBus);
        }
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
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, ctx) -> new WoodenBucketFluidHandler(stack),
                ModItems.WOODEN_BUCKET.get()
        );
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
