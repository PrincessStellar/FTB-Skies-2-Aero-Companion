package dev.ftb.mods.ftbskies2aerocompanion.client;

import dev.ftb.mods.ftbskies2aerocompanion.compat.ponder.AeroScoopPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;

public final class AeroScoopPonderBoot {
    private AeroScoopPonderBoot() {}

    public static void register() {
        PonderIndex.addPlugin(new AeroScoopPonderPlugin());
    }
}
