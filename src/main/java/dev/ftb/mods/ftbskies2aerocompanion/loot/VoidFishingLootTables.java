package dev.ftb.mods.ftbskies2aerocompanion.loot;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

public final class VoidFishingLootTables {
    public static final ResourceKey<LootTable> FISHING = key("gameplay/void_fishing");
    public static final ResourceKey<LootTable> FISH = key("gameplay/void_fishing/fish");
    public static final ResourceKey<LootTable> JUNK = key("gameplay/void_fishing/junk");
    public static final ResourceKey<LootTable> TREASURE = key("gameplay/void_fishing/treasure");

    private VoidFishingLootTables() {}

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(FTBSkies2AeroCompanion.MOD_ID, path));
    }
}
