package net.minecraft.world.level.levelgen.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class StructureFeatureIndexSavedData extends SavedData {
    private static final String TAG_REMAINING_INDEXES = "Remaining";
    private static final String TAG_All_INDEXES = "All";
    private final it.unimi.dsi.fastutil.objects.ObjectSet<net.minecraft.world.level.ChunkPos> all;
    private final it.unimi.dsi.fastutil.objects.ObjectSet<net.minecraft.world.level.ChunkPos> remaining;

    public static SavedData.Factory<StructureFeatureIndexSavedData> factory() {
        return new SavedData.Factory<>(
            StructureFeatureIndexSavedData::new, StructureFeatureIndexSavedData::load, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES
        );
    }

    private StructureFeatureIndexSavedData(it.unimi.dsi.fastutil.objects.ObjectSet<net.minecraft.world.level.ChunkPos> p_163532_, it.unimi.dsi.fastutil.objects.ObjectSet<net.minecraft.world.level.ChunkPos> p_163533_) {
        this.all = p_163532_;
        this.remaining = p_163533_;
    }

    public StructureFeatureIndexSavedData() {
        this(new it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<>(), new it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<>());
    }

    public static StructureFeatureIndexSavedData load(CompoundTag p_163535_, HolderLookup.Provider p_323754_) {
        return new StructureFeatureIndexSavedData(net.minecraft.nbt.NbtUtils.readChunks(p_163535_, TAG_All_INDEXES, it.unimi.dsi.fastutil.objects.ObjectOpenHashSet::new), net.minecraft.nbt.NbtUtils.readChunks(p_163535_, TAG_REMAINING_INDEXES, it.unimi.dsi.fastutil.objects.ObjectOpenHashSet::new));
    }

    @Override
    public CompoundTag save(CompoundTag p_73372_, HolderLookup.Provider p_323794_) {
        net.minecraft.nbt.NbtUtils.writeChunks(p_73372_, TAG_All_INDEXES, this.all);
        net.minecraft.nbt.NbtUtils.writeChunks(p_73372_, TAG_REMAINING_INDEXES, this.remaining);
        return p_73372_;
    }

    public void addIndex(net.minecraft.world.level.ChunkPos p_73366_) {
        this.all.add(p_73366_);
        this.remaining.add(p_73366_);
        this.setDirty();
    }

    public boolean hasStartIndex(net.minecraft.world.level.ChunkPos p_73370_) {
        return this.all.contains(p_73370_);
    }

    public boolean hasUnhandledIndex(net.minecraft.world.level.ChunkPos p_73374_) {
        return this.remaining.contains(p_73374_);
    }

    public void removeIndex(net.minecraft.world.level.ChunkPos p_73376_) {
        if (this.remaining.remove(p_73376_)) {
            this.setDirty();
        }
    }

    public it.unimi.dsi.fastutil.objects.ObjectSet<net.minecraft.world.level.ChunkPos> getAll() {
        return this.all;
    }
}
