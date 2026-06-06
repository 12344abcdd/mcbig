package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.ShulkerBulletRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBulletRenderer extends EntityRenderer<ShulkerBullet, ShulkerBulletRenderState> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/shulker/spark.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE_LOCATION);
    private final ShulkerBulletModel model;

    public ShulkerBulletRenderer(EntityRendererProvider.Context p_174368_) {
        super(p_174368_);
        this.model = new ShulkerBulletModel(p_174368_.bakeLayer(ModelLayers.SHULKER_BULLET));
    }

    protected int getBlockLightLevel(ShulkerBullet p_115869_, BlockPos p_115870_) {
        return 15;
    }

    public void render(ShulkerBulletRenderState p_364049_, PoseStack p_115853_, MultiBufferSource p_115854_, int p_115855_) {
        p_115853_.pushPose();
        float f = p_364049_.ageInTicks;
        p_115853_.translate(0.0F, 0.15F, 0.0F);
        p_115853_.mulPose(Axis.YP.rotationDegrees(Mth.sin(f * 0.1F) * 180.0F));
        p_115853_.mulPose(Axis.XP.rotationDegrees(Mth.cos(f * 0.1F) * 180.0F));
        p_115853_.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f * 0.15F) * 360.0F));
        p_115853_.scale(-0.5F, -0.5F, 0.5F);
        this.model.setupAnim(p_364049_);
        VertexConsumer vertexconsumer = p_115854_.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.renderToBuffer(p_115853_, vertexconsumer, p_115855_, OverlayTexture.NO_OVERLAY);
        p_115853_.scale(1.5F, 1.5F, 1.5F);
        VertexConsumer vertexconsumer1 = p_115854_.getBuffer(RENDER_TYPE);
        this.model.renderToBuffer(p_115853_, vertexconsumer1, p_115855_, OverlayTexture.NO_OVERLAY, 654311423);
        p_115853_.popPose();
        super.render(p_364049_, p_115853_, p_115854_, p_115855_);
    }

    public ShulkerBulletRenderState createRenderState() {
        return new ShulkerBulletRenderState();
    }

    public void extractRenderState(ShulkerBullet p_364481_, ShulkerBulletRenderState p_363271_, float p_360710_) {
        super.extractRenderState(p_364481_, p_363271_, p_360710_);
        p_363271_.yRot = p_364481_.getYRot(p_360710_);
        p_363271_.xRot = p_364481_.getXRot(p_360710_);
    }
}
