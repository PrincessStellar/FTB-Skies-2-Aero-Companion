package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModAeroRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;

public final class AeroScoopRecipe implements Recipe<SingleRecipeInput> {
    public static final MapCodec<AeroScoopRecipe> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
            Ingredient.CODEC.fieldOf("mesh").forGetter(r -> r.mesh),
            BiomeCriterion.CODEC.optionalFieldOf("biome", BiomeCriterion.ANY).forGetter(r -> r.biome),
            AeroScoopResult.CODEC.listOf().fieldOf("results").forGetter(r -> r.results)
    ).apply(b, AeroScoopRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AeroScoopRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.mesh,
            BiomeCriterion.STREAM_CODEC, r -> r.biome,
            AeroScoopResult.STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list()), r -> r.results,
            AeroScoopRecipe::new
    );

    private final Ingredient mesh;
    private final BiomeCriterion biome;
    private final List<AeroScoopResult> results;

    public AeroScoopRecipe(Ingredient mesh, BiomeCriterion biome, List<AeroScoopResult> results) {
        this.mesh = mesh;
        this.biome = biome;
        this.results = List.copyOf(results);
    }

    public Ingredient mesh() { return mesh; }
    public BiomeCriterion biome() { return biome; }
    public List<AeroScoopResult> results() { return results; }

    public boolean matchesMesh(ItemStack stack) {
        return mesh.test(stack);
    }

    @Override public boolean matches(SingleRecipeInput in, Level level) { return mesh.test(in.item()); }
    @Override public ItemStack assemble(SingleRecipeInput in, HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(mesh);
        return list;
    }

    @Override public RecipeSerializer<?> getSerializer() { return ModAeroRecipes.AEROSCOOP_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModAeroRecipes.AEROSCOOP_TYPE.get(); }
}
