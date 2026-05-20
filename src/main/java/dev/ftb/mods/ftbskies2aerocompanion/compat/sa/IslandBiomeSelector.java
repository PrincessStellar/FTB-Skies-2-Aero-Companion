package dev.ftb.mods.ftbskies2aerocompanion.compat.sa;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class IslandBiomeSelector {
    private IslandBiomeSelector() {}

    public static Optional<Holder<Biome>> pick(RegistryAccess registryAccess, long seedTag) {
        Registry<Biome> registry = registryAccess.registryOrThrow(Registries.BIOME);
        Optional<HolderSet.Named<Biome>> tagged = registry.getTag(IslandBiomeTags.HAS_FLOATING_ISLAND_BIOME);
        if (tagged.isEmpty()) return Optional.empty();
        List<Holder<Biome>> options = new ArrayList<>();
        for (Holder<Biome> h : tagged.get()) options.add(h);
        if (options.isEmpty()) return Optional.empty();
        int index = (int) Math.floorMod(seedTag, options.size());
        return Optional.of(options.get(index));
    }
}
