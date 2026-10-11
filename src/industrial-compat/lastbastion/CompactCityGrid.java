package lastbastion;

/** Separated, seed-dependent city centers for the LAST BASTION profile. */
public final class CompactCityGrid {
    public static final int CELL_CHUNKS = 80;
    public static final int JITTER_CHUNKS = 6;
    public static final int MAX_RADIUS_BLOCKS = 470;
    private CompactCityGrid() {}
    private static long mix(long x) {
        x = (x ^ (x >>> 30)) * 0xbf58476d1ce4e5b9L;
        x = (x ^ (x >>> 27)) * 0x94d049bb133111ebL;
        return x ^ (x >>> 31);
    }
    private static long regionHash(int regionX, int regionZ, long seed) {
        return mix(seed ^ ((long)regionX * 0x632be59bd9b4e019L)
                        ^ ((long)regionZ * 0x9e3779b97f4a7c15L));
    }
    public static int centerX(int regionX, int regionZ, long seed) {
        return regionX * CELL_CHUNKS + CELL_CHUNKS / 2
            + (int)Math.floorMod(regionHash(regionX, regionZ, seed), 2L * JITTER_CHUNKS + 1) - JITTER_CHUNKS;
    }
    public static int centerZ(int regionX, int regionZ, long seed) {
        return regionZ * CELL_CHUNKS + CELL_CHUNKS / 2
            + (int)Math.floorMod(mix(regionHash(regionX, regionZ, seed) ^ 0xd1b54a32d192ed03L), 2L * JITTER_CHUNKS + 1) - JITTER_CHUNKS;
    }
    public static boolean isCenter(int chunkX, int chunkZ, long seed) {
        int rx=Math.floorDiv(chunkX,CELL_CHUNKS), rz=Math.floorDiv(chunkZ,CELL_CHUNKS);
        return chunkX==centerX(rx,rz,seed) && chunkZ==centerZ(rx,rz,seed);
    }
    public static boolean withinLimit(int chunkX, int chunkZ, long seed) {
        int rx=Math.floorDiv(chunkX,CELL_CHUNKS), rz=Math.floorDiv(chunkZ,CELL_CHUNKS);
        long dx=16L*(chunkX-centerX(rx,rz,seed)), dz=16L*(chunkZ-centerZ(rx,rz,seed));
        return dx*dx+dz*dz <= (long)MAX_RADIUS_BLOCKS*MAX_RADIUS_BLOCKS;
    }
}
