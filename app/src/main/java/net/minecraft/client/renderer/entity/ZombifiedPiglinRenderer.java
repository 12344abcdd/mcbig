package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.ZombifiedPiglinModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombifiedPiglinRenderer extends HumanoidMobRenderer<ZombifiedPiglin, ZombifiedPiglinRenderState, ZombifiedPiglinModel> {
    private static final ResourceLocation ZOMBIFIED_PIGLIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/piglin/zombified_piglin.png");

    public ZombifiedPiglinRenderer(
        EntityRendererProvider.Context p_361123_,
        ModelLayerLocation p_360957_,
        ModelLayerLocation p_363099_,
        ModelLayerLocation p_364580_,
        ModelLayerLocation p_364456_,
        ModelLayerLocation p_362058_,
        ModelLayerLocation p_365154_
    ) {
        super(
            p_361123_,
            new ZombifiedPiglinModel(p_361123_.bakeLayer(p_360957_)),
            new ZombifiedPiglinModel(p_361123_.bakeLayer(p_363099_)),
            0.5F,
            PiglinRenderer.PIGLIN_CUSTOM_HEAD_TRANSFORMS
        );
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new HumanoidArmorModel(p_361123_.bakeLayer(p_364580_)),
                new HumanoidArmorModel(p_361123_.bakeLayer(p_364456_)),
                new HumanoidArmorModel(p_361123_.bakeLayer(p_362058_)),
                new HumanoidArmorModel(p_361123_.bakeLayer(p_362058_)),
                p_361123_.getEquipmentRenderer()
            )
        );
    }

    public ResourceLocation getTextureLocation(ZombifiedPiglinRenderState p_362156_) {
        return ZOMBIFIED_PIGLIN_LOCATION;
    }

    public ZombifiedPiglinRenderState createRenderState() {
        return new ZombifiedPiglinRenderState();
    }

    public void extractRenderState(ZombifiedPiglin p_363866_, ZombifiedPiglinRenderState p_361548_, float p_361739_) {
        super.extractRenderState(p_363866_, p_361548_, p_361739_);
        p_361548_.isAggressive = p_363866_.isAggressive();
    }
}
