package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class PlayerRespawnLogic {
    @Nullable
    protected static BlockPos getOverworldRespawnPos(ServerLevel p_183929_, me.alphamode.mcbig.math.BigInteger p_183930_, me.alphamode.mcbig.math.BigInteger p_183931_) {
        boolean flag = p_183929_.dimensionType().hasCeiling();
        LevelChunk levelchunk = p_183929_.getChunk(SectionPos.blockToSectionCoord(p_183930_), SectionPos.blockToSectionCoord(p_183931_));
        int i = flag
            ? p_183929_.getChunkSource().getGenerator().getSpawnHeight(p_183929_)
            : levelchunk.getHeight(Heightmap.Types.MOTION_BLOCKING, p_183930_.and(me.alphamode.mcbig.core.BigConstants.Ints.FIFTEEN), p_183931_.and(me.alphamode.mcbig.core.BigConstants.Ints.FIFTEEN));
        if (i < p_183929_.getMinY()) {
            return null;
        } else {
            int j = levelchunk.getHeight(Heightmap.Types.WORLD_SURFACE, p_183930_.and(me.alphamode.mcbig.core.BigConstants.Ints.FIFTEEN), p_183931_.and(me.alphamode.mcbig.core.BigConstants.Ints.FIFTEEN));
            if (j <= i && j > levelchunk.getHeight(Heightmap.Types.OCEAN_FLOOR, p_183930_.and(me.alphamode.mcbig.core.BigConstants.Ints.FIFTEEN), p_183931_.and(me.alphamode.mcbig.core.BigConstants.Ints.FIFTEEN))) {
                return null;
            } else {
                BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                for (int k = i + 1; k >= p_183929_.getMinY(); k--) {
                    blockpos$mutableblockpos.set(p_183930_, k, p_183931_);
                    BlockState blockstate = p_183929_.getBlockState(blockpos$mutableblockpos);
                    if (!blockstate.getFluidState().isEmpty()) {
                        break;
                    }

                    if (Block.isFaceFull(blockstate.getCollisionShape(p_183929_, blockpos$mutableblockpos), Direction.UP)) {
                        return blockpos$mutableblockpos.above().immutable();
                    }
                }

                return null;
            }
        }
    }

    @Nullable
    public static BlockPos getSpawnPosInChunk(ServerLevel p_183933_, ChunkPos p_183934_) {
        if (SharedConstants.debugVoidTerrain(p_183934_)) {
            return null;
        } else {
            for(me.alphamode.mcbig.math.BigInteger i = p_183934_.getMinBlockX(); i.compareTo(p_183934_.getMaxBlockX()) <= 0; i = i.add()) {
                for(me.alphamode.mcbig.math.BigInteger j = p_183934_.getMinBlockZ(); j.compareTo(p_183934_.getMaxBlockZ()) <= 0; j = j.add()) {
                    BlockPos blockpos = getOverworldRespawnPos(p_183933_, i, j);
                    if (blockpos != null) {
                        return blockpos;
                    }
                }
            }

            return null;
        }
    }
}
