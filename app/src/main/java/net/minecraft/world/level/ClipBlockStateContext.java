package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ClipBlockStateContext {
    private final me.alphamode.mcbig.core.BigVec3 from;
    private final me.alphamode.mcbig.core.BigVec3 to;
    private final Predicate<BlockState> block;

    public ClipBlockStateContext(me.alphamode.mcbig.core.BigVec3 p_151401_, me.alphamode.mcbig.core.BigVec3 p_151402_, Predicate<BlockState> p_151403_) {
        this.from = p_151401_;
        this.to = p_151402_;
        this.block = p_151403_;
    }

    public me.alphamode.mcbig.core.BigVec3 getTo() {
        return this.to;
    }

    public me.alphamode.mcbig.core.BigVec3 getFrom() {
        return this.from;
    }

    public Predicate<BlockState> isTargetBlock() {
        return this.block;
    }
}
