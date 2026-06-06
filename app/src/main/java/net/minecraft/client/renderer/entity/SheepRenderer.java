package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SheepWoolLayer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepRenderer extends AgeableMobRenderer<Sheep, SheepRenderState, SheepModel> {
    private static final ResourceLocation SHEEP_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/sheep/sheep.png");

    public SheepRenderer(EntityRendererProvider.Context p_174366_) {
        super(p_174366_, new SheepModel(p_174366_.bakeLayer(ModelLayers.SHEEP)), new SheepModel(p_174366_.bakeLayer(ModelLayers.SHEEP_BABY)), 0.7F);
        this.addLayer(new SheepWoolLayer(this, p_174366_.getModelSet()));
    }

    public ResourceLocation getTextureLocation(SheepRenderState p_360570_) {
        return SHEEP_LOCATION;
    }

    public SheepRenderState createRenderState() {
        return new SheepRenderState();
    }

    public void extractRenderState(Sheep p_362573_, SheepRenderState p_362333_, float p_360543_) {
        super.extractRenderState(p_362573_, p_362333_, p_360543_);
        p_362333_.headEatAngleScale = p_362573_.getHeadEatAngleScale(p_360543_);
        p_362333_.headEatPositionScale = p_362573_.getHeadEatPositionScale(p_360543_);
        p_362333_.isSheared = p_362573_.isSheared();
        p_362333_.woolColor = p_362573_.getColor();
        p_362333_.id = p_362573_.getId();
    }
}
