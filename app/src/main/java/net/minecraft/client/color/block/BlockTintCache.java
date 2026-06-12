package net.minecraft.client.color.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public class BlockTintCache {
    private static final int MAX_CACHE_ENTRIES = 256;
    private final ThreadLocal<BlockTintCache.LatestCacheInfo> latestChunkOnThread = ThreadLocal.withInitial(BlockTintCache.LatestCacheInfo::new);
    private final it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap<ChunkPos, BlockTintCache.CacheData> cache = new it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap<>(256, 0.25F);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ToIntFunction<BlockPos> source;

    public BlockTintCache(ToIntFunction<BlockPos> p_193811_) {
        this.source = p_193811_;
    }

    public int getColor(BlockPos p_193813_) {
        me.alphamode.mcbig.math.BigInteger i = SectionPos.blockToSectionCoord(p_193813_.getBigX());
        me.alphamode.mcbig.math.BigInteger j = SectionPos.blockToSectionCoord(p_193813_.getBigZ());
        BlockTintCache.LatestCacheInfo blocktintcache$latestcacheinfo = this.latestChunkOnThread.get();
        if (!blocktintcache$latestcacheinfo.x.equals(i)
            || !blocktintcache$latestcacheinfo.z.equals(j)
            || blocktintcache$latestcacheinfo.cache == null
            || blocktintcache$latestcacheinfo.cache.isInvalidated()) {
            blocktintcache$latestcacheinfo.x = i;
            blocktintcache$latestcacheinfo.z = j;
            blocktintcache$latestcacheinfo.cache = this.findOrCreateChunkCache(i, j);
        }

        int[] aint = blocktintcache$latestcacheinfo.cache.getLayer(p_193813_.getY());
        int k = p_193813_.getBigX().and(me.alphamode.mcbig.core.BigConstants.Ints.FIFTEEN).intValue();
        int l = p_193813_.getBigZ().and(me.alphamode.mcbig.core.BigConstants.Ints.FIFTEEN).intValue();
        int i1 = l << 4 | k;
        int j1 = aint[i1];
        if (j1 != -1) {
            return j1;
        } else {
            int k1 = this.source.applyAsInt(p_193813_);
            aint[i1] = k1;
            return k1;
        }
    }

    public void invalidateForChunk(me.alphamode.mcbig.math.BigInteger p_92656_, me.alphamode.mcbig.math.BigInteger p_92657_) {
        try {
            this.lock.writeLock().lock();

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    ChunkPos k = new ChunkPos(p_92656_.add(i), p_92657_.add(j));
                    BlockTintCache.CacheData blocktintcache$cachedata = this.cache.remove(k);
                    if (blocktintcache$cachedata != null) {
                        blocktintcache$cachedata.invalidate();
                    }
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void invalidateAll() {
        try {
            this.lock.writeLock().lock();
            this.cache.values().forEach(BlockTintCache.CacheData::invalidate);
            this.cache.clear();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private BlockTintCache.CacheData findOrCreateChunkCache(me.alphamode.mcbig.math.BigInteger p_193815_, me.alphamode.mcbig.math.BigInteger p_193816_) {
        ChunkPos i = new ChunkPos(p_193815_, p_193816_);
        this.lock.readLock().lock();

        try {
            BlockTintCache.CacheData blocktintcache$cachedata = this.cache.get(i);
            if (blocktintcache$cachedata != null) {
                return blocktintcache$cachedata;
            }
        } finally {
            this.lock.readLock().unlock();
        }

        this.lock.writeLock().lock();

        BlockTintCache.CacheData blocktintcache$cachedata1;
        try {
            BlockTintCache.CacheData blocktintcache$cachedata3 = this.cache.get(i);
            if (blocktintcache$cachedata3 == null) {
                blocktintcache$cachedata1 = new BlockTintCache.CacheData();
                if (this.cache.size() >= 256) {
                    BlockTintCache.CacheData blocktintcache$cachedata2 = this.cache.removeFirst();
                    if (blocktintcache$cachedata2 != null) {
                        blocktintcache$cachedata2.invalidate();
                    }
                }

                this.cache.put(i, blocktintcache$cachedata1);
                return blocktintcache$cachedata1;
            }

            blocktintcache$cachedata1 = blocktintcache$cachedata3;
        } finally {
            this.lock.writeLock().unlock();
        }

        return blocktintcache$cachedata1;
    }

    static class CacheData {
        private final Int2ObjectArrayMap<int[]> cache = new Int2ObjectArrayMap<>(16);
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private static final int BLOCKS_PER_LAYER = Mth.square(16);
        private volatile boolean invalidated;

        public int[] getLayer(int p_193824_) {
            this.lock.readLock().lock();

            try {
                int[] aint = this.cache.get(p_193824_);
                if (aint != null) {
                    return aint;
                }
            } finally {
                this.lock.readLock().unlock();
            }

            this.lock.writeLock().lock();

            int[] aint1;
            try {
                aint1 = this.cache.computeIfAbsent(p_193824_, p_193826_ -> this.allocateLayer());
            } finally {
                this.lock.writeLock().unlock();
            }

            return aint1;
        }

        private int[] allocateLayer() {
            int[] aint = new int[BLOCKS_PER_LAYER];
            Arrays.fill(aint, -1);
            return aint;
        }

        public boolean isInvalidated() {
            return this.invalidated;
        }

        public void invalidate() {
            this.invalidated = true;
        }
    }

    static class LatestCacheInfo {
        public me.alphamode.mcbig.math.BigInteger x = me.alphamode.mcbig.math.BigInteger.val(Integer.MIN_VALUE);
        public me.alphamode.mcbig.math.BigInteger z = me.alphamode.mcbig.math.BigInteger.val(Integer.MIN_VALUE);
        @Nullable
        BlockTintCache.CacheData cache;

        private LatestCacheInfo() {
        }
    }
}
