package dev.ftb.mods.ftbskies2aerocompanion.compat.ars_elemental;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import java.util.Optional;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class ArsElementalBrewing {

    private ArsElementalBrewing() {}

    @SubscribeEvent
    public static void onRegisterBrewing(RegisterBrewingRecipesEvent event) {
        if (!ModList.get().isLoaded("ars_elemental") || !ModList.get().isLoaded("irons_spellbooks")) {
            return;
        }

        Optional<Holder.Reference<Potion>> shock = BuiltInRegistries.POTION.getHolder(
                ResourceLocation.fromNamespaceAndPath("ars_elemental", "shock_potion"));
        if (shock.isEmpty()) return;

        Item lightning = BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "lightning_bottle"));
        if (lightning == Items.AIR) return;

        event.getBuilder().addMix(Potions.AWKWARD, lightning, shock.get());
    }
}
