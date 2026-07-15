package dev.ftb.mods.ftbskies2aerocompanion.item;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshItem;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshTier;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModBlocks;
import dev.ftb.mods.ftbskies2aerocompanion.bucket.WoodenBucketItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Unbreakable;
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

    public static final Map<VoidFishingRodTier, DeferredItem<FishingRodItem>> TIERED_VOID_FISHING_RODS =
            new EnumMap<>(VoidFishingRodTier.class);

    public static final DeferredItem<BlockItem> AIR_FILTER_ITEM = ITEMS.register(
            "air_filter",
            () -> new BlockItem(ModBlocks.AIR_FILTER.get(), new Item.Properties())
    );

    public static final DeferredItem<WoodenBucketItem> WOODEN_BUCKET = ITEMS.register(
            "wooden_bucket",
            () -> new WoodenBucketItem(new Item.Properties().stacksTo(1))
    );

    public static final Map<MeshTier, DeferredItem<MeshItem>> MESHES = new EnumMap<>(MeshTier.class);

    static {
        for (VoidFishingRodTier tier : VoidFishingRodTier.values()) {
            TIERED_VOID_FISHING_RODS.put(tier, ITEMS.register(
                    tier.itemName(),
                    () -> new FishingRodItem(propertiesFor(tier))
            ));
        }
        for (MeshTier tier : MeshTier.values()) {
            MESHES.put(tier, FTB_ITEMS.register(tier.idSuffix(), () -> new MeshItem(tier)));
        }
    }

    private static Item.Properties propertiesFor(VoidFishingRodTier tier) {
        Item.Properties props = new Item.Properties().stacksTo(1);
        if (tier.unbreakable()) {
            props.component(DataComponents.UNBREAKABLE, new Unbreakable(true));
        } else {
            props.durability(tier.durability());
        }
        return props;
    }

    private ModItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        FTB_ITEMS.register(bus);
    }
}
