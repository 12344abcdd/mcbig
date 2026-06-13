package net.minecraft.world.level.lighting;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.jetbrains.annotations.VisibleForTesting;

public final class SkyLightEngine extends LightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
    private static final long REMOVE_TOP_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(15);
    private static final long REMOVE_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseSkipOneDirection(15, Direction.UP);
    private static final long ADD_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.increaseSkipOneDirection(15, false, Direction.UP);
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    private final ChunkSkyLightSources emptyChunkSources;

    public SkyLightEngine(LightChunkGetter p_75843_) {
        this(p_75843_, new SkyLightSectionStorage(p_75843_));
    }

    @VisibleForTesting
    protected SkyLightEngine(LightChunkGetter p_282215_, SkyLightSectionStorage p_282341_) {
        super(p_282215_, p_282341_);
        this.emptyChunkSources = new ChunkSkyLightSources(p_282215_.getLevel());
    }

    private static boolean isSourceLevel(int p_285004_) {
        //return p_285004_ == 15;
        return true;
    }

    private int getLowestSourceY(me.alphamode.mcbig.math.BigInteger p_285058_, me.alphamode.mcbig.math.BigInteger p_285191_, int p_285111_) {
        ChunkSkyLightSources chunkskylightsources = this.getChunkSources(SectionPos.blockToSectionCoord(p_285058_), SectionPos.blockToSectionCoord(p_285191_));
        return chunkskylightsources == null
            ? p_285111_
            : chunkskylightsources.getLowestSourceY(SectionPos.sectionRelative(p_285058_), SectionPos.sectionRelative(p_285191_));
    }

    @Nullable
    private ChunkSkyLightSources getChunkSources(me.alphamode.mcbig.math.BigInteger p_285270_, me.alphamode.mcbig.math.BigInteger p_285307_) {
        LightChunk lightchunk = this.chunkSource.getChunkForLighting(p_285270_, p_285307_);
        return lightchunk != null ? lightchunk.getSkyLightSources() : null;
    }

    @Override
    protected void checkNode(BlockPos p_75859_) {/*
        me.alphamode.mcbig.math.BigInteger i = p_75859_.getBigX();
        me.alphamode.mcbig.math.BigInteger j = p_75859_.getBigY();
        me.alphamode.mcbig.math.BigInteger k = p_75859_.getBigZ();
        SectionPos l = SectionPos.of(p_75859_);
        int i1 = this.storage.lightOnInSection(l) ? this.getLowestSourceY(i, k, Integer.MAX_VALUE) : Integer.MAX_VALUE;
        if (i1 != Integer.MAX_VALUE) {
            this.updateSourcesInColumn(i, k, i1);
        }

        if (this.storage.storingLightForSection(l)) {
            boolean flag = j.compareTo(i1) >= 0;
            if (flag) {
                this.enqueueDecrease(p_75859_, REMOVE_SKY_SOURCE_ENTRY);
                this.enqueueIncrease(p_75859_, ADD_SKY_SOURCE_ENTRY);
            } else {
                int j1 = this.storage.getStoredLevel(p_75859_);
                if (j1 > 0) {
                    this.storage.setStoredLevel(p_75859_, 0);
                    this.enqueueDecrease(p_75859_, LightEngine.QueueEntry.decreaseAllDirections(j1));
                } else {
                    this.enqueueDecrease(p_75859_, PULL_LIGHT_IN_ENTRY);
                }
            }
        }*/
    }

    private void updateSourcesInColumn(me.alphamode.mcbig.math.BigInteger p_285053_, me.alphamode.mcbig.math.BigInteger p_285140_, int p_285337_) {/*
        int i = SectionPos.sectionToBlockCoord(this.storage.getBottomSectionY());
        this.removeSourcesBelow(p_285053_, p_285140_, p_285337_, i);
        this.addSourcesAbove(p_285053_, p_285140_, p_285337_, i);*/
    }

    private void removeSourcesBelow(me.alphamode.mcbig.math.BigInteger p_285475_, me.alphamode.mcbig.math.BigInteger p_285138_, int p_285130_, int p_285112_) {/*
        if (p_285130_ > p_285112_) {
            me.alphamode.mcbig.math.BigInteger i = SectionPos.blockToSectionCoord(p_285475_);
            me.alphamode.mcbig.math.BigInteger j = SectionPos.blockToSectionCoord(p_285138_);
            int k = p_285130_ - 1;

            for (int l = SectionPos.blockToSectionCoord(k); this.storage.hasLightDataAtOrBelow(l); l--) {
                if (this.storage.storingLightForSection(SectionPos.of(i, l, j))) {
                    int i1 = SectionPos.sectionToBlockCoord(l);
                    int j1 = i1 + 15;

                    for (int k1 = Math.min(j1, k); k1 >= i1; k1--) {
                        BlockPos l1 = new BlockPos(p_285475_, k1, p_285138_);
                        if (!isSourceLevel(this.storage.getStoredLevel(l1))) {
                            return;
                        }

                        this.storage.setStoredLevel(l1, 0);
                        this.enqueueDecrease(l1, k1 == p_285130_ - 1 ? REMOVE_TOP_SKY_SOURCE_ENTRY : REMOVE_SKY_SOURCE_ENTRY);
                    }
                }
            }
        }*/
    }

    private void addSourcesAbove(me.alphamode.mcbig.math.BigInteger p_285241_, me.alphamode.mcbig.math.BigInteger p_285212_, int p_284972_, int p_285134_) {/*
        me.alphamode.mcbig.math.BigInteger i = SectionPos.blockToSectionCoord(p_285241_);
        me.alphamode.mcbig.math.BigInteger j = SectionPos.blockToSectionCoord(p_285212_);
        int k = Math.max(
            Math.max(this.getLowestSourceY(p_285241_.subtract(), p_285212_, Integer.MIN_VALUE), this.getLowestSourceY(p_285241_.add(), p_285212_, Integer.MIN_VALUE)),
            Math.max(this.getLowestSourceY(p_285241_, p_285212_.subtract(), Integer.MIN_VALUE), this.getLowestSourceY(p_285241_, p_285212_.add(), Integer.MIN_VALUE))
        );
        int l = Math.max(p_284972_, p_285134_);

        for(SectionPos i1 = SectionPos.of(i, SectionPos.blockToSectionCoord(l), j); !this.storage.isAboveData(i1); i1 = i1.relative(Direction.UP)) {
            if (this.storage.storingLightForSection(i1)) {
                int j1 = SectionPos.sectionToBlockCoord(i1.y()).intValue();
                int k1 = j1 + 15;

                for (int l1 = Math.max(j1, l); l1 <= k1; l1++) {
                    BlockPos i2 = new BlockPos(p_285241_, l1, p_285212_);
                    if (isSourceLevel(this.storage.getStoredLevel(i2))) {
                        return;
                    }

                    this.storage.setStoredLevel(i2, 15);
                    if (l1 < k || l1 == p_284972_) {
                        this.enqueueIncrease(i2, ADD_SKY_SOURCE_ENTRY);
                    }
                }
            }
        }*/
    }

    @Override
    protected void propagateIncrease(BlockPos p_285341_, long p_285204_, int p_285003_) {/*
        BlockState blockstate = null;
        int i = this.countEmptySectionsBelowIfAtBorder(p_285341_);

        for (Direction direction : PROPAGATION_DIRECTIONS) {
            if (LightEngine.QueueEntry.shouldPropagateInDirection(p_285204_, direction)) {
                BlockPos j = p_285341_.relative(direction);
                if (this.storage.storingLightForSection(SectionPos.of(j))) {
                    int k = this.storage.getStoredLevel(j);
                    int l = p_285003_ - 1;
                    if (l > k) {
                        this.mutablePos.set(j);
                        BlockState blockstate1 = this.getState(this.mutablePos);
                        int i1 = p_285003_ - this.getOpacity(blockstate1);
                        if (i1 > k) {
                            if (blockstate == null) {
                                blockstate = LightEngine.QueueEntry.isFromEmptyShape(p_285204_)
                                    ? Blocks.AIR.defaultBlockState()
                                    : this.getState(this.mutablePos.set(p_285341_));
                            }

                            if (!this.shapeOccludes(blockstate, blockstate1, direction)) {
                                this.storage.setStoredLevel(j, i1);
                                if (i1 > 1) {
                                    this.enqueueIncrease(
                                        j, LightEngine.QueueEntry.increaseSkipOneDirection(i1, isEmptyShape(blockstate1), direction.getOpposite())
                                    );
                                }

                                this.propagateFromEmptySections(j, direction, i1, true, i);
                            }
                        }
                    }
                }
            }
        }*/
    }

    @Override
    protected void propagateDecrease(BlockPos p_285015_, long p_285395_) {/*
        int i = this.countEmptySectionsBelowIfAtBorder(p_285015_);
        int j = LightEngine.QueueEntry.getFromLevel(p_285395_);

        for (Direction direction : PROPAGATION_DIRECTIONS) {
            if (LightEngine.QueueEntry.shouldPropagateInDirection(p_285395_, direction)) {
                BlockPos k = p_285015_.relative(direction);
                if (this.storage.storingLightForSection(SectionPos.of(k))) {
                    int l = this.storage.getStoredLevel(k);
                    if (l != 0) {
                        if (l <= j - 1) {
                            this.storage.setStoredLevel(k, 0);
                            this.enqueueDecrease(k, LightEngine.QueueEntry.decreaseSkipOneDirection(l, direction.getOpposite()));
                            this.propagateFromEmptySections(k, direction, l, false, i);
                        } else {
                            this.enqueueIncrease(k, LightEngine.QueueEntry.increaseOnlyOneDirection(l, false, direction.getOpposite()));
                        }
                    }
                }
            }
        }*/
    }

    private int countEmptySectionsBelowIfAtBorder(BlockPos p_285356_) {
        int i = p_285356_.getY();
        int j = SectionPos.sectionRelative(i);
        if (j != 0) {
            return 0;
        } else {
            me.alphamode.mcbig.math.BigInteger k = p_285356_.getBigX();
            me.alphamode.mcbig.math.BigInteger l = p_285356_.getBigZ();
            int i1 = SectionPos.sectionRelative(k);
            int j1 = SectionPos.sectionRelative(l);
            if (i1 != 0 && i1 != 15 && j1 != 0 && j1 != 15) {
                return 0;
            } else {
                me.alphamode.mcbig.math.BigInteger k1 = SectionPos.blockToSectionCoord(k);
                int l1 = SectionPos.blockToSectionCoord(i);
                me.alphamode.mcbig.math.BigInteger i2 = SectionPos.blockToSectionCoord(l);
                int j2 = 0;

                while(!this.storage.storingLightForSection(SectionPos.of(k1, l1 - j2 - 1, i2)) && this.storage.hasLightDataAtOrBelow(l1 - j2 - 1)) {
                    j2++;
                }

                return j2;
            }
        }
    }

    private void propagateFromEmptySections(BlockPos p_284965_, Direction p_285308_, int p_284977_, boolean p_285001_, int p_285052_) {/*
        if (p_285052_ != 0) {
            me.alphamode.mcbig.math.BigInteger i = p_284965_.getBigX();
            me.alphamode.mcbig.math.BigInteger j = p_284965_.getBigZ();
            if (crossedSectionEdge(p_285308_, SectionPos.sectionRelative(i), SectionPos.sectionRelative(j))) {
                int k = p_284965_.getY();
                me.alphamode.mcbig.math.BigInteger l = SectionPos.blockToSectionCoord(i);
                me.alphamode.mcbig.math.BigInteger i1 = SectionPos.blockToSectionCoord(j);
                int j1 = SectionPos.blockToSectionCoord(k) - 1;
                int k1 = j1 - p_285052_ + 1;

                while (j1 >= k1) {
                    if (!this.storage.storingLightForSection(SectionPos.of(l, j1, i1))) {
                        j1--;
                    } else {
                        int l1 = SectionPos.sectionToBlockCoord(j1);

                        for (int i2 = 15; i2 >= 0; i2--) {
                            BlockPos j2 = new BlockPos(i, l1 + i2, j);
                            if (p_285001_) {
                                this.storage.setStoredLevel(j2, p_284977_);
                                if (p_284977_ > 1) {
                                    this.enqueueIncrease(j2, LightEngine.QueueEntry.increaseSkipOneDirection(p_284977_, true, p_285308_.getOpposite()));
                                }
                            } else {
                                this.storage.setStoredLevel(j2, 0);
                                this.enqueueDecrease(j2, LightEngine.QueueEntry.decreaseSkipOneDirection(p_284977_, p_285308_.getOpposite()));
                            }
                        }

                        j1--;
                    }
                }
            }
        }*/
    }

    private static boolean crossedSectionEdge(Direction p_285014_, int p_284991_, int p_285468_) {
        return switch (p_285014_) {
            case NORTH -> p_285468_ == 15;
            case SOUTH -> p_285468_ == 0;
            case WEST -> p_284991_ == 15;
            case EAST -> p_284991_ == 0;
            default -> false;
        };
    }

    @Override
    public void setLightEnabled(ChunkPos p_285459_, boolean p_285013_) {/*
        super.setLightEnabled(p_285459_, p_285013_);
        if (p_285013_) {
            ChunkSkyLightSources chunkskylightsources = Objects.requireNonNullElse(this.getChunkSources(p_285459_.x(), p_285459_.z()), this.emptyChunkSources);
            int i = chunkskylightsources.getHighestLowestSourceY() - 1;
            int j = SectionPos.blockToSectionCoord(i) + 1;
            SectionPos k = SectionPos.getZeroNode(p_285459_.x(), p_285459_.z());
            int l = this.storage.getTopSectionY(k);
            int i1 = Math.max(this.storage.getBottomSectionY(), j);

            for (int j1 = l - 1; j1 >= i1; j1--) {
                DataLayer datalayer = this.storage.getDataLayerToWrite(SectionPos.of(p_285459_.x(), j1, p_285459_.z()));
                if (datalayer != null && datalayer.isEmpty()) {
                    datalayer.fill(15);
                }
            }
        }*/
    }

    @Override
    public void propagateLightSources(ChunkPos p_285333_) {/*
        SectionPos i = SectionPos.getZeroNode(p_285333_.x(), p_285333_.z());
        this.storage.setLightEnabled(i, true);
        ChunkSkyLightSources chunkskylightsources = Objects.requireNonNullElse(this.getChunkSources(p_285333_.x(), p_285333_.z()), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources1 = Objects.requireNonNullElse(this.getChunkSources(p_285333_.x(), p_285333_.z().subtract()), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources2 = Objects.requireNonNullElse(this.getChunkSources(p_285333_.x(), p_285333_.z().add()), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources3 = Objects.requireNonNullElse(this.getChunkSources(p_285333_.x().subtract(), p_285333_.z()), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources4 = Objects.requireNonNullElse(this.getChunkSources(p_285333_.x().add(), p_285333_.z()), this.emptyChunkSources);
        int j = this.storage.getTopSectionY(i);
        int k = this.storage.getBottomSectionY();
        me.alphamode.mcbig.math.BigInteger l = SectionPos.sectionToBlockCoord(p_285333_.x());
        me.alphamode.mcbig.math.BigInteger i1 = SectionPos.sectionToBlockCoord(p_285333_.z());

        for (int j1 = j - 1; j1 >= k; j1--) {
            SectionPos k1 = SectionPos.of(p_285333_.x(), j1, p_285333_.z());
            DataLayer datalayer = this.storage.getDataLayerToWrite(k1);
            if (datalayer != null) {
                int l1 = SectionPos.sectionToBlockCoord(j1);
                int i2 = l1 + 15;
                boolean flag = false;

                for (int j2 = 0; j2 < 16; j2++) {
                    for (int k2 = 0; k2 < 16; k2++) {
                        int l2 = chunkskylightsources.getLowestSourceY(k2, j2);
                        if (l2 <= i2) {
                            int i3 = j2 == 0 ? chunkskylightsources1.getLowestSourceY(k2, 15) : chunkskylightsources.getLowestSourceY(k2, j2 - 1);
                            int j3 = j2 == 15 ? chunkskylightsources2.getLowestSourceY(k2, 0) : chunkskylightsources.getLowestSourceY(k2, j2 + 1);
                            int k3 = k2 == 0 ? chunkskylightsources3.getLowestSourceY(15, j2) : chunkskylightsources.getLowestSourceY(k2 - 1, j2);
                            int l3 = k2 == 15 ? chunkskylightsources4.getLowestSourceY(0, j2) : chunkskylightsources.getLowestSourceY(k2 + 1, j2);
                            int i4 = Math.max(Math.max(i3, j3), Math.max(k3, l3));

                            for (int j4 = i2; j4 >= Math.max(l1, l2); j4--) {
                                datalayer.set(k2, SectionPos.sectionRelative(j4), j2, 15);
                                if (j4 == l2 || j4 < i4) {
                                    BlockPos k4 = new BlockPos(l.add(k2), j4, i1.add(j2));
                                    this.enqueueIncrease(k4, LightEngine.QueueEntry.increaseSkySourceInDirections(j4 == l2, j4 < i3, j4 < j3, j4 < k3, j4 < l3));
                                }
                            }

                            if (l2 < l1) {
                                flag = true;
                            }
                        }
                    }
                }

                if (!flag) {
                    break;
                }
            }
        }*/
    }
}
