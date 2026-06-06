package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkeletonClothingLayer<S extends SkeletonRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    private final SkeletonModel<S> layerModel;
    private final ResourceLocation clothesLocation;

    public SkeletonClothingLayer(RenderLayerParent<S, M> p_326918_, EntityModelSet p_326830_, ModelLayerLocation p_326794_, ResourceLocation p_326858_) {
        super(p_326918_);
        this.clothesLocation = p_326858_;
        this.layerModel = new SkeletonModel<>(p_326830_.bakeLayer(p_326794_));
    }

    public void render(PoseStack p_326898_, MultiBufferSource p_326791_, int p_326885_, S p_361596_, float p_326921_, float p_326877_) {
        coloredCutoutModelCopyLayerRender(this.layerModel, this.clothesLocation, p_326898_, p_326791_, p_326885_, p_361596_, -1);
    }
}
