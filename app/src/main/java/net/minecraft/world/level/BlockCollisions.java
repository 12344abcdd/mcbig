package net.minecraft.world.level;

import com.google.common.collect.AbstractIterator;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockCollisions<T> extends AbstractIterator<T> {
    private final AABB box;
    private final CollisionContext context;
    private final Cursor3D cursor;
    private final BlockPos.MutableBlockPos pos;
    private final VoxelShape entityShape;
    private final CollisionGetter collisionGetter;
    private final boolean onlySuffocatingBlocks;
    @Nullable
    private BlockGetter cachedBlockGetter;
    private ChunkPos cachedBlockGetterPos;
    private final BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> resultProvider;

    public BlockCollisions(
        CollisionGetter p_286817_, @Nullable Entity p_286246_, AABB p_286624_, boolean p_286354_, BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> p_286303_
    ) {
        this(p_286817_, p_286246_ == null ? CollisionContext.empty() : CollisionContext.of(p_286246_), p_286624_, p_286354_, p_286303_);
    }

    public BlockCollisions(
        CollisionGetter p_361869_, CollisionContext p_362362_, AABB p_360932_, boolean p_362824_, BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> p_362971_
    ) {
        this.context = p_362362_;
        this.pos = new BlockPos.MutableBlockPos();
        this.entityShape = Shapes.create(p_360932_);
        this.collisionGetter = p_361869_;
        this.box = p_360932_;
        this.onlySuffocatingBlocks = p_362824_;
        this.resultProvider = p_362971_;
        me.alphamode.mcbig.math.BigInteger i = Mth.bigFloor(p_360932_.minX - 1.0E-7).subtract();
        me.alphamode.mcbig.math.BigInteger j = Mth.bigFloor(p_360932_.maxX + 1.0E-7).add();
        me.alphamode.mcbig.math.BigInteger k = Mth.bigFloor(p_360932_.minY - 1.0E-7).subtract();
        me.alphamode.mcbig.math.BigInteger l = Mth.bigFloor(p_360932_.maxY + 1.0E-7).add();
        me.alphamode.mcbig.math.BigInteger i1 = Mth.bigFloor(p_360932_.minZ - 1.0E-7).subtract();
        me.alphamode.mcbig.math.BigInteger j1 = Mth.bigFloor(p_360932_.maxZ + 1.0E-7).add();
        this.cursor = new Cursor3D(i, k, i1, j, l, j1);
    }

    @Nullable
    private BlockGetter getChunk(me.alphamode.mcbig.math.BigInteger p_186412_, me.alphamode.mcbig.math.BigInteger p_186413_) {
        me.alphamode.mcbig.math.BigInteger i = SectionPos.blockToSectionCoord(p_186412_);
        me.alphamode.mcbig.math.BigInteger j = SectionPos.blockToSectionCoord(p_186413_);
        ChunkPos k = new ChunkPos(i, j);
        if (this.cachedBlockGetter != null && this.cachedBlockGetterPos.equals(k)) {
            return this.cachedBlockGetter;
        } else {
            BlockGetter blockgetter = this.collisionGetter.getChunkForCollisions(i, j);
            this.cachedBlockGetter = blockgetter;
            this.cachedBlockGetterPos = k;
            return blockgetter;
        }
    }

    @Override
    protected T computeNext() {
        while (this.cursor.advance()) {
            me.alphamode.mcbig.math.BigInteger i = this.cursor.nextX();
            me.alphamode.mcbig.math.BigInteger j = this.cursor.nextY();
            me.alphamode.mcbig.math.BigInteger k = this.cursor.nextZ();
            int l = this.cursor.getNextType();
            if (l != 3) {
                BlockGetter blockgetter = this.getChunk(i, k);
                if (blockgetter != null) {
                    this.pos.set(i, j, k);
                    BlockState blockstate = blockgetter.getBlockState(this.pos);
                    if ((!this.onlySuffocatingBlocks || blockstate.isSuffocating(blockgetter, this.pos))
                        && (l != 1 || blockstate.hasLargeCollisionShape())
                        && (l != 2 || blockstate.is(Blocks.MOVING_PISTON))) {
                        VoxelShape voxelshape = this.context.getCollisionShape(blockstate, this.collisionGetter, this.pos);
                        if (voxelshape == Shapes.block()) {
                            if (this.box.intersects((double)i.doubleValue(), (double)j.doubleValue(), (double)k.doubleValue(), (double)i.doubleValue() + 1.0, (double)j.doubleValue() + 1.0, (double)k.doubleValue() + 1.0)) {
                                return this.resultProvider.apply(this.pos, voxelshape.move((double)i.doubleValue(), (double)j.doubleValue(), (double)k.doubleValue()));
                            }
                        } else {
                            VoxelShape voxelshape1 = voxelshape.move((double)i.doubleValue(), (double)j.doubleValue(), (double)k.doubleValue());
                            if (!voxelshape1.isEmpty() && Shapes.joinIsNotEmpty(voxelshape1, this.entityShape, BooleanOp.AND)) {
                                return this.resultProvider.apply(this.pos, voxelshape1);
                            }
                        }
                    }
                }
            }
        }

        return this.endOfData();
    }
}
