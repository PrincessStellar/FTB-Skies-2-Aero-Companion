package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop;

import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe.AeroScoopRecipe;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe.AeroScoopResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AeroScoopTickLogic {
    public static final int INTAKE_CLEARANCE = 5;

    private AeroScoopTickLogic() {}

    public static boolean isIntakeClear(Level level, BlockPos pos, Direction intake) {
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int i = 1; i <= INTAKE_CLEARANCE; i++) {
            cursor.setWithOffset(pos, intake.getStepX() * i, intake.getStepY() * i, intake.getStepZ() * i);
            if (!level.getBlockState(cursor).isAir()) {
                return false;
            }
        }
        return true;
    }

    public static AeroScoopRecipe pickRecipe(Level level, BlockPos pos, ItemStack mesh, RandomSource rng) {
        Holder<Biome> biome = level.getBiome(pos);
        List<AeroScoopRecipe> matches = new ArrayList<>();
        level.getRecipeManager().getAllRecipesFor(ModAeroRecipes.AEROSCOOP_TYPE.get()).forEach(holder -> {
            AeroScoopRecipe recipe = holder.value();
            if (recipe.matchesMesh(mesh) && recipe.biome().matches(biome)) {
                matches.add(recipe);
            }
        });
        if (matches.isEmpty()) {
            return null;
        }
        return matches.get(rng.nextInt(matches.size()));
    }

    public static List<ItemStack> rollResults(AeroScoopRecipe recipe, RandomSource rng) {
        List<ItemStack> rolled = new ArrayList<>();
        for (AeroScoopResult result : recipe.results()) {
            ItemStack stack = result.roll(rng);
            if (!stack.isEmpty()) {
                rolled.add(stack);
            }
        }
        return rolled;
    }

    public static int durabilityCostFor(ResourceKey<Level> dim) {
        if (dim == Level.NETHER) return 2;
        if (dim == Level.END) return 3;
        return 1;
    }

    /**
     * Push leftover stacks into the given handler. Anything that doesn't fit is returned.
     */
    public static List<ItemStack> insertAll(IItemHandler handler, List<ItemStack> stacks) {
        if (stacks.isEmpty()) return Collections.emptyList();
        List<ItemStack> overflow = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, stack, false);
            if (!remaining.isEmpty()) {
                overflow.add(remaining);
            }
        }
        return overflow;
    }
}
