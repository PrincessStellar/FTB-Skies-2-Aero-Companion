package dev.ftb.mods.ftbskies2aerocompanion.compat.fishingoverhaul;

import dev.ftb.mods.ftbskies2aerocompanion.mixin.VoidFishingHook;
import github.pitbox46.fishingoverhaul.ItemFishedEventPre;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

/**
 * Fishing Overhaul replaces vanilla's loot loop with a fishing minigame whose win handler spawns the loot
 * ItemEntity at the bobber. Over the void that bobber sits below the player, so the catch parabolas back into
 * the abyss while only the XP (spawned at the player) survives. We keep the minigame but redirect its payout:
 * the post-minigame {@link ItemFishedEvent} for a void hook is cancelled and its drops are deposited straight
 * into the player.
 *
 * <p>Fishing Overhaul fires its own {@link ItemFishedEventPre} between vanilla's pre-minigame
 * {@link ItemFishedEvent} and the win handler's, so we use it purely to flag the hook — that disambiguates the
 * win-handler event (which we own) from the vanilla one (which we ignore) without reading any of its internals.
 *
 * <p>Remove once Fishing Overhaul stops spawning loot at the hook position over non-water, or void fishing no
 * longer routes through it.
 */
public final class FishingOverhaulVoidDelivery {
    private FishingOverhaulVoidDelivery() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FishingOverhaulVoidDelivery::onItemFishedPre);
        NeoForge.EVENT_BUS.addListener(FishingOverhaulVoidDelivery::onItemFished);
    }

    private static void onItemFishedPre(ItemFishedEventPre event) {
        if (event.getHookEntity() instanceof VoidFishingHook vfh && vfh.ftbskies2aero$isVoidFishing()) {
            vfh.ftbskies2aero$setVoidDeliveryPending(true);
        }
    }

    private static void onItemFished(ItemFishedEvent event) {
        FishingHook hook = event.getHookEntity();
        if (!(hook instanceof VoidFishingHook vfh) || !vfh.ftbskies2aero$isVoidDeliveryPending()) {
            return;
        }
        vfh.ftbskies2aero$setVoidDeliveryPending(false);

        Level level = hook.level();
        if (level.isClientSide()) {
            return;
        }
        Player owner = hook.getPlayerOwner();
        if (owner == null) {
            return;
        }

        for (ItemStack drop : event.getDrops()) {
            if (drop.isEmpty()) {
                continue;
            }
            if (drop.is(ItemTags.FISHES)) {
                owner.awardStat(Stats.FISH_CAUGHT, 1);
            }
            level.addFreshEntity(new ExperienceOrb(level, owner.getX(), owner.getY() + 0.5, owner.getZ() + 0.5,
                    hook.getRandom().nextInt(6) + 1));

            ItemStack remainder = drop.copy();
            owner.getInventory().add(remainder);
            if (!remainder.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(level, owner.getX(), owner.getY(), owner.getZ(), remainder);
                itemEntity.setDeltaMovement(Vec3.ZERO);
                itemEntity.setPickUpDelay(0);
                level.addFreshEntity(itemEntity);
            }
        }

        level.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                0.2F, 1.5F + level.getRandom().nextFloat() * 0.4F);
        event.setCanceled(true);
    }
}
