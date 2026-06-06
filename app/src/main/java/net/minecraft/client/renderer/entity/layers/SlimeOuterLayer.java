package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlimeOuterLayer extends RenderLayer<SlimeRenderState, SlimeModel> {
    private final SlimeModel model;

    public SlimeOuterLayer(RenderLayerParent<SlimeRenderState, SlimeModel> p_174536_, EntityModelSet p_174537_) {
        super(p_174536_);
        this.model = new SlimeModel(p_174537_.bakeLayer(ModelLayers.SLIME_OUTER));
    }

    public void render(PoseStack p_117470_, MultiBufferSource p_117471_, int p_117472_, SlimeRenderState p_360800_, float p_117474_, float p_117475_) {
        boolean flag = p_360800_.appearsGlowing && p_360800_.isInvisible;
        if (!p_360800_.isInvisible || flag) {
            VertexConsumer vertexconsumer;
            if (flag) {
                vertexconsumer = p_117471_.getBuffer(RenderType.outline(SlimeRenderer.SLIME_LOCATION));
            } else {
                vertexconsumer = p_117471_.getBuffer(RenderType.entityTranslucent(SlimeRenderer.SLIME_LOCATION));
            }

            this.model.setupAnim(p_360800_);
            this.model.renderToBuffer(p_117470_, vertexconsumer, p_117472_, LivingEntityRenderer.getOverlayCoords(p_360800_, 0.0F));
        }
    }
}
