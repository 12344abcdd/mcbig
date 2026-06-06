package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;

public abstract class BiomeSource implements BiomeResolver {
    public static final Codec<BiomeSource> CODEC = BuiltInRegistries.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
    private final Supplier<Set<Holder<Biome>>> possibleBiomes = Suppliers.memoize(
        () -> this.collectPossibleBiomes().distinct().collect(ImmutableSet.toImmutableSet())
    );

    protected BiomeSource() {
    }

    protected abstract MapCodec<? extends BiomeSource> codec();

    protected abstract Stream<Holder<Biome>> collectPossibleBiomes();

    public Set<Holder<Biome>> possibleBiomes() {
        return this.possibleBiomes.get();
    }

    public Set<Holder<Biome>> getBiomesWithin(me.alphamode.mcbig.math.BigInteger p_186705_, int p_186706_, me.alphamode.mcbig.math.BigInteger p_186707_, int p_186708_, Climate.Sampler p_186709_) {
        me.alphamode.mcbig.math.BigInteger i = QuartPos.fromBlock(p_186705_.subtract(p_186708_));
        int j = QuartPos.fromBlock(p_186706_ - p_186708_);
        me.alphamode.mcbig.math.BigInteger k = QuartPos.fromBlock(p_186707_.subtract(p_186708_));
        me.alphamode.mcbig.math.BigInteger l = QuartPos.fromBlock(p_186705_.add(p_186708_));
        int i1 = QuartPos.fromBlock(p_186706_ + p_186708_);
        me.alphamode.mcbig.math.BigInteger j1 = QuartPos.fromBlock(p_186707_.add(p_186708_));
        int k1 = l.subtract(i).add().intValue();
        int l1 = i1 - j + 1;
        int i2 = j1.subtract(k).add().intValue();
        Set<Holder<Biome>> set = Sets.newHashSet();

        for (int j2 = 0; j2 < i2; j2++) {
            for (int k2 = 0; k2 < k1; k2++) {
                for (int l2 = 0; l2 < l1; l2++) {
                    me.alphamode.mcbig.math.BigInteger i3 = i.add(k2);
                    int j3 = j + l2;
                    me.alphamode.mcbig.math.BigInteger k3 = k.add(j2);
                    set.add(this.getNoiseBiome(i3, me.alphamode.mcbig.math.BigInteger.val(j3), k3, p_186709_));
                }
            }
        }

        return set;
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
        me.alphamode.mcbig.math.BigInteger p_220571_, int p_220572_, me.alphamode.mcbig.math.BigInteger p_220573_, int p_220574_, Predicate<Holder<Biome>> p_220575_, RandomSource p_220576_, Climate.Sampler p_220577_
    ) {
        return this.findBiomeHorizontal(p_220571_, p_220572_, p_220573_, p_220574_, 1, p_220575_, p_220576_, false, p_220577_);
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(
        BlockPos p_220578_, int p_220579_, int p_220580_, int p_220581_, Predicate<Holder<Biome>> p_220582_, Climate.Sampler p_220583_, LevelReader p_220584_
    ) {
        Set<Holder<Biome>> set = this.possibleBiomes().stream().filter(p_220582_).collect(Collectors.toUnmodifiableSet());
        if (set.isEmpty()) {
            return null;
        } else {
            int i = Math.floorDiv(p_220579_, p_220580_);
            int[] aint = Mth.outFromOrigin(p_220578_.getY(), p_220584_.getMinY() + 1, p_220584_.getMaxY() + 1, p_220581_).toArray();

            for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.spiralAround(BlockPos.ZERO, i, Direction.EAST, Direction.SOUTH)) {
                me.alphamode.mcbig.math.BigInteger j = p_220578_.getBigX().add(blockpos$mutableblockpos.getBigX().multiply(p_220580_));
                me.alphamode.mcbig.math.BigInteger k = p_220578_.getBigZ().add(blockpos$mutableblockpos.getBigZ().multiply(p_220580_));
                me.alphamode.mcbig.math.BigInteger l = QuartPos.fromBlock(j);
                me.alphamode.mcbig.math.BigInteger i1 = QuartPos.fromBlock(k);

                for (int j1 : aint) {
                    int k1 = QuartPos.fromBlock(j1);
                    Holder<Biome> holder = this.getNoiseBiome(l, me.alphamode.mcbig.math.BigInteger.val(k1), i1, p_220583_);
                    if (set.contains(holder)) {
                        return Pair.of(new BlockPos(j, j1, k), holder);
                    }
                }
            }

            return null;
        }
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
        me.alphamode.mcbig.math.BigInteger p_220561_,
        int p_220562_,
        me.alphamode.mcbig.math.BigInteger p_220563_,
        int p_220564_,
        int p_220565_,
        Predicate<Holder<Biome>> p_220566_,
        RandomSource p_220567_,
        boolean p_220568_,
        Climate.Sampler p_220569_
    ) {
        me.alphamode.mcbig.math.BigInteger i = QuartPos.fromBlock(p_220561_);
        me.alphamode.mcbig.math.BigInteger j = QuartPos.fromBlock(p_220563_);
        int k = QuartPos.fromBlock(p_220564_);
        int l = QuartPos.fromBlock(p_220562_);
        Pair<BlockPos, Holder<Biome>> pair = null;
        int i1 = 0;
        int j1 = p_220568_ ? 0 : k;
        int k1 = j1;

        while (k1 <= k) {
            for (int l1 = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -k1; l1 <= k1; l1 += p_220565_) {
                boolean flag = Math.abs(l1) == k1;

                for (int i2 = -k1; i2 <= k1; i2 += p_220565_) {
                    if (p_220568_) {
                        boolean flag1 = Math.abs(i2) == k1;
                        if (!flag1 && !flag) {
                            continue;
                        }
                    }

                    me.alphamode.mcbig.math.BigInteger k2 = i.add(i2);
                    me.alphamode.mcbig.math.BigInteger j2 = j.add(l1);
                    Holder<Biome> holder = this.getNoiseBiome(k2, me.alphamode.mcbig.math.BigInteger.val(l), j2, p_220569_);
                    if (p_220566_.test(holder)) {
                        if (pair == null || p_220567_.nextInt(i1 + 1) == 0) {
                            BlockPos blockpos = new BlockPos(QuartPos.toBlock(k2), p_220562_, QuartPos.toBlock(j2));
                            if (p_220568_) {
                                return Pair.of(blockpos, holder);
                            }

                            pair = Pair.of(blockpos, holder);
                        }

                        i1++;
                    }
                }
            }

            k1 += p_220565_;
        }

        return pair;
    }

    @Override
    public abstract Holder<Biome> getNoiseBiome(me.alphamode.mcbig.math.BigInteger p_204238_, me.alphamode.mcbig.math.BigInteger p_204239_, me.alphamode.mcbig.math.BigInteger p_204240_, Climate.Sampler p_204241_);

    public void addDebugInfo(List<String> p_207837_, BlockPos p_207838_, Climate.Sampler p_207839_) {
    }
}
