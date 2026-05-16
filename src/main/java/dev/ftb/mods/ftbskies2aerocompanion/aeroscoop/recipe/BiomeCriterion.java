package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public sealed interface BiomeCriterion permits BiomeCriterion.Any, BiomeCriterion.Id, BiomeCriterion.Tag {
    boolean matches(Holder<Biome> biome);
    String serialize();

    Any ANY = new Any();

    final class Any implements BiomeCriterion {
        private Any() {}
        @Override public boolean matches(Holder<Biome> biome) { return true; }
        @Override public String serialize() { return "ANY"; }
    }

    record Id(ResourceLocation id) implements BiomeCriterion {
        @Override public boolean matches(Holder<Biome> biome) {
            return biome.unwrapKey().map(k -> k.location().equals(id)).orElse(false);
        }
        @Override public String serialize() { return id.toString(); }
    }

    record Tag(TagKey<Biome> tag) implements BiomeCriterion {
        @Override public boolean matches(Holder<Biome> biome) { return biome.is(tag); }
        @Override public String serialize() { return "#" + tag.location(); }
    }

    Codec<BiomeCriterion> CODEC = Codec.STRING.flatXmap(
            BiomeCriterion::parse,
            c -> DataResult.success(c.serialize())
    );

    StreamCodec<RegistryFriendlyByteBuf, BiomeCriterion> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> ByteBufCodecs.STRING_UTF8.encode(buf, val.serialize()),
            buf -> parse(ByteBufCodecs.STRING_UTF8.decode(buf)).getOrThrow()
    );

    static DataResult<BiomeCriterion> parse(String s) {
        if (s == null || s.isBlank() || s.equalsIgnoreCase("ANY") || s.equals("*")) {
            return DataResult.success(ANY);
        }
        if (s.startsWith("#")) {
            ResourceLocation rl = ResourceLocation.tryParse(s.substring(1));
            if (rl == null) return DataResult.error(() -> "Invalid biome tag: " + s);
            return DataResult.success(new Tag(TagKey.create(Registries.BIOME, rl)));
        }
        ResourceLocation rl = ResourceLocation.tryParse(s);
        if (rl == null) return DataResult.error(() -> "Invalid biome id: " + s);
        return DataResult.success(new Id(rl));
    }
}
