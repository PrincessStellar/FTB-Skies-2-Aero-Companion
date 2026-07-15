package dev.ftb.mods.ftbskies2aerocompanion;

import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshTier;
import dev.ftb.mods.ftbskies2aerocompanion.compat.createnewage.CnaAddon;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import dev.ftb.mods.ftbskies2aerocompanion.item.VoidFishingRodTier;
import dev.ftb.mods.ftbskies2aerocompanion.skybound_anchor.ModSkyboundAnchorBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FTBSkies2AeroCompanion.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ftbskies2aerocompanion"))
                    .icon(() -> new ItemStack(ModSkyboundAnchorBlocks.SKYBOUND_ANCHOR_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.VOID_FISHING_ROD.get());
                        for (VoidFishingRodTier tier : VoidFishingRodTier.values()) {
                            output.accept(ModItems.TIERED_VOID_FISHING_RODS.get(tier).get());
                        }
                        output.accept(ModItems.AIR_FILTER_ITEM.get());
                        output.accept(ModSkyboundAnchorBlocks.SKYBOUND_ANCHOR_ITEM.get());
                        output.accept(ModItems.WOODEN_BUCKET.get());
                        for (MeshTier tier : MeshTier.values()) {
                            output.accept(ModItems.MESHES.get(tier).get());
                        }
                        CnaAddon.addToCreativeTab(output);
                    })
                    .build()
    );

    private ModCreativeTabs() {}

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
