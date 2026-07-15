package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.integrateddynamics;

import net.minecraft.world.Clearable;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityCableConnectableInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Sable's physics activator dupes inventory contents when it absorbs an
 * IntegratedDynamics inventory-holding block into a sub-level: the BE's NBT
 * (including the inventory) is snapshot into the new sub-level, then the
 * real-world block is removed — which triggers {@code preRemoveSideEffects}
 * → {@code Containers.dropContents} into the real world. The drop call never
 * clears the source inventory, and the BE does not implement {@link Clearable},
 * so Sable's clear-on-capture call is a no-op and the items live in both
 * places.
 *
 * <p>Fix: implement {@link Clearable} on the base class. {@code clearContent}
 * delegates to the BE's own {@link SimpleInventory}, which already inherits a
 * working {@code clearContent} via {@code WorldlyContainer} → {@code Container}
 * → {@code Clearable}. One mixin covers every BE that extends this base
 * (Variablestore and the mechanical machines: Mechanical Squeezer, Mechanical
 * Drying Basin, …). The manual Squeezer and Drying Basin extend
 * {@code CyclopsBlockEntity} directly and are handled by their own mixins.
 *
 * <p>Mirrors Create PR #10352, which applied the same fix to {@code
 * ItemDrainBlockEntity} for the same Sable dupe. Remove once CyclopsMC ships
 * its own {@link Clearable} impl upstream.
 */
@Pseudo
@Mixin(value = BlockEntityCableConnectableInventory.class, remap = false)
public abstract class BlockEntityCableConnectableInventoryMixin implements Clearable {

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
