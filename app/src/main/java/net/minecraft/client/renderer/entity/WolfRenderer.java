package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.animal.Wolf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfRenderer extends AgeableMobRenderer<Wolf, WolfRenderState, WolfModel> {
    public WolfRenderer(EntityRendererProvider.Context p_174452_) {
        super(p_174452_, new WolfModel(p_174452_.bakeLayer(ModelLayers.WOLF)), new WolfModel(p_174452_.bakeLayer(ModelLayers.WOLF_BABY)), 0.5F);
        this.addLayer(new WolfArmorLayer(this, p_174452_.getModelSet(), p_174452_.getEquipmentRenderer()));
        this.addLayer(new WolfCollarLayer(this));
    }

    protected int getModelTint(WolfRenderState p_365181_) {
        float f = p_365181_.wetShade;
        return f == 1.0F ? -1 : ARGB.colorFromFloat(1.0F, f, f, f);
    }

    public ResourceLocation getTextureLocation(WolfRenderState p_364302_) {
        return p_364302_.texture;
    }

    public WolfRenderState createRenderState() {
        return new WolfRenderState();
    }

    public void extractRenderState(Wolf p_363274_, WolfRenderState p_363549_, float p_362105_) {
        super.extractRenderState(p_363274_, p_363549_, p_362105_);
        p_363549_.isAngry = p_363274_.isAngry();
        p_363549_.isSitting = p_363274_.isInSittingPose();
        p_363549_.tailAngle = p_363274_.getTailAngle();
        p_363549_.headRollAngle = p_363274_.getHeadRollAngle(p_362105_);
        p_363549_.shakeAnim = p_363274_.getShakeAnim(p_362105_);
        p_363549_.texture = p_363274_.getTexture();
        p_363549_.wetShade = p_363274_.getWetShade(p_362105_);
        p_363549_.collarColor = p_363274_.isTame() ? p_363274_.getCollarColor() : null;
        p_363549_.bodyArmorItem = p_363274_.getBodyArmorItem().copy();
    }
}
