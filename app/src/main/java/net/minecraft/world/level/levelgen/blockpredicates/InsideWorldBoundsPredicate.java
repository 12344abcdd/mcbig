package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;

public class InsideWorldBoundsPredicate implements BlockPredicate {
    public static final MapCodec<InsideWorldBoundsPredicate> CODEC = RecordCodecBuilder.mapCodec(
        p_190473_ -> p_190473_.group(me.alphamode.mcbig.core.Vec3l.offsetCodec(16).optionalFieldOf("offset", BlockPos.ZERO).forGetter(p_190475_ -> p_190475_.offset))
                .apply(p_190473_, InsideWorldBoundsPredicate::new)
    );
    private final me.alphamode.mcbig.core.Vec3l offset;

    public InsideWorldBoundsPredicate(me.alphamode.mcbig.core.Vec3l p_190467_) {
        this.offset = p_190467_;
    }

    public boolean test(WorldGenLevel p_190470_, BlockPos p_190471_) {
        return !p_190470_.isOutsideBuildHeight(p_190471_.offset(this.offset));
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.INSIDE_WORLD_BOUNDS;
    }
}
