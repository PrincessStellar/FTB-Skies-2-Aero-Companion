package dev.ftb.mods.ftbskies2aerocompanion.voidconversion;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class VoidConversionSerializer implements RecipeSerializer<VoidConversionRecipe> {
    @Override
    public MapCodec<VoidConversionRecipe> codec() {
        return VoidConversionRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, VoidConversionRecipe> streamCodec() {
        return VoidConversionRecipe.STREAM_CODEC;
    }
}
