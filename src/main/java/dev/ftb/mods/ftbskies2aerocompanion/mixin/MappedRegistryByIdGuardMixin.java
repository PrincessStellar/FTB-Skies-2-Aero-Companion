package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

@Mixin(MappedRegistry.class)
public class MappedRegistryByIdGuardMixin {

    @Shadow
    @Final
    private ObjectList<Holder.Reference<?>> byId;

    @WrapOperation(
            method = "byId",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder$Reference;value()Ljava/lang/Object;")
    )
    private Object ftbskies2aero$nullSafeById(Holder.Reference<?> reference, Operation<Object> original) {
        return reference == null ? null : original.call(reference);
    }

    @Inject(method = "iterator", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$skipNullEntries(CallbackInfoReturnable<Iterator<Object>> cir) {
        cir.setReturnValue(Iterators.transform(
                Iterators.filter(this.byId.iterator(), Objects::nonNull),
                Holder.Reference::value
        ));
    }

    @ModifyReturnValue(method = "holders", at = @At("RETURN"))
    private Stream<Holder.Reference<?>> ftbskies2aero$filterNullHolders(Stream<Holder.Reference<?>> original) {
        return original.filter(Objects::nonNull);
    }
}
