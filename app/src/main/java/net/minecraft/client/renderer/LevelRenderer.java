package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.slf4j.Logger;

public class LevelRenderer implements ResourceManagerReloadListener, AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TRANSPARENCY_POST_CHAIN_ID = ResourceLocation.withDefaultNamespace("transparency");
    private static final ResourceLocation ENTITY_OUTLINE_POST_CHAIN_ID = ResourceLocation.withDefaultNamespace("entity_outline");
    public static final int SECTION_SIZE = 16;
    public static final int HALF_SECTION_SIZE = 8;
    public static final int NEARBY_SECTION_DISTANCE_IN_BLOCKS = 32;
    private static final int MINIMUM_TRANSPARENT_SORT_COUNT = 15;
    private final Minecraft minecraft;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final RenderBuffers renderBuffers;
    private final SkyRenderer skyRenderer = new SkyRenderer();
    private final CloudRenderer cloudRenderer = new CloudRenderer();
    private final WorldBorderRenderer worldBorderRenderer = new WorldBorderRenderer();
    private final WeatherEffectRenderer weatherEffectRenderer = new WeatherEffectRenderer();
    @Nullable
    private ClientLevel level;
    private final SectionOcclusionGraph sectionOcclusionGraph = new SectionOcclusionGraph();
    private final ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections = new ObjectArrayList<>(10000);
    private final ObjectArrayList<SectionRenderDispatcher.RenderSection> nearbyVisibleSections = new ObjectArrayList<>(50);
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    @Nullable
    private ViewArea viewArea;
    private int ticks;
    private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<>();
    private final it.unimi.dsi.fastutil.objects.Object2ObjectMap<BlockPos, SortedSet<BlockDestructionProgress>> destructionProgress = new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>();
    @Nullable
    private RenderTarget entityOutlineTarget;
    private final LevelTargetBundle targets = new LevelTargetBundle();
    private me.alphamode.mcbig.math.BigInteger lastCameraSectionX = me.alphamode.mcbig.math.BigInteger.val(Integer.MIN_VALUE);
    private me.alphamode.mcbig.math.BigInteger lastCameraSectionY = me.alphamode.mcbig.math.BigInteger.val(Integer.MIN_VALUE);
    private me.alphamode.mcbig.math.BigInteger lastCameraSectionZ = me.alphamode.mcbig.math.BigInteger.val(Integer.MIN_VALUE);
    private me.alphamode.mcbig.math.BigDecimal prevCamX = me.alphamode.mcbig.math.BigDecimal.val(Double.MIN_VALUE);
    private double prevCamY = Double.MIN_VALUE;
    private me.alphamode.mcbig.math.BigDecimal prevCamZ = me.alphamode.mcbig.math.BigDecimal.val(Double.MIN_VALUE);
    private double prevCamRotX = Double.MIN_VALUE;
    private double prevCamRotY = Double.MIN_VALUE;
    @Nullable
    private SectionRenderDispatcher sectionRenderDispatcher;
    private int lastViewDistance = -1;
    private final List<Entity> visibleEntities = new ArrayList<>();
    private int visibleEntityCount;
    private Frustum cullingFrustum;
    private boolean captureFrustum;
    @Nullable
    private Frustum capturedFrustum;
    @Nullable
    private BlockPos lastTranslucentSortBlockPos;
    private int translucencyResortIterationIndex;

    public LevelRenderer(Minecraft p_234245_, EntityRenderDispatcher p_234246_, BlockEntityRenderDispatcher p_234247_, RenderBuffers p_234248_) {
        this.minecraft = p_234245_;
        this.entityRenderDispatcher = p_234246_;
        this.blockEntityRenderDispatcher = p_234247_;
        this.renderBuffers = p_234248_;
    }

    public void tickParticles(Camera p_363405_) {
        this.weatherEffectRenderer.tickRainParticles(this.minecraft.level, p_363405_, this.ticks, this.minecraft.options.particles().get());
    }

    @Override
    public void close() {
        if (this.entityOutlineTarget != null) {
            this.entityOutlineTarget.destroyBuffers();
        }

        this.skyRenderer.close();
        this.cloudRenderer.close();
    }

    @Override
    public void onResourceManagerReload(ResourceManager p_109513_) {
        this.initOutline();
    }

    public void initOutline() {
        if (this.entityOutlineTarget != null) {
            this.entityOutlineTarget.destroyBuffers();
        }

        this.entityOutlineTarget = new TextureTarget(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), true);
        this.entityOutlineTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
    }

    @Nullable
    private PostChain getTransparencyChain() {
        if (!Minecraft.useShaderTransparency()) {
            return null;
        } else {
            PostChain postchain = this.minecraft.getShaderManager().getPostChain(TRANSPARENCY_POST_CHAIN_ID, LevelTargetBundle.SORTING_TARGETS);
            if (postchain == null) {
                this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
                this.minecraft.options.save();
            }

            return postchain;
        }
    }

    public void doEntityOutline() {
        if (this.shouldShowEntityOutlines()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE
            );
            this.entityOutlineTarget.blitAndBlendToScreen(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    protected boolean shouldShowEntityOutlines() {
        return !this.minecraft.gameRenderer.isPanoramicMode() && this.entityOutlineTarget != null && this.minecraft.player != null;
    }

    public void setLevel(@Nullable ClientLevel p_109702_) {
        this.lastCameraSectionX = me.alphamode.mcbig.math.BigInteger.val(Integer.MIN_VALUE);
        this.lastCameraSectionY = me.alphamode.mcbig.math.BigInteger.val(Integer.MIN_VALUE);
        this.lastCameraSectionZ = me.alphamode.mcbig.math.BigInteger.val(Integer.MIN_VALUE);
        this.entityRenderDispatcher.setLevel(p_109702_);
        this.level = p_109702_;
        if (p_109702_ != null) {
            this.allChanged();
        } else {
            if (this.viewArea != null) {
                this.viewArea.releaseAllBuffers();
                this.viewArea = null;
            }

            if (this.sectionRenderDispatcher != null) {
                this.sectionRenderDispatcher.dispose();
            }

            this.sectionRenderDispatcher = null;
            this.globalBlockEntities.clear();
            this.sectionOcclusionGraph.waitAndReset(null);
            this.clearVisibleSections();
        }
    }

    private void clearVisibleSections() {
        this.visibleSections.clear();
        this.nearbyVisibleSections.clear();
    }

    public void allChanged() {
        if (this.level != null) {
            this.level.clearTintCaches();
            if (this.sectionRenderDispatcher == null) {
                this.sectionRenderDispatcher = new SectionRenderDispatcher(
                    this.level,
                    this,
                    Util.backgroundExecutor(),
                    this.renderBuffers,
                    this.minecraft.getBlockRenderer(),
                    this.minecraft.getBlockEntityRenderDispatcher()
                );
            } else {
                this.sectionRenderDispatcher.setLevel(this.level);
            }

            this.cloudRenderer.markForRebuild();
            ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
            this.lastViewDistance = this.minecraft.options.getEffectiveRenderDistance();
            if (this.viewArea != null) {
                this.viewArea.releaseAllBuffers();
            }

            this.sectionRenderDispatcher.blockUntilClear();
            synchronized (this.globalBlockEntities) {
                this.globalBlockEntities.clear();
            }

            this.viewArea = new ViewArea(this.sectionRenderDispatcher, this.level, this.minecraft.options.getEffectiveRenderDistance(), this);
            this.sectionOcclusionGraph.waitAndReset(this.viewArea);
            this.clearVisibleSections();
            Camera camera = this.minecraft.gameRenderer.getMainCamera();
            this.viewArea.repositionCamera(SectionPos.of(camera.getPosition()));
        }
    }

    public void resize(int p_109488_, int p_109489_) {
        this.needsUpdate();
        if (this.entityOutlineTarget != null) {
            this.entityOutlineTarget.resize(p_109488_, p_109489_);
        }
    }

    public String getSectionStatistics() {
        int i = this.viewArea.sections.length;
        int j = this.countRenderedSections();
        return String.format(
            Locale.ROOT,
            "C: %d/%d %sD: %d, %s",
            j,
            i,
            this.minecraft.smartCull ? "(s) " : "",
            this.lastViewDistance,
            this.sectionRenderDispatcher == null ? "null" : this.sectionRenderDispatcher.getStats()
        );
    }

    public SectionRenderDispatcher getSectionRenderDispatcher() {
        return this.sectionRenderDispatcher;
    }

    public double getTotalSections() {
        return (double)this.viewArea.sections.length;
    }

    public double getLastViewDistance() {
        return (double)this.lastViewDistance;
    }

    public int countRenderedSections() {
        int i = 0;

        for (SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection : this.visibleSections) {
            if (sectionrenderdispatcher$rendersection.getCompiled().hasRenderableLayers()) {
                i++;
            }
        }

        return i;
    }

    public String getEntityStatistics() {
        return "E: " + this.visibleEntityCount + "/" + this.level.getEntityCount() + ", SD: " + this.level.getServerSimulationDistance();
    }

    private void setupRender(Camera p_194339_, Frustum p_194340_, boolean p_194341_, boolean p_194342_) {
        me.alphamode.mcbig.core.BigVec3 vec3 = p_194339_.getPosition();
        if (this.minecraft.options.getEffectiveRenderDistance() != this.lastViewDistance) {
            this.allChanged();
        }

        ProfilerFiller profilerfiller = Profiler.get();
        profilerfiller.push("camera");
        me.alphamode.mcbig.math.BigInteger i = SectionPos.posToSectionCoord(vec3.bigX());
        me.alphamode.mcbig.math.BigInteger j = SectionPos.posToSectionCoord(vec3.y());
        me.alphamode.mcbig.math.BigInteger k = SectionPos.posToSectionCoord(vec3.bigZ());
        if (!this.lastCameraSectionX.equals(i) || !this.lastCameraSectionY.equals(j) || !this.lastCameraSectionZ.equals(k)) {
            this.lastCameraSectionX = i;
            this.lastCameraSectionY = j;
            this.lastCameraSectionZ = k;
            this.viewArea.repositionCamera(SectionPos.of(vec3));
        }

        this.sectionRenderDispatcher.setCamera(vec3);
        profilerfiller.popPush("cull");
        me.alphamode.mcbig.math.BigDecimal d0 = vec3.x.divide(me.alphamode.mcbig.core.BigConstants.EIGHT).floor();
        double d1 = Math.floor(vec3.y / 8.0);
        me.alphamode.mcbig.math.BigDecimal d2 = vec3.z.divide(me.alphamode.mcbig.core.BigConstants.EIGHT).floor();
        if (!d0.equals(this.prevCamX) || d1 != this.prevCamY || !d2.equals(this.prevCamZ)) {
            this.sectionOcclusionGraph.invalidate();
        }

        this.prevCamX = d0;
        this.prevCamY = d1;
        this.prevCamZ = d2;
        profilerfiller.popPush("update");
        if (!p_194341_) {
            boolean flag = this.minecraft.smartCull;
            if (p_194342_ && this.level.getBlockState(p_194339_.getBlockPosition()).isSolidRender()) {
                flag = false;
            }

            profilerfiller.push("section_occlusion_graph");
            this.sectionOcclusionGraph.update(flag, p_194339_, p_194340_, this.visibleSections, this.level.getChunkSource().getLoadedEmptySections());
            profilerfiller.pop();
            double d3 = Math.floor((double)(p_194339_.getXRot() / 2.0F));
            double d4 = Math.floor((double)(p_194339_.getYRot() / 2.0F));
            if (this.sectionOcclusionGraph.consumeFrustumUpdate() || d3 != this.prevCamRotX || d4 != this.prevCamRotY) {
                this.applyFrustum(offsetFrustum(p_194340_));
                this.prevCamRotX = d3;
                this.prevCamRotY = d4;
            }
        }

        profilerfiller.pop();
    }

    public static Frustum offsetFrustum(Frustum p_296151_) {
        return new Frustum(p_296151_).offsetToFullyIncludeCameraCube(8);
    }

    private void applyFrustum(Frustum p_194355_) {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
        } else {
            Profiler.get().push("apply_frustum");
            this.clearVisibleSections();
            this.sectionOcclusionGraph.addSectionsInFrustum(p_194355_, this.visibleSections, this.nearbyVisibleSections);
            Profiler.get().pop();
        }
    }

    public void addRecentlyCompiledSection(SectionRenderDispatcher.RenderSection p_295462_) {
        this.sectionOcclusionGraph.schedulePropagationFrom(p_295462_);
    }

    public void prepareCullFrustum(me.alphamode.mcbig.core.BigVec3 p_253766_, Matrix4f p_254341_, Matrix4f p_323487_) {
        this.cullingFrustum = new Frustum(p_254341_, p_323487_);
        this.cullingFrustum.prepare(p_253766_.bigX(), p_253766_.y(), p_253766_.bigZ());
    }

    public void renderLevel(
        GraphicsResourceAllocator p_361796_,
        DeltaTracker p_348530_,
        boolean p_109603_,
        Camera p_109604_,
        GameRenderer p_109605_,
        Matrix4f p_254120_,
        Matrix4f p_323920_
    ) {
        float f = p_348530_.getGameTimeDeltaPartialTick(false);
        RenderSystem.setShaderGameTime(this.level.getGameTime(), f);
        this.blockEntityRenderDispatcher.prepare(this.level, p_109604_, this.minecraft.hitResult);
        this.entityRenderDispatcher.prepare(this.level, p_109604_, this.minecraft.crosshairPickEntity);
        final ProfilerFiller profilerfiller = Profiler.get();
        profilerfiller.popPush("light_update_queue");
        this.level.pollLightUpdates();
        profilerfiller.popPush("light_updates");
        this.level.getChunkSource().getLightEngine().runLightUpdates();
        me.alphamode.mcbig.core.BigVec3 vec3 = p_109604_.getPosition();
        me.alphamode.mcbig.math.BigDecimal d0 = vec3.bigX();
        double d1 = vec3.y();
        me.alphamode.mcbig.math.BigDecimal d2 = vec3.bigZ();
        profilerfiller.popPush("culling");
        boolean flag = this.capturedFrustum != null;
        Frustum frustum = flag ? this.capturedFrustum : this.cullingFrustum;
        Profiler.get().popPush("captureFrustum");
        if (this.captureFrustum) {
            this.capturedFrustum = flag ? new Frustum(p_254120_, p_323920_) : frustum;
            this.capturedFrustum.prepare(d0, d1, d2);
            this.captureFrustum = false;
        }

        profilerfiller.popPush("fog");
        float f1 = p_109605_.getRenderDistance();
        boolean flag1 = this.minecraft.level.effects().isFoggyAt(Mth.bigFloor(d0), Mth.floor(d1)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        Vector4f vector4f = FogRenderer.computeFogColor(
            p_109604_, f, this.minecraft.level, this.minecraft.options.getEffectiveRenderDistance(), p_109605_.getDarkenWorldAmount(f)
        );
        FogParameters fogparameters = FogRenderer.setupFog(p_109604_, FogRenderer.FogMode.FOG_TERRAIN, vector4f, f1, flag1, f);
        FogParameters fogparameters1 = FogRenderer.setupFog(p_109604_, FogRenderer.FogMode.FOG_SKY, vector4f, f1, flag1, f);
        profilerfiller.popPush("cullEntities");
        boolean flag2 = this.collectVisibleEntities(p_109604_, frustum, this.visibleEntities);
        this.visibleEntityCount = this.visibleEntities.size();
        profilerfiller.popPush("terrain_setup");
        this.setupRender(p_109604_, frustum, flag, this.minecraft.player.isSpectator());
        profilerfiller.popPush("compile_sections");
        this.compileSections(p_109604_);
        Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
        matrix4fstack.pushMatrix();
        matrix4fstack.mul(p_254120_);
        FrameGraphBuilder framegraphbuilder = new FrameGraphBuilder();
        this.targets.main = framegraphbuilder.importExternal("main", this.minecraft.getMainRenderTarget());
        int i = this.minecraft.getMainRenderTarget().width;
        int j = this.minecraft.getMainRenderTarget().height;
        RenderTargetDescriptor rendertargetdescriptor = new RenderTargetDescriptor(i, j, true);
        PostChain postchain = this.getTransparencyChain();
        if (postchain != null) {
            this.targets.translucent = framegraphbuilder.createInternal("translucent", rendertargetdescriptor);
            this.targets.itemEntity = framegraphbuilder.createInternal("item_entity", rendertargetdescriptor);
            this.targets.particles = framegraphbuilder.createInternal("particles", rendertargetdescriptor);
            this.targets.weather = framegraphbuilder.createInternal("weather", rendertargetdescriptor);
            this.targets.clouds = framegraphbuilder.createInternal("clouds", rendertargetdescriptor);
        }

        if (this.entityOutlineTarget != null) {
            this.targets.entityOutline = framegraphbuilder.importExternal("entity_outline", this.entityOutlineTarget);
        }

        FramePass framepass = framegraphbuilder.addPass("clear");
        this.targets.main = framepass.readsAndWrites(this.targets.main);
        framepass.executes(() -> {
            RenderSystem.clearColor(vector4f.x, vector4f.y, vector4f.z, 0.0F);
            RenderSystem.clear(16640);
        });
        if (!flag1) {
            this.addSkyPass(framegraphbuilder, p_109604_, f, fogparameters1);
        }

        this.addMainPass(framegraphbuilder, frustum, p_109604_, p_254120_, p_323920_, fogparameters, p_109603_, flag2, p_348530_, profilerfiller);
        PostChain postchain1 = this.minecraft.getShaderManager().getPostChain(ENTITY_OUTLINE_POST_CHAIN_ID, LevelTargetBundle.OUTLINE_TARGETS);
        if (flag2 && postchain1 != null) {
            postchain1.addToFrame(framegraphbuilder, i, j, this.targets);
        }

        this.addParticlesPass(framegraphbuilder, p_109604_, f, fogparameters);
        CloudStatus cloudstatus = this.minecraft.options.getCloudsType();
        if (cloudstatus != CloudStatus.OFF) {
            float f2 = this.level.effects().getCloudHeight();
            if (!Float.isNaN(f2)) {
                float f3 = (float)this.ticks + f;
                int k = this.level.getCloudColor(f);
                this.addCloudsPass(framegraphbuilder, p_254120_, p_323920_, cloudstatus, p_109604_.getPosition(), f3, k, f2 + 0.33F);
            }
        }

        this.addWeatherPass(framegraphbuilder, p_109604_.getPosition().toVanilla(), f, fogparameters);
        if (postchain != null) {
            postchain.addToFrame(framegraphbuilder, i, j, this.targets);
        }

        this.addLateDebugPass(framegraphbuilder, vec3.toVanilla(), fogparameters);
        profilerfiller.popPush("framegraph");
        framegraphbuilder.execute(p_361796_, new FrameGraphBuilder.Inspector() {
            @Override
            public void beforeExecutePass(String p_363206_) {
                profilerfiller.push(p_363206_);
            }

            @Override
            public void afterExecutePass(String p_362054_) {
                profilerfiller.pop();
            }
        });
        this.minecraft.getMainRenderTarget().bindWrite(false);
        this.visibleEntities.clear();
        this.targets.clear();
        matrix4fstack.popMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderFog(FogParameters.NO_FOG);
    }

    private void addMainPass(
        FrameGraphBuilder p_361593_,
        Frustum p_366590_,
        Camera p_363453_,
        Matrix4f p_362420_,
        Matrix4f p_361272_,
        FogParameters p_363661_,
        boolean p_363964_,
        boolean p_362137_,
        DeltaTracker p_360931_,
        ProfilerFiller p_362234_
    ) {
        FramePass framepass = p_361593_.addPass("main");
        this.targets.main = framepass.readsAndWrites(this.targets.main);
        if (this.targets.translucent != null) {
            this.targets.translucent = framepass.readsAndWrites(this.targets.translucent);
        }

        if (this.targets.itemEntity != null) {
            this.targets.itemEntity = framepass.readsAndWrites(this.targets.itemEntity);
        }

        if (this.targets.weather != null) {
            this.targets.weather = framepass.readsAndWrites(this.targets.weather);
        }

        if (p_362137_ && this.targets.entityOutline != null) {
            this.targets.entityOutline = framepass.readsAndWrites(this.targets.entityOutline);
        }

        ResourceHandle<RenderTarget> resourcehandle = this.targets.main;
        ResourceHandle<RenderTarget> resourcehandle1 = this.targets.translucent;
        ResourceHandle<RenderTarget> resourcehandle2 = this.targets.itemEntity;
        ResourceHandle<RenderTarget> resourcehandle3 = this.targets.weather;
        ResourceHandle<RenderTarget> resourcehandle4 = this.targets.entityOutline;
        framepass.executes(() -> {
            RenderSystem.setShaderFog(p_363661_);
            float f = p_360931_.getGameTimeDeltaPartialTick(false);
            me.alphamode.mcbig.core.BigVec3 vec3 = p_363453_.getPosition();
            me.alphamode.mcbig.math.BigDecimal d0 = vec3.bigX();
            double d1 = vec3.y();
            me.alphamode.mcbig.math.BigDecimal d2 = vec3.bigZ();
            p_362234_.push("terrain");
            this.renderSectionLayer(RenderType.solid(), d0, d1, d2, p_362420_, p_361272_);
            this.renderSectionLayer(RenderType.cutoutMipped(), d0, d1, d2, p_362420_, p_361272_);
            this.renderSectionLayer(RenderType.cutout(), d0, d1, d2, p_362420_, p_361272_);
            if (this.level.effects().constantAmbientLight()) {
                Lighting.setupNetherLevel();
            } else {
                Lighting.setupLevel();
            }

            if (resourcehandle2 != null) {
                resourcehandle2.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                resourcehandle2.get().clear();
                resourcehandle2.get().copyDepthFrom(this.minecraft.getMainRenderTarget());
                resourcehandle.get().bindWrite(false);
            }

            if (resourcehandle3 != null) {
                resourcehandle3.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                resourcehandle3.get().clear();
            }

            if (this.shouldShowEntityOutlines() && resourcehandle4 != null) {
                resourcehandle4.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                resourcehandle4.get().clear();
                resourcehandle.get().bindWrite(false);
            }

            PoseStack posestack = new PoseStack();
            MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
            MultiBufferSource.BufferSource multibuffersource$buffersource1 = this.renderBuffers.crumblingBufferSource();
            p_362234_.popPush("entities");
            this.renderEntities(posestack, multibuffersource$buffersource, p_363453_, p_360931_, this.visibleEntities);
            multibuffersource$buffersource.endLastBatch();
            this.checkPoseStack(posestack);
            p_362234_.popPush("blockentities");
            this.renderBlockEntities(posestack, multibuffersource$buffersource, multibuffersource$buffersource1, p_363453_, f);
            multibuffersource$buffersource.endLastBatch();
            this.checkPoseStack(posestack);
            multibuffersource$buffersource.endBatch(RenderType.solid());
            multibuffersource$buffersource.endBatch(RenderType.endPortal());
            multibuffersource$buffersource.endBatch(RenderType.endGateway());
            multibuffersource$buffersource.endBatch(Sheets.solidBlockSheet());
            multibuffersource$buffersource.endBatch(Sheets.cutoutBlockSheet());
            multibuffersource$buffersource.endBatch(Sheets.bedSheet());
            multibuffersource$buffersource.endBatch(Sheets.shulkerBoxSheet());
            multibuffersource$buffersource.endBatch(Sheets.signSheet());
            multibuffersource$buffersource.endBatch(Sheets.hangingSignSheet());
            multibuffersource$buffersource.endBatch(Sheets.chestSheet());
            this.renderBuffers.outlineBufferSource().endOutlineBatch();
            if (p_363964_) {
                this.renderBlockOutline(p_363453_, multibuffersource$buffersource, posestack, false);
            }

            p_362234_.popPush("debug");
            this.minecraft.debugRenderer.render(posestack, p_366590_, multibuffersource$buffersource, d0.doubleValue(), d1, d2.doubleValue());
            multibuffersource$buffersource.endLastBatch();
            this.checkPoseStack(posestack);
            multibuffersource$buffersource.endBatch(Sheets.translucentItemSheet());
            multibuffersource$buffersource.endBatch(Sheets.bannerSheet());
            multibuffersource$buffersource.endBatch(Sheets.shieldSheet());
            multibuffersource$buffersource.endBatch(RenderType.armorEntityGlint());
            multibuffersource$buffersource.endBatch(RenderType.glint());
            multibuffersource$buffersource.endBatch(RenderType.glintTranslucent());
            multibuffersource$buffersource.endBatch(RenderType.entityGlint());
            p_362234_.popPush("destroyProgress");
            this.renderBlockDestroyAnimation(posestack, p_363453_, multibuffersource$buffersource1);
            multibuffersource$buffersource1.endBatch();
            this.checkPoseStack(posestack);
            multibuffersource$buffersource.endBatch(RenderType.waterMask());
            multibuffersource$buffersource.endBatch();
            if (resourcehandle1 != null) {
                resourcehandle1.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                resourcehandle1.get().clear();
                resourcehandle1.get().copyDepthFrom(resourcehandle.get());
            }

            p_362234_.popPush("translucent");
            this.renderSectionLayer(RenderType.translucent(), d0, d1, d2, p_362420_, p_361272_);
            p_362234_.popPush("string");
            this.renderSectionLayer(RenderType.tripwire(), d0, d1, d2, p_362420_, p_361272_);
            if (p_363964_) {
                this.renderBlockOutline(p_363453_, multibuffersource$buffersource, posestack, true);
            }

            multibuffersource$buffersource.endBatch();
            p_362234_.pop();
        });
    }

    private void addParticlesPass(FrameGraphBuilder p_363357_, Camera p_365299_, float p_364282_, FogParameters p_362149_) {
        FramePass framepass = p_363357_.addPass("particles");
        if (this.targets.particles != null) {
            this.targets.particles = framepass.readsAndWrites(this.targets.particles);
            framepass.reads(this.targets.main);
        } else {
            this.targets.main = framepass.readsAndWrites(this.targets.main);
        }

        ResourceHandle<RenderTarget> resourcehandle = this.targets.main;
        ResourceHandle<RenderTarget> resourcehandle1 = this.targets.particles;
        framepass.executes(() -> {
            RenderSystem.setShaderFog(p_362149_);
            if (resourcehandle1 != null) {
                resourcehandle1.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                resourcehandle1.get().clear();
                resourcehandle1.get().copyDepthFrom(resourcehandle.get());
            }

            this.minecraft.particleEngine.render(p_365299_, p_364282_, this.renderBuffers.bufferSource());
        });
    }

    private void addCloudsPass(
        FrameGraphBuilder p_361907_,
        Matrix4f p_362864_,
        Matrix4f p_360602_,
        CloudStatus p_364196_,
        me.alphamode.mcbig.core.BigVec3 p_362985_,
        float p_365209_,
        int p_362342_,
        float p_362337_
    ) {
        FramePass framepass = p_361907_.addPass("clouds");
        if (this.targets.clouds != null) {
            this.targets.clouds = framepass.readsAndWrites(this.targets.clouds);
        } else {
            this.targets.main = framepass.readsAndWrites(this.targets.main);
        }

        ResourceHandle<RenderTarget> resourcehandle = this.targets.clouds;
        framepass.executes(() -> {
            if (resourcehandle != null) {
                resourcehandle.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                resourcehandle.get().clear();
            }

            this.cloudRenderer.render(p_362342_, p_364196_, p_362337_, p_362864_, p_360602_, p_362985_, p_365209_);
        });
    }

    private void addWeatherPass(FrameGraphBuilder p_364025_, Vec3 p_360771_, float p_362434_, FogParameters p_360974_) {
        int i = this.minecraft.options.getEffectiveRenderDistance() * 16;
        float f = this.minecraft.gameRenderer.getDepthFar();
        FramePass framepass = p_364025_.addPass("weather");
        if (this.targets.weather != null) {
            this.targets.weather = framepass.readsAndWrites(this.targets.weather);
        } else {
            this.targets.main = framepass.readsAndWrites(this.targets.main);
        }

        framepass.executes(() -> {
            RenderSystem.setShaderFog(p_360974_);
            MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
            this.weatherEffectRenderer.render(this.minecraft.level, multibuffersource$buffersource, this.ticks, p_362434_, p_360771_);
            this.worldBorderRenderer.render(this.level.getWorldBorder(), p_360771_, (double)i, (double)f);
            multibuffersource$buffersource.endBatch();
        });
    }

    private void addLateDebugPass(FrameGraphBuilder p_361973_, Vec3 p_363812_, FogParameters p_362448_) {
        FramePass framepass = p_361973_.addPass("late_debug");
        this.targets.main = framepass.readsAndWrites(this.targets.main);
        if (this.targets.itemEntity != null) {
            this.targets.itemEntity = framepass.readsAndWrites(this.targets.itemEntity);
        }

        ResourceHandle<RenderTarget> resourcehandle = this.targets.main;
        framepass.executes(() -> {
            RenderSystem.setShaderFog(p_362448_);
            resourcehandle.get().bindWrite(false);
            PoseStack posestack = new PoseStack();
            MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
            this.minecraft.debugRenderer.renderAfterTranslucents(posestack, multibuffersource$buffersource, p_363812_.x, p_363812_.y, p_363812_.z);
            multibuffersource$buffersource.endLastBatch();
            this.checkPoseStack(posestack);
        });
    }

    private boolean collectVisibleEntities(Camera p_363510_, Frustum p_361275_, List<Entity> p_363380_) {
        me.alphamode.mcbig.core.BigVec3 vec3 = p_363510_.getPosition();
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();
        boolean flag = false;
        boolean flag1 = this.shouldShowEntityOutlines();
        Entity.setViewScale(
            Mth.clamp((double)this.minecraft.options.getEffectiveRenderDistance() / 8.0, 1.0, 2.5) * this.minecraft.options.entityDistanceScaling().get()
        );

        for (Entity entity : this.level.entitiesForRendering()) {
            if (this.entityRenderDispatcher.shouldRender(entity, p_361275_, d0, d1, d2) || entity.hasIndirectPassenger(this.minecraft.player)) {
                BlockPos blockpos = entity.blockPosition();
                if ((this.level.isOutsideBuildHeight(blockpos.getY()) || this.isSectionCompiled(blockpos))
                    && (
                        entity != p_363510_.getEntity()
                            || p_363510_.isDetached()
                            || p_363510_.getEntity() instanceof LivingEntity && ((LivingEntity)p_363510_.getEntity()).isSleeping()
                    )
                    && (!(entity instanceof LocalPlayer) || p_363510_.getEntity() == entity)) {
                    p_363380_.add(entity);
                    if (flag1 && this.minecraft.shouldEntityAppearGlowing(entity)) {
                        flag = true;
                    }
                }
            }
        }

        return flag;
    }

    private void renderEntities(PoseStack p_363182_, MultiBufferSource.BufferSource p_360990_, Camera p_360717_, DeltaTracker p_362965_, List<Entity> p_362298_) {
        me.alphamode.mcbig.core.BigVec3 vec3 = p_360717_.getPosition();
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();
        TickRateManager tickratemanager = this.minecraft.level.tickRateManager();
        boolean flag = this.shouldShowEntityOutlines();

        for (Entity entity : p_362298_) {
            if (entity.tickCount == 0) {
                entity.xOld = entity.getX();
                entity.yOld = entity.getY();
                entity.zOld = entity.getZ();
            }

            MultiBufferSource multibuffersource;
            if (flag && this.minecraft.shouldEntityAppearGlowing(entity)) {
                OutlineBufferSource outlinebuffersource = this.renderBuffers.outlineBufferSource();
                multibuffersource = outlinebuffersource;
                int i = entity.getTeamColor();
                outlinebuffersource.setColor(ARGB.red(i), ARGB.green(i), ARGB.blue(i), 255);
            } else {
                multibuffersource = p_360990_;
            }

            float f = p_362965_.getGameTimeDeltaPartialTick(!tickratemanager.isEntityFrozen(entity));
            this.renderEntity(entity, d0, d1, d2, f, p_363182_, multibuffersource);
        }
    }

    private void renderBlockEntities(
        PoseStack p_362832_, MultiBufferSource.BufferSource p_363819_, MultiBufferSource.BufferSource p_361830_, Camera p_362042_, float p_362089_
    ) {
        me.alphamode.mcbig.core.BigVec3 vec3 = p_362042_.getPosition();
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();

        for (SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection : this.visibleSections) {
            List<BlockEntity> list = sectionrenderdispatcher$rendersection.getCompiled().getRenderableBlockEntities();
            if (!list.isEmpty()) {
                for (BlockEntity blockentity : list) {
                    BlockPos blockpos = blockentity.getBlockPos();
                    MultiBufferSource multibuffersource = p_363819_;
                    p_362832_.pushPose();
                    p_362832_.translate((double)blockpos.getX() - d0, (double)blockpos.getY() - d1, (double)blockpos.getZ() - d2);
                    SortedSet<BlockDestructionProgress> sortedset = this.destructionProgress.get(blockpos.asLong());
                    if (sortedset != null && !sortedset.isEmpty()) {
                        int i = sortedset.last().getProgress();
                        if (i >= 0) {
                            PoseStack.Pose posestack$pose = p_362832_.last();
                            VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(
                                p_361830_.getBuffer(ModelBakery.DESTROY_TYPES.get(i)), posestack$pose, 1.0F
                            );
                            multibuffersource = p_234298_ -> {
                                VertexConsumer vertexconsumer1 = p_363819_.getBuffer(p_234298_);
                                return p_234298_.affectsCrumbling() ? VertexMultiConsumer.create(vertexconsumer, vertexconsumer1) : vertexconsumer1;
                            };
                        }
                    }

                    this.blockEntityRenderDispatcher.render(blockentity, p_362089_, p_362832_, multibuffersource);
                    p_362832_.popPose();
                }
            }
        }

        synchronized (this.globalBlockEntities) {
            for (BlockEntity blockentity1 : this.globalBlockEntities) {
                BlockPos blockpos1 = blockentity1.getBlockPos();
                p_362832_.pushPose();
                p_362832_.translate((double)blockpos1.getX() - d0, (double)blockpos1.getY() - d1, (double)blockpos1.getZ() - d2);
                this.blockEntityRenderDispatcher.render(blockentity1, p_362089_, p_362832_, p_363819_);
                p_362832_.popPose();
            }
        }
    }

    private void renderBlockDestroyAnimation(PoseStack p_363901_, Camera p_361175_, MultiBufferSource.BufferSource p_365216_) {
        me.alphamode.mcbig.core.BigVec3 vec3 = p_361175_.getPosition();
        me.alphamode.mcbig.math.BigDecimal d0 = vec3.bigX();
        double d1 = vec3.y();
        me.alphamode.mcbig.math.BigDecimal d2 = vec3.bigZ();

        for (it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<BlockPos, SortedSet<BlockDestructionProgress>> entry : this.destructionProgress.object2ObjectEntrySet()) {
            BlockPos blockpos = entry.getKey();
            if (!(blockpos.distToCenterSqr(d0, d1, d2) > 1024.0)) {
                SortedSet<BlockDestructionProgress> sortedset = entry.getValue();
                if (sortedset != null && !sortedset.isEmpty()) {
                    int i = sortedset.last().getProgress();
                    p_363901_.pushPose();
                    p_363901_.translate((double)blockpos.getBigX().toBigDecimal().subtract(d0).doubleValue(), (double)blockpos.getY() - d1, (double)blockpos.getBigZ().toBigDecimal().subtract(d2).doubleValue());
                    PoseStack.Pose posestack$pose = p_363901_.last();
                    VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(
                        p_365216_.getBuffer(ModelBakery.DESTROY_TYPES.get(i)), posestack$pose, 1.0F
                    );
                    this.minecraft
                        .getBlockRenderer()
                        .renderBreakingTexture(this.level.getBlockState(blockpos), blockpos, this.level, p_363901_, vertexconsumer);
                    p_363901_.popPose();
                }
            }
        }
    }

    private void renderBlockOutline(Camera p_363911_, MultiBufferSource.BufferSource p_362782_, PoseStack p_361893_, boolean p_361698_) {
        if (this.minecraft.hitResult instanceof BlockHitResult blockhitresult) {
            if (blockhitresult.getType() != HitResult.Type.MISS) {
                BlockPos blockpos = blockhitresult.getBlockPos();
                BlockState blockstate = this.level.getBlockState(blockpos);
                if (!blockstate.isAir() && this.level.getWorldBorder().isWithinBounds(blockpos)) {
                    boolean flag = ItemBlockRenderTypes.getChunkRenderType(blockstate).sortOnUpload();
                    if (flag != p_361698_) {
                        return;
                    }

                    me.alphamode.mcbig.core.BigVec3 vec3 = p_363911_.getPosition();
                    Boolean obool = this.minecraft.options.highContrastBlockOutline().get();
                    if (obool) {
                        VertexConsumer vertexconsumer = p_362782_.getBuffer(RenderType.secondaryBlockOutline());
                        this.renderHitOutline(p_361893_, vertexconsumer, p_363911_.getEntity(), vec3.x, vec3.y, vec3.z, blockpos, blockstate, -16777216);
                    }

                    VertexConsumer vertexconsumer1 = p_362782_.getBuffer(RenderType.lines());
                    int i = obool ? -11010079 : ARGB.color(102, -16777216);
                    this.renderHitOutline(p_361893_, vertexconsumer1, p_363911_.getEntity(), vec3.x, vec3.y, vec3.z, blockpos, blockstate, i);
                    p_362782_.endLastBatch();
                }
            }
        }
    }

    private void checkPoseStack(PoseStack p_109589_) {
        if (!p_109589_.clear()) {
            throw new IllegalStateException("Pose stack not empty");
        }
    }

    private void renderEntity(
        Entity p_109518_, double p_109519_, double p_109520_, double p_109521_, float p_109522_, PoseStack p_109523_, MultiBufferSource p_109524_
    ) {
        double d0 = Mth.lerp((double)p_109522_, p_109518_.xOld, p_109518_.getX());
        double d1 = Mth.lerp((double)p_109522_, p_109518_.yOld, p_109518_.getY());
        double d2 = Mth.lerp((double)p_109522_, p_109518_.zOld, p_109518_.getZ());
        this.entityRenderDispatcher
            .render(
                p_109518_,
                d0 - p_109519_,
                d1 - p_109520_,
                d2 - p_109521_,
                p_109522_,
                p_109523_,
                p_109524_,
                this.entityRenderDispatcher.getPackedLightCoords(p_109518_, p_109522_)
            );
    }

    private void scheduleTranslucentSectionResort(me.alphamode.mcbig.core.BigVec3 p_361632_) {
        if (!this.visibleSections.isEmpty()) {
            BlockPos blockpos = BlockPos.containing(p_361632_);
            boolean flag = !blockpos.equals(this.lastTranslucentSortBlockPos);
            Profiler.get().push("translucent_sort");
            SectionRenderDispatcher.TranslucencyPointOfView sectionrenderdispatcher$translucencypointofview = new SectionRenderDispatcher.TranslucencyPointOfView();

            for (SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection : this.nearbyVisibleSections) {
                this.scheduleResort(sectionrenderdispatcher$rendersection, sectionrenderdispatcher$translucencypointofview, p_361632_, flag, true);
            }

            this.translucencyResortIterationIndex = this.translucencyResortIterationIndex % this.visibleSections.size();
            int i = Math.max(this.visibleSections.size() / 8, 15);

            while (i-- > 0) {
                int j = this.translucencyResortIterationIndex++ % this.visibleSections.size();
                this.scheduleResort(this.visibleSections.get(j), sectionrenderdispatcher$translucencypointofview, p_361632_, flag, false);
            }

            this.lastTranslucentSortBlockPos = blockpos;
            Profiler.get().pop();
        }
    }

    private void scheduleResort(
        SectionRenderDispatcher.RenderSection p_371775_,
        SectionRenderDispatcher.TranslucencyPointOfView p_371652_,
        me.alphamode.mcbig.core.BigVec3 p_371372_,
        boolean p_371855_,
        boolean p_371355_
    ) {
        p_371652_.set(p_371372_, p_371775_.getSectionNode());
        boolean flag = !p_371652_.equals(p_371775_.pointOfView.get());
        boolean flag1 = p_371855_ && (p_371652_.isAxisAligned() || p_371355_);
        if ((flag1 || flag) && !p_371775_.transparencyResortingScheduled() && p_371775_.hasTranslucentGeometry()) {
            p_371775_.resortTransparency(this.sectionRenderDispatcher);
        }
    }

    private void renderSectionLayer(RenderType p_294513_, me.alphamode.mcbig.math.BigDecimal p_295577_, double p_294761_, me.alphamode.mcbig.math.BigDecimal p_294297_, Matrix4f p_294782_, Matrix4f p_324517_) {
        RenderSystem.assertOnRenderThread();
        Zone zone = Profiler.get().zone(() -> "render_" + p_294513_.name);
        zone.addText(p_294513_::toString);
        boolean flag = p_294513_ != RenderType.translucent();
        ObjectListIterator<SectionRenderDispatcher.RenderSection> objectlistiterator = this.visibleSections
            .listIterator(flag ? 0 : this.visibleSections.size());
        p_294513_.setupRenderState();
        CompiledShaderProgram compiledshaderprogram = RenderSystem.getShader();
        if (compiledshaderprogram == null) {
            p_294513_.clearRenderState();
            zone.close();
        } else {
            compiledshaderprogram.setDefaultUniforms(VertexFormat.Mode.QUADS, p_294782_, p_324517_, this.minecraft.getWindow());
            compiledshaderprogram.apply();
            Uniform uniform = compiledshaderprogram.MODEL_OFFSET;

            while (flag ? objectlistiterator.hasNext() : objectlistiterator.hasPrevious()) {
                SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = flag ? objectlistiterator.next() : objectlistiterator.previous();
                if (!sectionrenderdispatcher$rendersection.getCompiled().isEmpty(p_294513_)) {
                    VertexBuffer vertexbuffer = sectionrenderdispatcher$rendersection.getBuffer(p_294513_);
                    BlockPos blockpos = sectionrenderdispatcher$rendersection.getOrigin();
                    if (uniform != null) {
                        uniform.set(
                            (blockpos.getBigX().toBigDecimal().subtract(p_295577_)).floatValue(),
                            (float)((double)blockpos.getY() - p_294761_),
                            (blockpos.getBigZ().toBigDecimal().subtract(p_294297_)).floatValue()
                        );
                        uniform.upload();
                    }

                    vertexbuffer.bind();
                    vertexbuffer.draw();
                }
            }

            if (uniform != null) {
                uniform.set(0.0F, 0.0F, 0.0F);
            }

            compiledshaderprogram.clear();
            VertexBuffer.unbind();
            zone.close();
            p_294513_.clearRenderState();
        }
    }

    public void captureFrustum() {
        this.captureFrustum = true;
    }

    public void killFrustum() {
        this.capturedFrustum = null;
    }

    public void tick() {
        if (this.level.tickRateManager().runsNormally()) {
            this.ticks++;
        }

        if (this.ticks % 20 == 0) {
            Iterator<BlockDestructionProgress> iterator = this.destroyingBlocks.values().iterator();

            while (iterator.hasNext()) {
                BlockDestructionProgress blockdestructionprogress = iterator.next();
                int i = blockdestructionprogress.getUpdatedRenderTick();
                if (this.ticks - i > 400) {
                    iterator.remove();
                    this.removeProgress(blockdestructionprogress);
                }
            }
        }
    }

    private void removeProgress(BlockDestructionProgress p_109766_) {
        BlockPos i = p_109766_.getPos();
        Set<BlockDestructionProgress> set = this.destructionProgress.get(i);
        set.remove(p_109766_);
        if (set.isEmpty()) {
            this.destructionProgress.remove(i);
        }
    }

    private void addSkyPass(FrameGraphBuilder p_362870_, Camera p_362177_, float p_363799_, FogParameters p_364999_) {
        FogType fogtype = p_362177_.getFluidInCamera();
        if (fogtype != FogType.POWDER_SNOW && fogtype != FogType.LAVA && !this.doesMobEffectBlockSky(p_362177_)) {
            DimensionSpecialEffects dimensionspecialeffects = this.level.effects();
            DimensionSpecialEffects.SkyType dimensionspecialeffects$skytype = dimensionspecialeffects.skyType();
            if (dimensionspecialeffects$skytype != DimensionSpecialEffects.SkyType.NONE) {
                FramePass framepass = p_362870_.addPass("sky");
                this.targets.main = framepass.readsAndWrites(this.targets.main);
                framepass.executes(() -> {
                    RenderSystem.setShaderFog(p_364999_);
                    if (dimensionspecialeffects$skytype == DimensionSpecialEffects.SkyType.END) {
                        this.skyRenderer.renderEndSky();
                    } else {
                        PoseStack posestack = new PoseStack();
                        float f = this.level.getSunAngle(p_363799_);
                        float f1 = this.level.getTimeOfDay(p_363799_);
                        float f2 = 1.0F - this.level.getRainLevel(p_363799_);
                        float f3 = this.level.getStarBrightness(p_363799_) * f2;
                        int i = dimensionspecialeffects.getSunriseOrSunsetColor(f1);
                        int j = this.level.getMoonPhase();
                        int k = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), p_363799_);
                        float f4 = ARGB.redFloat(k);
                        float f5 = ARGB.greenFloat(k);
                        float f6 = ARGB.blueFloat(k);
                        this.skyRenderer.renderSkyDisc(f4, f5, f6);
                        MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
                        if (dimensionspecialeffects.isSunriseOrSunset(f1)) {
                            this.skyRenderer.renderSunriseAndSunset(posestack, multibuffersource$buffersource, f, i);
                        }

                        this.skyRenderer.renderSunMoonAndStars(posestack, multibuffersource$buffersource, f1, j, f2, f3, p_364999_);
                        multibuffersource$buffersource.endBatch();
                        if (this.shouldRenderDarkDisc(p_363799_)) {
                            this.skyRenderer.renderDarkDisc(posestack);
                        }
                    }
                });
            }
        }
    }

    private boolean shouldRenderDarkDisc(float p_362498_) {
        return this.minecraft.player.getEyePosition(p_362498_).y - this.level.getLevelData().getHorizonHeight(this.level) < 0.0;
    }

    private boolean doesMobEffectBlockSky(Camera p_234311_) {
        return !(p_234311_.getEntity() instanceof LivingEntity livingentity)
            ? false
            : livingentity.hasEffect(MobEffects.BLINDNESS) || livingentity.hasEffect(MobEffects.DARKNESS);
    }

    private void compileSections(Camera p_194371_) {
        ProfilerFiller profilerfiller = Profiler.get();
        profilerfiller.push("populate_sections_to_compile");
        RenderRegionCache renderregioncache = new RenderRegionCache();
        BlockPos blockpos = p_194371_.getBlockPosition();
        List<SectionRenderDispatcher.RenderSection> list = Lists.newArrayList();

        for (SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection : this.visibleSections) {
            if (sectionrenderdispatcher$rendersection.isDirty() && sectionrenderdispatcher$rendersection.hasAllNeighbors()) {
                boolean flag = false;
                if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.NEARBY) {
                    BlockPos blockpos1 = sectionrenderdispatcher$rendersection.getOrigin().offset(8, 8, 8);
                    flag = blockpos1.distSqr(blockpos) < 768.0 || sectionrenderdispatcher$rendersection.isDirtyFromPlayer();
                } else if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                    flag = sectionrenderdispatcher$rendersection.isDirtyFromPlayer();
                }

                if (flag) {
                    profilerfiller.push("build_near_sync");
                    this.sectionRenderDispatcher.rebuildSectionSync(sectionrenderdispatcher$rendersection, renderregioncache);
                    sectionrenderdispatcher$rendersection.setNotDirty();
                    profilerfiller.pop();
                } else {
                    list.add(sectionrenderdispatcher$rendersection);
                }
            }
        }

        profilerfiller.popPush("upload");
        this.sectionRenderDispatcher.uploadAllPendingUploads();
        profilerfiller.popPush("schedule_async_compile");

        for (SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection1 : list) {
            sectionrenderdispatcher$rendersection1.rebuildSectionAsync(this.sectionRenderDispatcher, renderregioncache);
            sectionrenderdispatcher$rendersection1.setNotDirty();
        }

        profilerfiller.pop();
        this.scheduleTranslucentSectionResort(p_194371_.getPosition());
    }

    private void renderHitOutline(
        PoseStack p_109638_,
        VertexConsumer p_109639_,
        Entity p_109640_,
        me.alphamode.mcbig.math.BigDecimal p_109641_,
        double p_109642_,
        me.alphamode.mcbig.math.BigDecimal p_109643_,
        BlockPos p_109644_,
        BlockState p_109645_,
        int p_380403_
    ) {
        ShapeRenderer.renderShape(
            p_109638_,
            p_109639_,
            p_109645_.getShape(this.level, p_109644_, CollisionContext.of(p_109640_)),
            (double)p_109644_.getBigX().toBigDecimal().subtract(p_109641_).doubleValue(),
            (double)p_109644_.getY() - p_109642_,
            (double)p_109644_.getBigZ().toBigDecimal().subtract(p_109643_).doubleValue(),
            p_380403_
        );
    }

    public void blockChanged(BlockGetter p_109545_, BlockPos p_109546_, BlockState p_109547_, BlockState p_109548_, int p_109549_) {
        this.setBlockDirty(p_109546_, (p_109549_ & 8) != 0);
    }

    private void setBlockDirty(BlockPos p_109733_, boolean p_109734_) {
        for(me.alphamode.mcbig.math.BigInteger i = p_109733_.getBigZ().subtract(); i.compareTo(p_109733_.getBigZ().add()) <= 0; i = i.add()) {
            for(me.alphamode.mcbig.math.BigInteger j = p_109733_.getBigX().subtract(); j.compareTo(p_109733_.getBigX().add()) <= 0; j = j.add()) {
                for(int k = p_109733_.getY() - 1; k <= p_109733_.getY() + 1; k++) {
                    this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i), p_109734_);
                }
            }
        }
    }

    public void setBlocksDirty(me.alphamode.mcbig.math.BigInteger p_109495_, int p_109496_, me.alphamode.mcbig.math.BigInteger p_109497_, me.alphamode.mcbig.math.BigInteger p_109498_, int p_109499_, me.alphamode.mcbig.math.BigInteger p_109500_) {
        for(me.alphamode.mcbig.math.BigInteger i = p_109497_.subtract(); i.compareTo(p_109500_.add()) <= 0; i = i.add()) {
            for(me.alphamode.mcbig.math.BigInteger j = p_109495_.subtract(); j.compareTo(p_109498_.add()) <= 0; j = j.add()) {
                for(int k = p_109496_ - 1; k <= p_109499_ + 1; k++) {
                    this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i));
                }
            }
        }
    }

    public void setBlockDirty(BlockPos p_109722_, BlockState p_109723_, BlockState p_109724_) {
        if (this.minecraft.getModelManager().requiresRender(p_109723_, p_109724_)) {
            this.setBlocksDirty(p_109722_.getBigX(), p_109722_.getY(), p_109722_.getBigZ(), p_109722_.getBigX(), p_109722_.getY(), p_109722_.getBigZ());
        }
    }

    public void setSectionDirtyWithNeighbors(me.alphamode.mcbig.math.BigInteger p_109491_, int p_109492_, me.alphamode.mcbig.math.BigInteger p_109493_) {
        this.setSectionRangeDirty(p_109491_.subtract(), p_109492_ - 1, p_109493_.subtract(), p_109491_.add(), p_109492_ + 1, p_109493_.add());
    }

    public void setSectionRangeDirty(me.alphamode.mcbig.math.BigInteger p_363248_, int p_360721_, me.alphamode.mcbig.math.BigInteger p_360463_, me.alphamode.mcbig.math.BigInteger p_362591_, int p_364598_, me.alphamode.mcbig.math.BigInteger p_365249_) {
        for (me.alphamode.mcbig.math.BigInteger i = p_360463_; i.compareTo(p_365249_) <= 0; i = i.add()) {
            for (me.alphamode.mcbig.math.BigInteger j = p_363248_; j.compareTo(p_362591_) <= 0; j = j.add()) {
                for (int k = p_360721_; k <= p_364598_; k++) {
                    this.setSectionDirty(j, k, i);
                }
            }
        }
    }

    public void setSectionDirty(me.alphamode.mcbig.math.BigInteger p_109771_, int p_109772_, me.alphamode.mcbig.math.BigInteger p_109773_) {
        this.setSectionDirty(p_109771_, p_109772_, p_109773_, false);
    }

    private void setSectionDirty(me.alphamode.mcbig.math.BigInteger p_109502_, int p_109503_, me.alphamode.mcbig.math.BigInteger p_109504_, boolean p_109505_) {
        this.viewArea.setDirty(p_109502_, p_109503_, p_109504_, p_109505_);
    }

    public void onSectionBecomingNonEmpty(SectionPos p_366463_) {
        SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = this.viewArea.getRenderSection(p_366463_);
        if (sectionrenderdispatcher$rendersection != null) {
            this.sectionOcclusionGraph.schedulePropagationFrom(sectionrenderdispatcher$rendersection);
        }
    }

    public void addParticle(
        ParticleOptions p_109744_,
        boolean p_109745_,
        double p_109746_,
        double p_109747_,
        double p_109748_,
        double p_109749_,
        double p_109750_,
        double p_109751_
    ) {
        this.addParticle(p_109744_, p_109745_, false, p_109746_, p_109747_, p_109748_, p_109749_, p_109750_, p_109751_);
    }

    public void addParticle(
        ParticleOptions p_109753_,
        boolean p_109754_,
        boolean p_109755_,
        double p_109756_,
        double p_109757_,
        double p_109758_,
        double p_109759_,
        double p_109760_,
        double p_109761_
    ) {
        try {
            this.addParticleInternal(p_109753_, p_109754_, p_109755_, p_109756_, p_109757_, p_109758_, p_109759_, p_109760_, p_109761_);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while adding particle");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being added");
            crashreportcategory.setDetail("ID", BuiltInRegistries.PARTICLE_TYPE.getKey(p_109753_.getType()));
            crashreportcategory.setDetail(
                "Parameters",
                () -> ParticleTypes.CODEC.encodeStart(this.level.registryAccess().createSerializationContext(NbtOps.INSTANCE), p_109753_).toString()
            );
            crashreportcategory.setDetail("Position", () -> CrashReportCategory.formatLocation(this.level, p_109756_, p_109757_, p_109758_));
            throw new ReportedException(crashreport);
        }
    }

    public <T extends ParticleOptions> void addParticle(
        T p_109736_, double p_109737_, double p_109738_, double p_109739_, double p_109740_, double p_109741_, double p_109742_
    ) {
        this.addParticle(p_109736_, p_109736_.getType().getOverrideLimiter(), p_109737_, p_109738_, p_109739_, p_109740_, p_109741_, p_109742_);
    }

    @Nullable
    Particle addParticleInternal(
        ParticleOptions p_109796_,
        boolean p_109797_,
        double p_109798_,
        double p_109799_,
        double p_109800_,
        double p_109801_,
        double p_109802_,
        double p_109803_
    ) {
        return this.addParticleInternal(p_109796_, p_109797_, false, p_109798_, p_109799_, p_109800_, p_109801_, p_109802_, p_109803_);
    }

    @Nullable
    private Particle addParticleInternal(
        ParticleOptions p_109805_,
        boolean p_109806_,
        boolean p_109807_,
        double p_109808_,
        double p_109809_,
        double p_109810_,
        double p_109811_,
        double p_109812_,
        double p_109813_
    ) {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        ParticleStatus particlestatus = this.calculateParticleLevel(p_109807_);
        if (p_109806_) {
            return this.minecraft.particleEngine.createParticle(p_109805_, p_109808_, p_109809_, p_109810_, p_109811_, p_109812_, p_109813_);
        } else if (camera.getPosition().distanceToSqr(p_109808_, p_109809_, p_109810_) > 1024.0) {
            return null;
        } else {
            return particlestatus == ParticleStatus.MINIMAL
                ? null
                : this.minecraft.particleEngine.createParticle(p_109805_, p_109808_, p_109809_, p_109810_, p_109811_, p_109812_, p_109813_);
        }
    }

    private ParticleStatus calculateParticleLevel(boolean p_109768_) {
        ParticleStatus particlestatus = this.minecraft.options.particles().get();
        if (p_109768_ && particlestatus == ParticleStatus.MINIMAL && this.level.random.nextInt(10) == 0) {
            particlestatus = ParticleStatus.DECREASED;
        }

        if (particlestatus == ParticleStatus.DECREASED && this.level.random.nextInt(3) == 0) {
            particlestatus = ParticleStatus.MINIMAL;
        }

        return particlestatus;
    }

    public void destroyBlockProgress(int p_109775_, BlockPos p_109776_, int p_109777_) {
        if (p_109777_ >= 0 && p_109777_ < 10) {
            BlockDestructionProgress blockdestructionprogress1 = this.destroyingBlocks.get(p_109775_);
            if (blockdestructionprogress1 != null) {
                this.removeProgress(blockdestructionprogress1);
            }

            if (blockdestructionprogress1 == null
                || !java.util.Objects.equals(blockdestructionprogress1.getPos().getBigX(), p_109776_.getBigX())
                || !java.util.Objects.equals(blockdestructionprogress1.getPos().getBigY(), p_109776_.getBigY())
                || !java.util.Objects.equals(blockdestructionprogress1.getPos().getBigZ(), p_109776_.getBigZ())) {
                blockdestructionprogress1 = new BlockDestructionProgress(p_109775_, p_109776_);
                this.destroyingBlocks.put(p_109775_, blockdestructionprogress1);
            }

            blockdestructionprogress1.setProgress(p_109777_);
            blockdestructionprogress1.updateTick(this.ticks);
            this.destructionProgress
                .computeIfAbsent(blockdestructionprogress1.getPos(), p_234254_ -> Sets.newTreeSet())
                .add(blockdestructionprogress1);
        } else {
            BlockDestructionProgress blockdestructionprogress = this.destroyingBlocks.remove(p_109775_);
            if (blockdestructionprogress != null) {
                this.removeProgress(blockdestructionprogress);
            }
        }
    }

    public boolean hasRenderedAllSections() {
        return this.sectionRenderDispatcher.isQueueEmpty();
    }

    public void onChunkReadyToRender(ChunkPos p_383159_) {
        this.sectionOcclusionGraph.onChunkReadyToRender(p_383159_);
    }

    public void needsUpdate() {
        this.sectionOcclusionGraph.invalidate();
        this.cloudRenderer.markForRebuild();
    }

    public void updateGlobalBlockEntities(Collection<BlockEntity> p_109763_, Collection<BlockEntity> p_109764_) {
        synchronized (this.globalBlockEntities) {
            this.globalBlockEntities.removeAll(p_109763_);
            this.globalBlockEntities.addAll(p_109764_);
        }
    }

    public static int getLightColor(BlockAndTintGetter p_109542_, BlockPos p_109543_) {
        return getLightColor(p_109542_, p_109542_.getBlockState(p_109543_), p_109543_);
    }

    public static int getLightColor(BlockAndTintGetter p_109538_, BlockState p_109539_, BlockPos p_109540_) {
        if (p_109539_.emissiveRendering(p_109538_, p_109540_)) {
            return 15728880;
        } else {
            int i = p_109538_.getBrightness(LightLayer.SKY, p_109540_);
            int j = p_109538_.getBrightness(LightLayer.BLOCK, p_109540_);
            int k = p_109539_.getLightEmission();
            if (j < k) {
                j = k;
            }

            return i << 20 | j << 4;
        }
    }

    public boolean isSectionCompiled(BlockPos p_295788_) {
        SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = this.viewArea.getRenderSectionAt(p_295788_);
        return sectionrenderdispatcher$rendersection != null
            && sectionrenderdispatcher$rendersection.compiled.get() != SectionRenderDispatcher.CompiledSection.UNCOMPILED;
    }

    @Nullable
    public RenderTarget entityOutlineTarget() {
        return this.targets.entityOutline != null ? this.targets.entityOutline.get() : null;
    }

    @Nullable
    public RenderTarget getTranslucentTarget() {
        return this.targets.translucent != null ? this.targets.translucent.get() : null;
    }

    @Nullable
    public RenderTarget getItemEntityTarget() {
        return this.targets.itemEntity != null ? this.targets.itemEntity.get() : null;
    }

    @Nullable
    public RenderTarget getParticlesTarget() {
        return this.targets.particles != null ? this.targets.particles.get() : null;
    }

    @Nullable
    public RenderTarget getWeatherTarget() {
        return this.targets.weather != null ? this.targets.weather.get() : null;
    }

    @Nullable
    public RenderTarget getCloudsTarget() {
        return this.targets.clouds != null ? this.targets.clouds.get() : null;
    }

    @VisibleForDebug
    public ObjectArrayList<SectionRenderDispatcher.RenderSection> getVisibleSections() {
        return this.visibleSections;
    }

    @VisibleForDebug
    public SectionOcclusionGraph getSectionOcclusionGraph() {
        return this.sectionOcclusionGraph;
    }

    @Nullable
    public Frustum getCapturedFrustum() {
        return this.capturedFrustum;
    }

    public CloudRenderer getCloudRenderer() {
        return this.cloudRenderer;
    }
}
