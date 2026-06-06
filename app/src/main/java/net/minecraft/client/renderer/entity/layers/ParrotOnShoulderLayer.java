package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Parrot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotOnShoulderLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
    private final ParrotModel model;
    private final ParrotRenderState parrotState = new ParrotRenderState();

    public ParrotOnShoulderLayer(RenderLayerParent<PlayerRenderState, PlayerModel> p_174511_, EntityModelSet p_174512_) {
        super(p_174511_);
        this.model = new ParrotModel(p_174512_.bakeLayer(ModelLayers.PARROT));
        this.parrotState.pose = ParrotModel.Pose.ON_SHOULDER;
    }

    public void render(PoseStack p_117307_, MultiBufferSource p_117308_, int p_117309_, PlayerRenderState p_360723_, float p_117311_, float p_117312_) {
        Parrot.Variant parrot$variant = p_360723_.parrotOnLeftShoulder;
        if (parrot$variant != null) {
            this.renderOnShoulder(p_117307_, p_117308_, p_117309_, p_360723_, parrot$variant, p_117311_, p_117312_, true);
        }

        Parrot.Variant parrot$variant1 = p_360723_.parrotOnRightShoulder;
        if (parrot$variant1 != null) {
            this.renderOnShoulder(p_117307_, p_117308_, p_117309_, p_360723_, parrot$variant1, p_117311_, p_117312_, false);
        }
    }

    private void renderOnShoulder(
        PoseStack p_361619_,
        MultiBufferSource p_364834_,
        int p_364239_,
        PlayerRenderState p_364485_,
        Parrot.Variant p_365484_,
        float p_360279_,
        float p_363980_,
        boolean p_365529_
    ) {
        p_361619_.pushPose();
        p_361619_.translate(p_365529_ ? 0.4F : -0.4F, p_364485_.isCrouching ? -1.3F : -1.5F, 0.0F);
        this.parrotState.ageInTicks = p_364485_.ageInTicks;
        this.parrotState.walkAnimationPos = p_364485_.walkAnimationPos;
        this.parrotState.walkAnimationSpeed = p_364485_.walkAnimationSpeed;
        this.parrotState.yRot = p_360279_;
        this.parrotState.xRot = p_363980_;
        this.model.setupAnim(this.parrotState);
        this.model
            .renderToBuffer(
                p_361619_, p_364834_.getBuffer(this.model.renderType(ParrotRenderer.getVariantTexture(p_365484_))), p_364239_, OverlayTexture.NO_OVERLAY
            );
        p_361619_.popPose();
    }
}
