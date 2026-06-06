package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractZombieRenderer<T extends Zombie, S extends ZombieRenderState, M extends ZombieModel<S>> extends HumanoidMobRenderer<T, S, M> {
    private static final ResourceLocation ZOMBIE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

    protected AbstractZombieRenderer(EntityRendererProvider.Context p_173910_, M p_173911_, M p_173912_, M p_173913_, M p_362588_, M p_362370_, M p_362061_) {
        super(p_173910_, p_173911_, p_173912_, 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, p_173913_, p_362588_, p_362370_, p_362061_, p_173910_.getEquipmentRenderer()));
    }

    public ResourceLocation getTextureLocation(S p_362921_) {
        return ZOMBIE_LOCATION;
    }

    public void extractRenderState(T p_360674_, S p_365238_, float p_361332_) {
        super.extractRenderState(p_360674_, p_365238_, p_361332_);
        p_365238_.isAggressive = p_360674_.isAggressive();
        p_365238_.isConverting = p_360674_.isUnderWaterConverting();
    }

    protected boolean isShaking(S p_363333_) {
        return super.isShaking(p_363333_) || p_363333_.isConverting;
    }
}
