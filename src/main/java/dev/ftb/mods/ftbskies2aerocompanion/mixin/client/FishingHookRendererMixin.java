package dev.ftb.mods.ftbskies2aerocompanion.mixin.client;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
    private static final ResourceLocation FTBSKIES2AERO$VOID_TEXTURE =
            FTBSkies2AeroCompanion.id("textures/entity/void_fishing_hook.png");

    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/projectile/FishingHook;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true)
    private void ftbskies2aero$swapTexture(FishingHook hook, CallbackInfoReturnable<ResourceLocation> cir) {
        Player owner = hook.getPlayerOwner();
        if (owner == null) return;
        ItemStack main = owner.getMainHandItem();
        ItemStack off = owner.getOffhandItem();
        if (main.is(ModItems.VOID_FISHING_ROD.get()) || off.is(ModItems.VOID_FISHING_ROD.get())) {
            cir.setReturnValue(FTBSKIES2AERO$VOID_TEXTURE);
        }
    }
}
