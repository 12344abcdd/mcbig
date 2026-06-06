package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoatRenderer extends AbstractBoatRenderer {
    private final Model waterPatchModel;
    private final ResourceLocation texture;
    private final EntityModel<BoatRenderState> model;

    public BoatRenderer(EntityRendererProvider.Context p_234563_, ModelLayerLocation p_376172_) {
        super(p_234563_);
        this.texture = p_376172_.model().withPath(p_375447_ -> "textures/entity/" + p_375447_ + ".png");
        this.waterPatchModel = new Model.Simple(p_234563_.bakeLayer(ModelLayers.BOAT_WATER_PATCH), p_359275_ -> RenderType.waterMask());
        this.model = new BoatModel(p_234563_.bakeLayer(p_376172_));
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.model;
    }

    @Override
    protected RenderType renderType() {
        return this.model.renderType(this.texture);
    }

    @Override
    protected void renderTypeAdditions(BoatRenderState p_376691_, PoseStack p_376523_, MultiBufferSource p_376756_, int p_376697_) {
        if (!p_376691_.isUnderWater) {
            this.waterPatchModel
                .renderToBuffer(p_376523_, p_376756_.getBuffer(this.waterPatchModel.renderType(this.texture)), p_376697_, OverlayTexture.NO_OVERLAY);
        }
    }
}
