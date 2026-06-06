package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;

public class TryFindWaterGoal extends Goal {
    private final PathfinderMob mob;

    public TryFindWaterGoal(PathfinderMob p_25964_) {
        this.mob = p_25964_;
    }

    @Override
    public boolean canUse() {
        return this.mob.onGround() && !this.mob.level().getFluidState(this.mob.blockPosition()).is(FluidTags.WATER);
    }

    @Override
    public void start() {
        BlockPos blockpos = null;

        for (BlockPos blockpos1 : BlockPos.betweenClosed(
            Mth.bigFloor(this.mob.getX() - 2.0),
            Mth.bigFloor(this.mob.getY() - 2.0),
            Mth.bigFloor(this.mob.getZ() - 2.0),
            Mth.bigFloor(this.mob.getX() + 2.0),
            me.alphamode.mcbig.math.BigInteger.val(this.mob.getBlockY()),
            Mth.bigFloor(this.mob.getZ() + 2.0)
        )) {
            if (this.mob.level().getFluidState(blockpos1).is(FluidTags.WATER)) {
                blockpos = blockpos1;
                break;
            }
        }

        if (blockpos != null) {
            this.mob.getMoveControl().setWantedPosition((double)blockpos.getBigX().doubleValue(), (double)blockpos.getBigY().doubleValue(), (double)blockpos.getBigZ().doubleValue(), 1.0);
        }
    }
}
