package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record VoidFishingDrop(
        ItemStack stack,
        String pool,
        int weight,
        double chance,
        List<ResourceLocation> biomes
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, VoidFishingDrop> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, VoidFishingDrop::stack,
            ByteBufCodecs.STRING_UTF8, VoidFishingDrop::pool,
            ByteBufCodecs.VAR_INT, VoidFishingDrop::weight,
            ByteBufCodecs.DOUBLE, VoidFishingDrop::chance,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), VoidFishingDrop::biomes,
            VoidFishingDrop::new
    );
}
