package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SectionOcclusionGraph {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MINIMUM_ADVANCED_CULLING_DISTANCE = 60;
    private static final double CEILED_SECTION_DIAGONAL = Math.ceil(Math.sqrt(3.0) * 16.0);
    private boolean needsFullUpdate = true;
    @Nullable
    private Future<?> fullUpdateTask;
    @Nullable
    private ViewArea viewArea;
    private final AtomicReference<SectionOcclusionGraph.GraphState> currentGraph = new AtomicReference<>();
    private final AtomicReference<SectionOcclusionGraph.GraphEvents> nextGraphEvents = new AtomicReference<>();
    private final AtomicBoolean needsFrustumUpdate = new AtomicBoolean(false);

    public void waitAndReset(@Nullable ViewArea p_294431_) {
        if (this.fullUpdateTask != null) {
            try {
                this.fullUpdateTask.get();
                this.fullUpdateTask = null;
            } catch (Exception exception) {
                LOGGER.warn("Full update failed", (Throwable)exception);
            }
        }

        this.viewArea = p_294431_;
        if (p_294431_ != null) {
            this.currentGraph.set(new SectionOcclusionGraph.GraphState(p_294431_));
            this.invalidate();
        } else {
            this.currentGraph.set(null);
        }
    }

    public void invalidate() {
        this.needsFullUpdate = true;
    }

    public void addSectionsInFrustum(
        Frustum p_294180_, List<SectionRenderDispatcher.RenderSection> p_296160_, List<SectionRenderDispatcher.RenderSection> p_371751_
    ) {
        this.currentGraph.get().storage().sectionTree.visitNodes((p_370300_, p_370301_, p_370302_, p_370303_) -> {
            SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = p_370300_.getSection();
            if (sectionrenderdispatcher$rendersection != null) {
                p_296160_.add(sectionrenderdispatcher$rendersection);
                if (p_370303_) {
                    p_371751_.add(sectionrenderdispatcher$rendersection);
                }
            }
        }, p_294180_, 32);
    }

    public boolean consumeFrustumUpdate() {
        return this.needsFrustumUpdate.compareAndSet(true, false);
    }

    public void onChunkReadyToRender(ChunkPos p_294122_) {
        SectionOcclusionGraph.GraphEvents sectionocclusiongraph$graphevents = this.nextGraphEvents.get();
        if (sectionocclusiongraph$graphevents != null) {
            this.addNeighbors(sectionocclusiongraph$graphevents, p_294122_);
        }

        SectionOcclusionGraph.GraphEvents sectionocclusiongraph$graphevents1 = this.currentGraph.get().events;
        if (sectionocclusiongraph$graphevents1 != sectionocclusiongraph$graphevents) {
            this.addNeighbors(sectionocclusiongraph$graphevents1, p_294122_);
        }
    }

    public void schedulePropagationFrom(SectionRenderDispatcher.RenderSection p_295414_) {
        SectionOcclusionGraph.GraphEvents sectionocclusiongraph$graphevents = this.nextGraphEvents.get();
        if (sectionocclusiongraph$graphevents != null) {
            sectionocclusiongraph$graphevents.sectionsToPropagateFrom.add(p_295414_);
        }

        SectionOcclusionGraph.GraphEvents sectionocclusiongraph$graphevents1 = this.currentGraph.get().events;
        if (sectionocclusiongraph$graphevents1 != sectionocclusiongraph$graphevents) {
            sectionocclusiongraph$graphevents1.sectionsToPropagateFrom.add(p_295414_);
        }
    }

    public void update(boolean p_294298_, Camera p_294529_, Frustum p_294426_, List<SectionRenderDispatcher.RenderSection> p_295280_, it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<SectionPos> p_366410_) {
        me.alphamode.mcbig.core.BigVec3 vec3 = p_294529_.getPosition();
        if (this.needsFullUpdate && (this.fullUpdateTask == null || this.fullUpdateTask.isDone())) {
            this.scheduleFullUpdate(p_294298_, p_294529_, vec3, p_366410_);
        }

        this.runPartialUpdate(p_294298_, p_294426_, p_295280_, vec3, p_366410_);
    }

    private void scheduleFullUpdate(boolean p_294514_, Camera p_295663_, me.alphamode.mcbig.core.BigVec3 p_295096_, it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<SectionPos> p_366772_) {
        this.needsFullUpdate = false;
        it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<SectionPos> longopenhashset = p_366772_.clone();
        this.fullUpdateTask = CompletableFuture.runAsync(() -> {
            SectionOcclusionGraph.GraphState sectionocclusiongraph$graphstate = new SectionOcclusionGraph.GraphState(this.viewArea);
            this.nextGraphEvents.set(sectionocclusiongraph$graphstate.events);
            Queue<SectionOcclusionGraph.Node> queue = Queues.newArrayDeque();
            this.initializeQueueForFullUpdate(p_295663_, queue);
            queue.forEach(p_295724_ -> sectionocclusiongraph$graphstate.storage.sectionToNodeMap.put(p_295724_.section, p_295724_));
            this.runUpdates(sectionocclusiongraph$graphstate.storage, p_295096_, queue, p_294514_, p_294678_ -> {
            }, longopenhashset);
            this.currentGraph.set(sectionocclusiongraph$graphstate);
            this.nextGraphEvents.set(null);
            this.needsFrustumUpdate.set(true);
        }, Util.backgroundExecutor());
    }

    private void runPartialUpdate(
        boolean p_294795_, Frustum p_294341_, List<SectionRenderDispatcher.RenderSection> p_294796_, me.alphamode.mcbig.core.BigVec3 p_295915_, it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<SectionPos> p_366753_
    ) {
        SectionOcclusionGraph.GraphState sectionocclusiongraph$graphstate = this.currentGraph.get();
        this.queueSectionsWithNewNeighbors(sectionocclusiongraph$graphstate);
        if (!sectionocclusiongraph$graphstate.events.sectionsToPropagateFrom.isEmpty()) {
            Queue<SectionOcclusionGraph.Node> queue = Queues.newArrayDeque();

            while (!sectionocclusiongraph$graphstate.events.sectionsToPropagateFrom.isEmpty()) {
                SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = sectionocclusiongraph$graphstate.events
                    .sectionsToPropagateFrom
                    .poll();
                SectionOcclusionGraph.Node sectionocclusiongraph$node = sectionocclusiongraph$graphstate.storage
                    .sectionToNodeMap
                    .get(sectionrenderdispatcher$rendersection);
                if (sectionocclusiongraph$node != null && sectionocclusiongraph$node.section == sectionrenderdispatcher$rendersection) {
                    queue.add(sectionocclusiongraph$node);
                }
            }

            Frustum frustum = LevelRenderer.offsetFrustum(p_294341_);
            Consumer<SectionRenderDispatcher.RenderSection> consumer = p_370305_ -> {
                if (frustum.isVisible(p_370305_.getBoundingBox())) {
                    this.needsFrustumUpdate.set(true);
                }
            };
            this.runUpdates(sectionocclusiongraph$graphstate.storage, p_295915_, queue, p_294795_, consumer, p_366753_);
        }
    }

    private void queueSectionsWithNewNeighbors(SectionOcclusionGraph.GraphState p_296471_) {
        it.unimi.dsi.fastutil.objects.ObjectIterator<ChunkPos> longiterator = p_296471_.events.chunksWhichReceivedNeighbors.iterator();

        while (longiterator.hasNext()) {
            ChunkPos i = longiterator.next();
            List<SectionRenderDispatcher.RenderSection> list = p_296471_.storage.chunksWaitingForNeighbors.get(i);
            if (list != null && list.get(0).hasAllNeighbors()) {
                p_296471_.events.sectionsToPropagateFrom.addAll(list);
                p_296471_.storage.chunksWaitingForNeighbors.remove(i);
            }
        }

        p_296471_.events.chunksWhichReceivedNeighbors.clear();
    }

    private void addNeighbors(SectionOcclusionGraph.GraphEvents p_295866_, ChunkPos p_295968_) {
        p_295866_.chunksWhichReceivedNeighbors.add(new ChunkPos(p_295968_.x.subtract(), p_295968_.z));
        p_295866_.chunksWhichReceivedNeighbors.add(new ChunkPos(p_295968_.x, p_295968_.z.subtract()));
        p_295866_.chunksWhichReceivedNeighbors.add(new ChunkPos(p_295968_.x.add(), p_295968_.z));
        p_295866_.chunksWhichReceivedNeighbors.add(new ChunkPos(p_295968_.x, p_295968_.z.add()));
        p_295866_.chunksWhichReceivedNeighbors.add(new ChunkPos(p_295968_.x.subtract(), p_295968_.z.subtract()));
        p_295866_.chunksWhichReceivedNeighbors.add(new ChunkPos(p_295968_.x.subtract(), p_295968_.z.add()));
        p_295866_.chunksWhichReceivedNeighbors.add(new ChunkPos(p_295968_.x.add(), p_295968_.z.subtract()));
        p_295866_.chunksWhichReceivedNeighbors.add(new ChunkPos(p_295968_.x.add(), p_295968_.z.add()));
    }

    private void initializeQueueForFullUpdate(Camera p_295148_, Queue<SectionOcclusionGraph.Node> p_294801_) {
        BlockPos blockpos = p_295148_.getBlockPosition();
        SectionPos i = SectionPos.of(blockpos);
        int j = i.y().intValue();
        SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = this.viewArea.getRenderSection(i);
        if (sectionrenderdispatcher$rendersection == null) {
            LevelHeightAccessor levelheightaccessor = this.viewArea.getLevelHeightAccessor();
            boolean flag = j < levelheightaccessor.getMinSectionY();
            int k = flag ? levelheightaccessor.getMinSectionY() : levelheightaccessor.getMaxSectionY();
            int l = this.viewArea.getViewDistance();
            List<SectionOcclusionGraph.Node> list = Lists.newArrayList();
            me.alphamode.mcbig.math.BigInteger i1 = i.x();
            me.alphamode.mcbig.math.BigInteger j1 = i.z();

            for (int k1 = -l; k1 <= l; k1++) {
                for (int l1 = -l; l1 <= l; l1++) {
                    SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection1 = this.viewArea
                        .getRenderSection(SectionPos.of(i1.add(k1), k, j1.add(l1)));
                    if (sectionrenderdispatcher$rendersection1 != null && this.isInViewDistance(i, sectionrenderdispatcher$rendersection1.getSectionNode())) {
                        Direction direction = flag ? Direction.UP : Direction.DOWN;
                        SectionOcclusionGraph.Node sectionocclusiongraph$node = new SectionOcclusionGraph.Node(
                            sectionrenderdispatcher$rendersection1, direction, 0
                        );
                        sectionocclusiongraph$node.setDirections(sectionocclusiongraph$node.directions, direction);
                        if (k1 > 0) {
                            sectionocclusiongraph$node.setDirections(sectionocclusiongraph$node.directions, Direction.EAST);
                        } else if (k1 < 0) {
                            sectionocclusiongraph$node.setDirections(sectionocclusiongraph$node.directions, Direction.WEST);
                        }

                        if (l1 > 0) {
                            sectionocclusiongraph$node.setDirections(sectionocclusiongraph$node.directions, Direction.SOUTH);
                        } else if (l1 < 0) {
                            sectionocclusiongraph$node.setDirections(sectionocclusiongraph$node.directions, Direction.NORTH);
                        }

                        list.add(sectionocclusiongraph$node);
                    }
                }
            }

            list.sort(Comparator.comparingDouble(p_294459_ -> blockpos.distSqr(p_294459_.section.getOrigin().offset(8, 8, 8))));
            p_294801_.addAll(list);
        } else {
            p_294801_.add(new SectionOcclusionGraph.Node(sectionrenderdispatcher$rendersection, null, 0));
        }
    }

    private void runUpdates(
        SectionOcclusionGraph.GraphStorage p_295507_,
        me.alphamode.mcbig.core.BigVec3 p_294343_,
        Queue<SectionOcclusionGraph.Node> p_295598_,
        boolean p_295668_,
        Consumer<SectionRenderDispatcher.RenderSection> p_295393_,
        it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<SectionPos> p_366468_
    ) {
        int i = 16;
        BlockPos blockpos = new BlockPos(Mth.bigFloor(p_294343_.x.divide(me.alphamode.mcbig.core.BigConstants.SIXTEEN)).multiply(me.alphamode.mcbig.core.BigConstants.Ints.SIXTEEN), Mth.bigFloor(p_294343_.y / 16.0).multiply(me.alphamode.mcbig.core.BigConstants.Ints.SIXTEEN), Mth.bigFloor(p_294343_.z.divide(me.alphamode.mcbig.core.BigConstants.SIXTEEN)).multiply(me.alphamode.mcbig.core.BigConstants.Ints.SIXTEEN));
        SectionPos j = SectionPos.of(blockpos);
        BlockPos blockpos1 = blockpos.offset(8, 8, 8);

        while (!p_295598_.isEmpty()) {
            SectionOcclusionGraph.Node sectionocclusiongraph$node = p_295598_.poll();
            SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = sectionocclusiongraph$node.section;
            if (!p_366468_.contains(sectionocclusiongraph$node.section.getSectionNode())) {
                if (p_295507_.sectionTree.add(sectionocclusiongraph$node.section)) {
                    p_295393_.accept(sectionocclusiongraph$node.section);
                }
            } else {
                sectionocclusiongraph$node.section
                    .compiled
                    .compareAndSet(SectionRenderDispatcher.CompiledSection.UNCOMPILED, SectionRenderDispatcher.CompiledSection.EMPTY);
            }

            boolean flag = Math.abs(sectionrenderdispatcher$rendersection.getOrigin().getX() - blockpos.getX()) > 60
                || Math.abs(sectionrenderdispatcher$rendersection.getOrigin().getY() - blockpos.getY()) > 60
                || Math.abs(sectionrenderdispatcher$rendersection.getOrigin().getZ() - blockpos.getZ()) > 60;

            for (Direction direction : DIRECTIONS) {
                SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection1 = this.getRelativeFrom(
                    j, sectionrenderdispatcher$rendersection, direction
                );
                if (sectionrenderdispatcher$rendersection1 != null && (!p_295668_ || !sectionocclusiongraph$node.hasDirection(direction.getOpposite()))) {
                    if (p_295668_ && sectionocclusiongraph$node.hasSourceDirections()) {
                        SectionRenderDispatcher.CompiledSection sectionrenderdispatcher$compiledsection = sectionrenderdispatcher$rendersection.getCompiled();
                        boolean flag1 = false;

                        for (int k = 0; k < DIRECTIONS.length; k++) {
                            if (sectionocclusiongraph$node.hasSourceDirection(k)
                                && sectionrenderdispatcher$compiledsection.facesCanSeeEachother(DIRECTIONS[k].getOpposite(), direction)) {
                                flag1 = true;
                                break;
                            }
                        }

                        if (!flag1) {
                            continue;
                        }
                    }

                    if (p_295668_ && flag) {
                        BlockPos blockpos2 = sectionrenderdispatcher$rendersection1.getOrigin();
                        BlockPos blockpos3 = blockpos2.offset(
                            (direction.getAxis() == Direction.Axis.X ? blockpos1.getBigX().compareTo(blockpos2.getBigX()) <= 0 : blockpos1.getBigX().compareTo(blockpos2.getBigX()) >= 0) ? 0 : 16,
                            (direction.getAxis() == Direction.Axis.Y ? blockpos1.getBigY().compareTo(blockpos2.getBigY()) <= 0 : blockpos1.getBigY().compareTo(blockpos2.getBigY()) >= 0) ? 0 : 16,
                            (direction.getAxis() == Direction.Axis.Z ? blockpos1.getBigZ().compareTo(blockpos2.getBigZ()) <= 0 : blockpos1.getBigZ().compareTo(blockpos2.getBigZ()) >= 0) ? 0 : 16
                        );
                        me.alphamode.mcbig.core.BigVec3 vec31 = new me.alphamode.mcbig.core.BigVec3(blockpos3.getBigX().toBigDecimal(), (double)blockpos3.getBigY().doubleValue(), blockpos3.getBigZ().toBigDecimal());
                        me.alphamode.mcbig.core.BigVec3 vec3 = p_294343_.subtract(vec31).normalize().scale(CEILED_SECTION_DIAGONAL);
                        boolean flag2 = true;

                        while(p_294343_.subtract(vec31).lengthSqr().compareTo(me.alphamode.mcbig.core.BigConstants.OCCLUSION) > 0) {
                            vec31 = vec31.add(vec3);
                            LevelHeightAccessor levelheightaccessor = this.viewArea.getLevelHeightAccessor();
                            if (vec31.y > (double)levelheightaccessor.getMaxY() || vec31.y < (double)levelheightaccessor.getMinY()) {
                                break;
                            }

                            SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection2 = this.viewArea
                                .getRenderSectionAt(BlockPos.containing(vec31.x, vec31.y, vec31.z));
                            if (sectionrenderdispatcher$rendersection2 == null
                                || p_295507_.sectionToNodeMap.get(sectionrenderdispatcher$rendersection2) == null) {
                                flag2 = false;
                                break;
                            }
                        }

                        if (!flag2) {
                            continue;
                        }
                    }

                    SectionOcclusionGraph.Node sectionocclusiongraph$node1 = p_295507_.sectionToNodeMap.get(sectionrenderdispatcher$rendersection1);
                    if (sectionocclusiongraph$node1 != null) {
                        sectionocclusiongraph$node1.addSourceDirection(direction);
                    } else {
                        SectionOcclusionGraph.Node sectionocclusiongraph$node2 = new SectionOcclusionGraph.Node(
                            sectionrenderdispatcher$rendersection1, direction, sectionocclusiongraph$node.step + 1
                        );
                        sectionocclusiongraph$node2.setDirections(sectionocclusiongraph$node.directions, direction);
                        if (sectionrenderdispatcher$rendersection1.hasAllNeighbors()) {
                            p_295598_.add(sectionocclusiongraph$node2);
                            p_295507_.sectionToNodeMap.put(sectionrenderdispatcher$rendersection1, sectionocclusiongraph$node2);
                        } else if (this.isInViewDistance(j, sectionrenderdispatcher$rendersection1.getSectionNode())) {
                            p_295507_.sectionToNodeMap.put(sectionrenderdispatcher$rendersection1, sectionocclusiongraph$node2);
                            p_295507_.chunksWaitingForNeighbors
                                .computeIfAbsent(new ChunkPos(sectionrenderdispatcher$rendersection1.getOrigin()), p_294377_ -> new ArrayList())
                                .add(sectionrenderdispatcher$rendersection1);
                        }
                    }
                }
            }
        }
    }

    private boolean isInViewDistance(SectionPos p_366544_, SectionPos p_366485_) {
        return ChunkTrackingView.isInViewDistance(
            p_366544_.x(), p_366544_.z(), this.viewArea.getViewDistance(), p_366485_.x(), p_366485_.z()
        );
    }

    @Nullable
    private SectionRenderDispatcher.RenderSection getRelativeFrom(SectionPos p_366669_, SectionRenderDispatcher.RenderSection p_295211_, Direction p_294491_) {
        SectionPos i = p_295211_.getNeighborSectionNode(p_294491_);
        if (!this.isInViewDistance(p_366669_, i)) {
            return null;
        } else {
            return Mth.abs(p_366669_.y().intValue() - i.y().intValue()) > this.viewArea.getViewDistance() ? null : this.viewArea.getRenderSection(i);
        }
    }

    @Nullable
    @VisibleForDebug
    public SectionOcclusionGraph.Node getNode(SectionRenderDispatcher.RenderSection p_296364_) {
        return this.currentGraph.get().storage.sectionToNodeMap.get(p_296364_);
    }

    public Octree getOctree() {
        return this.currentGraph.get().storage.sectionTree;
    }

    static record GraphEvents(it.unimi.dsi.fastutil.objects.ObjectSet<ChunkPos> chunksWhichReceivedNeighbors, BlockingQueue<SectionRenderDispatcher.RenderSection> sectionsToPropagateFrom) {
        GraphEvents() {
            this(new it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<>(), new LinkedBlockingQueue<>());
        }
    }

    static record GraphState(SectionOcclusionGraph.GraphStorage storage, SectionOcclusionGraph.GraphEvents events) {
        GraphState(ViewArea p_366764_) {
            this(new SectionOcclusionGraph.GraphStorage(p_366764_), new SectionOcclusionGraph.GraphEvents());
        }
    }

    static class GraphStorage {
        public final SectionOcclusionGraph.SectionToNodeMap sectionToNodeMap;
        public final Octree sectionTree;
        public final it.unimi.dsi.fastutil.objects.Object2ObjectMap<ChunkPos, List<SectionRenderDispatcher.RenderSection>> chunksWaitingForNeighbors;

        public GraphStorage(ViewArea p_366828_) {
            this.sectionToNodeMap = new SectionOcclusionGraph.SectionToNodeMap(p_366828_.sections.length);
            this.sectionTree = new Octree(p_366828_.getCameraSectionPos(), p_366828_.getViewDistance(), p_366828_.sectionGridSizeY, p_366828_.level.getMinY());
            this.chunksWaitingForNeighbors = new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>();
        }
    }

    @VisibleForDebug
    public static class Node {
        @VisibleForDebug
        protected final SectionRenderDispatcher.RenderSection section;
        private byte sourceDirections;
        byte directions;
        @VisibleForDebug
        public final int step;

        Node(SectionRenderDispatcher.RenderSection p_295110_, @Nullable Direction p_295920_, int p_295951_) {
            this.section = p_295110_;
            if (p_295920_ != null) {
                this.addSourceDirection(p_295920_);
            }

            this.step = p_295951_;
        }

        void setDirections(byte p_295029_, Direction p_296033_) {
            this.directions = (byte)(this.directions | p_295029_ | 1 << p_296033_.ordinal());
        }

        boolean hasDirection(Direction p_294996_) {
            return (this.directions & 1 << p_294996_.ordinal()) > 0;
        }

        void addSourceDirection(Direction p_295444_) {
            this.sourceDirections = (byte)(this.sourceDirections | this.sourceDirections | 1 << p_295444_.ordinal());
        }

        @VisibleForDebug
        public boolean hasSourceDirection(int p_294302_) {
            return (this.sourceDirections & 1 << p_294302_) > 0;
        }

        boolean hasSourceDirections() {
            return this.sourceDirections != 0;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(this.section.getSectionNode().asLong());
        }

        @Override
        public boolean equals(Object p_295498_) {
            return !(p_295498_ instanceof SectionOcclusionGraph.Node sectionocclusiongraph$node)
                ? false
                : this.section.getSectionNode() == sectionocclusiongraph$node.section.getSectionNode();
        }
    }

    static class SectionToNodeMap {
        private final SectionOcclusionGraph.Node[] nodes;

        SectionToNodeMap(int p_296136_) {
            this.nodes = new SectionOcclusionGraph.Node[p_296136_];
        }

        public void put(SectionRenderDispatcher.RenderSection p_295644_, SectionOcclusionGraph.Node p_295953_) {
            this.nodes[p_295644_.index] = p_295953_;
        }

        @Nullable
        public SectionOcclusionGraph.Node get(SectionRenderDispatcher.RenderSection p_295721_) {
            int i = p_295721_.index;
            return i >= 0 && i < this.nodes.length ? this.nodes[i] : null;
        }
    }
}
