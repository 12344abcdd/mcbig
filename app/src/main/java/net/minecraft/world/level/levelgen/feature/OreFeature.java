package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class OreFeature extends Feature<OreConfiguration> {
    public OreFeature(Codec<OreConfiguration> p_66531_) {
        super(p_66531_);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> p_160177_) {
        RandomSource randomsource = p_160177_.random();
        BlockPos blockpos = p_160177_.origin();
        WorldGenLevel worldgenlevel = p_160177_.level();
        OreConfiguration oreconfiguration = p_160177_.config();
        float f = randomsource.nextFloat() * (float) Math.PI;
        float f1 = (float)oreconfiguration.size / 8.0F;
        int i = Mth.ceil(((float)oreconfiguration.size / 16.0F * 2.0F + 1.0F) / 2.0F);
        me.alphamode.mcbig.math.BigDecimal d0 = blockpos.getBigX().toBigDecimal().add(Math.sin((double)f) * (double)f1);
        me.alphamode.mcbig.math.BigDecimal d1 = blockpos.getBigX().toBigDecimal().subtract(Math.sin((double)f) * (double)f1);
        me.alphamode.mcbig.math.BigDecimal d2 = blockpos.getBigZ().toBigDecimal().add(Math.cos((double)f) * (double)f1);
        me.alphamode.mcbig.math.BigDecimal d3 = blockpos.getBigZ().toBigDecimal().subtract(Math.cos((double)f) * (double)f1);
        int j = 2;
        me.alphamode.mcbig.math.BigDecimal d4 = (blockpos.getBigY().toBigDecimal().add(randomsource.nextInt(3) - 2));
        me.alphamode.mcbig.math.BigDecimal d5 = (blockpos.getBigY().toBigDecimal().add(randomsource.nextInt(3) - 2));
        me.alphamode.mcbig.math.BigInteger k = blockpos.getBigX().subtract(Mth.ceil(f1) - i);
        me.alphamode.mcbig.math.BigInteger l = blockpos.getBigY().subtract(2 - i);
        me.alphamode.mcbig.math.BigInteger i1 = blockpos.getBigZ().subtract(Mth.ceil(f1) - i);
        int j1 = 2 * (Mth.ceil(f1) + i);
        int k1 = 2 * (2 + i);

        for(me.alphamode.mcbig.math.BigInteger l1 = k; l1.compareTo(k.add(j1)) <= 0; l1 = l1.add()) {
            for(me.alphamode.mcbig.math.BigInteger i2 = i1; i2.compareTo(i1.add(j1)) <= 0; i2 = i2.add()) {
                if (l.longValue() <= worldgenlevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, l1, i2)) {
                    return this.doPlace(worldgenlevel, randomsource, oreconfiguration, d0, d1, d2, d3, d4, d5, k, l, i1, j1, k1);
                }
            }
        }

        return false;
    }

    protected boolean doPlace(
        WorldGenLevel p_225172_,
        RandomSource p_225173_,
        OreConfiguration p_225174_,
        me.alphamode.mcbig.math.BigDecimal p_225175_,
        me.alphamode.mcbig.math.BigDecimal p_225176_,
        me.alphamode.mcbig.math.BigDecimal p_225177_,
        me.alphamode.mcbig.math.BigDecimal p_225178_,
        me.alphamode.mcbig.math.BigDecimal p_225179_,
        me.alphamode.mcbig.math.BigDecimal p_225180_,
        me.alphamode.mcbig.math.BigInteger p_225181_,
        me.alphamode.mcbig.math.BigInteger p_225182_,
        me.alphamode.mcbig.math.BigInteger p_225183_,
        int p_225184_,
        int p_225185_
    ) {
        int i = 0;
        BitSet bitset = new BitSet(p_225184_ * p_225185_ * p_225184_);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int j = p_225174_.size;
        me.alphamode.mcbig.math.BigDecimal[] adouble = new me.alphamode.mcbig.math.BigDecimal[j * 4];

        for (int k = 0; k < j; k++) {
            float f = (float)k / (float)j;
            me.alphamode.mcbig.math.BigDecimal d0 = Mth.lerp((double)f, p_225175_, p_225176_);
            me.alphamode.mcbig.math.BigDecimal d1 = Mth.lerp((double)f, p_225179_, p_225180_);
            me.alphamode.mcbig.math.BigDecimal d2 = Mth.lerp((double)f, p_225177_, p_225178_);
            double d3 = p_225173_.nextDouble() * (double)j / 16.0;
            double d4 = ((double)(Mth.sin((float) Math.PI * f) + 1.0F) * d3 + 1.0) / 2.0;
            adouble[k * 4 + 0] = d0;
            adouble[k * 4 + 1] = d1;
            adouble[k * 4 + 2] = d2;
            adouble[k * 4 + 3] = me.alphamode.mcbig.math.BigDecimal.val(d4);
        }

        for (int l3 = 0; l3 < j - 1; l3++) {
            if (!(adouble[l3 * 4 + 3].doubleValue() <= 0.0)) {
                for (int i4 = l3 + 1; i4 < j; i4++) {
                    if (!(adouble[i4 * 4 + 3].doubleValue() <= 0.0)) {
                        double d8 = adouble[l3 * 4 + 0].subtract(adouble[i4 * 4 + 0]).doubleValue();
                        double d10 = adouble[l3 * 4 + 1].subtract(adouble[i4 * 4 + 1]).doubleValue();
                        double d12 = adouble[l3 * 4 + 2].subtract(adouble[i4 * 4 + 2]).doubleValue();
                        double d14 = adouble[l3 * 4 + 3].subtract(adouble[i4 * 4 + 3]).doubleValue();
                        if (d14 * d14 > d8 * d8 + d10 * d10 + d12 * d12) {
                            if (d14 > 0.0) {
                                adouble[i4 * 4 + 3] = me.alphamode.mcbig.math.BigDecimal.val(-1.0);
                            } else {
                                adouble[l3 * 4 + 3] = me.alphamode.mcbig.math.BigDecimal.val(-1.0);
                            }
                        }
                    }
                }
            }
        }

        try (BulkSectionAccess bulksectionaccess = new BulkSectionAccess(p_225172_)) {
            for (int j4 = 0; j4 < j; j4++) {
                double d9 = adouble[j4 * 4 + 3].doubleValue();
                if (!(d9 < 0.0)) {
                    me.alphamode.mcbig.math.BigDecimal d11 = adouble[j4 * 4 + 0];
                    me.alphamode.mcbig.math.BigDecimal d13 = adouble[j4 * 4 + 1];
                    me.alphamode.mcbig.math.BigDecimal d15 = adouble[j4 * 4 + 2];
                    me.alphamode.mcbig.math.BigInteger k4 = Mth.bigFloor(d11.subtract(d9)).max(p_225181_);
                    me.alphamode.mcbig.math.BigInteger l = Mth.bigFloor(d13.subtract(d9)).max(p_225182_);
                    me.alphamode.mcbig.math.BigInteger i1 = Mth.bigFloor(d15.subtract(d9)).max(p_225183_);
                    me.alphamode.mcbig.math.BigInteger j1 = Mth.bigFloor(d11.add(d9)).max(k4);
                    me.alphamode.mcbig.math.BigInteger k1 = Mth.bigFloor(d13.add(d9)).max(l);
                    me.alphamode.mcbig.math.BigInteger l1 = Mth.bigFloor(d15.add(d9)).max(i1);

                    for(me.alphamode.mcbig.math.BigInteger i2 = k4; i2.compareTo(j1) <= 0; i2 = i2.add()) {
                        double d5 = (i2.toBigDecimal().add(me.alphamode.mcbig.core.BigConstants.AABB).subtract(d11)).divide(d9).doubleValue();
                        if (d5 * d5 < 1.0) {
                            for(me.alphamode.mcbig.math.BigInteger j2 = l; j2.compareTo(k1) <= 0; j2 = j2.add()) {
                                double d6 = (j2.toBigDecimal().add(me.alphamode.mcbig.core.BigConstants.AABB).subtract(d13)).divide(d9).doubleValue();
                                if (d5 * d5 + d6 * d6 < 1.0) {
                                    for(me.alphamode.mcbig.math.BigInteger k2 = i1; k2.compareTo(l1) <= 0; k2 = k2.add()) {
                                        double d7 = (k2.toBigDecimal().add(me.alphamode.mcbig.core.BigConstants.AABB).subtract(d15)).divide(d9).doubleValue();
                                        if (d5 * d5 + d6 * d6 + d7 * d7 < 1.0 && !p_225172_.isOutsideBuildHeight(j2.intValue())) {
                                            int l2 = i2.subtract(p_225181_).intValue() + (j2.subtract(p_225182_).intValue()) * p_225184_ + (k2.subtract(p_225183_).intValue()) * p_225184_ * p_225185_;
                                            if (!bitset.get(l2)) {
                                                bitset.set(l2);
                                                blockpos$mutableblockpos.set(i2, j2, k2);
                                                if (p_225172_.ensureCanWrite(blockpos$mutableblockpos)) {
                                                    LevelChunkSection levelchunksection = bulksectionaccess.getSection(blockpos$mutableblockpos);
                                                    if (levelchunksection != null) {
                                                        int i3 = SectionPos.sectionRelative(i2);
                                                        int j3 = SectionPos.sectionRelative(j2);
                                                        int k3 = SectionPos.sectionRelative(k2);
                                                        BlockState blockstate = levelchunksection.getBlockState(i3, j3, k3);

                                                        for (OreConfiguration.TargetBlockState oreconfiguration$targetblockstate : p_225174_.targetStates) {
                                                            if (canPlaceOre(
                                                                blockstate,
                                                                bulksectionaccess::getBlockState,
                                                                p_225173_,
                                                                p_225174_,
                                                                oreconfiguration$targetblockstate,
                                                                blockpos$mutableblockpos
                                                            )) {
                                                                levelchunksection.setBlockState(i3, j3, k3, oreconfiguration$targetblockstate.state, false);
                                                                i++;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return i > 0;
    }

    public static boolean canPlaceOre(
        BlockState p_225187_,
        Function<BlockPos, BlockState> p_225188_,
        RandomSource p_225189_,
        OreConfiguration p_225190_,
        OreConfiguration.TargetBlockState p_225191_,
        BlockPos.MutableBlockPos p_225192_
    ) {
        if (!p_225191_.target.test(p_225187_, p_225189_)) {
            return false;
        } else {
            return shouldSkipAirCheck(p_225189_, p_225190_.discardChanceOnAirExposure) ? true : !isAdjacentToAir(p_225188_, p_225192_);
        }
    }

    protected static boolean shouldSkipAirCheck(RandomSource p_225169_, float p_225170_) {
        if (p_225170_ <= 0.0F) {
            return true;
        } else {
            return p_225170_ >= 1.0F ? false : p_225169_.nextFloat() >= p_225170_;
        }
    }
}
