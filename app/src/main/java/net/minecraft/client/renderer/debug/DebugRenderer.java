package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugRenderer {
    public final PathfindingRenderer pathfindingRenderer = new PathfindingRenderer();
    public final DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
    public final DebugRenderer.SimpleDebugRenderer chunkBorderRenderer;
    public final DebugRenderer.SimpleDebugRenderer heightMapRenderer;
    public final DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
    public final DebugRenderer.SimpleDebugRenderer supportBlockRenderer;
    public final NeighborsUpdateRenderer neighborsUpdateRenderer;
    public final RedstoneWireOrientationsRenderer redstoneWireOrientationsRenderer;
    public final StructureRenderer structureRenderer;
    public final DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
    public final DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer;
    public final DebugRenderer.SimpleDebugRenderer solidFaceRenderer;
    public final DebugRenderer.SimpleDebugRenderer chunkRenderer;
    public final BrainDebugRenderer brainDebugRenderer;
    public final VillageSectionsDebugRenderer villageSectionsDebugRenderer;
    public final BeeDebugRenderer beeDebugRenderer;
    public final RaidDebugRenderer raidDebugRenderer;
    public final GoalSelectorDebugRenderer goalSelectorRenderer;
    public final GameTestDebugRenderer gameTestDebugRenderer;
    public final GameEventListenerRenderer gameEventListenerRenderer;
    public final LightSectionDebugRenderer skyLightSectionDebugRenderer;
    public final BreezeDebugRenderer breezeDebugRenderer;
    public final ChunkCullingDebugRenderer chunkCullingDebugRenderer;
    public final OctreeDebugRenderer octreeDebugRenderer;
    private boolean renderChunkborder;
    private boolean renderOctree;

    public DebugRenderer(Minecraft p_113433_) {
        this.waterDebugRenderer = new WaterDebugRenderer(p_113433_);
        this.chunkBorderRenderer = new ChunkBorderRenderer(p_113433_);
        this.heightMapRenderer = new HeightMapRenderer(p_113433_);
        this.collisionBoxRenderer = new CollisionBoxRenderer(p_113433_);
        this.supportBlockRenderer = new SupportBlockRenderer(p_113433_);
        this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(p_113433_);
        this.redstoneWireOrientationsRenderer = new RedstoneWireOrientationsRenderer(p_113433_);
        this.structureRenderer = new StructureRenderer(p_113433_);
        this.lightDebugRenderer = new LightDebugRenderer(p_113433_);
        this.worldGenAttemptRenderer = new WorldGenAttemptRenderer();
        this.solidFaceRenderer = new SolidFaceRenderer(p_113433_);
        this.chunkRenderer = new ChunkDebugRenderer(p_113433_);
        this.brainDebugRenderer = new BrainDebugRenderer(p_113433_);
        this.villageSectionsDebugRenderer = new VillageSectionsDebugRenderer();
        this.beeDebugRenderer = new BeeDebugRenderer(p_113433_);
        this.raidDebugRenderer = new RaidDebugRenderer(p_113433_);
        this.goalSelectorRenderer = new GoalSelectorDebugRenderer(p_113433_);
        this.gameTestDebugRenderer = new GameTestDebugRenderer();
        this.gameEventListenerRenderer = new GameEventListenerRenderer(p_113433_);
        this.skyLightSectionDebugRenderer = new LightSectionDebugRenderer(p_113433_, LightLayer.SKY);
        this.breezeDebugRenderer = new BreezeDebugRenderer(p_113433_);
        this.chunkCullingDebugRenderer = new ChunkCullingDebugRenderer(p_113433_);
        this.octreeDebugRenderer = new OctreeDebugRenderer(p_113433_);
    }

    public void clear() {
        this.pathfindingRenderer.clear();
        this.waterDebugRenderer.clear();
        this.chunkBorderRenderer.clear();
        this.heightMapRenderer.clear();
        this.collisionBoxRenderer.clear();
        this.supportBlockRenderer.clear();
        this.neighborsUpdateRenderer.clear();
        this.structureRenderer.clear();
        this.lightDebugRenderer.clear();
        this.worldGenAttemptRenderer.clear();
        this.solidFaceRenderer.clear();
        this.chunkRenderer.clear();
        this.brainDebugRenderer.clear();
        this.villageSectionsDebugRenderer.clear();
        this.beeDebugRenderer.clear();
        this.raidDebugRenderer.clear();
        this.goalSelectorRenderer.clear();
        this.gameTestDebugRenderer.clear();
        this.gameEventListenerRenderer.clear();
        this.skyLightSectionDebugRenderer.clear();
        this.breezeDebugRenderer.clear();
        this.chunkCullingDebugRenderer.clear();
    }

    public boolean switchRenderChunkborder() {
        this.renderChunkborder = !this.renderChunkborder;
        return this.renderChunkborder;
    }

    public boolean toggleRenderOctree() {
        return this.renderOctree = !this.renderOctree;
    }

    public void render(PoseStack p_113458_, Frustum p_366571_, MultiBufferSource.BufferSource p_113459_, double p_113460_, double p_113461_, double p_113462_) {
        if (this.renderChunkborder && !Minecraft.getInstance().showOnlyReducedInfo()) {
            this.chunkBorderRenderer.render(p_113458_, p_113459_, p_113460_, p_113461_, p_113462_);
        }

        if (this.renderOctree) {
            this.octreeDebugRenderer.render(p_113458_, p_366571_, p_113459_, p_113460_, p_113461_, p_113462_);
        }

        this.gameTestDebugRenderer.render(p_113458_, p_113459_, p_113460_, p_113461_, p_113462_);
    }

    public void renderAfterTranslucents(PoseStack p_361527_, MultiBufferSource.BufferSource p_364189_, double p_363297_, double p_362785_, double p_363711_) {
        this.chunkCullingDebugRenderer.render(p_361527_, p_364189_, p_363297_, p_362785_, p_363711_);
    }

    public static Optional<Entity> getTargetedEntity(@Nullable Entity p_113449_, int p_113450_) {
        if (p_113449_ == null) {
            return Optional.empty();
        } else {
            Vec3 vec3 = p_113449_.getEyePosition();
            Vec3 vec31 = p_113449_.getViewVector(1.0F).scale((double)p_113450_);
            Vec3 vec32 = vec3.add(vec31);
            AABB aabb = p_113449_.getBoundingBox().expandTowards(vec31).inflate(1.0);
            int i = p_113450_ * p_113450_;
            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(p_113449_, vec3, vec32, aabb, EntitySelector.CAN_BE_PICKED, (double)i);
            if (entityhitresult == null) {
                return Optional.empty();
            } else {
                return vec3.distanceToSqr(entityhitresult.getLocation()) > (double)i ? Optional.empty() : Optional.of(entityhitresult.getEntity());
            }
        }
    }

    public static void renderFilledUnitCube(
        PoseStack p_308923_, MultiBufferSource p_309126_, BlockPos p_309015_, float p_308976_, float p_308978_, float p_309148_, float p_309159_
    ) {
        renderFilledBox(p_308923_, p_309126_, p_309015_, p_309015_.offset(1, 1, 1), p_308976_, p_308978_, p_309148_, p_309159_);
    }

    public static void renderFilledBox(
        PoseStack p_270169_,
        MultiBufferSource p_270417_,
        BlockPos p_270790_,
        BlockPos p_270610_,
        float p_270515_,
        float p_270494_,
        float p_270869_,
        float p_270844_
    ) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.isInitialized()) {
            Vec3 vec3 = camera.getPosition().reverse().toVanilla();
            AABB aabb = AABB.encapsulatingFullBlocks(p_270790_, p_270610_).move(vec3);
            renderFilledBox(p_270169_, p_270417_, aabb, p_270515_, p_270494_, p_270869_, p_270844_);
        }
    }

    public static void renderFilledBox(
        PoseStack p_270877_,
        MultiBufferSource p_270925_,
        BlockPos p_270480_,
        float p_270569_,
        float p_270315_,
        float p_270182_,
        float p_270862_,
        float p_270973_
    ) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.isInitialized()) {
            Vec3 vec3 = camera.getPosition().reverse().toVanilla();
            AABB aabb = new AABB(p_270480_).move(vec3).inflate((double)p_270569_);
            renderFilledBox(p_270877_, p_270925_, aabb, p_270315_, p_270182_, p_270862_, p_270973_);
        }
    }

    public static void renderFilledBox(
        PoseStack p_271017_, MultiBufferSource p_270356_, AABB p_270833_, float p_270850_, float p_270249_, float p_270654_, float p_270476_
    ) {
        renderFilledBox(
            p_271017_,
            p_270356_,
            p_270833_.minX,
            p_270833_.minY,
            p_270833_.minZ,
            p_270833_.maxX,
            p_270833_.maxY,
            p_270833_.maxZ,
            p_270850_,
            p_270249_,
            p_270654_,
            p_270476_
        );
    }

    public static void renderFilledBox(
        PoseStack p_270616_,
        MultiBufferSource p_270769_,
        double p_270653_,
        double p_270967_,
        double p_270556_,
        double p_270724_,
        double p_270427_,
        double p_270138_,
        float p_270391_,
        float p_270093_,
        float p_270312_,
        float p_270567_
    ) {
        VertexConsumer vertexconsumer = p_270769_.getBuffer(RenderType.debugFilledBox());
        ShapeRenderer.addChainedFilledBoxVertices(
            p_270616_, vertexconsumer, p_270653_, p_270967_, p_270556_, p_270724_, p_270427_, p_270138_, p_270391_, p_270093_, p_270312_, p_270567_
        );
    }

    public static void renderFloatingText(
        PoseStack p_270671_, MultiBufferSource p_271023_, String p_270521_, int p_270729_, int p_270562_, int p_270828_, int p_270164_
    ) {
        renderFloatingText(p_270671_, p_271023_, p_270521_, (double)p_270729_ + 0.5, (double)p_270562_ + 0.5, (double)p_270828_ + 0.5, p_270164_);
    }

    public static void renderFloatingText(
        PoseStack p_270905_, MultiBufferSource p_270581_, String p_270305_, double p_270645_, double p_270746_, double p_270364_, int p_270977_
    ) {
        renderFloatingText(p_270905_, p_270581_, p_270305_, p_270645_, p_270746_, p_270364_, p_270977_, 0.02F);
    }

    public static void renderFloatingText(
        PoseStack p_270216_,
        MultiBufferSource p_270684_,
        String p_270564_,
        double p_270935_,
        double p_270856_,
        double p_270908_,
        int p_270180_,
        float p_270685_
    ) {
        renderFloatingText(p_270216_, p_270684_, p_270564_, p_270935_, p_270856_, p_270908_, p_270180_, p_270685_, true, 0.0F, false);
    }

    public static void renderFloatingText(
        PoseStack p_270649_,
        MultiBufferSource p_270695_,
        String p_270703_,
        double p_270942_,
        double p_270292_,
        double p_270885_,
        int p_270956_,
        float p_270657_,
        boolean p_270731_,
        float p_270825_,
        boolean p_270222_
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (camera.isInitialized() && minecraft.getEntityRenderDispatcher().options != null) {
            Font font = minecraft.font;
            me.alphamode.mcbig.math.BigDecimal d0 = camera.getPosition().x;
            double d1 = camera.getPosition().y;
            me.alphamode.mcbig.math.BigDecimal d2 = camera.getPosition().z;
            p_270649_.pushPose();
            p_270649_.translate((float)(p_270942_ - d0.doubleValue()), (float)(p_270292_ - d1) + 0.07F, (float)(p_270885_ - d2.doubleValue()));
            p_270649_.mulPose(camera.rotation());
            p_270649_.scale(p_270657_, -p_270657_, p_270657_);
            float f = p_270731_ ? (float)(-font.width(p_270703_)) / 2.0F : 0.0F;
            f -= p_270825_ / p_270657_;
            font.drawInBatch(
                p_270703_,
                f,
                0.0F,
                p_270956_,
                false,
                p_270649_.last().pose(),
                p_270695_,
                p_270222_ ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL,
                0,
                15728880
            );
            p_270649_.popPose();
        }
    }

    private static Vec3 mixColor(float p_362997_) {
        float f = 5.99999F;
        int i = (int)(Mth.clamp(p_362997_, 0.0F, 1.0F) * 5.99999F);
        float f1 = p_362997_ * 5.99999F - (float)i;

        return switch (i) {
            case 0 -> new Vec3(1.0, (double)f1, 0.0);
            case 1 -> new Vec3((double)(1.0F - f1), 1.0, 0.0);
            case 2 -> new Vec3(0.0, 1.0, (double)f1);
            case 3 -> new Vec3(0.0, 1.0 - (double)f1, 1.0);
            case 4 -> new Vec3((double)f1, 0.0, 1.0);
            case 5 -> new Vec3(1.0, 0.0, 1.0 - (double)f1);
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }

    private static Vec3 shiftHue(float p_363162_, float p_360992_, float p_363824_, float p_363167_) {
        Vec3 vec3 = mixColor(p_363167_).scale((double)p_363162_);
        Vec3 vec31 = mixColor((p_363167_ + 0.33333334F) % 1.0F).scale((double)p_360992_);
        Vec3 vec32 = mixColor((p_363167_ + 0.6666667F) % 1.0F).scale((double)p_363824_);
        Vec3 vec33 = vec3.add(vec31).add(vec32);
        double d0 = Math.max(Math.max(1.0, vec33.x), Math.max(vec33.y, vec33.z));
        return new Vec3(vec33.x / d0, vec33.y / d0, vec33.z / d0);
    }

    public static void renderVoxelShape(
        PoseStack p_361654_,
        VertexConsumer p_362910_,
        VoxelShape p_360834_,
        double p_361501_,
        double p_364243_,
        double p_363862_,
        float p_365460_,
        float p_364173_,
        float p_365309_,
        float p_360313_,
        boolean p_362872_
    ) {
        List<AABB> list = p_360834_.toAabbs();
        if (!list.isEmpty()) {
            int i = p_362872_ ? list.size() : list.size() * 8;
            ShapeRenderer.renderShape(
                p_361654_,
                p_362910_,
                Shapes.create(list.get(0)),
                p_361501_,
                p_364243_,
                p_363862_,
                ARGB.colorFromFloat(p_360313_, p_365460_, p_364173_, p_365309_)
            );

            for (int j = 1; j < list.size(); j++) {
                AABB aabb = list.get(j);
                float f = (float)j / (float)i;
                Vec3 vec3 = shiftHue(p_365460_, p_364173_, p_365309_, f);
                ShapeRenderer.renderShape(
                    p_361654_,
                    p_362910_,
                    Shapes.create(aabb),
                    p_361501_,
                    p_364243_,
                    p_363862_,
                    ARGB.colorFromFloat(p_360313_, (float)vec3.x, (float)vec3.y, (float)vec3.z)
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface SimpleDebugRenderer {
        void render(PoseStack p_113507_, MultiBufferSource p_113508_, double p_113509_, double p_113510_, double p_113511_);

        default void clear() {
        }
    }
}
