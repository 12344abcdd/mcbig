package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SaddleableRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SaddleLayer<S extends LivingEntityRenderState & SaddleableRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private final ResourceLocation textureLocation;
    private final M adultModel;
    private final M babyModel;

    public SaddleLayer(RenderLayerParent<S, M> p_360979_, M p_365032_, M p_361738_, ResourceLocation p_360654_) {
        super(p_360979_);
        this.adultModel = p_365032_;
        this.babyModel = p_361738_;
        this.textureLocation = p_360654_;
    }

    public SaddleLayer(RenderLayerParent<S, M> p_117390_, M p_117391_, ResourceLocation p_117392_) {
        this(p_117390_, p_117391_, p_117391_, p_117392_);
    }

    public void render(PoseStack p_117394_, MultiBufferSource p_117395_, int p_117396_, S p_363179_, float p_117398_, float p_117399_) {
        if (p_363179_.isSaddled()) {
            M m = p_363179_.isBaby ? this.babyModel : this.adultModel;
            m.setupAnim(p_363179_);
            VertexConsumer vertexconsumer = p_117395_.getBuffer(RenderType.entityCutoutNoCull(this.textureLocation));
            m.renderToBuffer(p_117394_, vertexconsumer, p_117396_, OverlayTexture.NO_OVERLAY);
        }
    }
}
