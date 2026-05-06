package dev.ftb.mods.ftbskies2aerocompanion.item;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FTBSkies2AeroCompanion.MOD_ID);

    public static final DeferredItem<FishingRodItem> VOID_FISHING_ROD = ITEMS.register(
            "void_fishing_rod",
            () -> new FishingRodItem(new Item.Properties().durability(256).stacksTo(1))
    );

    private ModItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
