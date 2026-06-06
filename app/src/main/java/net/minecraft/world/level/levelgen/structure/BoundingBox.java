package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class BoundingBox {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BoundingBox INFINITE = new BoundingBox(me.alphamode.mcbig.math.BigInteger.val(java.lang.Long.MIN_VALUE), me.alphamode.mcbig.math.BigInteger.val(java.lang.Long.MIN_VALUE), me.alphamode.mcbig.math.BigInteger.val(java.lang.Long.MIN_VALUE), me.alphamode.mcbig.math.BigInteger.val(java.lang.Long.MAX_VALUE), me.alphamode.mcbig.math.BigInteger.val(java.lang.Long.MAX_VALUE), me.alphamode.mcbig.math.BigInteger.val(java.lang.Long.MAX_VALUE));
    public static final Codec<BoundingBox> CODEC = me.alphamode.mcbig.core.ModdedCodec.STRING_STREAM
        .<BoundingBox>comapFlatMap(
            p_338100_ -> Util.fixedSize(p_338100_, 6)
                    .map(p_162385_ -> new BoundingBox(me.alphamode.mcbig.math.BigInteger.val(p_162385_[0]), me.alphamode.mcbig.math.BigInteger.val(p_162385_[1]), me.alphamode.mcbig.math.BigInteger.val(p_162385_[2]), me.alphamode.mcbig.math.BigInteger.val(p_162385_[3]), me.alphamode.mcbig.math.BigInteger.val(p_162385_[4]), me.alphamode.mcbig.math.BigInteger.val(p_162385_[5]))),
            p_162391_ -> Stream.of(p_162391_.minX.toString(), p_162391_.minY.toString(), p_162391_.minZ.toString(), p_162391_.maxX.toString(), p_162391_.maxY.toString(), p_162391_.maxZ.toString())
        )
        .stable();
    private me.alphamode.mcbig.math.BigInteger minX;
    private me.alphamode.mcbig.math.BigInteger minY;
    private me.alphamode.mcbig.math.BigInteger minZ;
    private me.alphamode.mcbig.math.BigInteger maxX;
    private me.alphamode.mcbig.math.BigInteger maxY;
    private me.alphamode.mcbig.math.BigInteger maxZ;

    public BoundingBox(BlockPos p_162364_) {
        this(p_162364_.getBigX(), p_162364_.getBigY(), p_162364_.getBigZ(), p_162364_.getBigX(), p_162364_.getBigY(), p_162364_.getBigZ());
    }

    public BoundingBox(int p_71001_, int p_71002_, int p_71003_, int p_71004_, int p_71005_, int p_71006_) {
        this(me.alphamode.mcbig.math.BigInteger.val(p_71001_), me.alphamode.mcbig.math.BigInteger.val(p_71002_), me.alphamode.mcbig.math.BigInteger.val(p_71003_), me.alphamode.mcbig.math.BigInteger.val(p_71004_), me.alphamode.mcbig.math.BigInteger.val(p_71005_), me.alphamode.mcbig.math.BigInteger.val(p_71006_));
    }

    public BoundingBox(me.alphamode.mcbig.math.BigInteger p_71001_, int p_71002_, me.alphamode.mcbig.math.BigInteger p_71003_, me.alphamode.mcbig.math.BigInteger p_71004_, int p_71005_, me.alphamode.mcbig.math.BigInteger p_71006_) {
        this(p_71001_, me.alphamode.mcbig.math.BigInteger.val(p_71002_), p_71003_, p_71004_, me.alphamode.mcbig.math.BigInteger.val(p_71005_), p_71006_);
    }

    public BoundingBox(me.alphamode.mcbig.math.BigInteger p_71001_, me.alphamode.mcbig.math.BigInteger p_71002_, me.alphamode.mcbig.math.BigInteger p_71003_, me.alphamode.mcbig.math.BigInteger p_71004_, me.alphamode.mcbig.math.BigInteger p_71005_, me.alphamode.mcbig.math.BigInteger p_71006_) {
        this.minX = p_71001_;
        this.minY = p_71002_;
        this.minZ = p_71003_;
        this.maxX = p_71004_;
        this.maxY = p_71005_;
        this.maxZ = p_71006_;
        if (p_71004_.compareTo(p_71001_) < 0 || p_71005_.compareTo(p_71002_) < 0 || p_71006_.compareTo(p_71003_) < 0) {
            Util.logAndPauseIfInIde("Invalid bounding box data, inverted bounds for: " + this);
            this.minX = p_71001_.min(p_71004_);
            this.minY = p_71002_.min(p_71005_);
            this.minZ = p_71003_.min(p_71006_);
            this.maxX = p_71001_.max(p_71004_);
            this.maxY = p_71002_.max(p_71005_);
            this.maxZ = p_71003_.max(p_71006_);
        }
    }

    public static BoundingBox fromCorners(me.alphamode.mcbig.core.Vec3l p_162376_, me.alphamode.mcbig.core.Vec3l p_162377_) {
        return new BoundingBox(
            p_162376_.getBigX().min(p_162377_.getBigX()),
            p_162376_.getBigY().min(p_162377_.getBigY()),
            p_162376_.getBigZ().min(p_162377_.getBigZ()),
            p_162376_.getBigX().max(p_162377_.getBigX()),
            p_162376_.getBigY().max(p_162377_.getBigY()),
            p_162376_.getBigZ().max(p_162377_.getBigZ())
        );
    }

    public static BoundingBox infinite() {
        return INFINITE;//new BoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static BoundingBox orientBox(
            me.alphamode.mcbig.math.BigInteger p_71032_, me.alphamode.mcbig.math.BigInteger p_71033_, me.alphamode.mcbig.math.BigInteger p_71034_, int p_71035_, int p_71036_, int p_71037_, int p_71038_, int p_71039_, int p_71040_, Direction p_71041_
    ) {
        switch (p_71041_) {
            case SOUTH:
            default:
                return new BoundingBox(
                    p_71032_.add(p_71035_),
                    p_71033_.add(p_71036_),
                    p_71034_.add(p_71037_),
                    p_71032_.add(p_71038_).subtract().add(p_71035_),
                    p_71033_.add(p_71039_).subtract().add(p_71036_),
                    p_71034_.add(p_71040_).subtract().add(p_71037_)
                );
            case NORTH:
                return new BoundingBox(
                    p_71032_.add(p_71035_),
                    p_71033_.add(p_71036_),
                    p_71034_.subtract(p_71040_).add().add(p_71037_),
                    p_71032_.add(p_71038_).subtract().add(p_71035_),
                    p_71033_.add(p_71039_).subtract().add(p_71036_),
                    p_71034_.add(p_71037_)
                );
            case WEST:
                return new BoundingBox(
                    p_71032_.subtract(p_71040_).add().add(p_71037_),
                    p_71033_.add(p_71036_),
                    p_71034_.add(p_71035_),
                    p_71032_.add(p_71037_),
                    p_71033_.add(p_71039_).subtract().add(p_71036_),
                    p_71034_.add(p_71038_).subtract().add(p_71035_)
                );
            case EAST:
                return new BoundingBox(
                    p_71032_.add(p_71037_),
                    p_71033_.add(p_71036_),
                    p_71034_.add(p_71035_),
                    p_71032_.add(p_71040_).subtract().add(p_71037_),
                    p_71033_.add(p_71039_).subtract().add(p_71036_),
                    p_71034_.add(p_71038_).subtract().add(p_71035_)
                );
        }
    }

    public Stream<ChunkPos> intersectingChunks() {
        me.alphamode.mcbig.math.BigInteger i = SectionPos.blockToSectionCoord(this.minX());
        me.alphamode.mcbig.math.BigInteger j = SectionPos.blockToSectionCoord(this.minZ());
        me.alphamode.mcbig.math.BigInteger k = SectionPos.blockToSectionCoord(this.maxX());
        me.alphamode.mcbig.math.BigInteger l = SectionPos.blockToSectionCoord(this.maxZ());
        return ChunkPos.rangeClosed(new ChunkPos(i, j), new ChunkPos(k, l));
    }

    public boolean intersects(BoundingBox p_71050_) {
        return this.maxX.compareTo(p_71050_.minX) >= 0
            && this.minX.compareTo(p_71050_.maxX) <= 0
            && this.maxZ.compareTo(p_71050_.minZ) >= 0
            && this.minZ.compareTo(p_71050_.maxZ) <= 0
            && this.maxY.compareTo(p_71050_.minY) >= 0
            && this.minY.compareTo(p_71050_.maxY) <= 0;
    }

    public boolean intersects(me.alphamode.mcbig.math.BigInteger p_71020_, me.alphamode.mcbig.math.BigInteger p_71021_, me.alphamode.mcbig.math.BigInteger p_71022_, me.alphamode.mcbig.math.BigInteger p_71023_) {
        return this.maxX.compareTo(p_71020_) >= 0 && this.minX.compareTo(p_71022_) <= 0 && this.maxZ.compareTo(p_71021_) >= 0 && this.minZ.compareTo(p_71023_) <= 0;
    }

    public static Optional<BoundingBox> encapsulatingPositions(Iterable<BlockPos> p_162379_) {
        Iterator<BlockPos> iterator = p_162379_.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        } else {
            BoundingBox boundingbox = new BoundingBox(iterator.next());
            iterator.forEachRemaining(boundingbox::encapsulate);
            return Optional.of(boundingbox);
        }
    }

    public static Optional<BoundingBox> encapsulatingBoxes(Iterable<BoundingBox> p_162389_) {
        Iterator<BoundingBox> iterator = p_162389_.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        } else {
            BoundingBox boundingbox = iterator.next();
            BoundingBox boundingbox1 = new BoundingBox(
                boundingbox.minX, boundingbox.minY, boundingbox.minZ, boundingbox.maxX, boundingbox.maxY, boundingbox.maxZ
            );
            iterator.forEachRemaining(boundingbox1::encapsulate);
            return Optional.of(boundingbox1);
        }
    }

    @Deprecated
    public BoundingBox encapsulate(BoundingBox p_162387_) {
        this.minX = this.minX.min(p_162387_.minX);
        this.minY = this.minY.min(p_162387_.minY);
        this.minZ = this.minZ.min(p_162387_.minZ);
        this.maxX = this.maxX.max(p_162387_.maxX);
        this.maxY = this.maxY.max(p_162387_.maxY);
        this.maxZ = this.maxZ.max(p_162387_.maxZ);
        return this;
    }

    @Deprecated
    public BoundingBox encapsulate(BlockPos p_162372_) {
        this.minX = this.minX.min(p_162372_.getBigX());
        this.minY = this.minY.min(p_162372_.getBigY());
        this.minZ = this.minZ.min(p_162372_.getBigZ());
        this.maxX = this.maxX.max(p_162372_.getBigX());
        this.maxY = this.maxY.max(p_162372_.getBigY());
        this.maxZ = this.maxZ.max(p_162372_.getBigZ());
        return this;
    }

    @Deprecated
    public BoundingBox move(me.alphamode.mcbig.math.BigInteger p_162368_, me.alphamode.mcbig.math.BigInteger p_162369_, me.alphamode.mcbig.math.BigInteger p_162370_) {
        this.minX = this.minX.add(p_162368_);
        this.minY = this.minY.add(p_162369_);
        this.minZ = this.minZ.add(p_162370_);
        this.maxX = this.maxX.add(p_162368_);
        this.maxY = this.maxY.add(p_162369_);
        this.maxZ = this.maxZ.add(p_162370_);
        return this;
    }

    @Deprecated
    public BoundingBox move(me.alphamode.mcbig.core.Vec3l p_162374_) {
        return this.move(p_162374_.getBigX(), p_162374_.getBigY(), p_162374_.getBigZ());
    }

    public BoundingBox moved(int p_71046_, int p_71047_, int p_71048_) {
        return new BoundingBox(
            this.minX.add(p_71046_), this.minY.add(p_71047_), this.minZ.add(p_71048_), this.maxX.add(p_71046_), this.maxY.add(p_71047_), this.maxZ.add(p_71048_)
        );
    }

    public BoundingBox inflatedBy(long p_191962_) {
        return this.inflatedBy(p_191962_, p_191962_, p_191962_);
    }

    public BoundingBox inflatedBy(long p_341249_, long p_340933_, long p_341033_) {
        return new BoundingBox(
                this.minX().subtract(p_341249_),
                this.minY().subtract(p_340933_),
                this.minZ().subtract(p_341033_),
                this.maxX().add(p_341249_),
                this.maxY().add(p_340933_),
                this.maxZ().add(p_341033_)
        );
    }

    public boolean isInside(me.alphamode.mcbig.core.Vec3l p_71052_) {
        return this.isInside(p_71052_.getBigX(), p_71052_.getBigY(), p_71052_.getBigZ());
    }

    public boolean isInside(me.alphamode.mcbig.math.BigInteger p_261671_, me.alphamode.mcbig.math.BigInteger p_261537_, me.alphamode.mcbig.math.BigInteger p_261678_) {
        return p_261671_.compareTo(this.minX) >= 0
            && p_261671_.compareTo(this.maxX) <= 0
            && p_261678_.compareTo(this.minZ) >= 0
            && p_261678_.compareTo(this.maxZ) <= 0
            && p_261537_.compareTo(this.minY) >= 0
            && p_261537_.compareTo(this.maxY) <= 0;
    }

    public Vec3i getLength() {
        return new Vec3i(this.maxX.subtract(this.minX).intValue(), this.maxY.subtract(this.minY).intValue(), this.maxZ.subtract(this.minZ).intValue());
    }

    public int getXSpan() {
        return this.maxX.subtract(this.minX).intValue() + 1;
    }

    public int getYSpan() {
        return this.maxY.subtract(this.minY).intValue() + 1;
    }

    public int getZSpan() {
        return this.maxZ.subtract(this.minZ).intValue() + 1;
    }

    public BlockPos getCenter() {
        return new BlockPos(
            this.minX.add((this.maxX.subtract(this.minX).add()).divide(me.alphamode.mcbig.core.BigConstants.Ints.TWO)), this.minY.add((this.maxY.subtract(this.minY).add()).divide(me.alphamode.mcbig.core.BigConstants.Ints.TWO)), this.minZ.add((this.maxZ.subtract(this.minZ).add()).divide(me.alphamode.mcbig.core.BigConstants.Ints.TWO))
        );
    }

    public void forAllCorners(Consumer<BlockPos> p_162381_) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        p_162381_.accept(blockpos$mutableblockpos.set(this.maxX, this.maxY, this.maxZ));
        p_162381_.accept(blockpos$mutableblockpos.set(this.minX, this.maxY, this.maxZ));
        p_162381_.accept(blockpos$mutableblockpos.set(this.maxX, this.minY, this.maxZ));
        p_162381_.accept(blockpos$mutableblockpos.set(this.minX, this.minY, this.maxZ));
        p_162381_.accept(blockpos$mutableblockpos.set(this.maxX, this.maxY, this.minZ));
        p_162381_.accept(blockpos$mutableblockpos.set(this.minX, this.maxY, this.minZ));
        p_162381_.accept(blockpos$mutableblockpos.set(this.maxX, this.minY, this.minZ));
        p_162381_.accept(blockpos$mutableblockpos.set(this.minX, this.minY, this.minZ));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("minX", this.minX)
            .add("minY", this.minY)
            .add("minZ", this.minZ)
            .add("maxX", this.maxX)
            .add("maxY", this.maxY)
            .add("maxZ", this.maxZ)
            .toString();
    }

    @Override
    public boolean equals(Object p_162393_) {
        if (this == p_162393_) {
            return true;
        } else {
            return !(p_162393_ instanceof BoundingBox boundingbox)
                ? false
                : this.minX == boundingbox.minX
                    && this.minY == boundingbox.minY
                    && this.minZ == boundingbox.minZ
                    && this.maxX == boundingbox.maxX
                    && this.maxY == boundingbox.maxY
                    && this.maxZ == boundingbox.maxZ;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public me.alphamode.mcbig.math.BigInteger minX() {
        return this.minX;
    }

    public me.alphamode.mcbig.math.BigInteger minY() {
        return this.minY;
    }

    public me.alphamode.mcbig.math.BigInteger minZ() {
        return this.minZ;
    }

    public me.alphamode.mcbig.math.BigInteger maxX() {
        return this.maxX;
    }

    public me.alphamode.mcbig.math.BigInteger maxY() {
        return this.maxY;
    }

    public me.alphamode.mcbig.math.BigInteger maxZ() {
        return this.maxZ;
    }
}
