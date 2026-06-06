package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class CreakingHeartBlock extends BaseEntityBlock {
    public static final MapCodec<CreakingHeartBlock> CODEC = simpleCodec(CreakingHeartBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty ACTIVE = BlockStateProperties.ACTIVE;
    public static final BooleanProperty NATURAL = BlockStateProperties.NATURAL;

    @Override
    public MapCodec<CreakingHeartBlock> codec() {
        return CODEC;
    }

    protected CreakingHeartBlock(BlockBehaviour.Properties p_380228_) {
        super(p_380228_);
        this.registerDefaultState(
            this.defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(ACTIVE, Boolean.valueOf(false)).setValue(NATURAL, Boolean.valueOf(false))
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_380178_, BlockState p_380317_) {
        return new CreakingHeartBlockEntity(p_380178_, p_380317_);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_379447_, BlockState p_379641_, BlockEntityType<T> p_380325_) {
        if (p_379447_.isClientSide) {
            return null;
        } else {
            return p_379641_.getValue(ACTIVE) ? createTickerHelper(p_380325_, BlockEntityType.CREAKING_HEART, CreakingHeartBlockEntity::serverTick) : null;
        }
    }

    public static boolean isNaturalNight(Level p_380307_) {
        return p_380307_.dimensionType().natural() && p_380307_.isNight();
    }

    @Override
    public void animateTick(BlockState p_379556_, Level p_379594_, BlockPos p_379297_, RandomSource p_379301_) {
        if (isNaturalNight(p_379594_)) {
            if (p_379556_.getValue(ACTIVE)) {
                if (p_379301_.nextInt(16) == 0 && isSurroundedByLogs(p_379594_, p_379297_)) {
                    p_379594_.playLocalSound(
                        (double)p_379297_.getX(),
                        (double)p_379297_.getY(),
                        (double)p_379297_.getZ(),
                        SoundEvents.CREAKING_HEART_IDLE,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F,
                        false
                    );
                }
            }
        }
    }

    @Override
    protected BlockState updateShape(
        BlockState p_379552_,
        LevelReader p_379446_,
        ScheduledTickAccess p_379318_,
        BlockPos p_379343_,
        Direction p_380340_,
        BlockPos p_380150_,
        BlockState p_379791_,
        RandomSource p_379888_
    ) {
        BlockState blockstate = super.updateShape(p_379552_, p_379446_, p_379318_, p_379343_, p_380340_, p_380150_, p_379791_, p_379888_);
        return updateState(blockstate, p_379446_, p_379343_);
    }

    private static BlockState updateState(BlockState p_380049_, LevelReader p_379463_, BlockPos p_379599_) {
        boolean flag = hasRequiredLogs(p_380049_, p_379463_, p_379599_);
        boolean flag1 = !p_380049_.getValue(ACTIVE);
        return flag && flag1 ? p_380049_.setValue(ACTIVE, Boolean.valueOf(true)) : p_380049_;
    }

    public static boolean hasRequiredLogs(BlockState p_379990_, LevelReader p_380123_, BlockPos p_380405_) {
        Direction.Axis direction$axis = p_379990_.getValue(AXIS);

        for (Direction direction : direction$axis.getDirections()) {
            BlockState blockstate = p_380123_.getBlockState(p_380405_.relative(direction));
            if (!blockstate.is(BlockTags.PALE_OAK_LOGS) || blockstate.getValue(AXIS) != direction$axis) {
                return false;
            }
        }

        return true;
    }

    private static boolean isSurroundedByLogs(LevelAccessor p_379444_, BlockPos p_380241_) {
        for (Direction direction : Direction.values()) {
            BlockPos blockpos = p_380241_.relative(direction);
            BlockState blockstate = p_379444_.getBlockState(blockpos);
            if (!blockstate.is(BlockTags.PALE_OAK_LOGS)) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_379431_) {
        return updateState(this.defaultBlockState().setValue(AXIS, p_379431_.getClickedFace().getAxis()), p_379431_.getLevel(), p_379431_.getClickedPos());
    }

    @Override
    protected BlockState rotate(BlockState p_380251_, Rotation p_379529_) {
        return RotatedPillarBlock.rotatePillar(p_380251_, p_379529_);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_379898_) {
        p_379898_.add(AXIS, ACTIVE, NATURAL);
    }

    @Override
    protected void onRemove(BlockState p_380377_, Level p_380022_, BlockPos p_379876_, BlockState p_379979_, boolean p_379655_) {
        if (p_380022_.getBlockEntity(p_379876_) instanceof CreakingHeartBlockEntity creakingheartblockentity) {
            creakingheartblockentity.removeProtector(null);
        }

        super.onRemove(p_380377_, p_380022_, p_379876_, p_379979_, p_379655_);
    }

    @Override
    protected void onExplosionHit(
        BlockState p_382935_, ServerLevel p_382804_, BlockPos p_383050_, Explosion p_383064_, BiConsumer<ItemStack, BlockPos> p_383124_
    ) {
        if (p_382804_.getBlockEntity(p_383050_) instanceof CreakingHeartBlockEntity creakingheartblockentity
            && p_383064_ instanceof ServerExplosion serverexplosion
            && p_383064_.getBlockInteraction().shouldAffectBlocklikeEntities()) {
            creakingheartblockentity.removeProtector(serverexplosion.getDamageSource());
            if (p_383064_.getIndirectSourceEntity() instanceof Player player && p_383064_.getBlockInteraction().shouldAffectBlocklikeEntities()) {
                this.tryAwardExperience(player, p_382935_, p_382804_, p_383050_);
            }
        }

        super.onExplosionHit(p_382935_, p_382804_, p_383050_, p_383064_, p_383124_);
    }

    @Override
    public BlockState playerWillDestroy(Level p_380319_, BlockPos p_379939_, BlockState p_379928_, Player p_380097_) {
        if (p_380319_.getBlockEntity(p_379939_) instanceof CreakingHeartBlockEntity creakingheartblockentity) {
            creakingheartblockentity.removeProtector(p_380097_.damageSources().playerAttack(p_380097_));
            this.tryAwardExperience(p_380097_, p_379928_, p_380319_, p_379939_);
        }

        return super.playerWillDestroy(p_380319_, p_379939_, p_379928_, p_380097_);
    }

    private void tryAwardExperience(Player p_386796_, BlockState p_382970_, Level p_383065_, BlockPos p_382941_) {
        if (!p_386796_.isCreative() && !p_386796_.isSpectator() && p_382970_.getValue(NATURAL) && p_383065_ instanceof ServerLevel serverlevel) {
            this.popExperience(serverlevel, p_382941_, p_383065_.random.nextIntBetweenInclusive(20, 24));
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState p_380993_) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState p_381152_, Level p_381142_, BlockPos p_381148_) {
        if (!p_381152_.getValue(ACTIVE)) {
            return 0;
        } else {
            return p_381142_.getBlockEntity(p_381148_) instanceof CreakingHeartBlockEntity creakingheartblockentity
                ? creakingheartblockentity.getAnalogOutputSignal()
                : 0;
        }
    }
}
