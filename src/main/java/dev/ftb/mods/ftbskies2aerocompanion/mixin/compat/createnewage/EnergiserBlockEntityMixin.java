package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.createnewage;

import dev.ftb.mods.ftbskies2aerocompanion.compat.createnewage.AeroEnergiser;
import dev.ftb.mods.ftbskies2aerocompanion.compat.createnewage.CnaAddonConfig;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.antarcticgardens.cna.content.energising.EnergiserBlock;
import org.antarcticgardens.cna.content.energising.EnergiserBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EnergiserBlockEntity.class, remap = false)
public abstract class EnergiserBlockEntityMixin {
    @Shadow
    public int tier;

    @Redirect(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lorg/antarcticgardens/cna/content/energising/EnergiserBlock;getCapacity(I)J"),
            require = 0,
            expect = 0
    )
    private long aero$resolveTierAndCapacity(int originalTier) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (self.getBlockState().getBlock() instanceof AeroEnergiser ae) {
            this.tier = ae.aeroTier();
            return EnergiserBlock.getCapacity(this.tier);
        }
        return EnergiserBlock.getCapacity(originalTier);
    }

    @Inject(method = "calculateStressApplied", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void aero$stress(CallbackInfoReturnable<Float> cir) {
        if (this.tier >= CnaAddonConfig.TIER_NETHERITE) {
            cir.setReturnValue((float) CnaAddonConfig.stressFor(this.tier));
        }
    }
}
