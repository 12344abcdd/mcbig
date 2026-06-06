package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.CamelRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.camel.Camel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CamelRenderer extends AgeableMobRenderer<Camel, CamelRenderState, CamelModel> {
    private static final ResourceLocation CAMEL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/camel/camel.png");

    public CamelRenderer(EntityRendererProvider.Context p_251790_) {
        super(p_251790_, new CamelModel(p_251790_.bakeLayer(ModelLayers.CAMEL)), new CamelModel(p_251790_.bakeLayer(ModelLayers.CAMEL_BABY)), 0.7F);
    }

    public ResourceLocation getTextureLocation(CamelRenderState p_364241_) {
        return CAMEL_LOCATION;
    }

    public CamelRenderState createRenderState() {
        return new CamelRenderState();
    }

    public void extractRenderState(Camel p_362547_, CamelRenderState p_362381_, float p_360896_) {
        super.extractRenderState(p_362547_, p_362381_, p_360896_);
        p_362381_.isSaddled = p_362547_.isSaddled();
        p_362381_.isRidden = p_362547_.isVehicle();
        p_362381_.jumpCooldown = Math.max((float)p_362547_.getJumpCooldown() - p_360896_, 0.0F);
        p_362381_.sitAnimationState.copyFrom(p_362547_.sitAnimationState);
        p_362381_.sitPoseAnimationState.copyFrom(p_362547_.sitPoseAnimationState);
        p_362381_.sitUpAnimationState.copyFrom(p_362547_.sitUpAnimationState);
        p_362381_.idleAnimationState.copyFrom(p_362547_.idleAnimationState);
        p_362381_.dashAnimationState.copyFrom(p_362547_.dashAnimationState);
    }
}
