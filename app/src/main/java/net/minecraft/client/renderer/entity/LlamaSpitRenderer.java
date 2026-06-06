package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LlamaSpitRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaSpitRenderer extends EntityRenderer<LlamaSpit, LlamaSpitRenderState> {
    private static final ResourceLocation LLAMA_SPIT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/llama/spit.png");
    private final LlamaSpitModel model;

    public LlamaSpitRenderer(EntityRendererProvider.Context p_174296_) {
        super(p_174296_);
        this.model = new LlamaSpitModel(p_174296_.bakeLayer(ModelLayers.LLAMA_SPIT));
    }

    public void render(LlamaSpitRenderState p_365428_, PoseStack p_115376_, MultiBufferSource p_115377_, int p_115378_) {
        p_115376_.pushPose();
        p_115376_.translate(0.0F, 0.15F, 0.0F);
        p_115376_.mulPose(Axis.YP.rotationDegrees(p_365428_.yRot - 90.0F));
        p_115376_.mulPose(Axis.ZP.rotationDegrees(p_365428_.xRot));
        this.model.setupAnim(p_365428_);
        VertexConsumer vertexconsumer = p_115377_.getBuffer(this.model.renderType(LLAMA_SPIT_LOCATION));
        this.model.renderToBuffer(p_115376_, vertexconsumer, p_115378_, OverlayTexture.NO_OVERLAY);
        p_115376_.popPose();
        super.render(p_365428_, p_115376_, p_115377_, p_115378_);
    }

    public LlamaSpitRenderState createRenderState() {
        return new LlamaSpitRenderState();
    }

    public void extractRenderState(LlamaSpit p_363068_, LlamaSpitRenderState p_363885_, float p_363897_) {
        super.extractRenderState(p_363068_, p_363885_, p_363897_);
        p_363885_.xRot = p_363068_.getXRot(p_363897_);
        p_363885_.yRot = p_363068_.getYRot(p_363897_);
    }
}
