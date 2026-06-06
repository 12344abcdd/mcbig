package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractBoatModel extends EntityModel<BoatRenderState> {
    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;

    public AbstractBoatModel(ModelPart p_376707_) {
        super(p_376707_);
        this.leftPaddle = p_376707_.getChild("left_paddle");
        this.rightPaddle = p_376707_.getChild("right_paddle");
    }

    public void setupAnim(BoatRenderState p_376907_) {
        super.setupAnim(p_376907_);
        animatePaddle(p_376907_.rowingTimeLeft, 0, this.leftPaddle);
        animatePaddle(p_376907_.rowingTimeRight, 1, this.rightPaddle);
    }

    private static void animatePaddle(float p_376567_, int p_376720_, ModelPart p_376932_) {
        p_376932_.xRot = Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (Mth.sin(-p_376567_) + 1.0F) / 2.0F);
        p_376932_.yRot = Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (Mth.sin(-p_376567_ + 1.0F) + 1.0F) / 2.0F);
        if (p_376720_ == 1) {
            p_376932_.yRot = (float) Math.PI - p_376932_.yRot;
        }
    }
}
