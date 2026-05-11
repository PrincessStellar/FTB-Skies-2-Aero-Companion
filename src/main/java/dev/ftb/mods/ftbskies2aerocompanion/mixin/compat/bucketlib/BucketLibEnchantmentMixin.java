package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.bucketlib;

import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BucketLib's {@code bucketLib$isInfinityEnchantment} (merged into
 * {@link Enchantment} via {@code de.cech12.bucketlib.mixin.EnchantmentMixin})
 * calls {@code VanillaRegistries.createLookup()} on first invocation, which
 * eagerly bootstraps the entire vanilla registry universe (including
 * {@code MultiNoiseBiomeSourceParameterLists}). With this pack's biome content
 * the climate {@code RTree} sort inside that bootstrap freezes the Render
 * thread for many minutes during JEI's recipe rebuild on world join.
 *
 * <p>We target {@link Enchantment} directly (not BucketLib's mixin class — you
 * cannot mixin into a mixin) with a higher priority so BucketLib's mixin
 * merges its private method in first; we then short-circuit that merged
 * method to {@code false}. The pack already ships
 * {@code infinityEnchantmentEnabled = false} in {@code bucketlib-server.toml},
 * so the only effect is skipping the unwanted registry bootstrap.
 *
 * <p>{@code require = 0} keeps this safe if BucketLib is absent — the
 * injector silently no-ops when the target method isn't present.
 */
@Mixin(value = Enchantment.class, priority = 2000)
public class BucketLibEnchantmentMixin {

    @Inject(
            method = "bucketLib$isInfinityEnchantment()Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0,
            expect = 0
    )
    private void ftbskies2$bypassInfinityLookup(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
