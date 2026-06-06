package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

@Immutable
public class BlockPos extends me.alphamode.mcbig.core.Vec3l {
    public static final Codec<BlockPos> CODEC = me.alphamode.mcbig.core.ModdedCodec.STRING_STREAM
        .<BlockPos>comapFlatMap(
            p_337445_ -> Util.fixedSize(p_337445_, 3).map(p_175270_ -> new BlockPos(me.alphamode.mcbig.math.BigInteger.val(p_175270_[0]), me.alphamode.mcbig.math.BigInteger.val(p_175270_[1]), me.alphamode.mcbig.math.BigInteger.val(p_175270_[2]))),
            p_121924_ -> Stream.of(p_121924_.getBigX().toString(), p_121924_.getBigY().toString(), p_121924_.getBigZ().toString())
        )
        .stable();
    public static final StreamCodec<ByteBuf, BlockPos> STREAM_CODEC = new StreamCodec<ByteBuf, BlockPos>() {
        public BlockPos decode(ByteBuf p_320431_) {
            return FriendlyByteBuf.readBlockPos(p_320431_);
        }

        public void encode(ByteBuf p_320258_, BlockPos p_320532_) {
            FriendlyByteBuf.writeBlockPos(p_320258_, p_320532_);
        }
    };
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);
    public static final int PACKED_HORIZONTAL_LENGTH = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
    public static final int PACKED_Y_LENGTH = 64 - 2 * PACKED_HORIZONTAL_LENGTH;
    private static final long PACKED_X_MASK = (1L << PACKED_HORIZONTAL_LENGTH) - 1L;
    private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
    private static final long PACKED_Z_MASK = (1L << PACKED_HORIZONTAL_LENGTH) - 1L;
    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET = PACKED_Y_LENGTH;
    private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_HORIZONTAL_LENGTH;
    public static final int MAX_HORIZONTAL_COORDINATE = (1 << PACKED_HORIZONTAL_LENGTH) / 2 - 1;

    public BlockPos(long p_121869_, long p_121870_, long p_121871_) {
        super(me.alphamode.mcbig.math.BigInteger.val(p_121869_), me.alphamode.mcbig.math.BigInteger.val(p_121870_), me.alphamode.mcbig.math.BigInteger.val(p_121871_));
    }

    public BlockPos(byte[] p_121869_, byte[] p_121870_, byte[] p_121871_) {
        super(new me.alphamode.mcbig.math.BigInteger(p_121869_), new me.alphamode.mcbig.math.BigInteger(p_121870_), new me.alphamode.mcbig.math.BigInteger(p_121871_));
    }

    public BlockPos(me.alphamode.mcbig.math.BigInteger p_121869_, me.alphamode.mcbig.math.BigInteger p_121870_, me.alphamode.mcbig.math.BigInteger p_121871_) {
        super(p_121869_, p_121870_, p_121871_);
    }

    public BlockPos(me.alphamode.mcbig.math.BigInteger p_121869_, long p_121870_, me.alphamode.mcbig.math.BigInteger p_121871_) {
        this(p_121869_, me.alphamode.mcbig.math.BigInteger.val(p_121870_), p_121871_);
    }

    public BlockPos(Vec3i p_121877_) {
        this(p_121877_.getX(), p_121877_.getY(), p_121877_.getZ());
    }

    public BlockPos(me.alphamode.mcbig.core.Vec3l p_121877_) {
        this(p_121877_.getBigX(), p_121877_.getBigY(), p_121877_.getBigZ());
    }

    public static BlockPos containing(double p_275310_, double p_275414_, double p_275737_) {
        return new BlockPos(Mth.bigFloor(p_275310_), Mth.bigFloor(p_275414_), Mth.bigFloor(p_275737_));
    }

    public static BlockPos containing(me.alphamode.mcbig.math.BigDecimal p_275310_, double p_275414_, me.alphamode.mcbig.math.BigDecimal p_275737_) {
        return new BlockPos(Mth.bigFloor(p_275310_), Mth.bigFloor(p_275414_), Mth.bigFloor(p_275737_));
    }

    public static BlockPos containing(me.alphamode.mcbig.math.BigDecimal p_275310_, me.alphamode.mcbig.math.BigDecimal p_275414_, me.alphamode.mcbig.math.BigDecimal p_275737_) {
        return new BlockPos(Mth.bigFloor(p_275310_), Mth.bigFloor(p_275414_), Mth.bigFloor(p_275737_));
    }

    public static BlockPos containing(Position p_275443_) {
        return containing(p_275443_.x(), p_275443_.y(), p_275443_.z());
    }

    public static BlockPos containing(me.alphamode.mcbig.core.BigPosition p_275443_) {
        return containing(p_275443_.bigX(), p_275443_.y(), p_275443_.bigZ());
    }

    public static BlockPos min(BlockPos p_333745_, BlockPos p_333943_) {
        return new BlockPos(
            Math.min(p_333745_.getX(), p_333943_.getX()), Math.min(p_333745_.getY(), p_333943_.getY()), Math.min(p_333745_.getZ(), p_333943_.getZ())
        );
    }

    public static BlockPos max(BlockPos p_334008_, BlockPos p_333743_) {
        return new BlockPos(
            Math.max(p_334008_.getX(), p_333743_.getX()), Math.max(p_334008_.getY(), p_333743_.getY()), Math.max(p_334008_.getZ(), p_333743_.getZ())
        );
    }

    public long asLong() {
        return asLong(this.getX(), this.getY(), this.getZ());
    }

    public static long asLong(int p_121883_, int p_121884_, int p_121885_) {
        long i = 0L;
        i |= ((long)p_121883_ & PACKED_X_MASK) << X_OFFSET;
        i |= ((long)p_121884_ & PACKED_Y_MASK) << 0;
        return i | ((long)p_121885_ & PACKED_Z_MASK) << Z_OFFSET;
    }

    public static long getFlatIndex(long p_122028_) {
        return p_122028_ & -16L;
    }

    public BlockPos offset(int p_121973_, int p_121974_, int p_121975_) {
        return p_121973_ == 0 && p_121974_ == 0 && p_121975_ == 0
            ? this
            : new BlockPos(this.getBigX().add(p_121973_), this.getBigY().add(p_121974_), this.getBigZ().add(p_121975_));
    }

    public BlockPos offset(me.alphamode.mcbig.math.BigInteger p_121973_, me.alphamode.mcbig.math.BigInteger p_121974_, me.alphamode.mcbig.math.BigInteger p_121975_) {
        return p_121973_.equals(me.alphamode.mcbig.math.BigInteger.ZERO) && p_121974_.equals(me.alphamode.mcbig.math.BigInteger.ZERO) && p_121975_.equals(me.alphamode.mcbig.math.BigInteger.ZERO)
                ? this
                : new BlockPos(this.getBigX().add(p_121973_), this.getBigY().add(p_121974_), this.getBigZ().add(p_121975_));
    }

    public Vec3 getCenter() {
        return Vec3.atCenterOf(this);
    }

    public Vec3 getBottomCenter() {
        return Vec3.atBottomCenterOf(this);
    }

    public BlockPos offset(Vec3i p_121956_) {
        return this.offset(p_121956_.getX(), p_121956_.getY(), p_121956_.getZ());
    }

    public BlockPos subtract(me.alphamode.mcbig.core.Vec3l p_121997_) {
        return this.offset(p_121997_.getBigX().negate(), p_121997_.getBigY().negate(), p_121997_.getBigZ().negate());
    }

    public BlockPos offset(me.alphamode.mcbig.core.Vec3l p_121956_) {
        return this.offset(p_121956_.getBigX(), p_121956_.getBigY(), p_121956_.getBigZ());
    }

    public BlockPos subtract(Vec3i p_121997_) {
        return this.offset(-p_121997_.getX(), -p_121997_.getY(), -p_121997_.getZ());
    }

    public BlockPos multiply(int p_175263_) {
        if (p_175263_ == 1) {
            return this;
        } else {
            return p_175263_ == 0 ? ZERO : new BlockPos(this.getBigX().multiply(p_175263_), this.getBigY().multiply(p_175263_), this.getBigZ().multiply(p_175263_));
        }
    }

    public BlockPos above() {
        return this.relative(Direction.UP);
    }

    public BlockPos above(int p_121972_) {
        return this.relative(Direction.UP, p_121972_);
    }

    public BlockPos below() {
        return this.relative(Direction.DOWN);
    }

    public BlockPos below(int p_122000_) {
        return this.relative(Direction.DOWN, p_122000_);
    }

    public BlockPos north() {
        return this.relative(Direction.NORTH);
    }

    public BlockPos north(int p_122014_) {
        return this.relative(Direction.NORTH, p_122014_);
    }

    public BlockPos south() {
        return this.relative(Direction.SOUTH);
    }

    public BlockPos south(int p_122021_) {
        return this.relative(Direction.SOUTH, p_122021_);
    }

    public BlockPos west() {
        return this.relative(Direction.WEST);
    }

    public BlockPos west(int p_122026_) {
        return this.relative(Direction.WEST, p_122026_);
    }

    public BlockPos east() {
        return this.relative(Direction.EAST);
    }

    public BlockPos east(int p_122031_) {
        return this.relative(Direction.EAST, p_122031_);
    }

    public BlockPos relative(Direction p_121946_) {
        return new BlockPos(this.getBigX().add(p_121946_.getStepX()), this.getBigY().add(p_121946_.getStepY()), this.getBigZ().add(p_121946_.getStepZ()));
    }

    public BlockPos relative(Direction p_121948_, int p_121949_) {
        return p_121949_ == 0
            ? this
            : new BlockPos(
                this.getBigX().add(p_121948_.getStepX() * p_121949_), this.getBigY().add(p_121948_.getStepY() * p_121949_), this.getBigZ().add(p_121948_.getStepZ() * p_121949_)
            );
    }

    public BlockPos relative(Direction.Axis p_121943_, int p_121944_) {
        if (p_121944_ == 0) {
            return this;
        } else {
            int i = p_121943_ == Direction.Axis.X ? p_121944_ : 0;
            int j = p_121943_ == Direction.Axis.Y ? p_121944_ : 0;
            int k = p_121943_ == Direction.Axis.Z ? p_121944_ : 0;
            return new BlockPos(this.getBigX().add(i), this.getBigY().add(j), this.getBigZ().add(k));
        }
    }

    public BlockPos rotate(Rotation p_121918_) {
        switch (p_121918_) {
            case NONE:
            default:
                return this;
            case CLOCKWISE_90:
                return new BlockPos(this.getBigZ().negate(), this.getBigY(), this.getBigX());
            case CLOCKWISE_180:
                return new BlockPos(this.getBigX().negate(), this.getBigY(), this.getBigZ().negate());
            case COUNTERCLOCKWISE_90:
                return new BlockPos(this.getBigZ(), this.getBigY(), this.getBigX().negate());
        }
    }

    public BlockPos cross(me.alphamode.mcbig.core.Vec3l p_122011_) {
        return new BlockPos(
            this.getBigY().multiply(p_122011_.getBigZ()).subtract(this.getBigZ().multiply(p_122011_.getBigY())),
            this.getBigZ().multiply(p_122011_.getBigX()).subtract(this.getBigX().multiply(p_122011_.getBigZ())),
            this.getBigX().multiply(p_122011_.getBigY()).subtract(this.getBigY().multiply(p_122011_.getBigX()))
        );
    }

    public BlockPos atY(int p_175289_) {
        return new BlockPos(this.getBigX(), me.alphamode.mcbig.math.BigInteger.val(p_175289_), this.getBigZ());
    }

    public BlockPos immutable() {
        return this;
    }

    public BlockPos.MutableBlockPos mutable() {
        return new BlockPos.MutableBlockPos(this.getBigX(), this.getBigY(), this.getBigZ());
    }

    public Vec3 clampLocationWithin(Vec3 p_350293_) {
        return new Vec3(
            Mth.clamp(p_350293_.x, (double)((float)this.getX() + 1.0E-5F), (double)this.getX() + 1.0 - 1.0E-5F),
            Mth.clamp(p_350293_.y, (double)((float)this.getY() + 1.0E-5F), (double)this.getY() + 1.0 - 1.0E-5F),
            Mth.clamp(p_350293_.z, (double)((float)this.getZ() + 1.0E-5F), (double)this.getZ() + 1.0 - 1.0E-5F)
        );
    }

    public static Iterable<BlockPos> randomInCube(RandomSource p_235651_, int p_235652_, BlockPos p_235653_, int p_235654_) {
        return randomBetweenClosed(
            p_235651_,
            p_235652_,
            p_235653_.getBigX().subtract(p_235654_),
            p_235653_.getBigY().subtract(p_235654_),
            p_235653_.getBigZ().subtract(p_235654_),
            p_235653_.getBigX().add(p_235654_),
            p_235653_.getBigY().add(p_235654_),
            p_235653_.getBigZ().add(p_235654_)
        );
    }

    @Deprecated
    public static Stream<BlockPos> squareOutSouthEast(BlockPos p_284978_) {
        return Stream.of(p_284978_, p_284978_.south(), p_284978_.east(), p_284978_.south().east());
    }

    public static Iterable<BlockPos> randomBetweenClosed(
        RandomSource p_235642_, int p_235643_, me.alphamode.mcbig.math.BigInteger p_235644_, me.alphamode.mcbig.math.BigInteger p_235645_, me.alphamode.mcbig.math.BigInteger p_235646_, me.alphamode.mcbig.math.BigInteger p_235647_, me.alphamode.mcbig.math.BigInteger p_235648_, me.alphamode.mcbig.math.BigInteger p_235649_
    ) {
        int i = p_235647_.subtract(p_235644_).add().intValue();
        int j = p_235648_.subtract(p_235645_).add().intValue();
        int k = p_235649_.subtract(p_235646_).add().intValue();
        return () -> new AbstractIterator<BlockPos>() {
                final BlockPos.MutableBlockPos nextPos = new BlockPos.MutableBlockPos();
                int counter = p_235643_;

                protected BlockPos computeNext() {
                    if (this.counter <= 0) {
                        return this.endOfData();
                    } else {
                        BlockPos blockpos = this.nextPos
                            .set(p_235644_.add(p_235642_.nextInt(i)), p_235645_.add(p_235642_.nextInt(j)), p_235646_.add(p_235642_.nextInt(k)));
                        this.counter--;
                        return blockpos;
                    }
                }
            };
    }

    public static Iterable<BlockPos> withinManhattan(BlockPos p_121926_, int p_121927_, int p_121928_, int p_121929_) {
        int i = p_121927_ + p_121928_ + p_121929_;
        me.alphamode.mcbig.math.BigInteger j = p_121926_.getBigX();
        me.alphamode.mcbig.math.BigInteger k = p_121926_.getBigY();
        me.alphamode.mcbig.math.BigInteger l = p_121926_.getBigZ();
        return () -> new AbstractIterator<BlockPos>() {
                private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
                private int currentDepth;
                private int maxX;
                private int maxY;
                private int x;
                private int y;
                private boolean zMirror;

                protected BlockPos computeNext() {
                    if (this.zMirror) {
                        this.zMirror = false;
                        this.cursor.setZ(l.subtract((this.cursor.getBigZ().subtract(l))));
                        return this.cursor;
                    } else {
                        BlockPos blockpos;
                        for (blockpos = null; blockpos == null; this.y++) {
                            if (this.y > this.maxY) {
                                this.x++;
                                if (this.x > this.maxX) {
                                    this.currentDepth++;
                                    if (this.currentDepth > i) {
                                        return this.endOfData();
                                    }

                                    this.maxX = Math.min(p_121927_, this.currentDepth);
                                    this.x = -this.maxX;
                                }

                                this.maxY = Math.min(p_121928_, this.currentDepth - Math.abs(this.x));
                                this.y = -this.maxY;
                            }

                            int i1 = this.x;
                            int j1 = this.y;
                            int k1 = this.currentDepth - Math.abs(i1) - Math.abs(j1);
                            if (k1 <= p_121929_) {
                                this.zMirror = k1 != 0;
                                blockpos = this.cursor.set(j.intValue() + i1, k.intValue() + j1, l.intValue() + k1);
                            }
                        }

                        return blockpos;
                    }
                }
            };
    }

    public static Optional<BlockPos> findClosestMatch(BlockPos p_121931_, int p_121932_, int p_121933_, Predicate<BlockPos> p_121934_) {
        for (BlockPos blockpos : withinManhattan(p_121931_, p_121932_, p_121933_, p_121932_)) {
            if (p_121934_.test(blockpos)) {
                return Optional.of(blockpos);
            }
        }

        return Optional.empty();
    }

    public static Stream<BlockPos> withinManhattanStream(BlockPos p_121986_, int p_121987_, int p_121988_, int p_121989_) {
        return StreamSupport.stream(withinManhattan(p_121986_, p_121987_, p_121988_, p_121989_).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(AABB p_361553_) {
        BlockPos blockpos = containing(p_361553_.minX, p_361553_.minY, p_361553_.minZ);
        BlockPos blockpos1 = containing(p_361553_.maxX, p_361553_.maxY, p_361553_.maxZ);
        return betweenClosed(blockpos, blockpos1);
    }

    public static Iterable<BlockPos> betweenClosed(BlockPos p_121941_, BlockPos p_121942_) {
        return betweenClosed(
            p_121941_.getBigX().min(p_121942_.getBigX()),
            p_121941_.getBigY().min(p_121942_.getBigY()),
            p_121941_.getBigZ().min(p_121942_.getBigZ()),
            p_121941_.getBigX().max(p_121942_.getBigX()),
            p_121941_.getBigY().max(p_121942_.getBigY()),
            p_121941_.getBigZ().max(p_121942_.getBigZ())
        );
    }

    public static Stream<BlockPos> betweenClosedStream(BlockPos p_121991_, BlockPos p_121992_) {
        return StreamSupport.stream(betweenClosed(p_121991_, p_121992_).spliterator(), false);
    }

    public static Stream<BlockPos> betweenClosedStream(BoundingBox p_121920_) {
        return betweenClosedStream(
            p_121920_.minX().min(p_121920_.maxX()),
            p_121920_.minY().min(p_121920_.maxY()),
            p_121920_.minZ().min(p_121920_.maxZ()),
            p_121920_.minX().max(p_121920_.maxX()),
            p_121920_.minY().max(p_121920_.maxY()),
            p_121920_.minZ().max(p_121920_.maxZ())
        );
    }

    public static Stream<BlockPos> betweenClosedStream(AABB p_121922_) {
        return betweenClosedStream(
            Mth.bigFloor(p_121922_.minX),
            Mth.bigFloor(p_121922_.minY),
            Mth.bigFloor(p_121922_.minZ),
            Mth.bigFloor(p_121922_.maxX),
            Mth.bigFloor(p_121922_.maxY),
            Mth.bigFloor(p_121922_.maxZ)
        );
    }

    public static Stream<BlockPos> betweenClosedStream(me.alphamode.mcbig.math.BigInteger p_121887_, me.alphamode.mcbig.math.BigInteger p_121888_, me.alphamode.mcbig.math.BigInteger p_121889_, me.alphamode.mcbig.math.BigInteger p_121890_, me.alphamode.mcbig.math.BigInteger p_121891_, me.alphamode.mcbig.math.BigInteger p_121892_) {
        return StreamSupport.stream(betweenClosed(p_121887_, p_121888_, p_121889_, p_121890_, p_121891_, p_121892_).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(me.alphamode.mcbig.math.BigInteger p_121977_, me.alphamode.mcbig.math.BigInteger p_121978_, me.alphamode.mcbig.math.BigInteger p_121979_, me.alphamode.mcbig.math.BigInteger p_121980_, me.alphamode.mcbig.math.BigInteger p_121981_, me.alphamode.mcbig.math.BigInteger p_121982_) {
        int i = p_121980_.subtract(p_121977_).intValue() + 1;
        int j = p_121981_.subtract(p_121978_).intValue() + 1;
        int k = p_121982_.subtract(p_121979_).intValue() + 1;
        int l = i * j * k;
        return () -> new AbstractIterator<BlockPos>() {
                private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
                private int index;

                protected BlockPos computeNext() {
                    if (this.index == l) {
                        return this.endOfData();
                    } else {
                        int i1 = this.index % i;
                        int j1 = this.index / i;
                        int k1 = j1 % j;
                        int l1 = j1 / j;
                        this.index++;
                        return this.cursor.set(p_121977_.add(i1), p_121978_.add(k1), p_121979_.add(l1));
                    }
                }
            };
    }

    public static Stream<BlockPos> betweenClosedStream(int p_121887_, int p_121888_, int p_121889_, int p_121890_, int p_121891_, int p_121892_) {
        return StreamSupport.stream(betweenClosed(p_121887_, p_121888_, p_121889_, p_121890_, p_121891_, p_121892_).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(int p_121977_, int p_121978_, int p_121979_, int p_121980_, int p_121981_, int p_121982_) {
        int i = p_121980_ - p_121977_ + 1;
        int j = p_121981_ - p_121978_ + 1;
        int k = p_121982_ - p_121979_ + 1;
        int l = i * j * k;
        return () -> new AbstractIterator<BlockPos>() {
            private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            private int index;

            protected BlockPos computeNext() {
                if (this.index == l) {
                    return this.endOfData();
                } else {
                    int i1 = this.index % i;
                    int j1 = this.index / i;
                    int k1 = j1 % j;
                    int l1 = j1 / j;
                    this.index++;
                    return this.cursor.set(p_121977_ + i1, p_121978_ + k1, p_121979_ + l1);
                }
            }
        };
    }

    public static Iterable<BlockPos.MutableBlockPos> spiralAround(BlockPos p_121936_, int p_121937_, Direction p_121938_, Direction p_121939_) {
        Validate.validState(p_121938_.getAxis() != p_121939_.getAxis(), "The two directions cannot be on the same axis");
        return () -> new AbstractIterator<BlockPos.MutableBlockPos>() {
                private final Direction[] directions = new Direction[]{p_121938_, p_121939_, p_121938_.getOpposite(), p_121939_.getOpposite()};
                private final BlockPos.MutableBlockPos cursor = p_121936_.mutable().move(p_121939_);
                private final int legs = 4 * p_121937_;
                private int leg = -1;
                private int legSize;
                private int legIndex;
                private me.alphamode.mcbig.math.BigInteger lastX = this.cursor.getBigX();
                private me.alphamode.mcbig.math.BigInteger lastY = this.cursor.getBigY();
                private me.alphamode.mcbig.math.BigInteger lastZ = this.cursor.getBigZ();

                protected BlockPos.MutableBlockPos computeNext() {
                    this.cursor.set(this.lastX, this.lastY, this.lastZ).move(this.directions[(this.leg + 4) % 4]);
                    this.lastX = this.cursor.getBigX();
                    this.lastY = this.cursor.getBigY();
                    this.lastZ = this.cursor.getBigZ();
                    if (this.legIndex >= this.legSize) {
                        if (this.leg >= this.legs) {
                            return this.endOfData();
                        }

                        this.leg++;
                        this.legIndex = 0;
                        this.legSize = this.leg / 2 + 1;
                    }

                    this.legIndex++;
                    return this.cursor;
                }
            };
    }

    public static int breadthFirstTraversal(
        BlockPos p_278078_,
        int p_277385_,
        int p_277666_,
        BiConsumer<BlockPos, Consumer<BlockPos>> p_277755_,
        Function<BlockPos, BlockPos.TraversalNodeStatus> p_382816_
    ) {
        Queue<Pair<BlockPos, Integer>> queue = new ArrayDeque<>();
        LongSet longset = new LongOpenHashSet();
        queue.add(Pair.of(p_278078_, 0));
        int i = 0;

        while (!queue.isEmpty()) {
            Pair<BlockPos, Integer> pair = queue.poll();
            BlockPos blockpos = pair.getLeft();
            int j = pair.getRight();
            long k = blockpos.asLong();
            if (longset.add(k)) {
                BlockPos.TraversalNodeStatus blockpos$traversalnodestatus = p_382816_.apply(blockpos);
                if (blockpos$traversalnodestatus != BlockPos.TraversalNodeStatus.SKIP) {
                    if (blockpos$traversalnodestatus == BlockPos.TraversalNodeStatus.STOP) {
                        break;
                    }

                    if (++i >= p_277666_) {
                        return i;
                    }

                    if (j < p_277385_) {
                        p_277755_.accept(blockpos, p_277234_ -> queue.add(Pair.of(p_277234_, j + 1)));
                    }
                }
            }
        }

        return i;
    }

    public static class MutableBlockPos extends BlockPos {
        public MutableBlockPos() {
            this(0, 0, 0);
        }

        public MutableBlockPos(int p_122130_, int p_122131_, int p_122132_) {
            super(p_122130_, p_122131_, p_122132_);
        }

        public MutableBlockPos(me.alphamode.mcbig.math.BigInteger p_122130_, me.alphamode.mcbig.math.BigInteger p_122131_, me.alphamode.mcbig.math.BigInteger p_122132_) {
            super(p_122130_, p_122131_, p_122132_);
        }

        public MutableBlockPos(me.alphamode.mcbig.math.BigInteger p_122130_, int p_122131_, me.alphamode.mcbig.math.BigInteger p_122132_) {
            super(p_122130_, p_122131_, p_122132_);
        }

        public MutableBlockPos(double p_122126_, double p_122127_, double p_122128_) {
            this(Mth.bigFloor(p_122126_), Mth.bigFloor(p_122127_), Mth.bigFloor(p_122128_));
        }

        @Override
        public BlockPos offset(int p_122163_, int p_122164_, int p_122165_) {
            return super.offset(p_122163_, p_122164_, p_122165_).immutable();
        }

        @Override
        public BlockPos multiply(int p_175305_) {
            return super.multiply(p_175305_).immutable();
        }

        @Override
        public BlockPos relative(Direction p_122152_, int p_122153_) {
            return super.relative(p_122152_, p_122153_).immutable();
        }

        @Override
        public BlockPos relative(Direction.Axis p_122145_, int p_122146_) {
            return super.relative(p_122145_, p_122146_).immutable();
        }

        @Override
        public BlockPos rotate(Rotation p_122138_) {
            return super.rotate(p_122138_).immutable();
        }

        public BlockPos.MutableBlockPos set(long p_122179_, long p_122180_, long p_122181_) {
            this.setX(me.alphamode.mcbig.math.BigInteger.val(p_122179_));
            this.setY(me.alphamode.mcbig.math.BigInteger.val(p_122180_));
            this.setZ(me.alphamode.mcbig.math.BigInteger.val(p_122181_));
            return this;
        }

        public BlockPos.MutableBlockPos set(me.alphamode.mcbig.math.BigInteger x, int y, me.alphamode.mcbig.math.BigInteger z) {
            this.setX(x);
            this.setY(y);
            this.setZ(z);
            return this;
        }

        public BlockPos.MutableBlockPos set(me.alphamode.mcbig.math.BigInteger x, me.alphamode.mcbig.math.BigInteger y, me.alphamode.mcbig.math.BigInteger z) {
            this.setX(x);
            this.setY(y);
            this.setZ(z);
            return this;
        }

        public BlockPos.MutableBlockPos set(double p_122170_, double p_122171_, double p_122172_) {
            return this.set(Mth.bigFloor(p_122170_), Mth.bigFloor(p_122171_), Mth.bigFloor(p_122172_));
        }

        public BlockPos.MutableBlockPos set(me.alphamode.mcbig.math.BigDecimal x, double y, me.alphamode.mcbig.math.BigDecimal z) {
            return this.set(Mth.bigFloor(x), Mth.bigFloor(y), Mth.bigFloor(z));
        }

        public BlockPos.MutableBlockPos set(me.alphamode.mcbig.core.Vec3l p_122191_) {
            return this.set(p_122191_.getBigX(), p_122191_.getBigY(), p_122191_.getBigZ());
        }

        public BlockPos.MutableBlockPos set(AxisCycle p_122140_, int p_122141_, int p_122142_, int p_122143_) {
            return this.set(
                p_122140_.cycle(p_122141_, p_122142_, p_122143_, Direction.Axis.X),
                p_122140_.cycle(p_122141_, p_122142_, p_122143_, Direction.Axis.Y),
                p_122140_.cycle(p_122141_, p_122142_, p_122143_, Direction.Axis.Z)
            );
        }

        public BlockPos.MutableBlockPos setWithOffset(me.alphamode.mcbig.core.Vec3l p_122160_, Direction p_122161_) {
            return this.set(p_122160_.getBigX().add(p_122161_.getStepX()), p_122160_.getBigY().add(p_122161_.getStepY()), p_122160_.getBigZ().add(p_122161_.getStepZ()));
        }

        public BlockPos.MutableBlockPos setWithOffset(me.alphamode.mcbig.core.Vec3l p_122155_, int p_122156_, int p_122157_, int p_122158_) {
            return this.set(p_122155_.getBigX().add(p_122156_), p_122155_.getBigY().add(p_122157_), p_122155_.getBigZ().add(p_122158_));
        }

        public BlockPos.MutableBlockPos setWithOffset(me.alphamode.mcbig.core.Vec3l p_175307_, me.alphamode.mcbig.core.Vec3l p_175308_) {
            return this.set(p_175307_.getBigX().add(p_175308_.getBigX()), p_175307_.getBigY().add(p_175308_.getBigY()), p_175307_.getBigZ().add(p_175308_.getBigZ()));
        }

        public BlockPos.MutableBlockPos move(Direction p_122174_) {
            return this.move(p_122174_, 1);
        }

        public BlockPos.MutableBlockPos move(Direction p_122176_, int p_122177_) {
            return this.set(
                this.getBigX().add(p_122176_.getStepX() * p_122177_), this.getBigY().add(p_122176_.getStepY() * p_122177_), this.getBigZ().add(p_122176_.getStepZ() * p_122177_)
            );
        }

        public BlockPos.MutableBlockPos move(int p_122185_, int p_122186_, int p_122187_) {
            return this.set(this.getBigX().add(p_122185_), this.getBigY().add(p_122186_), this.getBigZ().add(p_122187_));
        }

        public BlockPos.MutableBlockPos move(me.alphamode.mcbig.core.Vec3l p_122194_) {
            return this.set(this.getBigX().add(p_122194_.getBigX()), this.getBigY().add(p_122194_.getBigY()), this.getBigZ().add(p_122194_.getBigZ()));
        }

        public BlockPos.MutableBlockPos clamp(Direction.Axis p_122148_, int p_122149_, int p_122150_) {
            switch (p_122148_) {
                case X:
                    return this.set(Mth.clamp(this.getBigX(), me.alphamode.mcbig.math.BigInteger.val(p_122149_), me.alphamode.mcbig.math.BigInteger.val(p_122150_)), this.getBigY(), this.getBigZ());
                case Y:
                    return this.set(this.getBigX(), Mth.clamp(this.getBigY(), me.alphamode.mcbig.math.BigInteger.val(p_122149_), me.alphamode.mcbig.math.BigInteger.val(p_122150_)), this.getBigZ());
                case Z:
                    return this.set(this.getBigX(), this.getBigY(), Mth.clamp(this.getBigZ(), me.alphamode.mcbig.math.BigInteger.val(p_122149_), me.alphamode.mcbig.math.BigInteger.val(p_122150_)));
                default:
                    throw new IllegalStateException("Unable to clamp axis " + p_122148_);
            }
        }

        public BlockPos.MutableBlockPos setX(me.alphamode.mcbig.math.BigInteger p_175341_) {
            super.setX(p_175341_);
            return this;
        }

        public BlockPos.MutableBlockPos setX(long p_175341_) {
            super.setX(me.alphamode.mcbig.math.BigInteger.val(p_175341_));
            return this;
        }

        public BlockPos.MutableBlockPos setY(me.alphamode.mcbig.math.BigInteger p_175343_) {
            super.setY(p_175343_);
            return this;
        }

        public BlockPos.MutableBlockPos setY(long p_175343_) {
            super.setY(me.alphamode.mcbig.math.BigInteger.val(p_175343_));
            return this;
        }

        public BlockPos.MutableBlockPos setZ(me.alphamode.mcbig.math.BigInteger p_175345_) {
            super.setZ(p_175345_);
            return this;
        }

        public BlockPos.MutableBlockPos setZ(long p_175345_) {
            super.setZ(me.alphamode.mcbig.math.BigInteger.val(p_175345_));
            return this;
        }

        @Override
        public BlockPos immutable() {
            return new BlockPos(this);
        }
    }

    public static enum TraversalNodeStatus {
        ACCEPT,
        SKIP,
        STOP;
    }
}
