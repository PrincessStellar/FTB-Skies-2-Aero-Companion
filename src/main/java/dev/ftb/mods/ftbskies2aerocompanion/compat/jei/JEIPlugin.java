package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshTier;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModAeroRecipes;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe.AeroScoopRecipe;
import dev.ftb.mods.ftbskies2aerocompanion.compat.createnewage.CnaAddon;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import dev.ftb.mods.ftbskies2aerocompanion.voidconversion.ModRecipes;
import dev.ftb.mods.ftbskies2aerocompanion.voidconversion.VoidConversionRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final RecipeType<VoidFishingRecipe> VOID_FISHING_TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(FTBSkies2AeroCompanion.MOD_ID, "void_fishing"),
            VoidFishingRecipe.class
    );

    public static final RecipeType<VoidConversionRecipe> VOID_CONVERSION_TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(FTBSkies2AeroCompanion.MOD_ID, "void_conversion"),
            VoidConversionRecipe.class
    );

    public static final RecipeType<AeroScoopRecipe> AEROSCOOP_TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(FTBSkies2AeroCompanion.MOD_ID, "aeroscoop"),
            AeroScoopRecipe.class
    );

    private static final ResourceLocation PLUGIN_ID = FTBSkies2AeroCompanion.id("jei");

    private static IJeiRuntime runtime;
    private static VoidFishingRecipe currentVoidFishingRecipe;

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
            refreshVoidFishing();
            hideHiddenAeroScoopRecipes();
        });
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> LootTableLoader.clearSyncedDrops());
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    private static void hideHiddenAeroScoopRecipes() {
        if (runtime == null || Minecraft.getInstance().level == null) return;
        List<AeroScoopRecipe> hidden = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(ModAeroRecipes.AEROSCOOP_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .filter(AeroScoopRecipe::hidden)
                .toList();
        if (!hidden.isEmpty()) {
            runtime.getRecipeManager().hideRecipes(AEROSCOOP_TYPE, hidden);
        }
    }

    static void refreshVoidFishing() {
        if (runtime == null) return;
        List<VoidFishingDrop> drops = LootTableLoader.load();
        if (drops.isEmpty()) return;
        VoidFishingRecipe updated = new VoidFishingRecipe(drops);
        if (currentVoidFishingRecipe != null) {
            runtime.getRecipeManager().hideRecipes(VOID_FISHING_TYPE, List.of(currentVoidFishingRecipe));
        }
        runtime.getRecipeManager().addRecipes(VOID_FISHING_TYPE, List.of(updated));
        currentVoidFishingRecipe = updated;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new VoidFishingCategory(registration.getJeiHelpers().getGuiHelper()),
                new VoidConversionCategory(registration.getJeiHelpers().getGuiHelper()),
                new AeroScoopJEICategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<VoidFishingDrop> drops = LootTableLoader.load();
        if (!drops.isEmpty()) {
            currentVoidFishingRecipe = new VoidFishingRecipe(drops);
            registration.addRecipes(VOID_FISHING_TYPE, List.of(currentVoidFishingRecipe));
        }

        if (Minecraft.getInstance().level != null) {
            List<VoidConversionRecipe> conversions = Minecraft.getInstance().level.getRecipeManager()
                    .getAllRecipesFor(ModRecipes.VOID_CONVERSION_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();
            registration.addRecipes(VOID_CONVERSION_TYPE, conversions);

            List<AeroScoopRecipe> aeroscoops = Minecraft.getInstance().level.getRecipeManager()
                    .getAllRecipesFor(ModAeroRecipes.AEROSCOOP_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .filter(r -> !r.hidden())
                    .toList();
            registration.addRecipes(AEROSCOOP_TYPE, aeroscoops);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.VOID_FISHING_ROD.get()), VOID_FISHING_TYPE);
        for (var rod : ModItems.TIERED_VOID_FISHING_RODS.values()) {
            registration.addRecipeCatalyst(new ItemStack(rod.get()), VOID_FISHING_TYPE);
        }

        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().level.getRecipeManager()
                    .getAllRecipesFor(ModRecipes.VOID_CONVERSION_TYPE.get())
                    .forEach(holder -> registration.addRecipeCatalyst(
                            new ItemStack(holder.value().input()), VOID_CONVERSION_TYPE
                    ));
        }

        registration.addRecipeCatalyst(new ItemStack(ModItems.AIR_FILTER_ITEM.get()), AEROSCOOP_TYPE);
        for (MeshTier tier : MeshTier.values()) {
            registration.addRecipeCatalyst(new ItemStack(ModItems.MESHES.get(tier).get()), AEROSCOOP_TYPE);
        }

        @SuppressWarnings("unchecked")
        RecipeType<RecipeHolder<?>> energising = new RecipeType<>(
                ResourceLocation.fromNamespaceAndPath("create_new_age", "energising"),
                (Class<RecipeHolder<?>>) (Class<?>) RecipeHolder.class);
        for (CnaAddon.Energiser e : CnaAddon.Energiser.values()) {
            registration.addRecipeCatalyst(new ItemStack(CnaAddon.ENERGISERS.get(e).get()), energising);
        }
    }
}
