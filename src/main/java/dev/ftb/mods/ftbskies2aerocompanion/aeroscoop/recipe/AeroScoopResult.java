package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;

public record AeroScoopResult(Item item, int count, double chance) {
    public static final Codec<AeroScoopResult> CODEC = RecordCodecBuilder.create(b -> b.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(AeroScoopResult::item),
            Codec.INT.optionalFieldOf("count", 1).forGetter(AeroScoopResult::count),
            Codec.DOUBLE.fieldOf("chance").forGetter(AeroScoopResult::chance)
    ).apply(b, AeroScoopResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AeroScoopResult> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), AeroScoopResult::item,
            ByteBufCodecs.VAR_INT, AeroScoopResult::count,
            ByteBufCodecs.DOUBLE, AeroScoopResult::chance,
            AeroScoopResult::new
    );

    public ItemStack roll(RandomSource rng) {
        int guaranteed = (int) Math.floor(chance);
        double bonus = chance - guaranteed;
        int multiplier = guaranteed + (rng.nextDouble() < bonus ? 1 : 0);
        if (multiplier <= 0) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count * multiplier);
    }
}
