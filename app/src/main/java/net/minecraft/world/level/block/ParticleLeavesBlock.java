package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ParticleLeavesBlock extends LeavesBlock {
    public static final MapCodec<ParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_383234_ -> p_383234_.group(
                    ExtraCodecs.POSITIVE_INT.fieldOf("chance").forGetter(p_383200_ -> p_383200_.chance),
                    ParticleTypes.CODEC.fieldOf("particle").forGetter(p_382913_ -> p_382913_.particle),
                    propertiesCodec()
                )
                .apply(p_383234_, ParticleLeavesBlock::new)
    );
    private final ParticleOptions particle;
    private final int chance;

    @Override
    public MapCodec<ParticleLeavesBlock> codec() {
        return CODEC;
    }

    public ParticleLeavesBlock(int p_383011_, ParticleOptions p_383127_, BlockBehaviour.Properties p_383213_) {
        super(p_383213_);
        this.chance = p_383011_;
        this.particle = p_383127_;
    }

    @Override
    public void animateTick(BlockState p_383151_, Level p_383209_, BlockPos p_383215_, RandomSource p_383093_) {
        super.animateTick(p_383151_, p_383209_, p_383215_, p_383093_);
        if (p_383093_.nextInt(this.chance) == 0) {
            BlockPos blockpos = p_383215_.below();
            BlockState blockstate = p_383209_.getBlockState(blockpos);
            if (!isFaceFull(blockstate.getCollisionShape(p_383209_, blockpos), Direction.UP)) {
                ParticleUtils.spawnParticleBelow(p_383209_, p_383215_, p_383093_, this.particle);
            }
        }
    }
}
