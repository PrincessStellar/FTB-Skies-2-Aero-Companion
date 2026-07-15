package dev.ftb.mods.ftbskies2aerocompanion.loot;

public interface VoidFishingHook {
    boolean ftbskies2aero$isVoidFishing();

    boolean ftbskies2aero$isVoidDeliveryPending();

    void ftbskies2aero$setVoidDeliveryPending(boolean pending);
}
