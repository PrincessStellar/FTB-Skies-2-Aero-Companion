package dev.ftb.mods.ftbskies2aerocompanion.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
    @Unique
    private static final ResourceLocation FTBSKIES2AERO$VOID_TEXTURE =
            FTBSkies2AeroCompanion.id("textures/entity/void_fishing_hook.png");

    @Unique
    private static volatile RenderType ftbskies2aero$voidRenderType;

    @Unique
    private static RenderType ftbskies2aero$getVoidRenderType() {
        RenderType rt = ftbskies2aero$voidRenderType;
        if (rt == null) {
            rt = RenderType.entityCutout(FTBSKIES2AERO$VOID_TEXTURE);
            ftbskies2aero$voidRenderType = rt;
        }
        return rt;
    }

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
                    ordinal = 0))
    private VertexConsumer ftbskies2aero$swapBobberBuffer(MultiBufferSource source, RenderType type,
                                                          Operation<VertexConsumer> op,
                                                          @Local(argsOnly = true) FishingHook hook) {
        Player owner = hook.getPlayerOwner();
        if (owner != null
                && (owner.getMainHandItem().is(ModItems.VOID_FISHING_ROD.get())
                        || owner.getOffhandItem().is(ModItems.VOID_FISHING_ROD.get()))) {
            return op.call(source, ftbskies2aero$getVoidRenderType());
        }
        return op.call(source, type);
    }
}
