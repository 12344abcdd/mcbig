package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import org.slf4j.Logger;

public class SectionStorage<R, P> implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String SECTIONS_TAG = "Sections";
    private final SimpleRegionStorage simpleRegionStorage;
    private final it.unimi.dsi.fastutil.objects.Object2ObjectMap<SectionPos, Optional<R>> storage = new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>();
    private final it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet<ChunkPos> dirtyChunks = new it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet<>();
    private final Codec<P> codec;
    private final Function<R, P> packer;
    private final BiFunction<P, Runnable, R> unpacker;
    private final Function<Runnable, R> factory;
    private final RegistryAccess registryAccess;
    private final ChunkIOErrorReporter errorReporter;
    protected final LevelHeightAccessor levelHeightAccessor;
    private final it.unimi.dsi.fastutil.objects.ObjectSet<ChunkPos> loadedChunks = new it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<>();
    private final it.unimi.dsi.fastutil.objects.Object2ObjectMap<ChunkPos, CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>> pendingLoads = new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>();
    private final Object loadLock = new Object();

    public SectionStorage(
        SimpleRegionStorage p_321814_,
        Codec<P> p_363117_,
        Function<R, P> p_223510_,
        BiFunction<P, Runnable, R> p_360520_,
        Function<Runnable, R> p_223511_,
        RegistryAccess p_223515_,
        ChunkIOErrorReporter p_352357_,
        LevelHeightAccessor p_223516_
    ) {
        this.simpleRegionStorage = p_321814_;
        this.codec = p_363117_;
        this.packer = p_223510_;
        this.unpacker = p_360520_;
        this.factory = p_223511_;
        this.registryAccess = p_223515_;
        this.errorReporter = p_352357_;
        this.levelHeightAccessor = p_223516_;
    }

    protected void tick(BooleanSupplier p_63812_) {
        it.unimi.dsi.fastutil.objects.ObjectIterator<ChunkPos> longiterator = this.dirtyChunks.iterator();

        while (longiterator.hasNext() && p_63812_.getAsBoolean()) {
            ChunkPos chunkpos = longiterator.next();
            longiterator.remove();
            this.writeChunk(chunkpos);
        }

        this.unpackPendingLoads();
    }

    private void unpackPendingLoads() {
        synchronized (this.loadLock) {
            Iterator<it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<ChunkPos, CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>>> iterator = it.unimi.dsi.fastutil.objects.Object2ObjectMaps.fastIterator(this.pendingLoads);

            while (iterator.hasNext()) {
                it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<ChunkPos, CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>> entry = iterator.next();
                Optional<SectionStorage.PackedChunk<P>> optional = entry.getValue().getNow(null);
                if (optional != null) {
                    ChunkPos i = entry.getKey();
                    this.unpackChunk(i, optional.orElse(null));
                    iterator.remove();
                    this.loadedChunks.add(i);
                }
            }
        }
    }

    public void flushAll() {
        if (!this.dirtyChunks.isEmpty()) {
            this.dirtyChunks.forEach(this::writeChunk);
            this.dirtyChunks.clear();
        }
    }

    public boolean hasWork() {
        return !this.dirtyChunks.isEmpty();
    }

    @Nullable
    protected Optional<R> get(SectionPos p_63819_) {
        return this.storage.get(p_63819_);
    }

    protected Optional<R> getOrLoad(SectionPos p_63824_) {
        if (this.outsideStoredRange(p_63824_)) {
            return Optional.empty();
        } else {
            Optional<R> optional = this.get(p_63824_);
            if (optional != null) {
                return optional;
            } else {
                this.unpackChunk(p_63824_.chunk());
                optional = this.get(p_63824_);
                if (optional == null) {
                    throw (IllegalStateException)Util.pauseInIde(new IllegalStateException());
                } else {
                    return optional;
                }
            }
        }
    }

    protected boolean outsideStoredRange(SectionPos p_156631_) {
        int i = SectionPos.sectionToBlockCoord(p_156631_.y()).intValue();
        return this.levelHeightAccessor.isOutsideBuildHeight(i);
    }

    protected R getOrCreate(SectionPos p_63828_) {
        if (this.outsideStoredRange(p_63828_)) {
            throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("sectionPos out of bounds"));
        } else {
            Optional<R> optional = this.getOrLoad(p_63828_);
            if (optional.isPresent()) {
                return optional.get();
            } else {
                R r = this.factory.apply(() -> this.setDirty(p_63828_));
                this.storage.put(p_63828_, Optional.of(r));
                return r;
            }
        }
    }

    public CompletableFuture<?> prefetch(ChunkPos p_364373_) {
        synchronized (this.loadLock) {
            ChunkPos i = p_364373_;
            return this.loadedChunks.contains(i)
                ? CompletableFuture.completedFuture(null)
                : this.pendingLoads.computeIfAbsent(i, p_360206_ -> this.tryRead(p_364373_));
        }
    }

    private void unpackChunk(ChunkPos p_365521_) {
        ChunkPos i = p_365521_;
        CompletableFuture<Optional<SectionStorage.PackedChunk<P>>> completablefuture;
        synchronized (this.loadLock) {
            if (!this.loadedChunks.add(i)) {
                return;
            }

            completablefuture = this.pendingLoads.computeIfAbsent(i, p_360213_ -> this.tryRead(p_365521_));
        }

        this.unpackChunk(p_365521_, completablefuture.join().orElse(null));
        synchronized (this.loadLock) {
            this.pendingLoads.remove(i);
        }
    }

    private CompletableFuture<Optional<SectionStorage.PackedChunk<P>>> tryRead(ChunkPos p_223533_) {
        RegistryOps<Tag> registryops = this.registryAccess.createSerializationContext(NbtOps.INSTANCE);
        return this.simpleRegionStorage
            .read(p_223533_)
            .thenApplyAsync(
                p_360208_ -> p_360208_.map(
                        p_360215_ -> SectionStorage.PackedChunk.parse(this.codec, registryops, p_360215_, this.simpleRegionStorage, this.levelHeightAccessor)
                    ),
                Util.backgroundExecutor().forName("parseSection")
            )
            .exceptionally(p_382775_ -> {
                if (p_382775_ instanceof CompletionException) {
                    p_382775_ = p_382775_.getCause();
                }

                if (p_382775_ instanceof IOException ioexception) {
                    LOGGER.error("Error reading chunk {} data from disk", p_223533_, ioexception);
                    this.errorReporter.reportChunkLoadFailure(ioexception, this.simpleRegionStorage.storageInfo(), p_223533_);
                    return Optional.empty();
                } else {
                    throw new CompletionException(p_382775_);
                }
            });
    }

    private void unpackChunk(ChunkPos p_365130_, @Nullable SectionStorage.PackedChunk<P> p_361845_) {
        if (p_361845_ == null) {
            for (int i = this.levelHeightAccessor.getMinSectionY(); i <= this.levelHeightAccessor.getMaxSectionY(); i++) {
                this.storage.put(getKey(p_365130_, i), Optional.empty());
            }
        } else {
            boolean flag = p_361845_.versionChanged();

            for (int j = this.levelHeightAccessor.getMinSectionY(); j <= this.levelHeightAccessor.getMaxSectionY(); j++) {
                SectionPos k = getKey(p_365130_, j);
                Optional<R> optional = Optional.ofNullable(p_361845_.sectionsByY.get(j))
                    .map(p_360210_ -> this.unpacker.apply((P)p_360210_, () -> this.setDirty(k)));
                this.storage.put(k, optional);
                optional.ifPresent(p_223523_ -> {
                    this.onSectionLoad(k);
                    if (flag) {
                        this.setDirty(k);
                    }
                });
            }
        }
    }

    private void writeChunk(ChunkPos p_361540_) {
        RegistryOps<Tag> registryops = this.registryAccess.createSerializationContext(NbtOps.INSTANCE);
        Dynamic<Tag> dynamic = this.writeChunk(p_361540_, registryops);
        Tag tag = dynamic.getValue();
        if (tag instanceof CompoundTag) {
            this.simpleRegionStorage.write(p_361540_, (CompoundTag)tag).exceptionally(p_351992_ -> {
                this.errorReporter.reportChunkSaveFailure(p_351992_, this.simpleRegionStorage.storageInfo(), p_361540_);
                return null;
            });
        } else {
            LOGGER.error("Expected compound tag, got {}", tag);
        }
    }

    private <T> Dynamic<T> writeChunk(ChunkPos p_362535_, DynamicOps<T> p_360921_) {
        Map<T, T> map = Maps.newHashMap();

        for (int i = this.levelHeightAccessor.getMinSectionY(); i <= this.levelHeightAccessor.getMaxSectionY(); i++) {
            SectionPos j = getKey(p_362535_, i);
            Optional<R> optional = this.storage.get(j);
            if (optional != null && !optional.isEmpty()) {
                DataResult<T> dataresult = this.codec.encodeStart(p_360921_, this.packer.apply(optional.get()));
                String s = Integer.toString(i);
                dataresult.resultOrPartial(LOGGER::error).ifPresent(p_223531_ -> map.put(p_360921_.createString(s), (T)p_223531_));
            }
        }

        return new Dynamic<>(
            p_360921_,
            p_360921_.createMap(
                ImmutableMap.of(
                    p_360921_.createString("Sections"),
                    p_360921_.createMap(map),
                    p_360921_.createString("DataVersion"),
                    p_360921_.createInt(SharedConstants.getCurrentVersion().getDataVersion().getVersion())
                )
            )
        );
    }

    private static SectionPos getKey(ChunkPos p_156628_, int p_156629_) {
        return SectionPos.of(p_156628_, p_156629_);
    }

    protected void onSectionLoad(SectionPos p_63813_) {
    }

    protected void setDirty(SectionPos p_63788_) {
        Optional<R> optional = this.storage.get(p_63788_);
        if (optional != null && !optional.isEmpty()) {
            this.dirtyChunks.add(p_63788_.chunk());
        } else {
            LOGGER.warn("No data for position: {}", p_63788_);
        }
    }

    static int getVersion(Dynamic<?> p_63806_) {
        return p_63806_.get("DataVersion").asInt(1945);
    }

    public void flush(ChunkPos p_63797_) {
        if (this.dirtyChunks.remove(p_63797_)) {
            this.writeChunk(p_63797_);
        }
    }

    @Override
    public void close() throws IOException {
        this.simpleRegionStorage.close();
    }

    static record PackedChunk<T>(Int2ObjectMap<T> sectionsByY, boolean versionChanged) {
        public static <T> SectionStorage.PackedChunk<T> parse(
            Codec<T> p_365233_, DynamicOps<Tag> p_363840_, Tag p_364375_, SimpleRegionStorage p_362076_, LevelHeightAccessor p_362314_
        ) {
            Dynamic<Tag> dynamic = new Dynamic<>(p_363840_, p_364375_);
            int i = SectionStorage.getVersion(dynamic);
            int j = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
            boolean flag = i != j;
            Dynamic<Tag> dynamic1 = p_362076_.upgradeChunkTag(dynamic, i);
            OptionalDynamic<Tag> optionaldynamic = dynamic1.get("Sections");
            Int2ObjectMap<T> int2objectmap = new Int2ObjectOpenHashMap<>();

            for (int k = p_362314_.getMinSectionY(); k <= p_362314_.getMaxSectionY(); k++) {
                Optional<T> optional = optionaldynamic.get(Integer.toString(k))
                    .result()
                    .flatMap(p_361362_ -> p_365233_.parse((Dynamic<Tag>)p_361362_).resultOrPartial(SectionStorage.LOGGER::error));
                if (optional.isPresent()) {
                    int2objectmap.put(k, optional.get());
                }
            }

            return new SectionStorage.PackedChunk<>(int2objectmap, flag);
        }
    }
}
