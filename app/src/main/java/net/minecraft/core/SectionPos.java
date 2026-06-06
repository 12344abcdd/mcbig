package net.minecraft.core;

import it.unimi.dsi.fastutil.longs.LongConsumer;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import me.alphamode.mcbig.core.Vec3l;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.entity.EntityAccess;

public class SectionPos extends me.alphamode.mcbig.core.Vec3l {
    public static final int SECTION_BITS = 4;
    public static final int SECTION_SIZE = 16;
    public static final int SECTION_MASK = 15;
    public static final int SECTION_HALF_SIZE = 8;
    public static final int SECTION_MAX_INDEX = 15;
    private static final int PACKED_X_LENGTH = 22;
    private static final int PACKED_Y_LENGTH = 20;
    private static final int PACKED_Z_LENGTH = 22;
    private static final long PACKED_X_MASK = 4194303L;
    private static final long PACKED_Y_MASK = 1048575L;
    private static final long PACKED_Z_MASK = 4194303L;
    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET = 20;
    private static final int X_OFFSET = 42;
    private static final int RELATIVE_X_SHIFT = 8;
    private static final int RELATIVE_Y_SHIFT = 0;
    private static final int RELATIVE_Z_SHIFT = 4;

    SectionPos(long p_123162_, long p_123163_, long p_123164_) {
        super(p_123162_, p_123163_, p_123164_);
    }

    SectionPos(me.alphamode.mcbig.math.BigInteger p_123162_, me.alphamode.mcbig.math.BigInteger p_123163_, me.alphamode.mcbig.math.BigInteger p_123164_) {
        super(p_123162_, p_123163_, p_123164_);
    }

    public static SectionPos of(me.alphamode.mcbig.math.BigInteger p_123174_, me.alphamode.mcbig.math.BigInteger p_123175_, me.alphamode.mcbig.math.BigInteger p_123176_) {
        return new SectionPos(p_123174_, p_123175_, p_123176_);
    }

    public static SectionPos of(me.alphamode.mcbig.math.BigInteger p_123174_, long p_123175_, me.alphamode.mcbig.math.BigInteger p_123176_) {
        return of(p_123174_, me.alphamode.mcbig.math.BigInteger.val(p_123175_), p_123176_);
    }

    public static SectionPos of(long p_123174_, long p_123175_, long p_123176_) {
        return new SectionPos(p_123174_, p_123175_, p_123176_);
    }

    public static SectionPos of(BlockPos p_123200_) {
        return new SectionPos(blockToSectionCoord(p_123200_.getBigX()), blockToSectionCoord(p_123200_.getBigY()), blockToSectionCoord(p_123200_.getBigZ()));
    }

    public static SectionPos of(ChunkPos p_123197_, int p_123198_) {
        return new SectionPos(p_123197_.x, me.alphamode.mcbig.math.BigInteger.val(p_123198_), p_123197_.z);
    }

    public static SectionPos of(EntityAccess p_235862_) {
        return of(p_235862_.blockPosition());
    }

    public static SectionPos of(Position p_235864_) {
        return new SectionPos(blockToSectionCoord(p_235864_.x()), blockToSectionCoord(p_235864_.y()), blockToSectionCoord(p_235864_.z()));
    }

    public static SectionPos of(me.alphamode.mcbig.core.BigPosition p_235864_) {
        return new SectionPos(blockToSectionCoord(p_235864_.bigX()), me.alphamode.mcbig.math.BigInteger.val(blockToSectionCoord(p_235864_.y())), blockToSectionCoord(p_235864_.bigZ()));
    }

    public static SectionPos bottomOf(ChunkAccess p_175563_) {
        return of(p_175563_.getPos(), p_175563_.getMinSectionY());
    }

    @Override
    public SectionPos relative(Direction p_175592_) {
        return new SectionPos(this.getBigX().add(p_175592_.getStepX()), this.getBigY().add(p_175592_.getStepY()), this.getBigZ().add(p_175592_.getStepZ()));
    }

    public static me.alphamode.mcbig.math.BigInteger posToSectionCoord(double p_175553_) {
        return blockToSectionCoord(Mth.bigFloor(p_175553_));
    }

    public static me.alphamode.mcbig.math.BigInteger posToSectionCoord(me.alphamode.mcbig.math.BigDecimal p_175553_) {
        return blockToSectionCoord(Mth.bigFloor(p_175553_));
    }

    public static me.alphamode.mcbig.math.BigInteger blockToSectionCoord(me.alphamode.mcbig.math.BigInteger p_123172_) {
        return p_123172_.shiftRight(SECTION_BITS);
    }

    public static int blockToSectionCoord(double p_235866_) {
        return Mth.floor(p_235866_) >> 4;
    }

    public static me.alphamode.mcbig.math.BigInteger blockToSectionCoord(me.alphamode.mcbig.math.BigDecimal p_235866_) {
        return p_235866_.floor().toBigInteger().shiftRight(SECTION_BITS);
    }

