package dev.ftb.mods.ftbskies2aerocompanion.basebuffer;

public final class TeamBaseGrid {
    public static final int REGION_BLOCKS = 512;

    private TeamBaseGrid() {}

    public static int strideX() {
        return BaseExclusionConfig.BASE_SIZE_REGIONS.get() + BaseExclusionConfig.BASE_SEPARATION_REGIONS.get();
    }

    public static int strideZ() {
        return BaseExclusionConfig.BASE_SIZE_REGIONS.get() + BaseExclusionConfig.BASE_SEPARATION_REGIONS.get();
    }

    public static int maxBaseRegionX() {
        int max = BaseExclusionConfig.MAX_REGION_X.get();
        int stride = strideX();
        return (max / stride) * stride;
    }

    public static boolean isBaseRegion(int regionX, int regionZ) {
        if (regionX < 0 || regionZ < 0) return false;
        int sx = strideX();
        int sz = strideZ();
        if (regionX > maxBaseRegionX()) return false;
        return (regionX % sx) == 0 && (regionZ % sz) == 0;
    }

    public static int[] baseRegionCenterBlocks(int regionX, int regionZ) {
        int size = BaseExclusionConfig.BASE_SIZE_REGIONS.get();
        int halfSpan = (size * REGION_BLOCKS) / 2;
        int blockX = regionX * REGION_BLOCKS + halfSpan;
        int blockZ = regionZ * REGION_BLOCKS + halfSpan;
        return new int[] { blockX, blockZ };
    }

    public static boolean isWithinBaseExclusion(int blockX, int blockZ) {
        return isWithinBaseExclusion(blockX, blockZ, BaseExclusionConfig.EXCLUSION_RADIUS.get());
    }

    public static boolean isWithinBaseExclusion(int blockX, int blockZ, int radius) {
        if (radius <= 0) return false;
        int sx = strideX();
        int sz = strideZ();
        int strideBlocksX = sx * REGION_BLOCKS;
        int strideBlocksZ = sz * REGION_BLOCKS;
        int size = BaseExclusionConfig.BASE_SIZE_REGIONS.get();
        int halfSpan = (size * REGION_BLOCKS) / 2;
        int maxRX = maxBaseRegionX();

        int approxAX = Math.floorDiv(blockX - halfSpan, strideBlocksX);
        int approxAZ = Math.floorDiv(blockZ - halfSpan, strideBlocksZ);

        long radiusSq = (long) radius * radius;

        for (int da = 0; da <= 1; da++) {
            for (int db = 0; db <= 1; db++) {
                int a = approxAX + da;
                int b = approxAZ + db;
                if (a < 0 || b < 0) continue;
                int regionX = a * sx;
                int regionZ = b * sz;
                if (regionX > maxRX) continue;
                int cx = regionX * REGION_BLOCKS + halfSpan;
                int cz = regionZ * REGION_BLOCKS + halfSpan;
                long dx = blockX - cx;
                long dz = blockZ - cz;
                if (dx * dx + dz * dz <= radiusSq) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int[] nthBaseRegion(int index) {
        if (index < 0) throw new IllegalArgumentException("index must be >= 0");
        int sx = strideX();
        int sz = strideZ();
        int maxRX = maxBaseRegionX();
        int basesPerRow = (maxRX / sx) + 1;
        int row = index / basesPerRow;
        int col = index % basesPerRow;
        return new int[] { col * sx, row * sz };
    }
}
