package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;

public class BiomeManager {
    public static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
    private static final int ZOOM_BITS = 2;
    private static final int ZOOM = 4;
    private static final me.alphamode.mcbig.math.BigInteger ZOOM_MASK = me.alphamode.mcbig.math.BigInteger.val(3);
    private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
    private final long biomeZoomSeed;

    public BiomeManager(BiomeManager.NoiseBiomeSource p_186677_, long p_186678_) {
        this.noiseBiomeSource = p_186677_;
        this.biomeZoomSeed = p_186678_;
    }

    public static long obfuscateSeed(long p_47878_) {
        return Hashing.sha256().hashLong(p_47878_).asLong();
    }

    public BiomeManager withDifferentSource(BiomeManager.NoiseBiomeSource p_186688_) {
        return new BiomeManager(p_186688_, this.biomeZoomSeed);
    }

    public Holder<Biome> getBiome(BlockPos p_204215_) {
        me.alphamode.mcbig.math.BigInteger i = p_204215_.getBigX().subtract(me.alphamode.mcbig.core.BigConstants.Ints.TWO);
        me.alphamode.mcbig.math.BigInteger j = p_204215_.getBigY().subtract(me.alphamode.mcbig.core.BigConstants.Ints.TWO);
        me.alphamode.mcbig.math.BigInteger k = p_204215_.getBigZ().subtract(me.alphamode.mcbig.core.BigConstants.Ints.TWO);
        me.alphamode.mcbig.math.BigInteger l = i.shiftRight(ZOOM_BITS);
        me.alphamode.mcbig.math.BigInteger i1 = j.shiftRight(ZOOM_BITS);
        me.alphamode.mcbig.math.BigInteger j1 = k.shiftRight(ZOOM_BITS);
        double d0 = (double)(i.and(ZOOM_MASK).intValue()) / 4.0;
        double d1 = (double)(j.and(ZOOM_MASK).intValue()) / 4.0;
        double d2 = (double)(k.and(ZOOM_MASK).intValue()) / 4.0;
        int k1 = 0;
        double d3 = Double.POSITIVE_INFINITY;

        for (int l1 = 0; l1 < 8; l1++) {
            boolean flag = (l1 & 4) == 0;
            boolean flag1 = (l1 & 2) == 0;
            boolean flag2 = (l1 & 1) == 0;
            me.alphamode.mcbig.math.BigInteger i2 = flag ? l : l.add();
            me.alphamode.mcbig.math.BigInteger j2 = flag1 ? i1 : i1.add();
            me.alphamode.mcbig.math.BigInteger k2 = flag2 ? j1 : j1.add();
            double d4 = flag ? d0 : d0 - 1.0;
            double d5 = flag1 ? d1 : d1 - 1.0;
            double d6 = flag2 ? d2 : d2 - 1.0;
            double d7 = getFiddledDistance(this.biomeZoomSeed, i2, j2, k2, d4, d5, d6);
            if (d3 > d7) {
                k1 = l1;
                d3 = d7;
            }
        }

        me.alphamode.mcbig.math.BigInteger l2 = (k1 & 4) == 0 ? l : l.add();
        me.alphamode.mcbig.math.BigInteger i3 = (k1 & 2) == 0 ? i1 : i1.add();
        me.alphamode.mcbig.math.BigInteger j3 = (k1 & 1) == 0 ? j1 : j1.add();
        return this.noiseBiomeSource.getNoiseBiome(l2, i3, j3);
    }

    public Holder<Biome> getNoiseBiomeAtPosition(double p_204207_, double p_204208_, double p_204209_) {
        me.alphamode.mcbig.math.BigInteger i = QuartPos.fromBlock(Mth.bigFloor(p_204207_));
        me.alphamode.mcbig.math.BigInteger j = QuartPos.fromBlock(Mth.bigFloor(p_204208_));
        me.alphamode.mcbig.math.BigInteger k = QuartPos.fromBlock(Mth.bigFloor(p_204209_));
        return this.getNoiseBiomeAtQuart(i, j, k);
    }

    public Holder<Biome> getNoiseBiomeAtPosition(BlockPos p_204217_) {
        me.alphamode.mcbig.math.BigInteger i = QuartPos.fromBlock(p_204217_.getBigX());
        me.alphamode.mcbig.math.BigInteger j = QuartPos.fromBlock(p_204217_.getBigY());
        me.alphamode.mcbig.math.BigInteger k = QuartPos.fromBlock(p_204217_.getBigZ());
        return this.getNoiseBiomeAtQuart(i, j, k);
    }

    public Holder<Biome> getNoiseBiomeAtQuart(me.alphamode.mcbig.math.BigInteger p_204211_, me.alphamode.mcbig.math.BigInteger p_204212_, me.alphamode.mcbig.math.BigInteger p_204213_) {
        return this.noiseBiomeSource.getNoiseBiome(p_204211_, p_204212_, p_204213_);
    }

    public Holder<Biome> getNoiseBiomeAtQuart(int p_204211_, int p_204212_, int p_204213_) {
        return this.noiseBiomeSource.getNoiseBiome(p_204211_, p_204212_, p_204213_);
    }

    private static double getFiddledDistance(long p_186680_, me.alphamode.mcbig.math.BigInteger p_186681_, me.alphamode.mcbig.math.BigInteger p_186682_, me.alphamode.mcbig.math.BigInteger p_186683_, double p_186684_, double p_186685_, double p_186686_) {
        long $$7 = LinearCongruentialGenerator.next(p_186680_, (long)p_186681_.longValue());
        $$7 = LinearCongruentialGenerator.next($$7, (long)p_186682_.longValue());
        $$7 = LinearCongruentialGenerator.next($$7, (long)p_186683_.longValue());
        $$7 = LinearCongruentialGenerator.next($$7, (long)p_186681_.longValue());
        $$7 = LinearCongruentialGenerator.next($$7, (long)p_186682_.longValue());
        $$7 = LinearCongruentialGenerator.next($$7, (long)p_186683_.longValue());
        double d0 = getFiddle($$7);
        $$7 = LinearCongruentialGenerator.next($$7, p_186680_);
        double d1 = getFiddle($$7);
        $$7 = LinearCongruentialGenerator.next($$7, p_186680_);
        double d2 = getFiddle($$7);
        return Mth.square(p_186686_ + d2) + Mth.square(p_186685_ + d1) + Mth.square(p_186684_ + d0);
    }

    private static double getFiddle(long p_186690_) {
        double d0 = (double)Math.floorMod(p_186690_ >> 24, 1024) / 1024.0;
        return (d0 - 0.5) * 0.9;
    }

    public interface NoiseBiomeSource {
        Holder<Biome> getNoiseBiome(me.alphamode.mcbig.math.BigInteger p_204218_, me.alphamode.mcbig.math.BigInteger p_204219_, me.alphamode.mcbig.math.BigInteger p_204220_);

        default Holder<Biome> getNoiseBiome(long p_204218_, long p_204219_, long p_204220_) {
            return getNoiseBiome(me.alphamode.mcbig.math.BigInteger.val(p_204218_), me.alphamode.mcbig.math.BigInteger.val(p_204219_), me.alphamode.mcbig.math.BigInteger.val(p_204220_));
        }
    }
}
