package dev.ftb.mods.ftbskies2aerocompanion.compat.sa;

import org.sathrek.sky_archipelago.worldgen.generator.field.IslandShapeArchetype;

public final class ForcedArchetypeContext {
    private static final ThreadLocal<IslandShapeArchetype> REQUESTED = new ThreadLocal<>();

    private ForcedArchetypeContext() {}

    public static void set(IslandShapeArchetype archetype) {
        REQUESTED.set(archetype);
    }

    public static void clear() {
        REQUESTED.remove();
    }

    public static IslandShapeArchetype resolve(IslandShapeArchetype fallback) {
        IslandShapeArchetype requested = REQUESTED.get();
        return requested != null ? requested : fallback;
    }
}
