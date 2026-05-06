package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Reads our void-fishing loot tables from the mod jar at runtime, expands sub-table references,
 * and produces a flat list of {@link VoidFishingDrop} entries with normalised probabilities and
 * biome restrictions surfaced from {@code location_check} conditions.
 *
 * <p>Reading the JSON ourselves (rather than going through the live datapack registry) keeps the
 * JEI integration entirely client-side and avoids needing the player to be in a world for the
 * recipes to populate.
 */
public final class LootTableLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(LootTableLoader.class);
    private static final String ROOT = "/data/" + FTBSkies2AeroCompanion.MOD_ID + "/loot_table/gameplay/void_fishing.json";

    private LootTableLoader() {}

    public static List<VoidFishingDrop> load() {
        JsonObject root = readJson(ROOT);
        if (root == null) {
            LOGGER.warn("Void fishing loot table missing at {}", ROOT);
            return List.of();
        }
        List<VoidFishingDrop> drops = new ArrayList<>();
        walkTable(root, 1.0, "Root", List.of(), drops);
        drops.sort(Comparator.comparing(VoidFishingDrop::pool)
                .thenComparingDouble((VoidFishingDrop d) -> -d.chance()));
        return drops;
    }

    private static void walkTable(JsonObject table, double parentChance, String poolName, List<ResourceLocation> biomes, List<VoidFishingDrop> out) {
        JsonArray pools = table.getAsJsonArray("pools");
        if (pools == null) {
            return;
        }
        for (JsonElement pe : pools) {
            JsonObject pool = pe.getAsJsonObject();
            JsonArray entries = pool.getAsJsonArray("entries");
            if (entries == null) {
                continue;
            }
            int totalWeight = 0;
            for (JsonElement ee : entries) {
                totalWeight += weightOf(ee.getAsJsonObject());
            }
            if (totalWeight <= 0) {
                continue;
            }
            for (JsonElement ee : entries) {
                JsonObject entry = ee.getAsJsonObject();
                int w = weightOf(entry);
                double entryChance = parentChance * ((double) w / totalWeight);
                List<ResourceLocation> entryBiomes = collectBiomes(entry, biomes);
                String type = entry.has("type") ? entry.get("type").getAsString() : "";
                switch (type) {
                    case "minecraft:loot_table" -> {
                        String value = entry.get("value").getAsString();
                        JsonObject sub = readJson(toResourcePath(value));
                        if (sub != null) {
                            walkTable(sub, entryChance, prettyPoolName(value), entryBiomes, out);
                        }
                    }
                    case "minecraft:item" -> {
                        ItemStack stack = buildStack(entry);
                        if (!stack.isEmpty()) {
                            out.add(new VoidFishingDrop(stack, poolName, w, entryChance, entryBiomes));
                        }
                    }
                    default -> {
                        // tags, alternatives, dynamic, etc. — not used in vanilla fishing tables
                    }
                }
            }
        }
    }

    private static int weightOf(JsonObject entry) {
        return entry.has("weight") ? entry.get("weight").getAsInt() : 1;
    }

    private static List<ResourceLocation> collectBiomes(JsonObject entry, List<ResourceLocation> inherited) {
        if (!entry.has("conditions")) {
            return inherited;
        }
        List<ResourceLocation> result = new ArrayList<>(inherited);
        for (JsonElement ce : entry.getAsJsonArray("conditions")) {
            JsonObject cond = ce.getAsJsonObject();
            if (!cond.has("condition")) continue;
            if (!"minecraft:location_check".equals(cond.get("condition").getAsString())) continue;
            JsonObject pred = cond.has("predicate") ? cond.getAsJsonObject("predicate") : null;
            if (pred == null || !pred.has("biomes")) continue;
            JsonElement biomesEl = pred.get("biomes");
            if (biomesEl.isJsonArray()) {
                for (JsonElement b : biomesEl.getAsJsonArray()) {
                    result.add(ResourceLocation.parse(b.getAsString()));
                }
            } else if (biomesEl.isJsonPrimitive()) {
                result.add(ResourceLocation.parse(biomesEl.getAsString()));
            }
        }
        return Collections.unmodifiableList(result);
    }

    private static ItemStack buildStack(JsonObject entry) {
        String name = entry.get("name").getAsString();
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(name));
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        if (entry.has("functions")) {
            for (JsonElement fe : entry.getAsJsonArray("functions")) {
                applyFunction(stack, fe.getAsJsonObject());
            }
        }
        return stack;
    }

    private static void applyFunction(ItemStack stack, JsonObject fn) {
        if (!fn.has("function")) return;
        switch (fn.get("function").getAsString()) {
            case "minecraft:set_potion" -> {
                String id = fn.get("id").getAsString();
                ResourceKey<Potion> key = ResourceKey.create(Registries.POTION, ResourceLocation.parse(id));
                BuiltInRegistries.POTION.getHolder(key).ifPresent(h -> stack.set(DataComponents.POTION_CONTENTS, new PotionContents(h)));
            }
            // Display-irrelevant: set_count, set_damage, enchant_with_levels, etc.
            default -> {}
        }
    }

    private static String prettyPoolName(String tableId) {
        int slash = tableId.lastIndexOf('/');
        String tail = slash >= 0 ? tableId.substring(slash + 1) : tableId;
        return Character.toUpperCase(tail.charAt(0)) + tail.substring(1);
    }

    private static String toResourcePath(String namespacedId) {
        int colon = namespacedId.indexOf(':');
        String ns = colon >= 0 ? namespacedId.substring(0, colon) : "minecraft";
        String path = colon >= 0 ? namespacedId.substring(colon + 1) : namespacedId;
        return "/data/" + ns + "/loot_table/" + path + ".json";
    }

    private static JsonObject readJson(String resourcePath) {
        try (InputStream in = LootTableLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonElement el = JsonParser.parseReader(r);
                return el.isJsonObject() ? el.getAsJsonObject() : null;
            }
        } catch (IOException e) {
            LOGGER.warn("Failed reading loot table {}", resourcePath, e);
            return null;
        }
    }
}
