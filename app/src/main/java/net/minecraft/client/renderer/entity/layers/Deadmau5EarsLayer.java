package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerEarsModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Deadmau5EarsLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
    private final HumanoidModel<PlayerRenderState> model;

    public Deadmau5EarsLayer(RenderLayerParent<PlayerRenderState, PlayerModel> p_116860_, EntityModelSet p_360915_) {
        super(p_116860_);
        this.model = new PlayerEarsModel(p_360915_.bakeLayer(ModelLayers.PLAYER_EARS));
    }

    public void render(PoseStack p_116873_, MultiBufferSource p_116874_, int p_116875_, PlayerRenderState p_361714_, float p_116877_, float p_116878_) {
        if ("deadmau5".equals(p_361714_.name) && !p_361714_.isInvisible) {
            VertexConsumer vertexconsumer = p_116874_.getBuffer(RenderType.entitySolid(p_361714_.skin.texture()));
            int i = LivingEntityRenderer.getOverlayCoords(p_361714_, 0.0F);
            this.getParentModel().copyPropertiesTo(this.model);
            this.model.setupAnim(p_361714_);
            this.model.renderToBuffer(p_116873_, vertexconsumer, p_116875_, i);
        }
    }
}
