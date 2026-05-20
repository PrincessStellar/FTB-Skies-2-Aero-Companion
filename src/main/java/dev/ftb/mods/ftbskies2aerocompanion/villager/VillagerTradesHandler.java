package dev.ftb.mods.ftbskies2aerocompanion.villager;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class VillagerTradesHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(VillagerTradesHandler.class);

    private static final ResourceLocation INDUSTRIALIST = ResourceLocation.parse("modern_industrialization:industrialist");
    private static final ResourceLocation FLUIX_RESEARCHER = ResourceLocation.parse("ae2:fluix_researcher");

    private VillagerTradesHandler() {}

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        VillagerProfession profession = event.getType();
        ResourceLocation profKey = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession);
        Int2ObjectMap<List<ItemListing>> trades = event.getTrades();

        // Some tier lists arrive immutable (NeoForge or earlier listeners may swap in ImmutableList);
        // upgrade them in-place so removeIf/add don't throw UnsupportedOperationException.
        ensureMutableTiers(trades);

        // Roots silver_ingot is unavailable in this pack — strip those trades from every profession.
        for (List<ItemListing> tier : trades.values()) {
            tier.removeIf(l -> firstInputMatches(l, "roots:silver_ingot"));
        }

        if (profession == VillagerProfession.CARTOGRAPHER) {
            applyCartographer(trades);
            return;
        }

        if (INDUSTRIALIST.equals(profKey)) {
            applyIndustrialist(trades);
        } else if (FLUIX_RESEARCHER.equals(profKey)) {
            applyFluixResearcher(trades);
        }
    }

    private static void applyCartographer(Int2ObjectMap<List<ItemListing>> trades) {
        for (List<ItemListing> tier : trades.values()) {
            tier.removeIf(VillagerTradesHandler::isMapTrade);
        }
        trades.get(2).add(itemsForEmeralds("hangglider:hang_glider", 1, 5, 12, 10));
    }

    private static void applyIndustrialist(Int2ObjectMap<List<ItemListing>> trades) {
        List<ItemListing> l1 = trades.get(1);
        l1.removeIf(l -> outputMatches(l, "modern_industrialization:tin_ingot")
                || firstInputMatches(l, "modern_industrialization:lignite_coal"));
        l1.add(itemsForEmeralds("ftbmaterials:tin_ingot", 4, 5, 16, 2));

        List<ItemListing> l2 = trades.get(2);
        l2.removeIf(l -> outputMatches(l, "modern_industrialization:bronze_ingot")
                || outputMatches(l, "modern_industrialization:copper_gear")
                || firstInputMatches(l, "modern_industrialization:sulfur_dust"));
        l2.add(itemsForEmeralds("ftbmaterials:bronze_ingot", 6, 10, 12, 10));
        l2.add(itemsForEmeralds("ftbmaterials:copper_gear", 4, 5, 12, 10));
        l2.add(emeraldForItems("ftbmaterials:sulfur_dust", 4, 12, 5));

        List<ItemListing> l3 = trades.get(3);
        l3.removeIf(l -> outputMatches(l, "modern_industrialization:steel_ingot")
                || outputMatches(l, "modern_industrialization:bronze_gear"));
        l3.add(itemsForEmeralds("ftbmaterials:steel_ingot", 6, 10, 12, 20));
        l3.add(itemsForEmeralds("ftbmaterials:bronze_gear", 4, 5, 12, 20));

        List<ItemListing> l4 = trades.get(4);
        l4.removeIf(l -> outputMatches(l, "modern_industrialization:steel_gear")
                || outputMatches(l, "modern_industrialization:steel_plate"));
        l4.add(itemsForEmeralds("ftbmaterials:steel_gear", 5, 5, 12, 30));
        l4.add(itemsForEmeralds("ftbmaterials:steel_plate", 6, 10, 12, 30));
    }

    private static void applyFluixResearcher(Int2ObjectMap<List<ItemListing>> trades) {
        List<ItemListing> l2 = trades.get(2);
        l2.removeIf(l -> outputMatches(l, "minecraft:emerald"));
        l2.add(emeraldForItems("ae2:charged_certus_quartz_crystal", 3, 12, 10));
        l2.add(emeraldForItems("ftbmaterials:silicon_gem", 5, 12, 10));
    }

    private static void ensureMutableTiers(Int2ObjectMap<List<ItemListing>> trades) {
        for (Int2ObjectMap.Entry<List<ItemListing>> entry : trades.int2ObjectEntrySet()) {
            List<ItemListing> tier = entry.getValue();
            if (!(tier instanceof ArrayList)) {
                entry.setValue(new ArrayList<>(tier));
            }
        }
    }

    private static ItemListing itemsForEmeralds(String outputId, int outputCount, int emeraldCost, int maxUses, int xp) {
        return new VillagerTrades.ItemsForEmeralds(lookup(outputId), emeraldCost, outputCount, maxUses, xp);
    }

    private static ItemListing emeraldForItems(String inputId, int inputCount, int maxUses, int xp) {
        return new VillagerTrades.EmeraldForItems(lookup(inputId), inputCount, maxUses, xp);
    }

    private static Item lookup(String id) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        if (item == Items.AIR) {
            LOGGER.warn("Villager trade references missing item: {}", id);
        }
        return item;
    }

    private static boolean outputMatches(ItemListing listing, String id) {
        ItemStack out = probeOutput(listing);
        return out != null && registryName(out.getItem()).equals(id);
    }

    private static boolean firstInputMatches(ItemListing listing, String id) {
        ItemStack in = probeFirstInput(listing);
        return in != null && registryName(in.getItem()).equals(id);
    }

    private static boolean isMapTrade(ItemListing listing) {
        // TreasureMapForEmeralds dereferences the trader's level to locate structures, so we can't
        // safely probe it with a null entity. Identify it by class instead.
        if (listing instanceof VillagerTrades.TreasureMapForEmeralds) return true;
        ItemStack out = probeOutput(listing);
        if (out == null) return false;
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(out.getItem());
        return key != null && key.getPath().contains("map");
    }

    private static ItemStack probeOutput(ItemListing listing) {
        try {
            MerchantOffer offer = listing.getOffer(null, RandomSource.create());
            return offer == null ? null : offer.getResult();
        } catch (Throwable t) {
            return null;
        }
    }

    private static ItemStack probeFirstInput(ItemListing listing) {
        try {
            MerchantOffer offer = listing.getOffer(null, RandomSource.create());
            return offer == null ? null : offer.getCostA();
        } catch (Throwable t) {
            return null;
        }
    }

    private static String registryName(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key == null ? "" : key.toString();
    }
}
