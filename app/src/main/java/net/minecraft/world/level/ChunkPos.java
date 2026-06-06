package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

import me.alphamode.mcbig.math.BigInteger;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkPos implements java.lang.Comparable<ChunkPos> {
    public static final Codec<ChunkPos> CODEC = me.alphamode.mcbig.core.ModdedCodec.STRING_STREAM
            .<ChunkPos>comapFlatMap(
                    p_373123_ -> Util.fixedSize(p_373123_, 2).map(p_372804_ -> new ChunkPos(me.alphamode.mcbig.math.BigInteger.val(p_372804_[0]), me.alphamode.mcbig.math.BigInteger.val(p_372804_[1]))),
                    p_372801_ -> Stream.of(p_372801_.x.toString(), p_372801_.z.toString())
            )
            .stable();
    public static final StreamCodec<ByteBuf, ChunkPos> STREAM_CODEC = new StreamCodec<ByteBuf, ChunkPos>() {
        public ChunkPos decode(ByteBuf p_372878_) {
            return FriendlyByteBuf.readChunkPos(p_372878_);
        }

        public void encode(ByteBuf p_373102_, ChunkPos p_372822_) {
            FriendlyByteBuf.writeChunkPos(p_373102_, p_372822_);
        }
    };
    private static final int SAFETY_MARGIN = 1056;
    public static final ChunkPos INVALID_CHUNK_POS = null;//new ChunkPos(1875066, 1875066);
    private static final int SAFETY_MARGIN_CHUNKS = (32 + ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FULL).accumulatedDependencies().size() + 1) * 2;
    public static final int MAX_COORDINATE_VALUE = SectionPos.blockToSectionCoord(BlockPos.MAX_HORIZONTAL_COORDINATE) - SAFETY_MARGIN_CHUNKS;
    public static final ChunkPos ZERO = new ChunkPos(0, 0);
    private static final long COORD_BITS = 32L;
    private static final long COORD_MASK = 4294967295L;
    private static final int REGION_BITS = 5;
    public static final int REGION_SIZE = 32;
    private static final int REGION_MASK = 31;
    public static final int REGION_MAX_INDEX = 31;
    public final me.alphamode.mcbig.math.BigInteger x;
    public final me.alphamode.mcbig.math.BigInteger z;
    private static final me.alphamode.mcbig.math.BigInteger HASH_A = me.alphamode.mcbig.math.BigInteger.constant(1664525);
    private static final me.alphamode.mcbig.math.BigInteger HASH_C = me.alphamode.mcbig.math.BigInteger.constant(1013904223);
    private static final int HASH_Z_XOR = -559038737;

    public ChunkPos(long p_45580_, long p_45581_) {
        this.x = BigInteger.constant(p_45580_);
        this.z = BigInteger.constant(p_45581_);
    }

    public ChunkPos(me.alphamode.mcbig.math.BigInteger p_45582_, me.alphamode.mcbig.math.BigInteger p_45583_) {
        this.x = p_45582_;
        this.z = p_45583_;
    }

    public ChunkPos(BlockPos p_45587_) {
        this.x = SectionPos.blockToSectionCoord(p_45587_.getBigX());
        this.z = SectionPos.blockToSectionCoord(p_45587_.getBigZ());
    }

    public ChunkPos(long p_45585_) {
        this.x = BigInteger.constant((int)p_45585_);
        this.z = BigInteger.constant((int)(p_45585_ >> 32));
    }

    public static ChunkPos minFromRegion(me.alphamode.mcbig.math.BigInteger p_220338_, me.alphamode.mcbig.math.BigInteger p_220339_) {
        return new ChunkPos(p_220338_.shiftLeft(5), p_220339_.shiftLeft(5));
    }

    public static ChunkPos maxFromRegion(me.alphamode.mcbig.math.BigInteger p_220341_, me.alphamode.mcbig.math.BigInteger p_220342_) {
        return new ChunkPos((p_220341_.shiftLeft(5)).add(me.alphamode.mcbig.core.BigConstants.Ints.THIRTY_ONE), (p_220342_.shiftLeft(5)).add(me.alphamode.mcbig.core.BigConstants.Ints.THIRTY_ONE));
    }

    public static int getX(long p_45593_) {
        return (int)(p_45593_ & 4294967295L);
    }

    public static int getZ(long p_45603_) {
        return (int)(p_45603_ >>> 32 & 4294967295L);
    }

    @Override
    public int hashCode() {
        return hash(this.x, this.z);
    }

    public static int hash(me.alphamode.mcbig.math.BigInteger p_220344_, me.alphamode.mcbig.math.BigInteger p_220345_) {
        int i = 1664525 * p_220344_.intValue() + 1013904223;
        int j = 1664525 * (p_220345_.intValue() ^ -559038737) + 1013904223;
        return i ^ j;
    }

    @Override
    public boolean equals(Object p_45607_) {
        if (this == p_45607_) {
            return true;
        } else {
            return !(p_45607_ instanceof ChunkPos chunkpos) ? false : this.x.equals(chunkpos.x) && this.z.equals(chunkpos.z);
        }
    }

    public me.alphamode.mcbig.math.BigInteger getMiddleBlockX() {
        return this.getBlockX(8);
    }

    public me.alphamode.mcbig.math.BigInteger getMiddleBlockZ() {
        return this.getBlockZ(8);
    }

    public me.alphamode.mcbig.math.BigInteger getMinBlockX() {
        return SectionPos.sectionToBlockCoord(this.x);
    }

    public me.alphamode.mcbig.math.BigInteger getMinBlockZ() {
        return SectionPos.sectionToBlockCoord(this.z);
    }

    public me.alphamode.mcbig.math.BigInteger getMaxBlockX() {
        return this.getBlockX(15);
    }

    public me.alphamode.mcbig.math.BigInteger getMaxBlockZ() {
        return this.getBlockZ(15);
    }

    public me.alphamode.mcbig.math.BigInteger getRegionX() {
        return this.x.shiftRight(5);
    }

    public me.alphamode.mcbig.math.BigInteger getRegionZ() {
        return this.z.shiftRight(5);
    }

    public int getRegionLocalX() {
        return this.x.and(me.alphamode.mcbig.core.BigConstants.Ints.THIRTY_ONE).intValue();
    }

    public int getRegionLocalZ() {
        return this.z.and(me.alphamode.mcbig.core.BigConstants.Ints.THIRTY_ONE).intValue();
    }

    public BlockPos getBlockAt(int p_151385_, int p_151386_, int p_151387_) {
        return new BlockPos(this.getBlockX(p_151385_), p_151386_, this.getBlockZ(p_151387_));
    }

    public me.alphamode.mcbig.math.BigInteger getBlockX(int p_151383_) {
        return SectionPos.sectionToBlockCoord(this.x, p_151383_);
    }

    public me.alphamode.mcbig.math.BigInteger getBlockZ(int p_151392_) {
        return SectionPos.sectionToBlockCoord(this.z, p_151392_);
    }

    public BlockPos getMiddleBlockPosition(int p_151395_) {
        return new BlockPos(this.getMiddleBlockX(), p_151395_, this.getMiddleBlockZ());
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.z + "]";
    }

    public BlockPos getWorldPosition() {
        return new BlockPos(this.getMinBlockX(), 0, this.getMinBlockZ());
    }

    public int getChessboardDistance(ChunkPos p_45595_) {
        return this.getChessboardDistance(p_45595_.x, p_45595_.z);
    }

    public int getChessboardDistance(me.alphamode.mcbig.math.BigInteger p_347518_, me.alphamode.mcbig.math.BigInteger p_347577_) {
        return this.x.subtract(p_347518_).abs().max(this.z.subtract(p_347577_).abs()).intValue();
    }

    public int distanceSquared(ChunkPos p_296302_) {
        return this.distanceSquared(p_296302_.x, p_296302_.z);
    }

    private int distanceSquared(me.alphamode.mcbig.math.BigInteger p_295272_, me.alphamode.mcbig.math.BigInteger p_296154_) {
        int i = p_295272_.subtract(this.x).intValue();
        int j = p_296154_.subtract(this.z).intValue();
        return i * i + j * j;
    }

    public static Stream<ChunkPos> rangeClosed(ChunkPos p_45597_, int p_45598_) {
        return rangeClosed(new ChunkPos(p_45597_.x.subtract(p_45598_), p_45597_.z.subtract(p_45598_)), new ChunkPos(p_45597_.x.add(p_45598_), p_45597_.z.add(p_45598_)));
    }

    public static Stream<ChunkPos> rangeClosed(final ChunkPos p_45600_, final ChunkPos p_45601_) {
        int i = Math.abs(p_45600_.x.subtract(p_45601_.x).intValue()) + 1;;
        int j = Math.abs(p_45600_.z.subtract(p_45601_.z).intValue()) + 1;
        final me.alphamode.mcbig.math.BigInteger k = p_45600_.x.compareTo(p_45601_.x) < 0 ? me.alphamode.mcbig.math.BigInteger.ONE : me.alphamode.mcbig.math.BigInteger.NEGATIVE_ONE;
        final me.alphamode.mcbig.math.BigInteger l = p_45600_.z.compareTo(p_45601_.z) < 0 ? me.alphamode.mcbig.math.BigInteger.ONE : me.alphamode.mcbig.math.BigInteger.NEGATIVE_ONE;
        return StreamSupport.stream(new AbstractSpliterator<ChunkPos>((long)(i * j), 64) {
            @Nullable
            private ChunkPos pos;

            @Override
            public boolean tryAdvance(Consumer<? super ChunkPos> p_372881_) {
                if (this.pos == null) {
                    this.pos = p_45600_;
                } else {
                    me.alphamode.mcbig.math.BigInteger i1 = this.pos.x;
                    me.alphamode.mcbig.math.BigInteger j1 = this.pos.z;
                    if (i1.equals(p_45601_.x)) {
                        if (j1.equals(p_45601_.z)) {
                            return false;
                        }

                        this.pos = new ChunkPos(p_45600_.x, j1.add(l));
                    } else {
                        this.pos = new ChunkPos(i1.add(k), j1);
                    }
                }

                p_372881_.accept(this.pos);
                return true;
            }
        }, false);
    }

    @Override
    public int compareTo(ChunkPos o) {
        int compareX = this.x.compareTo(o.x);
        if (compareX != 0) {
            return compareX;
        }

        // If x values are equal, compare the z values
        return this.z.compareTo(o.z);
    }

    public me.alphamode.mcbig.math.BigInteger x() {
        return this.x;
    }

    public me.alphamode.mcbig.math.BigInteger z() {
        return this.z;
    }
}
