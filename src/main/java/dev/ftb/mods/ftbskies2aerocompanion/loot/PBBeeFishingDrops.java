package dev.ftb.mods.ftbskies2aerocompanion.loot;

import cy.jdkdigital.productivebees.common.crafting.ingredient.BeeIngredient;
import cy.jdkdigital.productivebees.common.recipe.BeeFishingRecipe;
import cy.jdkdigital.productivebees.init.ModDataComponents;
import cy.jdkdigital.productivebees.init.ModRecipeTypes;
import dev.ftb.mods.ftbskies2aerocompanion.compat.jei.VoidFishingDrop;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class PBBeeFishingDrops {
    private static final Logger LOGGER = LoggerFactory.getLogger(PBBeeFishingDrops.class);
    private static final String POOL = "Bees";
    private static final boolean PB_LOADED = ModList.get().isLoaded("productivebees");

    private PBBeeFishingDrops() {}

    public static List<VoidFishingDrop> resolve(MinecraftServer server) {
        if (!PB_LOADED) {
            return List.of();
        }
        try {
            return collect(server);
        } catch (Throwable t) {
            LOGGER.warn("Failed resolving Productive Bees bee_fishing recipes", t);
            return List.of();
        }
    }

    private static List<VoidFishingDrop> collect(MinecraftServer server) {
        List<RecipeHolder<BeeFishingRecipe>> holders =
                server.getRecipeManager().getAllRecipesFor(ModRecipeTypes.BEE_FISHING_TYPE.get());
        List<VoidFishingDrop> out = new ArrayList<>(holders.size());
        for (RecipeHolder<BeeFishingRecipe> holder : holders) {
            BeeFishingRecipe recipe = holder.value();
            BeeIngredient ingredient = recipe.output != null ? recipe.output.get() : null;
            if (ingredient == null) continue;
            ItemStack stack = toSpawnEgg(ingredient);
            if (stack.isEmpty()) continue;
            out.add(new VoidFishingDrop(stack, POOL, 0, recipe.chance, biomesOf(recipe.biomes)));
        }
        return out;
    }

    private static ItemStack toSpawnEgg(BeeIngredient ingredient) {
        EntityType<?> entity = ingredient.getBeeEntity();
        Item egg = entity != null ? SpawnEggItem.byId(entity) : null;
        ItemStack stack = egg != null ? new ItemStack(egg) : new ItemStack(Items.BEE_SPAWN_EGG);
        ResourceLocation beeType = ingredient.getBeeType();
        if (ingredient.isConfigurable() && beeType != null) {
            stack.set(ModDataComponents.BEE_TYPE.get(), beeType);
        }
        return stack;
    }

    private static List<ResourceLocation> biomesOf(HolderSet<Biome> set) {
        if (set == null) return List.of();
        List<ResourceLocation> result = new ArrayList<>();
        for (Holder<Biome> h : set) {
            h.unwrapKey().ifPresent(k -> result.add(k.location()));
        }
        return List.copyOf(result);
    }
}
