package dev.ftb.mods.ftbskies2aerocompanion.compat.sable;

public final class SubLevelMoveGuard {

    private static final ThreadLocal<int[]> DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private SubLevelMoveGuard() {}

    public static void enter() {
        DEPTH.get()[0]++;
    }

    public static void exit() {
        int[] depth = DEPTH.get();
        if (depth[0] > 0) {
            depth[0]--;
        }
    }

    public static boolean isActive() {
        return DEPTH.get()[0] > 0;
    }
}
