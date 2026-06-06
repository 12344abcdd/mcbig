package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArrowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ArrowRenderer<T extends AbstractArrow, S extends ArrowRenderState> extends EntityRenderer<T, S> {
    private final ArrowModel model;

    public ArrowRenderer(EntityRendererProvider.Context p_173917_) {
        super(p_173917_);
        this.model = new ArrowModel(p_173917_.bakeLayer(ModelLayers.ARROW));
    }

    public void render(S p_361021_, PoseStack p_113822_, MultiBufferSource p_113823_, int p_113824_) {
        p_113822_.pushPose();
        p_113822_.mulPose(Axis.YP.rotationDegrees(p_361021_.yRot - 90.0F));
        p_113822_.mulPose(Axis.ZP.rotationDegrees(p_361021_.xRot));
        VertexConsumer vertexconsumer = p_113823_.getBuffer(RenderType.entityCutout(this.getTextureLocation(p_361021_)));
        this.model.setupAnim(p_361021_);
        this.model.renderToBuffer(p_113822_, vertexconsumer, p_113824_, OverlayTexture.NO_OVERLAY);
        p_113822_.popPose();
        super.render(p_361021_, p_113822_, p_113823_, p_113824_);
    }

    protected abstract ResourceLocation getTextureLocation(S p_368566_);

    public void extractRenderState(T p_361771_, S p_364204_, float p_360538_) {
        super.extractRenderState(p_361771_, p_364204_, p_360538_);
        p_364204_.xRot = p_361771_.getXRot(p_360538_);
        p_364204_.yRot = p_361771_.getYRot(p_360538_);
        p_364204_.shake = (float)p_361771_.shakeTime - p_360538_;
    }
}
