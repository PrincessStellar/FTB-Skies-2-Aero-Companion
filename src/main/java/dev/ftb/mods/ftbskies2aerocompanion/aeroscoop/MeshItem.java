package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop;

import com.oierbravo.createsifter.content.contraptions.components.meshes.Mesh;
import net.minecraft.world.item.ItemStack;

public class MeshItem extends Mesh {
    public final MeshTier tier;

    public MeshItem(MeshTier tier) {
        super(propsFor(tier));
        this.tier = tier;
    }

    private static Properties propsFor(MeshTier tier) {
        Properties props = new Properties().stacksTo(1);
        if (!tier.unbreakable()) {
            props = props.durability(tier.durability);
        }
        return props;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !tier.unbreakable() && super.isBarVisible(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
