package dev.ftb.mods.ftbskies2aerocompanion.compat.integrateddynamics;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class IntegratedDynamicsDeferredReform {

    private static Boolean idLoaded;

    private IntegratedDynamicsDeferredReform() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (idLoaded == null) {
            idLoaded = ModList.get().isLoaded("integrateddynamics");
        }
        if (!idLoaded) {
            return;
        }
        IntegratedDynamicsNetworkReform.drainDeferred(event.getServer());
    }
}
