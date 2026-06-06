package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreezeEyesLayer extends RenderLayer<BreezeRenderState, BreezeModel> {
    private static final RenderType BREEZE_EYES = RenderType.breezeEyes(ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze_eyes.png"));

    public BreezeEyesLayer(RenderLayerParent<BreezeRenderState, BreezeModel> p_312409_) {
        super(p_312409_);
    }

    public void render(PoseStack p_312883_, MultiBufferSource p_312624_, int p_312874_, BreezeRenderState p_365352_, float p_312852_, float p_311980_) {
        VertexConsumer vertexconsumer = p_312624_.getBuffer(BREEZE_EYES);
        BreezeModel breezemodel = this.getParentModel();
        BreezeRenderer.enable(breezemodel, breezemodel.head(), breezemodel.eyes())
            .renderToBuffer(p_312883_, vertexconsumer, p_312874_, OverlayTexture.NO_OVERLAY);
    }
}