    public static int sectionRelative(int p_123208_) {
        return p_123208_ & 15;
    }

    public static int sectionRelative(me.alphamode.mcbig.math.BigInteger p_123208_) {
        return p_123208_.and(me.alphamode.mcbig.core.BigConstants.Ints.FIFTEEN).intValue();
    }

    public static short sectionRelativePos(BlockPos p_123219_) {
        int i = sectionRelative(p_123219_.getBigX());
        int j = sectionRelative(p_123219_.getBigY());
        int k = sectionRelative(p_123219_.getBigZ());
        return (short)(i << 8 | k << 4 | j << 0);
    }

    public static int sectionRelativeX(short p_123205_) {
        return p_123205_ >>> 8 & 15;
    }

    public static int sectionRelativeY(short p_123221_) {
        return p_123221_ >>> 0 & 15;
    }

    public static int sectionRelativeZ(short p_123228_) {
        return p_123228_ >>> 4 & 15;
    }

    public me.alphamode.mcbig.math.BigInteger relativeToBlockX(short p_123233_) {
        return this.minBlockX().add(sectionRelativeX(p_123233_));
    }

    public me.alphamode.mcbig.math.BigInteger relativeToBlockY(short p_123238_) {
        return this.minBlockY().add(sectionRelativeY(p_123238_));
    }

    public me.alphamode.mcbig.math.BigInteger relativeToBlockZ(short p_123243_) {
        return this.minBlockZ().add(sectionRelativeZ(p_123243_));
    }

    public BlockPos relativeToBlockPos(short p_123246_) {
        return new BlockPos(this.relativeToBlockX(p_123246_), this.relativeToBlockY(p_123246_), this.relativeToBlockZ(p_123246_));
    }

    public static int sectionToBlockCoord(int p_123224_) {
        return p_123224_ << 4;
    }

    public static int sectionToBlockCoord(int p_175555_, int p_175556_) {
        return sectionToBlockCoord(p_175555_) + p_175556_;
    }

    public static me.alphamode.mcbig.math.BigInteger sectionToBlockCoord(me.alphamode.mcbig.math.BigInteger p_123224_) {
        return p_123224_.shiftLeft(4);
    }

    public static me.alphamode.mcbig.math.BigInteger sectionToBlockCoord(me.alphamode.mcbig.math.BigInteger p_175555_, int p_175556_) {
        return sectionToBlockCoord(p_175555_).add(p_175556_);
    }

    public me.alphamode.mcbig.math.BigInteger x() {
        return this.getBigX();
    }

    public me.alphamode.mcbig.math.BigInteger y() {
        return this.getBigY();
    }

    public me.alphamode.mcbig.math.BigInteger z() {
        return this.getBigZ();
    }

    public me.alphamode.mcbig.math.BigInteger minBlockX() {
        return sectionToBlockCoord(this.x());
    }

    public me.alphamode.mcbig.math.BigInteger minBlockY() {
        return sectionToBlockCoord(this.y());
    }

    public me.alphamode.mcbig.math.BigInteger minBlockZ() {
        return sectionToBlockCoord(this.z());
    }

    public me.alphamode.mcbig.math.BigInteger maxBlockX() {
        return sectionToBlockCoord(this.x(), 15);
    }

    public me.alphamode.mcbig.math.BigInteger maxBlockY() {
        return sectionToBlockCoord(this.y(), 15);
    }

    public me.alphamode.mcbig.math.BigInteger maxBlockZ() {
        return sectionToBlockCoord(this.z(), 15);
    }

    public static SectionPos getZeroNode(int p_285381_, int p_285068_) {
        return of(p_285381_, 0, p_285068_);
    }

    public static SectionPos getZeroNode(me.alphamode.mcbig.math.BigInteger p_285381_, me.alphamode.mcbig.math.BigInteger p_285068_) {
        return of(p_285381_, 0, p_285068_);
    }

    public static long getZeroNode(long p_123241_) {
        return p_123241_ & -1048576L;
    }

    public SectionPos zeroNode() {
        return new SectionPos(x(), me.alphamode.mcbig.math.BigInteger.ZERO, z());
    }

    public BlockPos origin() {
        return new BlockPos(sectionToBlockCoord(this.x()), sectionToBlockCoord(this.y()), sectionToBlockCoord(this.z()));
    }

    public BlockPos center() {
        int i = 8;
        return this.origin().offset(8, 8, 8);
    }

    public ChunkPos chunk() {
        return new ChunkPos(this.x(), this.z());
    }

    public static long asLong(int p_123210_, int p_123211_, int p_123212_) {
        long i = 0L;
        i |= ((long)p_123210_ & 4194303L) << 42;
        i |= ((long)p_123211_ & 1048575L) << 0;
        return i | ((long)p_123212_ & 4194303L) << 20;
    }

    public long asLong() {
        return asLong(this.x().intValue(), this.y().intValue(), this.z().intValue());
    }

