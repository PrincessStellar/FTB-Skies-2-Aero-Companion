package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.integrateddynamics;

import net.minecraft.world.Clearable;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDryingBasin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

/**
 * The manual Drying Basin extends {@code CyclopsBlockEntity}, not
 * {@code BlockEntityCableConnectableInventory}, so {@link
 * BlockEntityCableConnectableInventoryMixin} does not cover it and its single
 * input slot dupes when Sable captures it onto a sub-level. Same fix and
 * removal condition as that mixin (mirrors Create PR #10352).
 */
@Pseudo
@Mixin(value = BlockEntityDryingBasin.class, remap = false)
public abstract class BlockEntityDryingBasinMixin implements Clearable {

    @Shadow
    public abstract SimpleInventory getInventory();

    @Override
    public void clearContent() {
        SimpleInventory inv = getInventory();
        if (inv != null) {
            inv.clearContent();
        }
    }
}
