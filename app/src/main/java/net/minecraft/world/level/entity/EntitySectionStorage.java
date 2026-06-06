package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Objects;
import java.util.Spliterators;
import java.util.PrimitiveIterator.OfLong;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

public class EntitySectionStorage<T extends EntityAccess> {
    public static final int CHONKY_ENTITY_SEARCH_GRACE = 2;
    public static final int MAX_NON_CHONKY_ENTITY_SIZE = 4;
    private final Class<T> entityClass;
    private final it.unimi.dsi.fastutil.objects.Object2ObjectFunction<ChunkPos, Visibility> intialSectionVisibility;
    private final it.unimi.dsi.fastutil.objects.Object2ObjectMap<SectionPos, EntitySection<T>> sections = new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>();
    private final it.unimi.dsi.fastutil.objects.ObjectSortedSet<SectionPos> sectionIds = new it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet<>();

    public EntitySectionStorage(Class<T> p_156855_, it.unimi.dsi.fastutil.objects.Object2ObjectFunction<ChunkPos, Visibility> p_156856_) {
        this.entityClass = p_156855_;
        this.intialSectionVisibility = p_156856_;
    }

    public void forEachAccessibleNonEmptySection(AABB p_188363_, AbortableIterationConsumer<EntitySection<T>> p_261588_) {
        me.alphamode.mcbig.math.BigInteger i = SectionPos.posToSectionCoord(p_188363_.minX - 2.0);
        me.alphamode.mcbig.math.BigInteger j = SectionPos.posToSectionCoord(p_188363_.minY - 4.0);
        me.alphamode.mcbig.math.BigInteger k = SectionPos.posToSectionCoord(p_188363_.minZ - 2.0);
        me.alphamode.mcbig.math.BigInteger l = SectionPos.posToSectionCoord(p_188363_.maxX + 2.0);
        me.alphamode.mcbig.math.BigInteger i1 = SectionPos.posToSectionCoord(p_188363_.maxY + 0.0);
        me.alphamode.mcbig.math.BigInteger j1 = SectionPos.posToSectionCoord(p_188363_.maxZ + 2.0);

        for (me.alphamode.mcbig.math.BigInteger k1 = i; k1.compareTo(l) <= 0; k1 = k1.add()) {
            SectionPos l1 = SectionPos.of(k1, me.alphamode.mcbig.math.BigInteger.ZERO, me.alphamode.mcbig.math.BigInteger.ZERO);
            SectionPos i2 = SectionPos.of(k1, -1, me.alphamode.mcbig.math.BigInteger.val(-1));
            it.unimi.dsi.fastutil.objects.ObjectIterator<SectionPos> longiterator = this.sectionIds.subSet(l1, i2.offset(1L)).iterator();

            while (longiterator.hasNext()) {
                SectionPos j2 = longiterator.next();
                me.alphamode.mcbig.math.BigInteger k2 = j2.y();
                me.alphamode.mcbig.math.BigInteger l2 = j2.z();
                if (k2.compareTo(j) >= 0 && k2.compareTo(i1) <= 0 && l2.compareTo(k) >= 0 && l2.compareTo(j1) <= 0) {
                    EntitySection<T> entitysection = this.sections.get(j2);
                    if (entitysection != null
                        && !entitysection.isEmpty()
                        && entitysection.getStatus().isAccessible()
                        && p_261588_.accept(entitysection).shouldAbort()) {
                        return;
                    }
                }
            }
        }
    }

    public java.util.stream.Stream<SectionPos> getExistingSectionPositionsInChunk(ChunkPos p_156862_) {
        me.alphamode.mcbig.math.BigInteger i = p_156862_.x;
        me.alphamode.mcbig.math.BigInteger j = p_156862_.z;
        it.unimi.dsi.fastutil.objects.ObjectSortedSet<SectionPos> longsortedset = this.getChunkSections(i, j);
        if (longsortedset.isEmpty()) {
            return java.util.stream.Stream.empty();
        } else {
            it.unimi.dsi.fastutil.objects.ObjectIterator<SectionPos> oflong = longsortedset.iterator();
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(oflong, 1301), false);
        }
    }

    private it.unimi.dsi.fastutil.objects.ObjectSortedSet<SectionPos> getChunkSections(me.alphamode.mcbig.math.BigInteger p_156859_, me.alphamode.mcbig.math.BigInteger p_156860_) {
        SectionPos i = SectionPos.of(p_156859_, 0, p_156860_);
        SectionPos j = SectionPos.of(p_156859_, -1, p_156860_);
        return this.sectionIds.subSet(i, j.offset(1L));
    }

    public Stream<EntitySection<T>> getExistingSectionsInChunk(ChunkPos p_156889_) {
        return this.getExistingSectionPositionsInChunk(p_156889_).map(this.sections::get).filter(Objects::nonNull);
    }

    private static ChunkPos getChunkKeyFromSectionKey(SectionPos p_156900_) {
        return p_156900_.chunk();
    }

    public EntitySection<T> getOrCreateSection(SectionPos p_156894_) {
        return this.sections.computeIfAbsent(p_156894_, this::createSection);
    }

    @Nullable
    public EntitySection<T> getSection(SectionPos p_156896_) {
        return this.sections.get(p_156896_);
    }

    private EntitySection<T> createSection(SectionPos p_156902_) {
        ChunkPos i = getChunkKeyFromSectionKey(p_156902_);
        Visibility visibility = this.intialSectionVisibility.get(i);
        this.sectionIds.add(p_156902_);
        return new EntitySection<>(this.entityClass, visibility);
    }

    public it.unimi.dsi.fastutil.objects.ObjectSet<ChunkPos> getAllChunksWithExistingSections() {
        it.unimi.dsi.fastutil.objects.ObjectSet<ChunkPos> longset = new it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<>();
        this.sections.keySet().forEach(p_156886_ -> longset.add(getChunkKeyFromSectionKey(p_156886_)));
        return longset;
    }

    public void getEntities(AABB p_261820_, AbortableIterationConsumer<T> p_261992_) {
        this.forEachAccessibleNonEmptySection(p_261820_, p_261459_ -> p_261459_.getEntities(p_261820_, p_261992_));
    }

    public <U extends T> void getEntities(EntityTypeTest<T, U> p_261630_, AABB p_261843_, AbortableIterationConsumer<U> p_261742_) {
        this.forEachAccessibleNonEmptySection(p_261843_, p_261463_ -> p_261463_.getEntities(p_261630_, p_261843_, p_261742_));
    }

    public void remove(SectionPos p_156898_) {
        this.sections.remove(p_156898_);
        this.sectionIds.remove(p_156898_);
    }

    @VisibleForDebug
    public int count() {
        return this.sectionIds.size();
    }
}
