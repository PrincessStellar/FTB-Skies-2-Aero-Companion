package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.tropicraft;

import dev.ftb.mods.ftbskies2aerocompanion.compat.tropicraft.PinaColadaModelRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;
import java.util.Optional;

@Mixin(ItemRenderer.class)
public abstract class CocktailItemRendererMixin {

    @Unique
    private static final ResourceLocation ftbskies2$COCKTAIL_ITEM =
            ResourceLocation.fromNamespaceAndPath("tropicraft", "cocktail");
    @Unique
    private static final ResourceLocation ftbskies2$COCKTAIL_COMPONENT =
            ResourceLocation.fromNamespaceAndPath("tropicraft", "cocktail");
    @Unique
    private static final ResourceLocation ftbskies2$PINA_COLADA_ID =
            ResourceLocation.fromNamespaceAndPath("tropicraft", "pina_colada");

    @Unique private static Method ftbskies2$drinkAccessor;
    @Unique private static boolean ftbskies2$accessorResolved;

    @Inject(
            method = "getModel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ftbskies2$swapPinaColadaModel(ItemStack stack, Level level, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!ftbskies2$COCKTAIL_ITEM.equals(itemId)) return;

        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ftbskies2$COCKTAIL_COMPONENT);
        if (type == null) return;

        Object value = stack.get(type);
        if (value == null) return;

        if (!ftbskies2$accessorResolved) {
            ftbskies2$accessorResolved = true;
            try {
                ftbskies2$drinkAccessor = value.getClass().getMethod("drink");
            } catch (NoSuchMethodException ignored) {
            }
        }
        if (ftbskies2$drinkAccessor == null) return;

        Optional<?> drinkOpt;
        try {
            Object result = ftbskies2$drinkAccessor.invoke(value);
            if (!(result instanceof Optional<?> o)) return;
            drinkOpt = o;
        } catch (ReflectiveOperationException e) {
            return;
        }
        if (drinkOpt.isEmpty()) return;
        if (!(drinkOpt.get() instanceof Holder<?> holder)) return;

        Optional<? extends ResourceKey<?>> keyOpt = holder.unwrapKey();
        if (keyOpt.isEmpty()) return;
        if (!ftbskies2$PINA_COLADA_ID.equals(keyOpt.get().location())) return;

        BakedModel model = Minecraft.getInstance().getModelManager()
                .getModel(PinaColadaModelRegistration.MODEL_RESOURCE);
        if (model == null) return;
        cir.setReturnValue(model);
    }
}
