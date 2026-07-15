package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.createnewage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "org.antarcticgardens.cna.content.electricity.generation.magnet.ImplementedMagnetBlock", remap = false)
public class ImplementedMagnetBlockTooltipMixin {
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$removeHardcodedMagnetTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        ci.cancel();
    }
}
