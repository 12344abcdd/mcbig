package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.GiantZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Giant;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GiantMobRenderer extends MobRenderer<Giant, ZombieRenderState, HumanoidModel<ZombieRenderState>> {
    private static final ResourceLocation ZOMBIE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

    public GiantMobRenderer(EntityRendererProvider.Context p_174131_, float p_174132_) {
        super(p_174131_, new GiantZombieModel(p_174131_.bakeLayer(ModelLayers.GIANT)), 0.5F * p_174132_);
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new GiantZombieModel(p_174131_.bakeLayer(ModelLayers.GIANT_INNER_ARMOR)),
                new GiantZombieModel(p_174131_.bakeLayer(ModelLayers.GIANT_OUTER_ARMOR)),
                p_174131_.getEquipmentRenderer()
            )
        );
    }

    public ResourceLocation getTextureLocation(ZombieRenderState p_360439_) {
        return ZOMBIE_LOCATION;
    }

    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    public void extractRenderState(Giant p_362549_, ZombieRenderState p_363714_, float p_361511_) {
        super.extractRenderState(p_362549_, p_363714_, p_361511_);
        HumanoidMobRenderer.extractHumanoidRenderState(p_362549_, p_363714_, p_361511_, this.itemModelResolver);
    }
}
