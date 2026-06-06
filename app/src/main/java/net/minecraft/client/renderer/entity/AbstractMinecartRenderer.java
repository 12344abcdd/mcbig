package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Objects;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.OldMinecartBehavior;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractMinecartRenderer<T extends AbstractMinecart, S extends MinecartRenderState> extends EntityRenderer<T, S> {
    private static final ResourceLocation MINECART_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/minecart.png");
    protected final MinecartModel model;
    private final BlockRenderDispatcher blockRenderer;

    public AbstractMinecartRenderer(EntityRendererProvider.Context p_363408_, ModelLayerLocation p_360820_) {
        super(p_363408_);
        this.shadowRadius = 0.7F;
        this.model = new MinecartModel(p_363408_.bakeLayer(p_360820_));
        this.blockRenderer = p_363408_.getBlockRenderDispatcher();
    }

    public void render(S p_363194_, PoseStack p_360847_, MultiBufferSource p_363596_, int p_361740_) {
        super.render(p_363194_, p_360847_, p_363596_, p_361740_);
        p_360847_.pushPose();
        long i = p_363194_.offsetSeed;
        float f = (((float)(i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float f1 = (((float)(i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float f2 = (((float)(i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        p_360847_.translate(f, f1, f2);
        if (p_363194_.isNewRender) {
            newRender(p_363194_, p_360847_);
        } else {
            oldRender(p_363194_, p_360847_);
        }

        float f3 = p_363194_.hurtTime;
        if (f3 > 0.0F) {
            p_360847_.mulPose(Axis.XP.rotationDegrees(Mth.sin(f3) * f3 * p_363194_.damageTime / 10.0F * (float)p_363194_.hurtDir));
        }

        BlockState blockstate = p_363194_.displayBlockState;
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            p_360847_.pushPose();
            float f4 = 0.75F;
            p_360847_.scale(0.75F, 0.75F, 0.75F);
            p_360847_.translate(-0.5F, (float)(p_363194_.displayOffset - 8) / 16.0F, 0.5F);
            p_360847_.mulPose(Axis.YP.rotationDegrees(90.0F));
            this.renderMinecartContents(p_363194_, blockstate, p_360847_, p_363596_, p_361740_);
            p_360847_.popPose();
        }

        p_360847_.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim(p_363194_);
        VertexConsumer vertexconsumer = p_363596_.getBuffer(this.model.renderType(MINECART_LOCATION));
        this.model.renderToBuffer(p_360847_, vertexconsumer, p_361740_, OverlayTexture.NO_OVERLAY);
        p_360847_.popPose();
    }

    private static <S extends MinecartRenderState> void newRender(S p_360917_, PoseStack p_362046_) {
        p_362046_.mulPose(Axis.YP.rotationDegrees(p_360917_.yRot));
        p_362046_.mulPose(Axis.ZP.rotationDegrees(-p_360917_.xRot));
        p_362046_.translate(0.0F, 0.375F, 0.0F);
    }

    private static <S extends MinecartRenderState> void oldRender(S p_364095_, PoseStack p_360278_) {
        double d0 = p_364095_.x;
        double d1 = p_364095_.y;
        double d2 = p_364095_.z;
        float f = p_364095_.xRot;
        float f1 = p_364095_.yRot;
        if (p_364095_.posOnRail != null && p_364095_.frontPos != null && p_364095_.backPos != null) {
            Vec3 vec3 = p_364095_.frontPos;
            Vec3 vec31 = p_364095_.backPos;
            p_360278_.translate(p_364095_.posOnRail.x - d0, (vec3.y + vec31.y) / 2.0 - d1, p_364095_.posOnRail.z - d2);
            Vec3 vec32 = vec31.add(-vec3.x, -vec3.y, -vec3.z);
            if (vec32.length() != 0.0) {
                vec32 = vec32.normalize();
                f1 = (float)(Math.atan2(vec32.z, vec32.x) * 180.0 / Math.PI);
                f = (float)(Math.atan(vec32.y) * 73.0);
            }
        }

        p_360278_.translate(0.0F, 0.375F, 0.0F);
        p_360278_.mulPose(Axis.YP.rotationDegrees(180.0F - f1));
        p_360278_.mulPose(Axis.ZP.rotationDegrees(-f));
    }

    public void extractRenderState(T p_360458_, S p_361455_, float p_363949_) {
        super.extractRenderState(p_360458_, p_361455_, p_363949_);
        if (p_360458_.getBehavior() instanceof NewMinecartBehavior newminecartbehavior) {
            newExtractState(p_360458_, newminecartbehavior, p_361455_, p_363949_);
            p_361455_.isNewRender = true;
        } else if (p_360458_.getBehavior() instanceof OldMinecartBehavior oldminecartbehavior) {
            oldExtractState(p_360458_, oldminecartbehavior, p_361455_, p_363949_);
            p_361455_.isNewRender = false;
        }

        long i = (long)p_360458_.getId() * 493286711L;
        p_361455_.offsetSeed = i * i * 4392167121L + i * 98761L;
        p_361455_.hurtTime = (float)p_360458_.getHurtTime() - p_363949_;
        p_361455_.hurtDir = p_360458_.getHurtDir();
        p_361455_.damageTime = Math.max(p_360458_.getDamage() - p_363949_, 0.0F);
        p_361455_.displayOffset = p_360458_.getDisplayOffset();
        p_361455_.displayBlockState = p_360458_.getDisplayBlockState();
    }

    private static <T extends AbstractMinecart, S extends MinecartRenderState> void newExtractState(
        T p_365349_, NewMinecartBehavior p_365110_, S p_363052_, float p_364223_
    ) {
        if (p_365110_.cartHasPosRotLerp()) {
            p_363052_.renderPos = p_365110_.getCartLerpPosition(p_364223_);
            p_363052_.xRot = p_365110_.getCartLerpXRot(p_364223_);
            p_363052_.yRot = p_365110_.getCartLerpYRot(p_364223_);
        } else {
            p_363052_.renderPos = null;
            p_363052_.xRot = p_365349_.getXRot();
            p_363052_.yRot = p_365349_.getYRot();
        }
    }

    private static <T extends AbstractMinecart, S extends MinecartRenderState> void oldExtractState(
        T p_363303_, OldMinecartBehavior p_363748_, S p_360336_, float p_363476_
    ) {
        float f = 0.3F;
        p_360336_.xRot = p_363303_.getXRot(p_363476_);
        p_360336_.yRot = p_363303_.getYRot(p_363476_);
        double d0 = p_360336_.x;
        double d1 = p_360336_.y;
        double d2 = p_360336_.z;
        Vec3 vec3 = p_363748_.getPos(d0, d1, d2);
        if (vec3 != null) {
            p_360336_.posOnRail = vec3;
            Vec3 vec31 = p_363748_.getPosOffs(d0, d1, d2, 0.3F);
            Vec3 vec32 = p_363748_.getPosOffs(d0, d1, d2, -0.3F);
            p_360336_.frontPos = Objects.requireNonNullElse(vec31, vec3);
            p_360336_.backPos = Objects.requireNonNullElse(vec32, vec3);
        } else {
            p_360336_.posOnRail = null;
            p_360336_.frontPos = null;
            p_360336_.backPos = null;
        }
    }

    protected void renderMinecartContents(S p_365106_, BlockState p_363143_, PoseStack p_363296_, MultiBufferSource p_362225_, int p_361529_) {
        this.blockRenderer.renderSingleBlock(p_363143_, p_363296_, p_362225_, p_361529_, OverlayTexture.NO_OVERLAY);
    }

    protected AABB getBoundingBoxForCulling(T p_361172_) {
        AABB aabb = super.getBoundingBoxForCulling(p_361172_);
        return p_361172_.hasCustomDisplay() ? aabb.inflate((double)Math.abs(p_361172_.getDisplayOffset()) / 16.0) : aabb;
    }

    public Vec3 getRenderOffset(S p_363702_) {
        Vec3 vec3 = super.getRenderOffset(p_363702_);
        return p_363702_.isNewRender && p_363702_.renderPos != null
            ? vec3.add(p_363702_.renderPos.x - p_363702_.x, p_363702_.renderPos.y - p_363702_.y, p_363702_.renderPos.z - p_363702_.z)
            : vec3;
    }
}
