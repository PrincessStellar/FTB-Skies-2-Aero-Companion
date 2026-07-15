package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop;

import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public enum MeshTier {
    CLOTH(0.20F, 150, 0xC8C8C8),
    IRON(0.35F, 300, 0xD8D8D8),
    GOLD(0.50F, 600, 0xFCDC4F),
    DIAMOND(0.65F, 1500, 0x6CECCB),
    BLAZING(0.80F, 3000, 0xFFA000),
    CREATIVE(0.90F, 0, 0xB445F5);

    public final float efficiency;
    public final int durability;
    public final int color;

    MeshTier(float efficiency, int durability, int color) {
        this.efficiency = efficiency;
        this.durability = durability;
        this.color = color;
    }

    public boolean unbreakable() {
        return durability <= 0;
    }

    public String idSuffix() {
        return name().toLowerCase(Locale.ROOT) + "_mesh";
    }

    public static MeshTier of(ItemStack stack) {
        if (stack.getItem() instanceof MeshItem mesh) {
            return mesh.tier;
        }
        return null;
    }
}
