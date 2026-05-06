package dev.ftb.mods.ftbskies2aerocompanion.voidconversion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public final class VoidConversionRecipe implements Recipe<SingleRecipeInput> {
    public static final MapCodec<VoidConversionRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("input").forGetter(r -> r.input),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("output").forGetter(r -> r.output)
    ).apply(builder, VoidConversionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VoidConversionRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), r -> r.input,
            ByteBufCodecs.registry(Registries.ITEM), r -> r.output,
            VoidConversionRecipe::new
    );

    private final Item input;
    private final Item output;

    public VoidConversionRecipe(Item input, Item output) {
        this.input = input;
        this.output = output;
    }

    public Item input() {
        return input;
    }

    public Item output() {
        return output;
    }

    @Override
    public boolean matches(SingleRecipeInput in, Level level) {
        return in.item().is(input);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput in, HolderLookup.Provider registries) {
        return new ItemStack(output, in.item().getCount());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(output);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(input));
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.VOID_CONVERSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.VOID_CONVERSION_TYPE.get();
    }
}
