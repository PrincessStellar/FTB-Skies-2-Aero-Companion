package dev.ftb.mods.ftbskies2aerocompanion;

import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.CreateIntegration;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshTier;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModAeroRecipes;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModBlockEntities;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModBlocks;
import dev.ftb.mods.ftbskies2aerocompanion.bucket.ModBucketComponents;
import dev.ftb.mods.ftbskies2aerocompanion.bucket.WoodenBucketFluidHandler;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import dev.ftb.mods.ftbskies2aerocompanion.voidconversion.ModRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(FTBSkies2AeroCompanion.MOD_ID)
public class FTBSkies2AeroCompanion {
    public static final String MOD_ID = "ftbskies2aerocompanion";

    public FTBSkies2AeroCompanion(IEventBus eventBus, ModContainer container) {
        ModItems.register(eventBus);
        ModBlocks.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModRecipes.register(eventBus);
        ModAeroRecipes.register(eventBus);
        ModBucketComponents.register(eventBus);

        eventBus.addListener(FTBSkies2AeroCompanion::onBuildCreativeTabs);
        eventBus.addListener(FTBSkies2AeroCompanion::onRegisterCapabilities);
        eventBus.addListener(FTBSkies2AeroCompanion::onCommonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            dev.ftb.mods.ftbskies2aerocompanion.client.ClientBootstrap.init(eventBus);
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

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(new ItemStack(ModItems.VOID_FISHING_ROD.get()));
            event.accept(new ItemStack(ModItems.AIR_FILTER_ITEM.get()));
            event.accept(new ItemStack(ModItems.WOODEN_BUCKET.get()));
            for (MeshTier tier : MeshTier.values()) {
                event.accept(new ItemStack(ModItems.MESHES.get(tier).get()));
            }
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
