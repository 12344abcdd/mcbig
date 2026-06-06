package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import net.minecraft.world.level.ChunkPos;

public interface ChunkTrackingView {
    ChunkTrackingView EMPTY = new ChunkTrackingView() {
        @Override
        public boolean contains(me.alphamode.mcbig.math.BigInteger p_294225_, me.alphamode.mcbig.math.BigInteger p_294897_, boolean p_294644_) {
            return false;
        }

        @Override
        public void forEach(Consumer<ChunkPos> p_295201_) {
        }
    };

    static ChunkTrackingView of(ChunkPos p_296254_, int p_295979_) {
        return new ChunkTrackingView.Positioned(p_296254_, p_295979_);
    }

    static void difference(ChunkTrackingView p_294391_, ChunkTrackingView p_294272_, Consumer<ChunkPos> p_295078_, Consumer<ChunkPos> p_294115_) {
        if (!p_294391_.equals(p_294272_)) {
            if (p_294391_ instanceof ChunkTrackingView.Positioned chunktrackingview$positioned
                    && p_294272_ instanceof ChunkTrackingView.Positioned chunktrackingview$positioned1
                    && chunktrackingview$positioned.squareIntersects(chunktrackingview$positioned1)) {
                me.alphamode.mcbig.math.BigInteger i = chunktrackingview$positioned.minX().min(chunktrackingview$positioned1.minX());
                me.alphamode.mcbig.math.BigInteger j = chunktrackingview$positioned.minZ().min(chunktrackingview$positioned1.minZ());
                me.alphamode.mcbig.math.BigInteger k = chunktrackingview$positioned.maxX().max(chunktrackingview$positioned1.maxX());
                me.alphamode.mcbig.math.BigInteger l = chunktrackingview$positioned.maxZ().max(chunktrackingview$positioned1.maxZ());

                for (me.alphamode.mcbig.math.BigInteger i1 = i; i1.compareTo(k) <= 0; i1 = i1.add()) {
                    for (me.alphamode.mcbig.math.BigInteger j1 = j; j1.compareTo(l) <= 0; j1 = j1.add()) {
                        boolean flag = chunktrackingview$positioned.contains(i1, j1);
                        boolean flag1 = chunktrackingview$positioned1.contains(i1, j1);
                        if (flag != flag1) {
                            if (flag1) {
                                p_295078_.accept(new ChunkPos(i1, j1));
                            } else {
                                p_294115_.accept(new ChunkPos(i1, j1));
                            }
                        }
                    }
                }

                return;
            }

            p_294391_.forEach(p_294115_);
            p_294272_.forEach(p_295078_);
        }
    }

    default boolean contains(ChunkPos p_296112_) {
        return this.contains(p_296112_.x, p_296112_.z);
    }

    default boolean contains(me.alphamode.mcbig.math.BigInteger p_295374_, me.alphamode.mcbig.math.BigInteger p_296479_) {
        return this.contains(p_295374_, p_296479_, true);
    }

    boolean contains(me.alphamode.mcbig.math.BigInteger p_294429_, me.alphamode.mcbig.math.BigInteger p_295591_, boolean p_296102_);

    void forEach(Consumer<ChunkPos> p_294937_);

    default boolean isInViewDistance(me.alphamode.mcbig.math.BigInteger p_295863_, me.alphamode.mcbig.math.BigInteger p_294569_) {
        return this.contains(p_295863_, p_294569_, false);
    }

    static boolean isInViewDistance(me.alphamode.mcbig.math.BigInteger p_294551_, me.alphamode.mcbig.math.BigInteger p_294918_, int p_296415_, me.alphamode.mcbig.math.BigInteger p_296475_, me.alphamode.mcbig.math.BigInteger p_295248_) {
        return isWithinDistance(p_294551_, p_294918_, p_296415_, p_296475_, p_295248_, false);
    }

    static boolean isWithinDistance(me.alphamode.mcbig.math.BigInteger p_294927_, me.alphamode.mcbig.math.BigInteger p_295703_, int p_294990_, me.alphamode.mcbig.math.BigInteger p_295161_, me.alphamode.mcbig.math.BigInteger p_295394_, boolean p_295219_) {
        int i = p_295219_ ? 2 : 1;
        me.alphamode.mcbig.math.BigInteger j = me.alphamode.mcbig.math.BigInteger.ZERO.max(p_295161_.subtract(p_294927_).abs().subtract(i));
        me.alphamode.mcbig.math.BigInteger k = me.alphamode.mcbig.math.BigInteger.ZERO.max(p_295394_.subtract(p_295703_).abs().subtract(i));
        me.alphamode.mcbig.math.BigInteger l = j.multiply(j).add(k.multiply(k));
        int i1 = p_294990_ * p_294990_;
        return l.longValue() < (long)i1;
    }

    public static record Positioned(ChunkPos center, int viewDistance) implements ChunkTrackingView {
        me.alphamode.mcbig.math.BigInteger minX() {
            return this.center.x.subtract(this.viewDistance - 1);
        }

        me.alphamode.mcbig.math.BigInteger minZ() {
            return this.center.z.subtract(this.viewDistance - 1);
        }

        me.alphamode.mcbig.math.BigInteger maxX() {
            return this.center.x.add(this.viewDistance + 1);
        }

        me.alphamode.mcbig.math.BigInteger maxZ() {
            return this.center.z.add(this.viewDistance + 1);
        }

        @VisibleForTesting
        protected boolean squareIntersects(ChunkTrackingView.Positioned p_295100_) {
            return this.minX().compareTo(p_295100_.maxX()) <= 0 && this.maxX().compareTo(p_295100_.minX()) >= 0 && this.minZ().compareTo(p_295100_.maxZ()) <= 0 && this.maxZ().compareTo(p_295100_.minZ()) >= 0;
        }

        @Override
        public boolean contains(me.alphamode.mcbig.math.BigInteger p_295177_, me.alphamode.mcbig.math.BigInteger p_294248_, boolean p_294703_) {
            return ChunkTrackingView.isWithinDistance(this.center.x, this.center.z, this.viewDistance, p_295177_, p_294248_, p_294703_);
        }

        @Override
        public void forEach(Consumer<ChunkPos> p_294236_) {
            for (me.alphamode.mcbig.math.BigInteger i = this.minX(); i.compareTo(this.maxX()) <= 0; i = i.add()) {
                for (me.alphamode.mcbig.math.BigInteger j = this.minZ(); j.compareTo(this.maxZ()) <= 0; j = j.add()) {
                    if (this.contains(i, j)) {
                        p_294236_.accept(new ChunkPos(i, j));
                    }
                }
            }
        }
    }
}
