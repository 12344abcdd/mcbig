package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.dragon.EnderDragonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class EnderDragonRenderer extends EntityRenderer<EnderDragon, EnderDragonRenderState> {
    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/end_crystal/end_crystal_beam.png");
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_exploding.png");
    private static final ResourceLocation DRAGON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation DRAGON_EYES_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_eyes.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(DRAGON_LOCATION);
    private static final RenderType DECAL = RenderType.entityDecal(DRAGON_LOCATION);
    private static final RenderType EYES = RenderType.eyes(DRAGON_EYES_LOCATION);
    private static final RenderType BEAM = RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    private final EnderDragonModel model;

    public EnderDragonRenderer(EntityRendererProvider.Context p_173973_) {
        super(p_173973_);
        this.shadowRadius = 0.5F;
        this.model = new EnderDragonModel(p_173973_.bakeLayer(ModelLayers.ENDER_DRAGON));
    }

    public void render(EnderDragonRenderState p_364805_, PoseStack p_114211_, MultiBufferSource p_114212_, int p_114213_) {
        p_114211_.pushPose();
        float f = p_364805_.getHistoricalPos(7).yRot();
        float f1 = (float)(p_364805_.getHistoricalPos(5).y() - p_364805_.getHistoricalPos(10).y());
        p_114211_.mulPose(Axis.YP.rotationDegrees(-f));
        p_114211_.mulPose(Axis.XP.rotationDegrees(f1 * 10.0F));
        p_114211_.translate(0.0F, 0.0F, 1.0F);
        p_114211_.scale(-1.0F, -1.0F, 1.0F);
        p_114211_.translate(0.0F, -1.501F, 0.0F);
        this.model.setupAnim(p_364805_);
        if (p_364805_.deathTime > 0.0F) {
            float f2 = p_364805_.deathTime / 200.0F;
            int i = ARGB.color(Mth.floor(f2 * 255.0F), -1);
            VertexConsumer vertexconsumer = p_114212_.getBuffer(RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION));
            this.model.renderToBuffer(p_114211_, vertexconsumer, p_114213_, OverlayTexture.NO_OVERLAY, i);
            VertexConsumer vertexconsumer1 = p_114212_.getBuffer(DECAL);
            this.model.renderToBuffer(p_114211_, vertexconsumer1, p_114213_, OverlayTexture.pack(0.0F, p_364805_.hasRedOverlay));
        } else {
            VertexConsumer vertexconsumer2 = p_114212_.getBuffer(RENDER_TYPE);
            this.model.renderToBuffer(p_114211_, vertexconsumer2, p_114213_, OverlayTexture.pack(0.0F, p_364805_.hasRedOverlay));
        }

        VertexConsumer vertexconsumer3 = p_114212_.getBuffer(EYES);
        this.model.renderToBuffer(p_114211_, vertexconsumer3, p_114213_, OverlayTexture.NO_OVERLAY);
        if (p_364805_.deathTime > 0.0F) {
            float f3 = p_364805_.deathTime / 200.0F;
            p_114211_.pushPose();
            p_114211_.translate(0.0F, -1.0F, -2.0F);
            renderRays(p_114211_, f3, p_114212_.getBuffer(RenderType.dragonRays()));
            renderRays(p_114211_, f3, p_114212_.getBuffer(RenderType.dragonRaysDepth()));
            p_114211_.popPose();
        }

        p_114211_.popPose();
        if (p_364805_.beamOffset != null) {
            renderCrystalBeams(
                (float)p_364805_.beamOffset.x,
                (float)p_364805_.beamOffset.y,
                (float)p_364805_.beamOffset.z,
                p_364805_.ageInTicks,
                p_114211_,
                p_114212_,
                p_114213_
            );
        }

        super.render(p_364805_, p_114211_, p_114212_, p_114213_);
    }

    private static void renderRays(PoseStack p_352922_, float p_352903_, VertexConsumer p_352894_) {
        p_352922_.pushPose();
        float f = Math.min(p_352903_ > 0.8F ? (p_352903_ - 0.8F) / 0.2F : 0.0F, 1.0F);
        int i = ARGB.colorFromFloat(1.0F - f, 1.0F, 1.0F, 1.0F);
        int j = 16711935;
        RandomSource randomsource = RandomSource.create(432L);
        Vector3f vector3f = new Vector3f();
        Vector3f vector3f1 = new Vector3f();
        Vector3f vector3f2 = new Vector3f();
        Vector3f vector3f3 = new Vector3f();
        Quaternionf quaternionf = new Quaternionf();
        int k = Mth.floor((p_352903_ + p_352903_ * p_352903_) / 2.0F * 60.0F);

        for (int l = 0; l < k; l++) {
            quaternionf.rotationXYZ(
                    randomsource.nextFloat() * (float) (Math.PI * 2),
                    randomsource.nextFloat() * (float) (Math.PI * 2),
                    randomsource.nextFloat() * (float) (Math.PI * 2)
                )
                .rotateXYZ(
                    randomsource.nextFloat() * (float) (Math.PI * 2),
                    randomsource.nextFloat() * (float) (Math.PI * 2),
                    randomsource.nextFloat() * (float) (Math.PI * 2) + p_352903_ * (float) (Math.PI / 2)
                );
            p_352922_.mulPose(quaternionf);
            float f1 = randomsource.nextFloat() * 20.0F + 5.0F + f * 10.0F;
            float f2 = randomsource.nextFloat() * 2.0F + 1.0F + f * 2.0F;
            vector3f1.set(-HALF_SQRT_3 * f2, f1, -0.5F * f2);
            vector3f2.set(HALF_SQRT_3 * f2, f1, -0.5F * f2);
            vector3f3.set(0.0F, f1, f2);
            PoseStack.Pose posestack$pose = p_352922_.last();
            p_352894_.addVertex(posestack$pose, vector3f).setColor(i);
            p_352894_.addVertex(posestack$pose, vector3f1).setColor(16711935);
            p_352894_.addVertex(posestack$pose, vector3f2).setColor(16711935);
            p_352894_.addVertex(posestack$pose, vector3f).setColor(i);
            p_352894_.addVertex(posestack$pose, vector3f2).setColor(16711935);
            p_352894_.addVertex(posestack$pose, vector3f3).setColor(16711935);
            p_352894_.addVertex(posestack$pose, vector3f).setColor(i);
            p_352894_.addVertex(posestack$pose, vector3f3).setColor(16711935);
            p_352894_.addVertex(posestack$pose, vector3f1).setColor(16711935);
        }

        p_352922_.popPose();
    }

    public static void renderCrystalBeams(
        float p_114188_, float p_114189_, float p_114190_, float p_114191_, PoseStack p_114193_, MultiBufferSource p_114194_, int p_114192_
    ) {
        float f = Mth.sqrt(p_114188_ * p_114188_ + p_114190_ * p_114190_);
        float f1 = Mth.sqrt(p_114188_ * p_114188_ + p_114189_ * p_114189_ + p_114190_ * p_114190_);
        p_114193_.pushPose();
        p_114193_.translate(0.0F, 2.0F, 0.0F);
        p_114193_.mulPose(Axis.YP.rotation((float)(-Math.atan2((double)p_114190_, (double)p_114188_)) - (float) (Math.PI / 2)));
        p_114193_.mulPose(Axis.XP.rotation((float)(-Math.atan2((double)f, (double)p_114189_)) - (float) (Math.PI / 2)));
        VertexConsumer vertexconsumer = p_114194_.getBuffer(BEAM);
        float f2 = 0.0F - p_114191_ * 0.01F;
        float f3 = f1 / 32.0F - p_114191_ * 0.01F;
        int i = 8;
        float f4 = 0.0F;
        float f5 = 0.75F;
        float f6 = 0.0F;
        PoseStack.Pose posestack$pose = p_114193_.last();

        for (int j = 1; j <= 8; j++) {
            float f7 = Mth.sin((float)j * (float) (Math.PI * 2) / 8.0F) * 0.75F;
            float f8 = Mth.cos((float)j * (float) (Math.PI * 2) / 8.0F) * 0.75F;
            float f9 = (float)j / 8.0F;
            vertexconsumer.addVertex(posestack$pose, f4 * 0.2F, f5 * 0.2F, 0.0F)
                .setColor(-16777216)
                .setUv(f6, f2)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(p_114192_)
                .setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
            vertexconsumer.addVertex(posestack$pose, f4, f5, f1)
                .setColor(-1)
                .setUv(f6, f3)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(p_114192_)
                .setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
            vertexconsumer.addVertex(posestack$pose, f7, f8, f1)
                .setColor(-1)
                .setUv(f9, f3)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(p_114192_)
                .setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
            vertexconsumer.addVertex(posestack$pose, f7 * 0.2F, f8 * 0.2F, 0.0F)
                .setColor(-16777216)
                .setUv(f9, f2)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(p_114192_)
                .setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
            f4 = f7;
            f5 = f8;
            f6 = f9;
        }

        p_114193_.popPose();
    }

    public EnderDragonRenderState createRenderState() {
        return new EnderDragonRenderState();
    }

    public void extractRenderState(EnderDragon p_361171_, EnderDragonRenderState p_363002_, float p_363418_) {
        super.extractRenderState(p_361171_, p_363002_, p_363418_);
        p_363002_.flapTime = Mth.lerp(p_363418_, p_361171_.oFlapTime, p_361171_.flapTime);
        p_363002_.deathTime = p_361171_.dragonDeathTime > 0 ? (float)p_361171_.dragonDeathTime + p_363418_ : 0.0F;
        p_363002_.hasRedOverlay = p_361171_.hurtTime > 0;
        EndCrystal endcrystal = p_361171_.nearestCrystal;
        if (endcrystal != null) {
            Vec3 vec3 = endcrystal.getPosition(p_363418_).add(0.0, (double)EndCrystalRenderer.getY((float)endcrystal.time + p_363418_), 0.0);
            p_363002_.beamOffset = vec3.subtract(p_361171_.getPosition(p_363418_));
        } else {
            p_363002_.beamOffset = null;
        }

        DragonPhaseInstance dragonphaseinstance = p_361171_.getPhaseManager().getCurrentPhase();
        p_363002_.isLandingOrTakingOff = dragonphaseinstance == EnderDragonPhase.LANDING || dragonphaseinstance == EnderDragonPhase.TAKEOFF;
        p_363002_.isSitting = dragonphaseinstance.isSitting();
        BlockPos blockpos = p_361171_.level()
            .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(p_361171_.getFightOrigin()));
        p_363002_.distanceToEgg = blockpos.distToCenterSqr(p_361171_.position());
        p_363002_.partialTicks = p_361171_.isDeadOrDying() ? 0.0F : p_363418_;
        p_363002_.flightHistory.copyFrom(p_361171_.flightHistory);
    }

    protected boolean affectedByCulling(EnderDragon p_361699_) {
        return false;
    }
}
