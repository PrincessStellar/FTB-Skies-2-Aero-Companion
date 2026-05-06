package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final RecipeType<VoidFishingRecipe> VOID_FISHING_TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(FTBSkies2AeroCompanion.MOD_ID, "void_fishing"),
            VoidFishingRecipe.class
    );

    private static final ResourceLocation PLUGIN_ID = FTBSkies2AeroCompanion.id("jei");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new VoidFishingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<VoidFishingDrop> drops = LootTableLoader.load();
        if (drops.isEmpty()) {
            return;
        }
        registration.addRecipes(VOID_FISHING_TYPE, List.of(new VoidFishingRecipe(drops)));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.VOID_FISHING_ROD.get()), VOID_FISHING_TYPE);
    }
}
