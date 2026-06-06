package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemInHandLayer<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel> extends RenderLayer<S, M> {
    public ItemInHandLayer(RenderLayerParent<S, M> p_234846_) {
        super(p_234846_);
    }

    public void render(PoseStack p_117193_, MultiBufferSource p_117194_, int p_117195_, S p_386634_, float p_117197_, float p_117198_) {
        this.renderArmWithItem(p_386634_, p_386634_.rightHandItem, HumanoidArm.RIGHT, p_117193_, p_117194_, p_117195_);
        this.renderArmWithItem(p_386634_, p_386634_.leftHandItem, HumanoidArm.LEFT, p_117193_, p_117194_, p_117195_);
    }

    protected void renderArmWithItem(
        S p_388299_, ItemStackRenderState p_388580_, HumanoidArm p_117188_, PoseStack p_117189_, MultiBufferSource p_117190_, int p_117191_
    ) {
        if (!p_388580_.isEmpty()) {
            p_117189_.pushPose();
            this.getParentModel().translateToHand(p_117188_, p_117189_);
            p_117189_.mulPose(Axis.XP.rotationDegrees(-90.0F));
            p_117189_.mulPose(Axis.YP.rotationDegrees(180.0F));
            boolean flag = p_117188_ == HumanoidArm.LEFT;
            p_117189_.translate((float)(flag ? -1 : 1) / 16.0F, 0.125F, -0.625F);
            p_388580_.render(p_117189_, p_117190_, p_117191_, OverlayTexture.NO_OVERLAY);
            p_117189_.popPose();
        }
    }
}
