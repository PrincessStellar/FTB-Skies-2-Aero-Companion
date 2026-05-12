package dev.ftb.mods.ftbskies2aerocompanion.client;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModBlockEntities;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.client.AeroScoopBlockEntityRenderer;
import dev.ftb.mods.ftbskies2aerocompanion.bucket.ModBucketComponents;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

public final class ClientBootstrap {
    private ClientBootstrap() {}

    public static void init(IEventBus eventBus) {
        eventBus.addListener(ClientBootstrap::onClientSetup);
        eventBus.addListener(ClientBootstrap::onRegisterRenderers);
        if (ModList.get().isLoaded("create")) {
            AeroScoopPonderBoot.register();
        }
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.AIR_FILTER_BE.get(), AeroScoopBlockEntityRenderer::new);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.VOID_FISHING_ROD.get(),
                    FTBSkies2AeroCompanion.id("cast"),
                    (ClampedItemPropertyFunction) (stack, level, entity, seed) -> {
                        if (entity == null) {
                            return 0.0F;
                        }
                        boolean mainHand = entity.getMainHandItem() == stack;
                        boolean offHand = entity.getOffhandItem() == stack;
                        if (entity.getMainHandItem().getItem() instanceof FishingRodItem) {
                            offHand = false;
                        }
                        Player player = entity instanceof Player p ? p : null;
                        return (mainHand || offHand) && player != null && player.fishing != null ? 1.0F : 0.0F;
                    }
            );

            ItemProperties.register(
                    ModItems.WOODEN_BUCKET.get(),
                    FTBSkies2AeroCompanion.id("filled"),
                    (ClampedItemPropertyFunction) (stack, level, entity, seed) -> {
                        SimpleFluidContent c = stack.get(ModBucketComponents.WOODEN_BUCKET_CONTENTS.get());
                        return c != null && !c.isEmpty() ? 1.0F : 0.0F;
                    }
            );
        });
    }
}
