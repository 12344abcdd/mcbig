package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Chicken;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChickenRenderer extends AgeableMobRenderer<Chicken, ChickenRenderState, ChickenModel> {
    private static final ResourceLocation CHICKEN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/chicken.png");

    public ChickenRenderer(EntityRendererProvider.Context p_173952_) {
        super(p_173952_, new ChickenModel(p_173952_.bakeLayer(ModelLayers.CHICKEN)), new ChickenModel(p_173952_.bakeLayer(ModelLayers.CHICKEN_BABY)), 0.3F);
    }

    public ResourceLocation getTextureLocation(ChickenRenderState p_364952_) {
        return CHICKEN_LOCATION;
    }

    public ChickenRenderState createRenderState() {
        return new ChickenRenderState();
    }

    public void extractRenderState(Chicken p_361981_, ChickenRenderState p_365088_, float p_364120_) {
        super.extractRenderState(p_361981_, p_365088_, p_364120_);
        p_365088_.flap = Mth.lerp(p_364120_, p_361981_.oFlap, p_361981_.flap);
        p_365088_.flapSpeed = Mth.lerp(p_364120_, p_361981_.oFlapSpeed, p_361981_.flapSpeed);
    }
}
