package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe.AeroScoopRecipe;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe.AeroScoopRecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModAeroRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, FTBSkies2AeroCompanion.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, FTBSkies2AeroCompanion.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, AeroScoopRecipeSerializer> AEROSCOOP_SERIALIZER =
            SERIALIZERS.register("aeroscoop", AeroScoopRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AeroScoopRecipe>> AEROSCOOP_TYPE =
            TYPES.register("aeroscoop", () -> RecipeType.simple(FTBSkies2AeroCompanion.id("aeroscoop")));

    private ModAeroRecipes() {}

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
        TYPES.register(bus);
    }
}
