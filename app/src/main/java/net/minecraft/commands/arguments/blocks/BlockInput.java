package net.minecraft.commands.arguments.blocks;

import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockInput implements Predicate<BlockInWorld> {
    private final BlockState state;
    private final Set<Property<?>> properties;
    @Nullable
    private final CompoundTag tag;

    public BlockInput(BlockState p_114666_, Set<Property<?>> p_114667_, @Nullable CompoundTag p_114668_) {
        this.state = p_114666_;
        this.properties = p_114667_;
        this.tag = p_114668_;
    }

    public BlockState getState() {
        return this.state;
    }

    public Set<Property<?>> getDefinedProperties() {
        return this.properties;
    }

    public boolean test(BlockInWorld p_114675_) {
        BlockState blockstate = p_114675_.getState();
        if (!blockstate.is(this.state.getBlock())) {
            return false;
        } else {
            for (Property<?> property : this.properties) {
                if (blockstate.getValue(property) != this.state.getValue(property)) {
                    return false;
                }
            }

            if (this.tag == null) {
                return true;
            } else {
                BlockEntity blockentity = p_114675_.getEntity();
                return blockentity != null && NbtUtils.compareNbt(this.tag, blockentity.saveWithFullMetadata(p_114675_.getLevel().registryAccess()), true);
            }
        }
    }

    public boolean test(ServerLevel p_173524_, BlockPos p_173525_) {
        return this.test(new BlockInWorld(p_173524_, p_173525_, false));
    }

    public boolean place(ServerLevel p_114671_, BlockPos p_114672_, int p_114673_) {
        BlockState blockstate = Block.updateFromNeighbourShapes(this.state, p_114671_, p_114672_);
        if (blockstate.isAir()) {
            blockstate = this.state;
        }

        blockstate = this.overwriteWithDefinedProperties(blockstate);
        if (!p_114671_.setBlock(p_114672_, blockstate, p_114673_)) {
            return false;
        } else {
            if (this.tag != null) {
                BlockEntity blockentity = p_114671_.getBlockEntity(p_114672_);
                if (blockentity != null) {
                    blockentity.loadWithComponents(this.tag, p_114671_.registryAccess());
                }
            }

            return true;
        }
    }

    private BlockState overwriteWithDefinedProperties(BlockState p_383160_) {
        if (p_383160_ == this.state) {
            return p_383160_;
        } else {
            for (Property<?> property : this.properties) {
                p_383160_ = copyProperty(p_383160_, this.state, property);
            }

            return p_383160_;
        }
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState p_382881_, BlockState p_382920_, Property<T> p_382944_) {
        return p_382881_.setValue(p_382944_, p_382920_.getValue(p_382944_));
    }
}
