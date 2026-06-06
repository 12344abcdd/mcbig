package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class ForcedChunksSavedData extends SavedData {
    public static final String FILE_ID = "chunks";
    private static final String TAG_FORCED = "Forced";
    private final it.unimi.dsi.fastutil.objects.ObjectSet<net.minecraft.world.level.ChunkPos> chunks;

    public static SavedData.Factory<ForcedChunksSavedData> factory() {
        return new SavedData.Factory<>(ForcedChunksSavedData::new, ForcedChunksSavedData::load, DataFixTypes.SAVED_DATA_FORCED_CHUNKS);
    }

    private ForcedChunksSavedData(it.unimi.dsi.fastutil.objects.ObjectSet<net.minecraft.world.level.ChunkPos> p_151482_) {
        this.chunks = p_151482_;
    }

    public ForcedChunksSavedData() {
        this(new it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<>());
    }

    public static ForcedChunksSavedData load(CompoundTag p_151484_, HolderLookup.Provider p_323940_) {
        it.unimi.dsi.fastutil.objects.ObjectSet<net.minecraft.world.level.ChunkPos> chunks = new it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<>();
        var chunksList = p_151484_.getList(TAG_FORCED, net.minecraft.nbt.Tag.TAG_COMPOUND);
        chunksList.forEach(tag -> {
            CompoundTag chunk = (CompoundTag) tag;
            chunks.add(new net.minecraft.world.level.ChunkPos(new me.alphamode.mcbig.math.BigInteger(chunk.getByteArray("x")), new me.alphamode.mcbig.math.BigInteger(chunk.getByteArray("x"))));
        });
        return new ForcedChunksSavedData(chunks);
    }

    @Override
    public CompoundTag save(CompoundTag p_46120_, HolderLookup.Provider p_324573_) {
        var chunks = new net.minecraft.nbt.ListTag();
        this.chunks.forEach(chunkPos -> {
            CompoundTag chunk = new CompoundTag();
            chunk.putByteArray("x", chunkPos.x.toByteArray());
            chunk.putByteArray("z", chunkPos.z.toByteArray());
        });
        p_46120_.put(TAG_FORCED, chunks);
        return p_46120_;
    }

    public it.unimi.dsi.fastutil.objects.ObjectSet<net.minecraft.world.level.ChunkPos> getChunks() {
        return this.chunks;
    }
}
