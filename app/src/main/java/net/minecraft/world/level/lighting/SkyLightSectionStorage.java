package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class SkyLightSectionStorage extends LayerLightSectionStorage<SkyLightSectionStorage.SkyDataLayerStorageMap> {
    protected SkyLightSectionStorage(LightChunkGetter p_75868_) {
        super(
            LightLayer.SKY,
            p_75868_,
            new SkyLightSectionStorage.SkyDataLayerStorageMap(new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>(), new it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap<>(), Integer.MAX_VALUE)
        );
    }

    @Override
    protected int getLightValue(BlockPos p_75880_) {
        return this.getLightValue(p_75880_, false);
    }

    protected int getLightValue(BlockPos p_164458_, boolean p_164459_) {
        SectionPos i = SectionPos.of(p_164458_);
        int j = i.getY();
        SkyLightSectionStorage.SkyDataLayerStorageMap skylightsectionstorage$skydatalayerstoragemap = p_164459_
            ? this.updatingSectionData
            : this.visibleSectionData;
        int k = skylightsectionstorage$skydatalayerstoragemap.topSections.getInt(SectionPos.getZeroNode(i.x(), i.z()));
        if (k != skylightsectionstorage$skydatalayerstoragemap.currentLowestY && j < k) {
            DataLayer datalayer = this.getDataLayer(skylightsectionstorage$skydatalayerstoragemap, i);
            if (datalayer == null) {
                for(p_164458_ = new BlockPos(p_164458_.getBigX(), 0, p_164458_.getBigZ());
                    datalayer == null;
                    datalayer = this.getDataLayer(skylightsectionstorage$skydatalayerstoragemap, i)
                ) {
                    if (++j >= k) {
                        return 15;
                    }

                    i = i.relative(Direction.UP);
                }
            }

            return datalayer.get(
                SectionPos.sectionRelative(p_164458_.getBigX()),
                SectionPos.sectionRelative(p_164458_.getBigY()),
                SectionPos.sectionRelative(p_164458_.getBigZ())
            );
        } else {
            return p_164459_ && !this.lightOnInSection(i) ? 0 : 15;
        }
    }

    @Override
    protected void onNodeAdded(SectionPos p_75885_) {
        int i = p_75885_.y().intValue();
        if (this.updatingSectionData.currentLowestY > i) {
            this.updatingSectionData.currentLowestY = i;
            this.updatingSectionData.topSections.defaultReturnValue(this.updatingSectionData.currentLowestY);
        }

        SectionPos j = SectionPos.getZeroNode(p_75885_.x(), p_75885_.z());
        int k = this.updatingSectionData.topSections.getInt(j);
        if (k < i + 1) {
            this.updatingSectionData.topSections.put(j, i + 1);
        }
    }

    @Override
    protected void onNodeRemoved(SectionPos p_75887_) {
        SectionPos i = SectionPos.getZeroNode(p_75887_.x(), p_75887_.z());
        int j = p_75887_.getY();
        if (this.updatingSectionData.topSections.getInt(i) == j + 1) {
            SectionPos k;
            for(k = p_75887_; !this.storingLightForSection(k) && this.hasLightDataAtOrBelow(j); k = k.relative(Direction.DOWN)) {
                j--;
            }

            if (this.storingLightForSection(k)) {
                this.updatingSectionData.topSections.put(i, j + 1);
            } else {
                this.updatingSectionData.topSections.removeInt(i);
            }
        }
    }

    @Override
    protected DataLayer createDataLayer(SectionPos p_75883_) {
        DataLayer datalayer = this.queuedSections.get(p_75883_);
        if (datalayer != null) {
            return datalayer;
        } else {
            int i = this.updatingSectionData.topSections.getInt(SectionPos.getZeroNode(p_75883_.x(), p_75883_.z()));
            if (i != this.updatingSectionData.currentLowestY && p_75883_.getY() < i) {
                SectionPos j = p_75883_.relative(Direction.UP);

                DataLayer datalayer1;
                while ((datalayer1 = this.getDataLayer(j, true)) == null) {
                    j = j.relative(Direction.UP);
                }

                return repeatFirstLayer(datalayer1);
            } else {
                return this.lightOnInSection(p_75883_) ? new DataLayer(15) : new DataLayer();
            }
        }
    }

    private static DataLayer repeatFirstLayer(DataLayer p_182513_) {
        if (p_182513_.isDefinitelyHomogenous()) {
            return p_182513_.copy();
        } else {
            byte[] abyte = p_182513_.getData();
            byte[] abyte1 = new byte[2048];

            for (int i = 0; i < 16; i++) {
                System.arraycopy(abyte, 0, abyte1, i * 128, 128);
            }

            return new DataLayer(abyte1);
        }
    }

    protected boolean hasLightDataAtOrBelow(int p_278270_) {
        return p_278270_ >= this.updatingSectionData.currentLowestY;
    }

    protected boolean isAboveData(SectionPos p_75891_) {
        SectionPos i = SectionPos.getZeroNode(p_75891_.x(), p_75891_.z());
        int j = this.updatingSectionData.topSections.getInt(i);
        return j == this.updatingSectionData.currentLowestY || p_75891_.y().longValue() >= j;
    }

    protected int getTopSectionY(SectionPos p_285094_) {
        return this.updatingSectionData.topSections.getInt(p_285094_);
    }

    protected int getBottomSectionY() {
        return this.updatingSectionData.currentLowestY;
    }

    protected static final class SkyDataLayerStorageMap extends DataLayerStorageMap<SkyLightSectionStorage.SkyDataLayerStorageMap> {
        int currentLowestY;
        final it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap<SectionPos> topSections;

        public SkyDataLayerStorageMap(it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<SectionPos, DataLayer> p_75903_, it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap<SectionPos> p_75904_, int p_75905_) {
            super(p_75903_);
            this.topSections = p_75904_;
            p_75904_.defaultReturnValue(p_75905_);
            this.currentLowestY = p_75905_;
        }

        public SkyLightSectionStorage.SkyDataLayerStorageMap copy() {
            return new SkyLightSectionStorage.SkyDataLayerStorageMap(this.map.clone(), this.topSections.clone(), this.currentLowestY);
        }
    }
}
