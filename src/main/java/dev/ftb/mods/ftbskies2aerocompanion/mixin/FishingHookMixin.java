package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import dev.ftb.mods.ftbskies2aerocompanion.loot.VoidFishingLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the {@link ModItems#VOID_FISHING_ROD void fishing rod} catch fish over the void.
 *
 * <p>Only fires when the bobber's owner is wielding a void fishing rod at the moment
 * the conditions are first met — vanilla fishing rods cast a normal hook with normal
 * physics. When a void-rod bobber descends to or below {@code playerY - 1} with an
 * unbroken column of air all the way down past the world floor, this mixin pins it at
 * that height and spoofs the fluid checks vanilla uses to gate fishing. Vanilla then
 * transitions the hook to BOBBING and runs the normal {@code catchingFish} loop, so
 * loot, lure speed, luck, and rod retrieve all behave exactly as they do over water.
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    @Shadow
    public abstract Player getPlayerOwner();

    @Unique
    private static final double VOID_BOB_AMPLITUDE = 0.08;

    @Unique
    private static final double VOID_BOB_PERIOD_TICKS = 80.0;

    @Unique
    private boolean ftbskies2aero$voidFishing = false;

    @Unique
    private double ftbskies2aero$voidFishY = 0.0;

    @Unique
    private double ftbskies2aero$voidBobY() {
        FishingHook self = (FishingHook) (Object) this;
        double phase = (self.tickCount * 2.0 * Math.PI) / VOID_BOB_PERIOD_TICKS;
        return ftbskies2aero$voidFishY + Math.sin(phase) * VOID_BOB_AMPLITUDE;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void ftbskies2aero$detectAndLock(CallbackInfo ci) {
        FishingHook self = (FishingHook) (Object) this;

        if (ftbskies2aero$voidFishing) {
            self.setPos(self.getX(), ftbskies2aero$voidBobY(), self.getZ());
            self.setDeltaMovement(Vec3.ZERO);
            return;
        }

        Player owner = getPlayerOwner();
        if (owner == null) {
            return;
        }
        if (!ftbskies2aero$ownerWieldsVoidRod(owner)) {
            return;
        }

        double targetY = owner.getY() - 1.0;
        if (self.getY() > targetY) {
            return;
        }
        if (!ftbskies2aero$columnClearToVoid(self.level(), self.blockPosition())) {
            return;
        }

        ftbskies2aero$voidFishing = true;
        ftbskies2aero$voidFishY = targetY;
        self.setPos(self.getX(), ftbskies2aero$voidBobY(), self.getZ());
        self.setDeltaMovement(Vec3.ZERO);
        ftbskies2aero$emitVoidLockOn(self);
    }

    @Unique
    private static void ftbskies2aero$emitVoidLockOn(FishingHook self) {
        if (self.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    self.getX(), self.getY() + 0.1, self.getZ(),
                    14, 0.25, 0.05, 0.25, 0.02);
            sl.sendParticles(ParticleTypes.SOUL,
                    self.getX(), self.getY() + 0.1, self.getZ(),
                    6, 0.2, 0.05, 0.2, 0.01);
            sl.playSound(null, self.getX(), self.getY(), self.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 0.6F, 0.85F);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void ftbskies2aero$relock(CallbackInfo ci) {
        if (!ftbskies2aero$voidFishing) {
            return;
        }
        FishingHook self = (FishingHook) (Object) this;
        self.setPos(self.getX(), ftbskies2aero$voidBobY(), self.getZ());
        self.setDeltaMovement(Vec3.ZERO);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean ftbskies2aero$forceWaterTag(FluidState state, TagKey<Fluid> tag) {
        if (ftbskies2aero$voidFishing && tag == FluidTags.WATER) {
            return true;
        }
        return state.is(tag);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private float ftbskies2aero$forceWaterHeight(FluidState state, BlockGetter getter, BlockPos pos) {
        if (ftbskies2aero$voidFishing) {
            return 1.0F;
        }
        return state.getHeight(getter, pos);
    }

    @Inject(method = "calculateOpenWater", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$forceOpenWater(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (ftbskies2aero$voidFishing) {
            cir.setReturnValue(true);
        }
    }

    @ModifyArg(method = {"tick", "catchingFish"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"),
            index = 0,
            require = 0)
    private ParticleOptions ftbskies2aero$swapParticle(ParticleOptions original) {
        if (!ftbskies2aero$voidFishing) return original;
        if (original == ParticleTypes.BUBBLE) return ParticleTypes.SOUL_FIRE_FLAME;
        if (original == ParticleTypes.SPLASH) return ParticleTypes.SOUL;
        if (original == ParticleTypes.FISHING) return ParticleTypes.SOUL_FIRE_FLAME;
        return original;
    }

    @ModifyArg(method = "catchingFish",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/FishingHook;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"),
            index = 0)
    private SoundEvent ftbskies2aero$swapSound(SoundEvent original) {
        if (ftbskies2aero$voidFishing && original == SoundEvents.FISHING_BOBBER_SPLASH) {
            return SoundEvents.ENDERMAN_TELEPORT;
        }
        return original;
    }

    @ModifyArg(method = "retrieve",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/ReloadableServerRegistries$Holder;getLootTable(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/world/level/storage/loot/LootTable;"))
    private ResourceKey<LootTable> ftbskies2aero$swapLootTable(ResourceKey<LootTable> original) {
        if (ftbskies2aero$voidFishing && original == BuiltInLootTables.FISHING) {
            return VoidFishingLootTables.FISHING;
        }
        return original;
    }

    /**
     * Vanilla constructs the loot ItemEntity at {@code this.getX/Y/Z()} — i.e. the hook position, which over the void
     * is below the player and the items end up flying along a parabola back into the abyss. Teleporting the hook to
     * the player right before {@code retrieve} runs makes vanilla spawn the items on top of the player instead.
     */
    @Inject(method = "retrieve", at = @At("HEAD"))
    private void ftbskies2aero$snapHookToPlayer(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!ftbskies2aero$voidFishing) return;
        FishingHook self = (FishingHook) (Object) this;
        if (self.level().isClientSide) return;
        Player owner = getPlayerOwner();
        if (owner == null) return;
        self.setPos(owner.getX(), owner.getY(), owner.getZ());
    }

    /**
     * Vanilla shoots the loot ItemEntity from the hook (down in the void) up toward the player along a parabola —
     * over the void that means the item flies once, misses, and falls back into the abyss. For void fishing,
     * deposit the loot directly into the player's inventory; if it doesn't fit, drop the remainder at their feet.
     */
    @Redirect(method = "retrieve",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean ftbskies2aero$deliverLootToPlayer(Level level, Entity entity) {
        if (!ftbskies2aero$voidFishing || !(entity instanceof ItemEntity itemEntity)) {
            return level.addFreshEntity(entity);
        }
        Player owner = getPlayerOwner();
        if (owner == null) {
            return level.addFreshEntity(entity);
        }
        ItemStack stack = itemEntity.getItem();
        owner.getInventory().add(stack);
        if (stack.isEmpty()) {
            level.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                    0.2F, 1.5F + level.random.nextFloat() * 0.4F);
            return true;
        }
        itemEntity.setItem(stack);
        itemEntity.setPos(owner.getX(), owner.getY(), owner.getZ());
        itemEntity.setDeltaMovement(Vec3.ZERO);
        itemEntity.setPickUpDelay(0);
        return level.addFreshEntity(itemEntity);
    }

    @Unique
    private static boolean ftbskies2aero$ownerWieldsVoidRod(Player owner) {
        return ftbskies2aero$isAnyVoidRod(owner.getMainHandItem())
                || ftbskies2aero$isAnyVoidRod(owner.getOffhandItem());
    }

    @Unique
    private static boolean ftbskies2aero$isAnyVoidRod(ItemStack stack) {
        if (stack.is(ModItems.VOID_FISHING_ROD.get())) return true;
        for (var rod : ModItems.TIERED_VOID_FISHING_RODS.values()) {
            if (stack.is(rod.get())) return true;
        }
        return false;
    }

    @Unique
    private boolean ftbskies2aero$columnClearToVoid(Level level, BlockPos hookPos) {
        int x = hookPos.getX();
        int z = hookPos.getZ();
        int minY = level.getMinBuildHeight();
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int y = hookPos.getY(); y >= minY; y--) {
            mut.set(x, y, z);
            BlockState state = level.getBlockState(mut);
            if (!state.isAir()) {
                return false;
            }
        }
        return true;
    }
}
