package dev.ftb.mods.ftbskies2aerocompanion.voidconversion;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, FTBSkies2AeroCompanion.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, FTBSkies2AeroCompanion.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, VoidConversionSerializer> VOID_CONVERSION_SERIALIZER =
            SERIALIZERS.register("void_conversion", VoidConversionSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<VoidConversionRecipe>> VOID_CONVERSION_TYPE =
            TYPES.register("void_conversion", () -> RecipeType.simple(FTBSkies2AeroCompanion.id("void_conversion")));

    private ModRecipes() {}

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
        TYPES.register(bus);
    }
}
