package net.minecraft.world.level;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public interface LevelReader extends BlockAndTintGetter, CollisionGetter, SignalGetter, BiomeManager.NoiseBiomeSource {
    @Nullable
    ChunkAccess getChunk(me.alphamode.mcbig.math.BigInteger p_46823_, me.alphamode.mcbig.math.BigInteger p_46824_, ChunkStatus p_46825_, boolean p_46826_);

    @Deprecated
    boolean hasChunk(me.alphamode.mcbig.math.BigInteger p_46838_, me.alphamode.mcbig.math.BigInteger p_46839_);

    default boolean hasChunk(int p_46838_, int p_46839_) {
        return hasChunk(me.alphamode.mcbig.math.BigInteger.val(p_46838_), me.alphamode.mcbig.math.BigInteger.val(p_46839_));
    }

    int getHeight(Heightmap.Types p_46827_, me.alphamode.mcbig.math.BigInteger p_46828_, me.alphamode.mcbig.math.BigInteger p_46829_);

    default int getHeight(Heightmap.Types p_46827_, int p_46828_, int p_46829_) {
        return getHeight(p_46827_, me.alphamode.mcbig.math.BigInteger.val(p_46828_), me.alphamode.mcbig.math.BigInteger.val(p_46829_));
    }

    int getSkyDarken();

    BiomeManager getBiomeManager();

    default Holder<Biome> getBiome(BlockPos p_204167_) {
        return this.getBiomeManager().getBiome(p_204167_);
    }

    default Stream<BlockState> getBlockStatesIfLoaded(AABB p_46848_) {
        me.alphamode.mcbig.math.BigInteger i = Mth.bigFloor(p_46848_.minX);
        me.alphamode.mcbig.math.BigInteger j = Mth.bigFloor(p_46848_.maxX);
        int k = Mth.floor(p_46848_.minY);
        int l = Mth.floor(p_46848_.maxY);
        me.alphamode.mcbig.math.BigInteger i1 = Mth.bigFloor(p_46848_.minZ);
        me.alphamode.mcbig.math.BigInteger j1 = Mth.bigFloor(p_46848_.maxZ);
        return this.hasChunksAt(i, k, i1, j, l, j1) ? this.getBlockStates(p_46848_) : Stream.empty();
    }

    @Override
    default int getBlockTint(BlockPos p_46836_, ColorResolver p_46837_) {
        return p_46837_.getColor(this.getBiome(p_46836_).value(), (double)p_46836_.getBigX().doubleValue(), (double)p_46836_.getBigZ().doubleValue());
    }

    @Override
    default Holder<Biome> getNoiseBiome(me.alphamode.mcbig.math.BigInteger p_204163_, me.alphamode.mcbig.math.BigInteger p_204164_, me.alphamode.mcbig.math.BigInteger p_204165_) {
        ChunkAccess chunkaccess = this.getChunk(QuartPos.toSection(p_204163_), QuartPos.toSection(p_204165_), ChunkStatus.BIOMES, false);
        return chunkaccess != null ? chunkaccess.getNoiseBiome(p_204163_, p_204164_, p_204165_) : this.getUncachedNoiseBiome(p_204163_, p_204164_, p_204165_);
    }

    Holder<Biome> getUncachedNoiseBiome(me.alphamode.mcbig.math.BigInteger p_204159_, me.alphamode.mcbig.math.BigInteger p_204160_, me.alphamode.mcbig.math.BigInteger p_204161_);

    boolean isClientSide();

    int getSeaLevel();

    DimensionType dimensionType();

    @Override
    default int getMinY() {
        return this.dimensionType().minY();
    }

    @Override
    default int getHeight() {
        return this.dimensionType().height();
    }

    default BlockPos getHeightmapPos(Heightmap.Types p_46830_, BlockPos p_46831_) {
        return new BlockPos(p_46831_.getBigX(), this.getHeight(p_46830_, p_46831_.getBigX(), p_46831_.getBigZ()), p_46831_.getBigZ());
    }

    default boolean isEmptyBlock(BlockPos p_46860_) {
        return this.getBlockState(p_46860_).isAir();
    }

    default boolean canSeeSkyFromBelowWater(BlockPos p_46862_) {
        if (p_46862_.getBigY().compareTo(this.getSeaLevel()) >= 0) {
            return this.canSeeSky(p_46862_);
        } else {
            BlockPos blockpos = new BlockPos(p_46862_.getBigX(), me.alphamode.mcbig.math.BigInteger.val(this.getSeaLevel()), p_46862_.getBigZ());
            if (!this.canSeeSky(blockpos)) {
                return false;
            } else {
                for(BlockPos blockpos1 = blockpos.below(); blockpos1.getBigY().compareTo(p_46862_.getBigY()) > 0; blockpos1 = blockpos1.below()) {
                    BlockState blockstate = this.getBlockState(blockpos1);
                    if (blockstate.getLightBlock() > 0 && !blockstate.liquid()) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    default float getPathfindingCostFromLightLevels(BlockPos p_220420_) {
        return this.getLightLevelDependentMagicValue(p_220420_) - 0.5F;
    }

    @Deprecated
    default float getLightLevelDependentMagicValue(BlockPos p_220418_) {
        float f = (float)this.getMaxLocalRawBrightness(p_220418_) / 15.0F;
        float f1 = f / (4.0F - 3.0F * f);
        return Mth.lerp(this.dimensionType().ambientLight(), f1, 1.0F);
    }

    default ChunkAccess getChunk(BlockPos p_46866_) {
        return this.getChunk(SectionPos.blockToSectionCoord(p_46866_.getBigX()), SectionPos.blockToSectionCoord(p_46866_.getBigZ()));
    }

    default ChunkAccess getChunk(me.alphamode.mcbig.math.BigInteger p_46807_, me.alphamode.mcbig.math.BigInteger p_46808_) {
        return this.getChunk(p_46807_, p_46808_, ChunkStatus.FULL, true);
    }

    default ChunkAccess getChunk(int p_46807_, int p_46808_) {
        return this.getChunk(me.alphamode.mcbig.math.BigInteger.val(p_46807_), me.alphamode.mcbig.math.BigInteger.val(p_46808_));
    }

    default ChunkAccess getChunk(me.alphamode.mcbig.math.BigInteger p_46820_, me.alphamode.mcbig.math.BigInteger p_46821_, ChunkStatus p_46822_) {
        return this.getChunk(p_46820_, p_46821_, p_46822_, true);
    }

    @Nullable
    @Override
    default BlockGetter getChunkForCollisions(me.alphamode.mcbig.math.BigInteger p_46845_, me.alphamode.mcbig.math.BigInteger p_46846_) {
        return this.getChunk(p_46845_, p_46846_, ChunkStatus.EMPTY, false);
    }

    default boolean isWaterAt(BlockPos p_46802_) {
        return this.getFluidState(p_46802_).is(FluidTags.WATER);
    }

    default boolean containsAnyLiquid(AABB p_46856_) {
        me.alphamode.mcbig.math.BigInteger i = Mth.bigFloor(p_46856_.minX);
        me.alphamode.mcbig.math.BigInteger j = Mth.bigCeil(p_46856_.maxX);
        me.alphamode.mcbig.math.BigInteger k = Mth.bigFloor(p_46856_.minY);
        me.alphamode.mcbig.math.BigInteger l = Mth.bigCeil(p_46856_.maxY);
        me.alphamode.mcbig.math.BigInteger i1 = Mth.bigFloor(p_46856_.minZ);
        me.alphamode.mcbig.math.BigInteger j1 = Mth.bigCeil(p_46856_.maxZ);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for(me.alphamode.mcbig.math.BigInteger k1 = i; k1.compareTo(j) < 0; k1 = k1.add()) {
            for(me.alphamode.mcbig.math.BigInteger l1 = k; l1.compareTo(l) < 0; l1 = l1.add()) {
                for(me.alphamode.mcbig.math.BigInteger i2 = i1; i2.compareTo(j1) < 0; i2 = i2.add()) {
                    BlockState blockstate = this.getBlockState(blockpos$mutableblockpos.set(k1, l1, i2));
                    if (!blockstate.getFluidState().isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    default int getMaxLocalRawBrightness(BlockPos p_46804_) {
        return this.getMaxLocalRawBrightness(p_46804_, this.getSkyDarken());
    }

    default int getMaxLocalRawBrightness(BlockPos p_46850_, int p_46851_) {
        return/* p_46850_.getX() >= -30000000 && p_46850_.getZ() >= -30000000 && p_46850_.getX() < 30000000 && p_46850_.getZ() < 30000000
            ? */this.getRawBrightness(p_46850_, p_46851_)/*
            : 15*/;
    }

    @Deprecated
    default boolean hasChunkAt(me.alphamode.mcbig.math.BigInteger p_151578_, me.alphamode.mcbig.math.BigInteger p_151579_) {
        return this.hasChunk(SectionPos.blockToSectionCoord(p_151578_), SectionPos.blockToSectionCoord(p_151579_));
    }

    @Deprecated
    default boolean hasChunkAt(BlockPos p_46806_) {
        return this.hasChunkAt(p_46806_.getBigX(), p_46806_.getBigZ());
    }

    @Deprecated
    default boolean hasChunksAt(BlockPos p_46833_, BlockPos p_46834_) {
        return this.hasChunksAt(p_46833_.getBigX(), p_46833_.getY(), p_46833_.getBigZ(), p_46834_.getBigX(), p_46834_.getY(), p_46834_.getBigZ());
    }

    @Deprecated
    default boolean hasChunksAt(me.alphamode.mcbig.math.BigInteger p_46813_, int p_46814_, me.alphamode.mcbig.math.BigInteger p_46815_, me.alphamode.mcbig.math.BigInteger p_46816_, int p_46817_, me.alphamode.mcbig.math.BigInteger p_46818_) {
        return p_46817_ >= this.getMinY() && p_46814_ <= this.getMaxY() ? this.hasChunksAt(p_46813_, p_46815_, p_46816_, p_46818_) : false;
    }

    @Deprecated
    default boolean hasChunksAt(me.alphamode.mcbig.math.BigInteger p_151573_, me.alphamode.mcbig.math.BigInteger p_151574_, me.alphamode.mcbig.math.BigInteger p_151575_, me.alphamode.mcbig.math.BigInteger p_151576_) {
        me.alphamode.mcbig.math.BigInteger i = SectionPos.blockToSectionCoord(p_151573_);
        me.alphamode.mcbig.math.BigInteger j = SectionPos.blockToSectionCoord(p_151575_);
        me.alphamode.mcbig.math.BigInteger k = SectionPos.blockToSectionCoord(p_151574_);
        me.alphamode.mcbig.math.BigInteger l = SectionPos.blockToSectionCoord(p_151576_);

        for(me.alphamode.mcbig.math.BigInteger i1 = i; i1.compareTo(j) <= 0; i1 = i1.add()) {
            for(me.alphamode.mcbig.math.BigInteger j1 = k; j1.compareTo(l) <= 0; j1 = j1.add()) {
                if (!this.hasChunk(i1, j1)) {
                    return false;
                }
            }
        }

        return true;
    }

    RegistryAccess registryAccess();

    FeatureFlagSet enabledFeatures();

    default <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<? extends T>> p_249578_) {
        Registry<T> registry = this.registryAccess().lookupOrThrow(p_249578_);
        return registry.filterFeatures(this.enabledFeatures());
    }
}
