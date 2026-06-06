package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreezeWindLayer extends RenderLayer<BreezeRenderState, BreezeModel> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze_wind.png");
    private final BreezeModel model;

    public BreezeWindLayer(EntityRendererProvider.Context p_350777_, RenderLayerParent<BreezeRenderState, BreezeModel> p_312625_) {
        super(p_312625_);
        this.model = new BreezeModel(p_350777_.bakeLayer(ModelLayers.BREEZE_WIND));
    }

    public void render(PoseStack p_312704_, MultiBufferSource p_312359_, int p_312773_, BreezeRenderState p_362521_, float p_312146_, float p_312128_) {
        VertexConsumer vertexconsumer = p_312359_.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(p_362521_.ageInTicks) % 1.0F, 0.0F));
        this.model.setupAnim(p_362521_);
        BreezeRenderer.enable(this.model, this.model.wind()).renderToBuffer(p_312704_, vertexconsumer, p_312773_, OverlayTexture.NO_OVERLAY);
    }

    private float xOffset(float p_312086_) {
        return p_312086_ * 0.02F;
    }
}
