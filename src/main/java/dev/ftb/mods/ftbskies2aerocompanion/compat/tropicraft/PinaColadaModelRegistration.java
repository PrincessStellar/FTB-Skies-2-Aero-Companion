package dev.ftb.mods.ftbskies2aerocompanion.compat.tropicraft;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID, value = Dist.CLIENT)
public final class PinaColadaModelRegistration {

    public static final ResourceLocation MODEL_ID =
            ResourceLocation.fromNamespaceAndPath("ftb", "item/pina_colada");
    public static final ModelResourceLocation MODEL_RESOURCE =
            ModelResourceLocation.standalone(MODEL_ID);

    private PinaColadaModelRegistration() {}

    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        event.register(MODEL_RESOURCE);
    }
}
