package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderChunkRegion implements BlockAndTintGetter {
    public static final int RADIUS = 1;
    public static final int SIZE = 3;
    private final me.alphamode.mcbig.math.BigInteger minChunkX;
    private final me.alphamode.mcbig.math.BigInteger minChunkZ;
    protected final RenderChunk[] chunks;
    protected final Level level;

    RenderChunkRegion(Level p_200456_, me.alphamode.mcbig.math.BigInteger p_200457_, me.alphamode.mcbig.math.BigInteger p_200458_, RenderChunk[] p_350680_) {
        this.level = p_200456_;
        this.minChunkX = p_200457_;
        this.minChunkZ = p_200458_;
        this.chunks = p_350680_;
    }

    @Override
    public BlockState getBlockState(BlockPos p_112947_) {
        return this.getChunk(SectionPos.blockToSectionCoord(p_112947_.getBigX()), SectionPos.blockToSectionCoord(p_112947_.getBigZ())).getBlockState(p_112947_);
    }

    @Override
    public FluidState getFluidState(BlockPos p_112943_) {
        return this.getChunk(SectionPos.blockToSectionCoord(p_112943_.getBigX()), SectionPos.blockToSectionCoord(p_112943_.getBigZ()))
            .getBlockState(p_112943_)
            .getFluidState();
    }

    @Override
    public float getShade(Direction p_112940_, boolean p_112941_) {
        return this.level.getShade(p_112940_, p_112941_);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos p_112945_) {
        return this.getChunk(SectionPos.blockToSectionCoord(p_112945_.getBigX()), SectionPos.blockToSectionCoord(p_112945_.getBigZ())).getBlockEntity(p_112945_);
    }

    private RenderChunk getChunk(me.alphamode.mcbig.math.BigInteger p_350795_, me.alphamode.mcbig.math.BigInteger p_350558_) {
        return this.chunks[index(this.minChunkX, this.minChunkZ, p_350795_, p_350558_)];
    }

    @Override
    public int getBlockTint(BlockPos p_112937_, ColorResolver p_112938_) {
        return this.level.getBlockTint(p_112937_, p_112938_);
    }

    @Override
    public int getMinY() {
        return this.level.getMinY();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    public static int index(me.alphamode.mcbig.math.BigInteger p_350911_, me.alphamode.mcbig.math.BigInteger p_350842_, me.alphamode.mcbig.math.BigInteger p_350891_, me.alphamode.mcbig.math.BigInteger p_350833_) {
        return p_350891_.subtract(p_350911_).intValue() + (p_350833_.subtract(p_350842_)).intValue() * 3;
    }
}
