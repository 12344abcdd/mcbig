package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class BlockLightSectionStorage extends LayerLightSectionStorage<BlockLightSectionStorage.BlockDataLayerStorageMap> {
    protected BlockLightSectionStorage(LightChunkGetter p_75511_) {
        super(LightLayer.BLOCK, p_75511_, new BlockLightSectionStorage.BlockDataLayerStorageMap(new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>()));
    }

    @Override
    protected int getLightValue(BlockPos p_75513_) {
        SectionPos i = SectionPos.of(p_75513_);
        DataLayer datalayer = this.getDataLayer(i, false);
        return datalayer == null
            ? 0
            : datalayer.get(
                SectionPos.sectionRelative(p_75513_.getBigX()),
                SectionPos.sectionRelative(p_75513_.getBigY()),
                SectionPos.sectionRelative(p_75513_.getBigZ())
            );
    }

    protected static final class BlockDataLayerStorageMap extends DataLayerStorageMap<BlockLightSectionStorage.BlockDataLayerStorageMap> {
        public BlockDataLayerStorageMap(it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<SectionPos, DataLayer> p_75515_) {
            super(p_75515_);
        }

        public BlockLightSectionStorage.BlockDataLayerStorageMap copy() {
            return new BlockLightSectionStorage.BlockDataLayerStorageMap(this.map.clone());
        }
    }
}
