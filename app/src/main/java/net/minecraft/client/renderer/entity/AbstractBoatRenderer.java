package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractBoatRenderer extends EntityRenderer<AbstractBoat, BoatRenderState> {
    public AbstractBoatRenderer(EntityRendererProvider.Context p_376164_) {
        super(p_376164_);
        this.shadowRadius = 0.8F;
    }

    public void render(BoatRenderState p_376222_, PoseStack p_376926_, MultiBufferSource p_376211_, int p_376589_) {
        p_376926_.pushPose();
        p_376926_.translate(0.0F, 0.375F, 0.0F);
        p_376926_.mulPose(Axis.YP.rotationDegrees(180.0F - p_376222_.yRot));
        float f = p_376222_.hurtTime;
        if (f > 0.0F) {
            p_376926_.mulPose(Axis.XP.rotationDegrees(Mth.sin(f) * f * p_376222_.damageTime / 10.0F * (float)p_376222_.hurtDir));
        }

        if (!Mth.equal(p_376222_.bubbleAngle, 0.0F)) {
            p_376926_.mulPose(new Quaternionf().setAngleAxis(p_376222_.bubbleAngle * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
        }

        p_376926_.scale(-1.0F, -1.0F, 1.0F);
        p_376926_.mulPose(Axis.YP.rotationDegrees(90.0F));
        EntityModel<BoatRenderState> entitymodel = this.model();
        entitymodel.setupAnim(p_376222_);
        VertexConsumer vertexconsumer = p_376211_.getBuffer(this.renderType());
        entitymodel.renderToBuffer(p_376926_, vertexconsumer, p_376589_, OverlayTexture.NO_OVERLAY);
        this.renderTypeAdditions(p_376222_, p_376926_, p_376211_, p_376589_);
        p_376926_.popPose();
        super.render(p_376222_, p_376926_, p_376211_, p_376589_);
    }

    protected void renderTypeAdditions(BoatRenderState p_376875_, PoseStack p_376118_, MultiBufferSource p_376628_, int p_376899_) {
    }

    protected abstract EntityModel<BoatRenderState> model();

    protected abstract RenderType renderType();

    public BoatRenderState createRenderState() {
        return new BoatRenderState();
    }

    public void extractRenderState(AbstractBoat p_376890_, BoatRenderState p_376855_, float p_376877_) {
        super.extractRenderState(p_376890_, p_376855_, p_376877_);
        p_376855_.yRot = p_376890_.getYRot(p_376877_);
        p_376855_.hurtTime = (float)p_376890_.getHurtTime() - p_376877_;
        p_376855_.hurtDir = p_376890_.getHurtDir();
        p_376855_.damageTime = Math.max(p_376890_.getDamage() - p_376877_, 0.0F);
        p_376855_.bubbleAngle = p_376890_.getBubbleAngle(p_376877_);
        p_376855_.isUnderWater = p_376890_.isUnderWater();
        p_376855_.rowingTimeLeft = p_376890_.getRowingTime(0, p_376877_);
        p_376855_.rowingTimeRight = p_376890_.getRowingTime(1, p_376877_);
    }
}
