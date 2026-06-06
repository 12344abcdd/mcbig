package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.item.CrossbowItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class IllagerRenderer<T extends AbstractIllager, S extends IllagerRenderState> extends MobRenderer<T, S, IllagerModel<S>> {
    protected IllagerRenderer(EntityRendererProvider.Context p_174182_, IllagerModel<S> p_174183_, float p_174184_) {
        super(p_174182_, p_174183_, p_174184_);
        this.addLayer(new CustomHeadLayer<>(this, p_174182_.getModelSet()));
    }

    public void extractRenderState(T p_365030_, S p_364586_, float p_360560_) {
        super.extractRenderState(p_365030_, p_364586_, p_360560_);
        ArmedEntityRenderState.extractArmedEntityRenderState(p_365030_, p_364586_, this.itemModelResolver);
        p_364586_.isRiding = p_365030_.isPassenger();
        p_364586_.mainArm = p_365030_.getMainArm();
        p_364586_.armPose = p_365030_.getArmPose();
        p_364586_.maxCrossbowChargeDuration = p_364586_.armPose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE
            ? CrossbowItem.getChargeDuration(p_365030_.getUseItem(), p_365030_)
            : 0;
        p_364586_.ticksUsingItem = p_365030_.getTicksUsingItem();
        p_364586_.attackAnim = p_365030_.getAttackAnim(p_360560_);
        p_364586_.isAggressive = p_365030_.isAggressive();
    }
}
