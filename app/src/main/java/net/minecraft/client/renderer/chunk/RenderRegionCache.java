package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderRegionCache {
    private final it.unimi.dsi.fastutil.objects.Object2ObjectMap<ChunkPos, RenderRegionCache.ChunkInfo> chunkInfoCache = new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>();

    @Nullable
    public RenderChunkRegion createRegion(Level p_200466_, SectionPos p_350879_) {
        RenderRegionCache.ChunkInfo renderregioncache$chunkinfo = this.getChunkInfo(p_200466_, p_350879_.x(), p_350879_.z());
        if (renderregioncache$chunkinfo.chunk().isSectionEmpty(p_350879_.getY())) {
            return null;
        } else {
            me.alphamode.mcbig.math.BigInteger i = p_350879_.x().subtract();
            me.alphamode.mcbig.math.BigInteger j = p_350879_.z().subtract();
            me.alphamode.mcbig.math.BigInteger k = p_350879_.x().add();
            me.alphamode.mcbig.math.BigInteger l = p_350879_.z().add();
            RenderChunk[] arenderchunk = new RenderChunk[9];

            for (me.alphamode.mcbig.math.BigInteger i1 = j; i1.compareTo(l) <= 0; i1 = i1.add()) {
                for (me.alphamode.mcbig.math.BigInteger j1 = i; j1.compareTo(k) <= 0; j1 = j1.add()) {
                    int k1 = RenderChunkRegion.index(i, j, j1, i1);
                    RenderRegionCache.ChunkInfo renderregioncache$chunkinfo1 = j1.equals(p_350879_.x()) && i1.equals(p_350879_.z())
                        ? renderregioncache$chunkinfo
                        : this.getChunkInfo(p_200466_, j1, i1);
                    arenderchunk[k1] = renderregioncache$chunkinfo1.renderChunk();
                }
            }

            return new RenderChunkRegion(p_200466_, i, j, arenderchunk);
        }
    }

    private RenderRegionCache.ChunkInfo getChunkInfo(Level p_350834_, me.alphamode.mcbig.math.BigInteger p_350803_, me.alphamode.mcbig.math.BigInteger p_350907_) {
        return this.chunkInfoCache
            .computeIfAbsent(
                new ChunkPos(p_350803_, p_350907_),
                p_200464_ -> new RenderRegionCache.ChunkInfo(p_350834_.getChunk(((ChunkPos)p_200464_).x, ((ChunkPos)p_200464_).z))
            );
    }

    @OnlyIn(Dist.CLIENT)
    static final class ChunkInfo {
        private final LevelChunk chunk;
        @Nullable
        private RenderChunk renderChunk;

        ChunkInfo(LevelChunk p_200479_) {
            this.chunk = p_200479_;
        }

        public LevelChunk chunk() {
            return this.chunk;
        }

        public RenderChunk renderChunk() {
            if (this.renderChunk == null) {
                this.renderChunk = new RenderChunk(this.chunk);
            }

            return this.renderChunk;
        }
    }
}
