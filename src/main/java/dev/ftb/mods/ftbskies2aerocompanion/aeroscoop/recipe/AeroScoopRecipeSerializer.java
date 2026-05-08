package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AeroScoopRecipeSerializer implements RecipeSerializer<AeroScoopRecipe> {
    @Override
    public MapCodec<AeroScoopRecipe> codec() {
        return AeroScoopRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AeroScoopRecipe> streamCodec() {
        return AeroScoopRecipe.STREAM_CODEC;
    }
}
