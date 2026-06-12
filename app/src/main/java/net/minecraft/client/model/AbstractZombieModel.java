package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;

public abstract class AbstractZombieModel<S extends ZombieRenderState> extends HumanoidModel<S> {
    protected AbstractZombieModel(ModelPart p_170337_) {
        super(p_170337_);
    }

    public void setupAnim(S p_361684_) {
        super.setupAnim(p_361684_);
        float f = p_361684_.attackTime;
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, p_361684_.isAggressive, f, p_361684_.ageInTicks);
    }
}
