package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingRodItem.class)
public abstract class FishingRodItemMixin {

    @WrapOperation(method = "use",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void ftbskies2aero$swapThrow(Level level, Player target, double x, double y, double z,
                                         SoundEvent sound, SoundSource src, float vol, float pitch,
                                         Operation<Void> op,
                                         @Local(argsOnly = true) Player user,
                                         @Local(argsOnly = true) InteractionHand hand) {
        if (sound == SoundEvents.FISHING_BOBBER_THROW
                && user.getItemInHand(hand).is(ModItems.VOID_FISHING_ROD.get())) {
            level.playSound(null, x, y, z, SoundEvents.WIND_CHARGE_THROW, src, vol, 0.65F);
        } else {
            op.call(level, target, x, y, z, sound, src, vol, pitch);
        }
    }
}
