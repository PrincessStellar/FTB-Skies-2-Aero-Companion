package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.createnewage;

import dev.ftb.mods.ftbskies2aerocompanion.compat.createnewage.AeroEnergiser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin {
    @Unique
    private static BlockEntityType<?> ftbskies2aero$energiserType;

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void aero$allowAeroEnergiser(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!(state.getBlock() instanceof AeroEnergiser)) {
            return;
        }
        if (ftbskies2aero$energiserType == null) {
            ftbskies2aero$energiserType = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(
                    ResourceLocation.fromNamespaceAndPath("create_new_age", "energiser"));
        }
        if ((Object) this == ftbskies2aero$energiserType) {
            cir.setReturnValue(true);
        }
    }
}
