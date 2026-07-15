package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.functionalstorage;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

@Mixin(targets = "com.buuz135.functionalstorage.inventory.ControllerInventoryHandler", remap = false)
public abstract class ControllerInventoryHandlerPerfMixin {
    @Unique
    private List<?> ftbskies2aero$cachedHandlers;
    @Unique
    private int ftbskies2aero$cachedSize;
    @Unique
    private Set<Object> ftbskies2aero$membership;

    @WrapOperation(
            method = {"getStackInSlot", "insertItem", "extractItem"},
            at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z")
    )
    private boolean ftbskies2aero$fastHandlerContains(List<?> handlers, Object handler, Operation<Boolean> original) {
        if (handlers != ftbskies2aero$cachedHandlers || handlers.size() != ftbskies2aero$cachedSize) {
            Set<Object> rebuilt = Collections.newSetFromMap(new IdentityHashMap<>());
            rebuilt.addAll(handlers);
            ftbskies2aero$membership = rebuilt;
            ftbskies2aero$cachedHandlers = handlers;
            ftbskies2aero$cachedSize = handlers.size();
        }
        return ftbskies2aero$membership.contains(handler);
    }
}
