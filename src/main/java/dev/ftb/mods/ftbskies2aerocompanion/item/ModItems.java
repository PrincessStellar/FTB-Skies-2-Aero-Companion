package dev.ftb.mods.ftbskies2aerocompanion.item;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshItem;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshTier;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FTBSkies2AeroCompanion.MOD_ID);
    public static final DeferredRegister.Items FTB_ITEMS = DeferredRegister.createItems("ftb");

    public static final DeferredItem<FishingRodItem> VOID_FISHING_ROD = ITEMS.register(
            "void_fishing_rod",
            () -> new FishingRodItem(new Item.Properties().durability(256).stacksTo(1))
    );

    public static final DeferredItem<BlockItem> AIR_FILTER_ITEM = ITEMS.register(
            "air_filter",
            () -> new BlockItem(ModBlocks.AIR_FILTER.get(), new Item.Properties())
    );

    public static final Map<MeshTier, DeferredItem<MeshItem>> MESHES = new EnumMap<>(MeshTier.class);

    static {
        for (MeshTier tier : MeshTier.values()) {
            MESHES.put(tier, FTB_ITEMS.register(tier.idSuffix(), () -> new MeshItem(tier)));
        }
    }

    public static final DeferredItem<BlockItem> IRON_DUST = FTB_ITEMS.register(
            "iron_dust", () -> new BlockItem(ModBlocks.IRON_DUST.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> GOLD_DUST = FTB_ITEMS.register(
            "gold_dust", () -> new BlockItem(ModBlocks.GOLD_DUST.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> DIAMOND_DUST = FTB_ITEMS.register(
            "diamond_dust", () -> new BlockItem(ModBlocks.DIAMOND_DUST.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> BLAZING_DUST = FTB_ITEMS.register(
            "blazing_dust", () -> new BlockItem(ModBlocks.BLAZING_DUST.get(), new Item.Properties()));

    private ModItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        FTB_ITEMS.register(bus);
    }
}
