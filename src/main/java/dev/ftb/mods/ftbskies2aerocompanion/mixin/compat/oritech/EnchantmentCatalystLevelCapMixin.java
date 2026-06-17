package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.oritech;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftbskies2aerocompanion.compat.oritech.OritechCatalystConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import rearth.oritech.api.item.containers.SimpleInventoryStorage;
import rearth.oritech.block.entity.arcane.EnchantmentCatalystBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EnchantmentCatalystBlockEntity.class, remap = false)
public abstract class EnchantmentCatalystLevelCapMixin {

    @Shadow
    @Final
    public SimpleInventoryStorage inventory;

    @ModifyReturnValue(method = "canProceed", at = @At("RETURN"))
    private boolean ftbskies2aero$capOverenchant(boolean original) {
        if (!original) {
            return false;
        }
        ItemStack book = inventory.getItem(0);
        ItemEnchantments stored = book.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored == null || stored.isEmpty()) {
            return original;
        }
        Holder<Enchantment> enchantment = stored.keySet().stream().findFirst().orElse(null);
        if (enchantment == null) {
            return original;
        }
        int targetLevel = inventory.getItem(1).getEnchantments().getLevel(enchantment);
        if (targetLevel + 1 > OritechCatalystConfig.maxOverenchantLevel()) {
            return false;
        }
        return original;
    }
}
