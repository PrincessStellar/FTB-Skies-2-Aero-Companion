package dev.ftb.mods.ftbskies2aerocompanion.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.event.EffectResolveEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Set;

public final class ShipSpellGuard {
    private static final Set<String> SHIP_NAMESPACES = Set.of("simulated", "sable", "aeronautics", "offroad");

    private ShipSpellGuard() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ShipSpellGuard::onEffectResolve);
    }

    private static void onEffectResolve(EffectResolveEvent.Pre event) {
        if (!(event.rayTraceResult instanceof EntityHitResult hit)) {
            return;
        }
        Entity entity = hit.getEntity();
        String namespace = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace();
        if (SHIP_NAMESPACES.contains(namespace)) {
            event.setCanceled(true);
        }
    }
}
