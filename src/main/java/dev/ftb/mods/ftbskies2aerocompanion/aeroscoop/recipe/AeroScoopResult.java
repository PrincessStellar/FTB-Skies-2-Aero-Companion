package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record AeroScoopResult(ItemStack stack, double chance) {
    public static final Codec<AeroScoopResult> CODEC = RecordCodecBuilder.create(b -> b.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(r -> r.stack.getItem()),
            Codec.INT.optionalFieldOf("count", 1).forGetter(r -> r.stack.getCount()),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(r -> r.stack.getComponentsPatch()),
            Codec.DOUBLE.fieldOf("chance").forGetter(AeroScoopResult::chance)
    ).apply(b, AeroScoopResult::create));

    public static final StreamCodec<RegistryFriendlyByteBuf, AeroScoopResult> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, AeroScoopResult::stack,
            ByteBufCodecs.DOUBLE, AeroScoopResult::chance,
            AeroScoopResult::new
    );

    private static AeroScoopResult create(Item item, int count, DataComponentPatch patch, double chance) {
        ItemStack stack = new ItemStack(item, count);
        stack.applyComponents(patch);
        return new AeroScoopResult(stack, chance);
    }

    public Item item() {
        return stack.getItem();
    }

    public int count() {
        return stack.getCount();
    }

    public ItemStack roll(RandomSource rng) {
        int guaranteed = (int) Math.floor(chance);
        double bonus = chance - guaranteed;
        int multiplier = guaranteed + (rng.nextDouble() < bonus ? 1 : 0);
        if (multiplier <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.copy();
        result.setCount(stack.getCount() * multiplier);
        return result;
    }
}
