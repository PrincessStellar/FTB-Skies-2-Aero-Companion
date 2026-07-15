package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.createsifter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.oierbravo.createsifter.content.contraptions.components.sifter.AbstractSifterBlockEntity;
import com.oierbravo.createsifter.content.contraptions.components.sifter.AbstractSifterRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSifterRenderer.class)
public abstract class AbstractSifterRendererMeshFlatMixin {

    @Inject(
            method = "renderStaticBlock(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/world/item/ItemStack;Lcom/oierbravo/createsifter/content/contraptions/components/sifter/AbstractSifterBlockEntity;)V",
            at = @At("HEAD")
    )
    private void ftbskies2$flattenMeshPush(PoseStack pose, MultiBufferSource buffer, int light, int overlay, ItemStack stack, AbstractSifterBlockEntity be, CallbackInfo ci) {
        pose.pushPose();
        pose.translate(0.0D, -0.5D, 0.0D);
        pose.mulPose(Axis.XP.rotationDegrees(-90F));
    }

    @Inject(
            method = "renderStaticBlock(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/world/item/ItemStack;Lcom/oierbravo/createsifter/content/contraptions/components/sifter/AbstractSifterBlockEntity;)V",
            at = @At("RETURN")
    )
    private void ftbskies2$flattenMeshPop(PoseStack pose, MultiBufferSource buffer, int light, int overlay, ItemStack stack, AbstractSifterBlockEntity be, CallbackInfo ci) {
        pose.popPose();
    }
}
