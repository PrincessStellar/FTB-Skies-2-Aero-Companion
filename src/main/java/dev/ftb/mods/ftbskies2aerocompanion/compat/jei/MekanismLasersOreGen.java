package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class MekanismLasersOreGen {
    public static final String MOD_ID = "mekanism_lasers";

    private static final ResourceLocation ORE_GENERATOR_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "ore_generator");
    private static final TagKey<Block> ORE_BLACKLIST = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "ore_blacklist"));

    static final int COLUMNS = 9;
    static final int ROWS = 6;
    static final int PER_PAGE = COLUMNS * ROWS;

    private MekanismLasersOreGen() {
    }

    public static ItemStack oreGeneratorStack() {
        return new ItemStack(BuiltInRegistries.BLOCK.get(ORE_GENERATOR_ID));
    }

    public static List<OreGeneratorRecipe> buildRecipes() {
        List<Block> pool = computePool();
        int total = pool.size();
        if (total == 0) {
            return List.of();
        }
        double chance = 1.0 / total;
        List<OreGeneratorEntry> entries = new ArrayList<>(total);
        for (Block block : pool) {
            entries.add(new OreGeneratorEntry(new ItemStack(block), chance));
        }
        List<OreGeneratorRecipe> recipes = new ArrayList<>();
        for (int i = 0; i < entries.size(); i += PER_PAGE) {
            recipes.add(new OreGeneratorRecipe(List.copyOf(entries.subList(i, Math.min(i + PER_PAGE, entries.size())))));
        }
        return recipes;
    }

    private static List<Block> computePool() {
        Optional<HolderSet.Named<Block>> ores = BuiltInRegistries.BLOCK.getTag(Tags.Blocks.ORES);
        if (ores.isEmpty()) {
            return List.of();
        }
        Set<String> configBlacklist = readConfigBlacklist();
        List<Block> pool = new ArrayList<>();
        for (Holder<Block> holder : ores.get()) {
            if (holder.is(ORE_BLACKLIST)) {
                continue;
            }
            Optional<ResourceLocation> id = holder.unwrapKey().map(net.minecraft.resources.ResourceKey::location);
            if (id.isPresent() && configBlacklist.contains(id.get().toString())) {
                continue;
            }
            pool.add(holder.value());
        }
        return pool;
    }

    private static Set<String> readConfigBlacklist() {
        try {
            Class<?> configClass = Class.forName("com.folumo.mekanism_lasers.Config");
            Object configValue = configClass.getField("blacklistedOres").get(null);
            Object list = configValue.getClass().getMethod("get").invoke(configValue);
            Set<String> result = new HashSet<>();
            if (list instanceof List<?> values) {
                for (Object value : values) {
                    result.add(String.valueOf(value));
                }
            }
            return result;
        } catch (Throwable ignored) {
            return Set.of();
        }
    }
}
