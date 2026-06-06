package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerLikeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CrossedArmsItemLayer<S extends HoldingEntityRenderState, M extends EntityModel<S> & VillagerLikeModel> extends RenderLayer<S, M> {
    public CrossedArmsItemLayer(RenderLayerParent<S, M> p_234818_) {
        super(p_234818_);
    }

    public void render(PoseStack p_116699_, MultiBufferSource p_116700_, int p_116701_, S p_387384_, float p_116703_, float p_116704_) {
        ItemStackRenderState itemstackrenderstate = p_387384_.heldItem;
        if (!itemstackrenderstate.isEmpty()) {
            p_116699_.pushPose();
            this.applyTranslation(p_387384_, p_116699_);
            itemstackrenderstate.render(p_116699_, p_116700_, p_116701_, OverlayTexture.NO_OVERLAY);
            p_116699_.popPose();
        }
    }

    protected void applyTranslation(S p_386824_, PoseStack p_383057_) {
        this.getParentModel().translateToArms(p_383057_);
        p_383057_.mulPose(Axis.XP.rotation(0.75F));
        p_383057_.scale(1.07F, 1.07F, 1.07F);
        p_383057_.translate(0.0F, 0.13F, -0.34F);
        p_383057_.mulPose(Axis.XP.rotation((float) Math.PI));
    }
}
