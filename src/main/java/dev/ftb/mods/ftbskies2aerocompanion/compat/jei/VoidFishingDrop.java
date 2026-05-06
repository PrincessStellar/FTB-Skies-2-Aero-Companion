package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * One leaf entry from the void-fishing loot table after sub-tables have been expanded.
 *
 * @param stack    presentable {@link ItemStack} (potion variants are baked in)
 * @param pool     human-readable pool name ({@code "Fish"}, {@code "Junk"}, or {@code "Treasure"})
 * @param weight   raw weight inside its sub-table
 * @param chance   normalised total probability across the whole table, in {@code [0, 1]}
 * @param biomes   biome restrictions inherited from {@code location_check} conditions; empty when unrestricted
 */
public record VoidFishingDrop(
        ItemStack stack,
        String pool,
        int weight,
        double chance,
        List<ResourceLocation> biomes
) {}
