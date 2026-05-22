package dev.ftb.mods.ftbskies2aerocompanion.item;

public enum VoidFishingRodTier {
    INFERIUM("inferium", "inferium_fishing_rod", 2000),
    PRUDENTIUM("prudentium", "prudentium_fishing_rod", 2800),
    TERTIUM("tertium", "tertium_fishing_rod", 4000),
    IMPERIUM("imperium", "imperium_fishing_rod", 6000),
    SUPREMIUM("supremium", "supremium_fishing_rod", 0),
    AWAKENED_SUPREMIUM("awakened_supremium", "awakened_supremium_fishing_rod", 0);

    private final String id;
    private final String maInputPath;
    private final int durability;

    VoidFishingRodTier(String id, String maInputPath, int durability) {
        this.id = id;
        this.maInputPath = maInputPath;
        this.durability = durability;
    }

    public String id() {
        return id;
    }

    public String itemName() {
        return id + "_void_fishing_rod";
    }

    public String recipeName() {
        return "void_conversion_" + itemName();
    }

    public String maInputPath() {
        return maInputPath;
    }

    public int durability() {
        return durability;
    }

    public boolean unbreakable() {
        return durability == 0;
    }
}
