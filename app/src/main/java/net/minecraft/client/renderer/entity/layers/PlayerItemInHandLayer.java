package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerItemInHandLayer<S extends PlayerRenderState, M extends EntityModel<S> & ArmedModel & HeadedModel> extends ItemInHandLayer<S, M> {
    private static final float X_ROT_MIN = (float) (-Math.PI / 6);
    private static final float X_ROT_MAX = (float) (Math.PI / 2);

    public PlayerItemInHandLayer(RenderLayerParent<S, M> p_234866_) {
        super(p_234866_);
    }

    protected void renderArmWithItem(
        S p_363789_, ItemStackRenderState p_386948_, HumanoidArm p_365271_, PoseStack p_362346_, MultiBufferSource p_360927_, int p_362805_
    ) {
        if (!p_386948_.isEmpty()) {
            InteractionHand interactionhand = p_365271_ == p_363789_.mainArm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            if (p_363789_.isUsingItem && p_363789_.useItemHand == interactionhand && p_363789_.attackTime < 1.0E-5F && !p_363789_.heldOnHead.isEmpty()) {
                this.renderItemHeldToEye(p_363789_.heldOnHead, p_365271_, p_362346_, p_360927_, p_362805_);
            } else {
                super.renderArmWithItem(p_363789_, p_386948_, p_365271_, p_362346_, p_360927_, p_362805_);
            }
        }
    }

    private void renderItemHeldToEye(ItemStackRenderState p_386957_, HumanoidArm p_388092_, PoseStack p_388896_, MultiBufferSource p_386448_, int p_388927_) {
        p_388896_.pushPose();
        this.getParentModel().root().translateAndRotate(p_388896_);
        ModelPart modelpart = this.getParentModel().getHead();
        float f = modelpart.xRot;
        modelpart.xRot = Mth.clamp(modelpart.xRot, (float) (-Math.PI / 6), (float) (Math.PI / 2));
        modelpart.translateAndRotate(p_388896_);
        modelpart.xRot = f;
        CustomHeadLayer.translateToHead(p_388896_, CustomHeadLayer.Transforms.DEFAULT);
        boolean flag = p_388092_ == HumanoidArm.LEFT;
        p_388896_.translate((flag ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
        p_386957_.render(p_388896_, p_386448_, p_388927_, OverlayTexture.NO_OVERLAY);
        p_388896_.popPose();
    }
}
