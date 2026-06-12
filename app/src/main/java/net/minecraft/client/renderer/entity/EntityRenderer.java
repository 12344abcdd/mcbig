package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public abstract class EntityRenderer<T extends Entity, S extends EntityRenderState> {
    protected static final float NAMETAG_SCALE = 0.025F;
    public static final int LEASH_RENDER_STEPS = 24;
    protected final EntityRenderDispatcher entityRenderDispatcher;
    private final Font font;
    protected float shadowRadius;
    protected float shadowStrength = 1.0F;
    private final S reusedState = this.createRenderState();

    protected EntityRenderer(EntityRendererProvider.Context p_174008_) {
        this.entityRenderDispatcher = p_174008_.getEntityRenderDispatcher();
        this.font = p_174008_.getFont();
    }

    public final int getPackedLightCoords(T p_114506_, float p_114507_) {
        BlockPos blockpos = BlockPos.containing(p_114506_.getLightProbePosition(p_114507_));
        return LightTexture.pack(this.getBlockLightLevel(p_114506_, blockpos), this.getSkyLightLevel(p_114506_, blockpos));
    }

    protected int getSkyLightLevel(T p_114509_, BlockPos p_114510_) {
        return p_114509_.level().getBrightness(LightLayer.SKY, p_114510_);
    }

    protected int getBlockLightLevel(T p_114496_, BlockPos p_114497_) {
        return p_114496_.isOnFire() ? 15 : p_114496_.level().getBrightness(LightLayer.BLOCK, p_114497_);
    }

    public boolean shouldRender(T p_114491_, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
        if (!p_114491_.shouldRender(p_114493_, p_114494_, p_114495_)) {
            return false;
        } else if (!this.affectedByCulling(p_114491_)) {
            return true;
        } else {
            AABB aabb = this.getBoundingBoxForCulling(p_114491_).inflate(0.5);
            if (aabb.hasNaN() || aabb.getSize() == 0.0) {
                aabb = new AABB(
                    p_114491_.getX() - 2.0,
                    p_114491_.getY() - 2.0,
                    p_114491_.getZ() - 2.0,
                    p_114491_.getX() + 2.0,
                    p_114491_.getY() + 2.0,
                    p_114491_.getZ() + 2.0
                );
            }

            if (p_114492_.isVisible(aabb)) {
                return true;
            } else {
                if (p_114491_ instanceof Leashable leashable) {
                    Entity entity = leashable.getLeashHolder();
                    if (entity != null) {
                        return p_114492_.isVisible(this.entityRenderDispatcher.getRenderer(entity).getBoundingBoxForCulling(entity));
                    }
                }

                return false;
            }
        }
    }

    protected AABB getBoundingBoxForCulling(T p_361260_) {
        return p_361260_.getBoundingBox();
    }

    protected boolean affectedByCulling(T p_365169_) {
        return true;
    }

    public Vec3 getRenderOffset(S p_364311_) {
        return p_364311_.passengerOffset != null ? p_364311_.passengerOffset : Vec3.ZERO;
    }

    public void render(S p_364816_, PoseStack p_114488_, MultiBufferSource p_114489_, int p_114490_) {
        EntityRenderState.LeashState entityrenderstate$leashstate = p_364816_.leashState;
        if (entityrenderstate$leashstate != null) {
            renderLeash(p_114488_, p_114489_, entityrenderstate$leashstate);
        }

        if (p_364816_.nameTag != null) {
            this.renderNameTag(p_364816_, p_364816_.nameTag, p_114488_, p_114489_, p_114490_);
        }
    }

    private static void renderLeash(PoseStack p_352205_, MultiBufferSource p_352444_, EntityRenderState.LeashState p_364529_) {
        float f = 0.025F;
        float f1 = (float)(p_364529_.end.x - p_364529_.start.x);
        float f2 = (float)(p_364529_.end.y - p_364529_.start.y);
        float f3 = (float)(p_364529_.end.z - p_364529_.start.z);
        float f4 = Mth.invSqrt(f1 * f1 + f3 * f3) * 0.025F / 2.0F;
        float f5 = f3 * f4;
        float f6 = f1 * f4;
        p_352205_.pushPose();
        p_352205_.translate(p_364529_.offset);
        VertexConsumer vertexconsumer = p_352444_.getBuffer(RenderType.leash());
        Matrix4f matrix4f = p_352205_.last().pose();

        for (int i = 0; i <= 24; i++) {
            addVertexPair(
                vertexconsumer,
                matrix4f,
                f1,
                f2,
                f3,
                p_364529_.startBlockLight,
                p_364529_.endBlockLight,
                p_364529_.startSkyLight,
                p_364529_.endSkyLight,
                0.025F,
                0.025F,
                f5,
                f6,
                i,
                false
            );
        }

        for (int j = 24; j >= 0; j--) {
            addVertexPair(
                vertexconsumer,
                matrix4f,
                f1,
                f2,
                f3,
                p_364529_.startBlockLight,
                p_364529_.endBlockLight,
                p_364529_.startSkyLight,
                p_364529_.endSkyLight,
                0.025F,
                0.0F,
                f5,
                f6,
                j,
                true
            );
        }

        p_352205_.popPose();
    }

    private static void addVertexPair(
        VertexConsumer p_352095_,
        Matrix4f p_352142_,
        float p_352462_,
        float p_352226_,
        float p_352086_,
        int p_352406_,
        int p_352470_,
        int p_352371_,
        int p_352167_,
        float p_352293_,
        float p_352138_,
        float p_352315_,
        float p_352162_,
        int p_352291_,
        boolean p_352079_
    ) {
        float f = (float)p_352291_ / 24.0F;
        int i = (int)Mth.lerp(f, (float)p_352406_, (float)p_352470_);
        int j = (int)Mth.lerp(f, (float)p_352371_, (float)p_352167_);
        int k = LightTexture.pack(i, j);
        float f1 = p_352291_ % 2 == (p_352079_ ? 1 : 0) ? 0.7F : 1.0F;
        float f2 = 0.5F * f1;
        float f3 = 0.4F * f1;
        float f4 = 0.3F * f1;
        float f5 = p_352462_ * f;
        float f6 = p_352226_ > 0.0F ? p_352226_ * f * f : p_352226_ - p_352226_ * (1.0F - f) * (1.0F - f);
        float f7 = p_352086_ * f;
        p_352095_.addVertex(p_352142_, f5 - p_352315_, f6 + p_352138_, f7 + p_352162_).setColor(f2, f3, f4, 1.0F).setLight(k);
        p_352095_.addVertex(p_352142_, f5 + p_352315_, f6 + p_352293_ - p_352138_, f7 - p_352162_).setColor(f2, f3, f4, 1.0F).setLight(k);
    }

    protected boolean shouldShowName(T p_114504_, double p_361299_) {
        return p_114504_.shouldShowName() || p_114504_.hasCustomName() && p_114504_ == this.entityRenderDispatcher.crosshairPickEntity;
    }

    public Font getFont() {
        return this.font;
    }

    protected void renderNameTag(S p_360768_, Component p_114499_, PoseStack p_114500_, MultiBufferSource p_114501_, int p_114502_) {
        Vec3 vec3 = p_360768_.nameTagAttachment;
        if (vec3 != null) {
            boolean flag = !p_360768_.isDiscrete;
            int i = "deadmau5".equals(p_114499_.getString()) ? -10 : 0;
            p_114500_.pushPose();
            p_114500_.translate(vec3.x, vec3.y + 0.5, vec3.z);
            p_114500_.mulPose(this.entityRenderDispatcher.cameraOrientation());
            p_114500_.scale(0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = p_114500_.last().pose();
            Font font = this.getFont();
            float f = (float)(-font.width(p_114499_)) / 2.0F;
            int j = (int)(Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
            font.drawInBatch(
                p_114499_, f, (float)i, -2130706433, false, matrix4f, p_114501_, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, p_114502_
            );
            if (flag) {
                font.drawInBatch(
                    p_114499_, f, (float)i, -1, false, matrix4f, p_114501_, Font.DisplayMode.NORMAL, 0, LightTexture.lightCoordsWithEmission(p_114502_, 2)
                );
            }

            p_114500_.popPose();
        }
    }

    @Nullable
    protected Component getNameTag(T p_364352_) {
        return p_364352_.getDisplayName();
    }

    protected float getShadowRadius(S p_365191_) {
        return this.shadowRadius;
    }

    protected float getShadowStrength(S p_383214_) {
        return this.shadowStrength;
    }

    public abstract S createRenderState();

    public final S createRenderState(T p_361382_, float p_360885_) {
        S s = this.reusedState;
        this.extractRenderState(p_361382_, s, p_360885_);
        return s;
    }

    public void extractRenderState(T p_362104_, S p_361028_, float p_362204_) {
        p_361028_.x = Mth.lerp((double)p_362204_, p_362104_.xOld, p_362104_.getX());
        p_361028_.y = Mth.lerp((double)p_362204_, p_362104_.yOld, p_362104_.getY());
        p_361028_.z = Mth.lerp((double)p_362204_, p_362104_.zOld, p_362104_.getZ());
        p_361028_.isInvisible = p_362104_.isInvisible();
        p_361028_.ageInTicks = (float)p_362104_.tickCount + p_362204_;
        p_361028_.boundingBoxWidth = p_362104_.getBbWidth();
        p_361028_.boundingBoxHeight = p_362104_.getBbHeight();
        p_361028_.eyeHeight = p_362104_.getEyeHeight();
        if (p_362104_.isPassenger()
            && p_362104_.getVehicle() instanceof AbstractMinecart abstractminecart
            && abstractminecart.getBehavior() instanceof NewMinecartBehavior newminecartbehavior
            && newminecartbehavior.cartHasPosRotLerp()) {
            double d2 = Mth.lerp((double)p_362204_, abstractminecart.xOld, abstractminecart.getX());
            double d0 = Mth.lerp((double)p_362204_, abstractminecart.yOld, abstractminecart.getY());
            double d1 = Mth.lerp((double)p_362204_, abstractminecart.zOld, abstractminecart.getZ());
            p_361028_.passengerOffset = newminecartbehavior.getCartLerpPosition(p_362204_).subtract(new Vec3(d2, d0, d1));
        } else {
            p_361028_.passengerOffset = null;
        }

        p_361028_.distanceToCameraSq = this.entityRenderDispatcher.distanceToSqr(p_362104_);
        boolean flag = p_361028_.distanceToCameraSq < 4096.0 && this.shouldShowName(p_362104_, p_361028_.distanceToCameraSq);
        if (flag) {
            p_361028_.nameTag = this.getNameTag(p_362104_);
            p_361028_.nameTagAttachment = p_362104_.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, p_362104_.getYRot(p_362204_));
        } else {
            p_361028_.nameTag = null;
        }

        p_361028_.isDiscrete = p_362104_.isDiscrete();
        Entity entity = p_362104_ instanceof Leashable leashable ? leashable.getLeashHolder() : null;
        if (entity != null) {
            float f = p_362104_.getPreciseBodyRotation(p_362204_) * (float) (Math.PI / 180.0);
            Vec3 vec3 = p_362104_.getLeashOffset(p_362204_).yRot(-f);
            BlockPos blockpos1 = BlockPos.containing(p_362104_.getEyePosition(p_362204_));
            BlockPos blockpos = BlockPos.containing(entity.getEyePosition(p_362204_));
            if (p_361028_.leashState == null) {
                p_361028_.leashState = new EntityRenderState.LeashState();
            }

            EntityRenderState.LeashState entityrenderstate$leashstate = p_361028_.leashState;
            entityrenderstate$leashstate.offset = vec3;
            entityrenderstate$leashstate.start = p_362104_.getPosition(p_362204_).add(vec3);
            entityrenderstate$leashstate.end = entity.getRopeHoldPosition(p_362204_);
            entityrenderstate$leashstate.startBlockLight = this.getBlockLightLevel(p_362104_, blockpos1);
            entityrenderstate$leashstate.endBlockLight = this.entityRenderDispatcher.getRenderer(entity).getBlockLightLevel(entity, blockpos);
            entityrenderstate$leashstate.startSkyLight = p_362104_.level().getBrightness(LightLayer.SKY, blockpos1);
            entityrenderstate$leashstate.endSkyLight = p_362104_.level().getBrightness(LightLayer.SKY, blockpos);
        } else {
            p_361028_.leashState = null;
        }

        p_361028_.displayFireAnimation = p_362104_.displayFireAnimation();
    }
}