    public SectionPos offset(Direction direction) {
        return this.offset(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    public SectionPos offset(int p_175571_, int p_175572_, int p_175573_) {
        return p_175571_ == 0 && p_175572_ == 0 && p_175573_ == 0 ? this : new SectionPos(this.x().add(p_175571_), this.y().add(p_175572_), this.z().add(p_175573_));
    }

    public SectionPos offset(long num) {
        return of(getBigX(), getBigY().add(num), getBigZ().add(num));
    }

    public Stream<BlockPos> blocksInside() {
        return BlockPos.betweenClosedStream(this.minBlockX(), this.minBlockY(), this.minBlockZ(), this.maxBlockX(), this.maxBlockY(), this.maxBlockZ());
    }

    public static Stream<SectionPos> cube(SectionPos p_123202_, int p_123203_) {
        me.alphamode.mcbig.math.BigInteger i = p_123202_.x();
        me.alphamode.mcbig.math.BigInteger j = p_123202_.y();
        me.alphamode.mcbig.math.BigInteger k = p_123202_.z();
        return betweenClosedStream(i.subtract(p_123203_), j.subtract(p_123203_), k.subtract(p_123203_), i.add(p_123203_), j.add(p_123203_), k.add(p_123203_));
    }

    public static Stream<SectionPos> aroundChunk(ChunkPos p_175558_, int p_175559_, int p_175560_, int p_175561_) {
        me.alphamode.mcbig.math.BigInteger i = p_175558_.x;
        me.alphamode.mcbig.math.BigInteger j = p_175558_.z;
        return betweenClosedStream(i.subtract(p_175559_), me.alphamode.mcbig.math.BigInteger.val(p_175560_), j.subtract(p_175559_), i.add(p_175559_), me.alphamode.mcbig.math.BigInteger.val(p_175561_).subtract(), j.add(p_175559_));
    }

    public static Stream<SectionPos> betweenClosedStream(
        final me.alphamode.mcbig.math.BigInteger p_123178_, final me.alphamode.mcbig.math.BigInteger p_123179_, final me.alphamode.mcbig.math.BigInteger p_123180_, final me.alphamode.mcbig.math.BigInteger p_123181_, final me.alphamode.mcbig.math.BigInteger p_123182_, final me.alphamode.mcbig.math.BigInteger p_123183_
    ) {
        return StreamSupport.stream(
            new AbstractSpliterator<SectionPos>((long)((p_123181_.subtract(p_123178_).add()).multiply(p_123182_.subtract(p_123179_).add()).multiply(p_123183_.subtract(p_123180_).add()).longValue()), 64) {
                final Cursor3D cursor = new Cursor3D(p_123178_, p_123179_, p_123180_, p_123181_, p_123182_, p_123183_);

                @Override
                public boolean tryAdvance(Consumer<? super SectionPos> p_123271_) {
                    if (this.cursor.advance()) {
                        p_123271_.accept(new SectionPos(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()));
                        return true;
                    } else {
                        return false;
                    }
                }
            }, false
        );
    }

    public static void aroundAndAtBlockPos(BlockPos p_194643_, java.util.function.Consumer<SectionPos> p_194644_) {
        aroundAndAtBlockPos(p_194643_.getBigX(), p_194643_.getBigY(), p_194643_.getBigZ(), p_194644_);
    }

    public static void aroundAndAtBlockPos(me.alphamode.mcbig.math.BigInteger p_194635_, me.alphamode.mcbig.math.BigInteger p_194636_, me.alphamode.mcbig.math.BigInteger p_194637_, java.util.function.Consumer<SectionPos> p_194638_) {
        me.alphamode.mcbig.math.BigInteger i = blockToSectionCoord(p_194635_.subtract());
        me.alphamode.mcbig.math.BigInteger j = blockToSectionCoord(p_194635_.add());
        me.alphamode.mcbig.math.BigInteger k = blockToSectionCoord(p_194636_.subtract());
        me.alphamode.mcbig.math.BigInteger l = blockToSectionCoord(p_194636_.add());
        me.alphamode.mcbig.math.BigInteger i1 = blockToSectionCoord(p_194637_.subtract());
        me.alphamode.mcbig.math.BigInteger j1 = blockToSectionCoord(p_194637_.add());
        if (i.equals(j) && k.equals(l) && i1.equals(j1)) {
            p_194638_.accept(of(i, k, i1));
        } else {
            for(me.alphamode.mcbig.math.BigInteger k1 = i; k1.compareTo(j) <= 0; k1 = k1.add()) {
                for(me.alphamode.mcbig.math.BigInteger l1 = k; l1.compareTo(l) <= 0; l1 = l1.add()) {
                    for(me.alphamode.mcbig.math.BigInteger i2 = i1; i2.compareTo(j1) <= 0; i2 = i2.add()) {
                        p_194638_.accept(of(k1, l1, i2));
                    }
                }
            }
        }
    }
}
