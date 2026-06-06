package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CowRenderer extends AgeableMobRenderer<Cow, LivingEntityRenderState, CowModel> {
    private static final ResourceLocation COW_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/cow/cow.png");

    public CowRenderer(EntityRendererProvider.Context p_173956_) {
        super(p_173956_, new CowModel(p_173956_.bakeLayer(ModelLayers.COW)), new CowModel(p_173956_.bakeLayer(ModelLayers.COW_BABY)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState p_364051_) {
        return COW_LOCATION;
    }

    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    public void extractRenderState(Cow p_364800_, LivingEntityRenderState p_363914_, float p_360614_) {
        super.extractRenderState(p_364800_, p_363914_, p_360614_);
    }
}
