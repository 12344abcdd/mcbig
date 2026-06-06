package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSkeletonRenderer<T extends AbstractSkeleton, S extends SkeletonRenderState> extends HumanoidMobRenderer<T, S, SkeletonModel<S>> {
    public AbstractSkeletonRenderer(
        EntityRendererProvider.Context p_365063_, ModelLayerLocation p_364353_, ModelLayerLocation p_361271_, ModelLayerLocation p_360962_
    ) {
        this(p_365063_, p_361271_, p_360962_, new SkeletonModel<>(p_365063_.bakeLayer(p_364353_)));
    }

    public AbstractSkeletonRenderer(
        EntityRendererProvider.Context p_360948_, ModelLayerLocation p_364416_, ModelLayerLocation p_360716_, SkeletonModel<S> p_361394_
    ) {
        super(p_360948_, p_361394_, 0.5F);
        this.addLayer(
            new HumanoidArmorLayer<>(
                this, new SkeletonModel(p_360948_.bakeLayer(p_364416_)), new SkeletonModel(p_360948_.bakeLayer(p_360716_)), p_360948_.getEquipmentRenderer()
            )
        );
    }

    public void extractRenderState(T p_360621_, S p_364836_, float p_362389_) {
        super.extractRenderState(p_360621_, p_364836_, p_362389_);
        p_364836_.isAggressive = p_360621_.isAggressive();
        p_364836_.isShaking = p_360621_.isShaking();
        p_364836_.isHoldingBow = p_360621_.getMainHandItem().is(Items.BOW);
    }

    protected boolean isShaking(S p_364410_) {
        return p_364410_.isShaking;
    }

    protected HumanoidModel.ArmPose getArmPose(AbstractSkeleton p_389585_, HumanoidArm p_389423_) {
        return p_389585_.getMainArm() == p_389423_ && p_389585_.isAggressive() && p_389585_.getMainHandItem().is(Items.BOW)
            ? HumanoidModel.ArmPose.BOW_AND_ARROW
            : HumanoidModel.ArmPose.EMPTY;
    }
}
