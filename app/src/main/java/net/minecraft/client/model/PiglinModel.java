package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PiglinModel extends AbstractPiglinModel<PiglinRenderState> {
    public PiglinModel(ModelPart p_170810_) {
        super(p_170810_);
    }

    public void setupAnim(PiglinRenderState p_362843_) {
        super.setupAnim(p_362843_);
        float f = (float) (Math.PI / 6);
        float f1 = p_362843_.attackTime;
        PiglinArmPose piglinarmpose = p_362843_.armPose;
        if (piglinarmpose == PiglinArmPose.DANCING) {
            float f2 = p_362843_.ageInTicks / 60.0F;
            this.rightEar.zRot = (float) (Math.PI / 6) + (float) (Math.PI / 180.0) * Mth.sin(f2 * 30.0F) * 10.0F;
            this.leftEar.zRot = (float) (-Math.PI / 6) - (float) (Math.PI / 180.0) * Mth.cos(f2 * 30.0F) * 10.0F;
            this.head.x = this.head.x + Mth.sin(f2 * 10.0F);
            this.head.y = this.head.y + Mth.sin(f2 * 40.0F) + 0.4F;
            this.rightArm.zRot = (float) (Math.PI / 180.0) * (70.0F + Mth.cos(f2 * 40.0F) * 10.0F);
            this.leftArm.zRot = this.rightArm.zRot * -1.0F;
            this.rightArm.y = this.rightArm.y + (Mth.sin(f2 * 40.0F) * 0.5F - 0.5F);
            this.leftArm.y = this.leftArm.y + Mth.sin(f2 * 40.0F) * 0.5F + 0.5F;
            this.body.y = this.body.y + Mth.sin(f2 * 40.0F) * 0.35F;
        } else if (piglinarmpose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && f1 == 0.0F) {
            this.holdWeaponHigh(p_362843_);
        } else if (piglinarmpose == PiglinArmPose.CROSSBOW_HOLD) {
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, p_362843_.mainArm == HumanoidArm.RIGHT);
        } else if (piglinarmpose == PiglinArmPose.CROSSBOW_CHARGE) {
            AnimationUtils.animateCrossbowCharge(
                this.rightArm, this.leftArm, p_362843_.maxCrossbowChageDuration, p_362843_.ticksUsingItem, p_362843_.mainArm == HumanoidArm.RIGHT
            );
        } else if (piglinarmpose == PiglinArmPose.ADMIRING_ITEM) {
            this.head.xRot = 0.5F;
            this.head.yRot = 0.0F;
            if (p_362843_.mainArm == HumanoidArm.LEFT) {
                this.rightArm.yRot = -0.5F;
                this.rightArm.xRot = -0.9F;
            } else {
                this.leftArm.yRot = 0.5F;
                this.leftArm.xRot = -0.9F;
            }
        }
    }

    protected void setupAttackAnimation(PiglinRenderState p_362671_, float p_103352_) {
        float f = p_362671_.attackTime;
        if (f > 0.0F && p_362671_.armPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
            AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, p_362671_.mainArm, f, p_362671_.ageInTicks);
        } else {
            super.setupAttackAnimation(p_362671_, p_103352_);
        }
    }

    private void holdWeaponHigh(PiglinRenderState p_361494_) {
        if (p_361494_.mainArm == HumanoidArm.LEFT) {
            this.leftArm.xRot = -1.8F;
        } else {
            this.rightArm.xRot = -1.8F;
        }
    }

    @Override
    public void setAllVisible(boolean p_361670_) {
        super.setAllVisible(p_361670_);
        this.leftSleeve.visible = p_361670_;
        this.rightSleeve.visible = p_361670_;
        this.leftPants.visible = p_361670_;
        this.rightPants.visible = p_361670_;
        this.jacket.visible = p_361670_;
    }
}
