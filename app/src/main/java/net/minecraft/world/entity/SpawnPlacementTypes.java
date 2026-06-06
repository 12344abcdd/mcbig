package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public interface SpawnPlacementTypes {
    SpawnPlacementType NO_RESTRICTIONS = (p_321554_, p_321832_, p_321540_) -> true;
    SpawnPlacementType IN_WATER = (p_379078_, p_379079_, p_379080_) -> {
        if (p_379080_ != null && p_379078_.getWorldBorder().isWithinBounds(p_379079_)) {
            BlockPos blockpos = p_379079_.above();
            return p_379078_.getFluidState(p_379079_).is(FluidTags.WATER) && !p_379078_.getBlockState(blockpos).isRedstoneConductor(p_379078_, blockpos);
        } else {
            return false;
        }
    };
    SpawnPlacementType IN_LAVA = (p_379075_, p_379076_, p_379077_) -> p_379077_ != null && p_379075_.getWorldBorder().isWithinBounds(p_379076_)
            ? p_379075_.getFluidState(p_379076_).is(FluidTags.LAVA)
            : false;
    SpawnPlacementType ON_GROUND = new SpawnPlacementType() {
        @Override
        public boolean isSpawnPositionOk(LevelReader p_321666_, BlockPos p_321783_, @Nullable EntityType<?> p_321839_) {
            if (p_321839_ != null && p_321666_.getWorldBorder().isWithinBounds(p_321783_)) {
                BlockPos blockpos = p_321783_.above();
                BlockPos blockpos1 = p_321783_.below();
                BlockState blockstate = p_321666_.getBlockState(blockpos1);
                return !blockstate.isValidSpawn(p_321666_, blockpos1, p_321839_)
                    ? false
                    : this.isValidEmptySpawnBlock(p_321666_, p_321783_, p_321839_) && this.isValidEmptySpawnBlock(p_321666_, blockpos, p_321839_);
            } else {
                return false;
            }
        }

        private boolean isValidEmptySpawnBlock(LevelReader p_321512_, BlockPos p_321822_, EntityType<?> p_321785_) {
            BlockState blockstate = p_321512_.getBlockState(p_321822_);
            return NaturalSpawner.isValidEmptySpawnBlock(p_321512_, p_321822_, blockstate, blockstate.getFluidState(), p_321785_);
        }

        @Override
        public BlockPos adjustSpawnPosition(LevelReader p_321527_, BlockPos p_321602_) {
            BlockPos blockpos = p_321602_.below();
            return p_321527_.getBlockState(blockpos).isPathfindable(PathComputationType.LAND) ? blockpos : p_321602_;
        }
    };
}
