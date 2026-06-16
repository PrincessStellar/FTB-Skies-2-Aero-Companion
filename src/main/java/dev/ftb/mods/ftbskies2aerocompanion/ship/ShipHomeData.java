package dev.ftb.mods.ftbskies2aerocompanion.ship;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ShipHomeData extends SavedData {
    private static final String FILE_NAME = "ftbskies2aero_ship_homes";

    /** Set on server start so mixins (which only have a player UUID) can locate this without an explicit server arg. */
    @Nullable private static MinecraftServer activeServer;

    private final Map<UUID, ShipBinding> bedBindings = new HashMap<>();
    private final Map<UUID, Map<String, ShipBinding>> homeBindings = new HashMap<>();
    private final Map<String, ShipBinding> warpBindings = new HashMap<>();
    private final Map<UUID, ShipBinding> compactReturnBindings = new HashMap<>();

    public static void setActiveServer(@Nullable MinecraftServer server) {
        activeServer = server;
    }

    public static @Nullable ShipHomeData current() {
        return activeServer == null ? null : get(activeServer);
    }

    public static ShipHomeData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ShipHomeData::new, ShipHomeData::load),
                FILE_NAME
        );
    }

    public Optional<ShipBinding> getBed(UUID player) {
        return Optional.ofNullable(bedBindings.get(player));
    }

    public void setBed(UUID player, ShipBinding binding) {
        bedBindings.put(player, binding);
        setDirty();
    }

    public void clearBed(UUID player) {
        if (bedBindings.remove(player) != null) {
            setDirty();
        }
    }

    public Optional<ShipBinding> getHome(UUID player, String name) {
        Map<String, ShipBinding> homes = homeBindings.get(player);
        return homes == null ? Optional.empty() : Optional.ofNullable(homes.get(name));
    }

    public void setHome(UUID player, String name, ShipBinding binding) {
        homeBindings.computeIfAbsent(player, k -> new HashMap<>()).put(name, binding);
        setDirty();
    }

    public void clearHome(UUID player, String name) {
        Map<String, ShipBinding> homes = homeBindings.get(player);
        if (homes != null && homes.remove(name) != null) {
            if (homes.isEmpty()) {
                homeBindings.remove(player);
            }
            setDirty();
        }
    }

    public Optional<ShipBinding> getWarp(String name) {
        return Optional.ofNullable(warpBindings.get(name));
    }

    public void setWarp(String name, ShipBinding binding) {
        warpBindings.put(name, binding);
        setDirty();
    }

    public void clearWarp(String name) {
        if (warpBindings.remove(name) != null) {
            setDirty();
        }
    }

    public Optional<ShipBinding> getCompactReturn(UUID player) {
        return Optional.ofNullable(compactReturnBindings.get(player));
    }

    public void setCompactReturn(UUID player, ShipBinding binding) {
        compactReturnBindings.put(player, binding);
        setDirty();
    }

    public void clearCompactReturn(UUID player) {
        if (compactReturnBindings.remove(player) != null) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag beds = new ListTag();
        bedBindings.forEach((uuid, b) -> {
            CompoundTag e = b.write();
            e.putUUID("player", uuid);
            beds.add(e);
        });
        tag.put("beds", beds);

        ListTag homes = new ListTag();
        homeBindings.forEach((uuid, names) -> names.forEach((n, b) -> {
            CompoundTag e = b.write();
            e.putUUID("player", uuid);
            e.putString("name", n);
            homes.add(e);
        }));
        tag.put("homes", homes);

        ListTag warps = new ListTag();
        warpBindings.forEach((n, b) -> {
            CompoundTag e = b.write();
            e.putString("name", n);
            warps.add(e);
        });
        tag.put("warps", warps);

        ListTag compactReturns = new ListTag();
        compactReturnBindings.forEach((uuid, b) -> {
            CompoundTag e = b.write();
            e.putUUID("player", uuid);
            compactReturns.add(e);
        });
        tag.put("compact_returns", compactReturns);
        return tag;
    }

    public static ShipHomeData load(CompoundTag tag, HolderLookup.Provider registries) {
        ShipHomeData d = new ShipHomeData();
        for (Tag t : tag.getList("beds", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            d.bedBindings.put(c.getUUID("player"), ShipBinding.read(c));
        }
        for (Tag t : tag.getList("homes", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            d.homeBindings.computeIfAbsent(c.getUUID("player"), k -> new HashMap<>())
                    .put(c.getString("name"), ShipBinding.read(c));
        }
        for (Tag t : tag.getList("warps", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            d.warpBindings.put(c.getString("name"), ShipBinding.read(c));
        }
        for (Tag t : tag.getList("compact_returns", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            d.compactReturnBindings.put(c.getUUID("player"), ShipBinding.read(c));
        }
        return d;
    }
}
