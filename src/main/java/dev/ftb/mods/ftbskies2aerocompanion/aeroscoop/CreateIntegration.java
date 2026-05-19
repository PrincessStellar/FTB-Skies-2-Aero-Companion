package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CreateIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateIntegration.class);

    private CreateIntegration() {}

    public static void register() {
        if (!ModList.get().isLoaded("create")) {
            LOGGER.info("Create not loaded; AeroScoop MovementBehaviour skipped.");
            return;
        }
        try {
            MovementBehaviour.REGISTRY.register(ModBlocks.AIR_FILTER.get(), new AeroScoopMovementBehaviour());
            MovingInteractionBehaviour.REGISTRY.register(ModBlocks.AIR_FILTER.get(), new AeroScoopMovingInteraction());
            LOGGER.info("Registered AeroScoop MovementBehaviour and MovingInteractionBehaviour for Create contraptions.");
        } catch (Throwable t) {
            LOGGER.error("Failed to register AeroScoop Create integrations", t);
        }
    }
}
