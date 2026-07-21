package dev.ftb.mods.ftbskies2aerocompanion.compat.sable;

/**
 * Marks that a Sable sub-level assembly or disassembly is moving blocks on the current thread.
 * Blocks whose removal has side effects (dropping inventory, refunding items, tearing down a
 * connection) can check this to tell a genuine break from being relocated by a ship, and skip
 * those side effects during the move. Set by the brackets on {@code SubLevelAssemblyHelper.moveBlocks}
 * and {@code SimAssemblyHelper.disassembleSubLevel}.
 */
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
