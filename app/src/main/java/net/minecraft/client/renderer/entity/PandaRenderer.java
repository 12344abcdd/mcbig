package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PandaRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaRenderer extends AgeableMobRenderer<Panda, PandaRenderState, PandaModel> {
    private static final Map<Panda.Gene, ResourceLocation> TEXTURES = Util.make(Maps.newEnumMap(Panda.Gene.class), p_349904_ -> {
        p_349904_.put(Panda.Gene.NORMAL, ResourceLocation.withDefaultNamespace("textures/entity/panda/panda.png"));
        p_349904_.put(Panda.Gene.LAZY, ResourceLocation.withDefaultNamespace("textures/entity/panda/lazy_panda.png"));
        p_349904_.put(Panda.Gene.WORRIED, ResourceLocation.withDefaultNamespace("textures/entity/panda/worried_panda.png"));
        p_349904_.put(Panda.Gene.PLAYFUL, ResourceLocation.withDefaultNamespace("textures/entity/panda/playful_panda.png"));
        p_349904_.put(Panda.Gene.BROWN, ResourceLocation.withDefaultNamespace("textures/entity/panda/brown_panda.png"));
        p_349904_.put(Panda.Gene.WEAK, ResourceLocation.withDefaultNamespace("textures/entity/panda/weak_panda.png"));
        p_349904_.put(Panda.Gene.AGGRESSIVE, ResourceLocation.withDefaultNamespace("textures/entity/panda/aggressive_panda.png"));
    });

    public PandaRenderer(EntityRendererProvider.Context p_174334_) {
        super(p_174334_, new PandaModel(p_174334_.bakeLayer(ModelLayers.PANDA)), new PandaModel(p_174334_.bakeLayer(ModelLayers.PANDA_BABY)), 0.9F);
        this.addLayer(new PandaHoldsItemLayer(this));
    }

    public ResourceLocation getTextureLocation(PandaRenderState p_364827_) {
        return TEXTURES.getOrDefault(p_364827_.variant, TEXTURES.get(Panda.Gene.NORMAL));
    }

    public PandaRenderState createRenderState() {
        return new PandaRenderState();
    }

    public void extractRenderState(Panda p_363780_, PandaRenderState p_363283_, float p_360846_) {
        super.extractRenderState(p_363780_, p_363283_, p_360846_);
        HoldingEntityRenderState.extractHoldingEntityRenderState(p_363780_, p_363283_, this.itemModelResolver);
        p_363283_.variant = p_363780_.getVariant();
        p_363283_.isUnhappy = p_363780_.getUnhappyCounter() > 0;
        p_363283_.isSneezing = p_363780_.isSneezing();
        p_363283_.sneezeTime = p_363780_.getSneezeCounter();
        p_363283_.isEating = p_363780_.isEating();
        p_363283_.isScared = p_363780_.isScared();
        p_363283_.isSitting = p_363780_.isSitting();
        p_363283_.sitAmount = p_363780_.getSitAmount(p_360846_);
        p_363283_.lieOnBackAmount = p_363780_.getLieOnBackAmount(p_360846_);
        p_363283_.rollAmount = p_363780_.isBaby() ? 0.0F : p_363780_.getRollAmount(p_360846_);
        p_363283_.rollTime = p_363780_.rollCounter > 0 ? (float)p_363780_.rollCounter + p_360846_ : 0.0F;
    }

    protected void setupRotations(PandaRenderState p_362586_, PoseStack p_115634_, float p_115635_, float p_115636_) {
        super.setupRotations(p_362586_, p_115634_, p_115635_, p_115636_);
        if (p_362586_.rollTime > 0.0F) {
            float f = Mth.frac(p_362586_.rollTime);
            int i = Mth.floor(p_362586_.rollTime);
            int j = i + 1;
            float f1 = 7.0F;
            float f2 = p_362586_.isBaby ? 0.3F : 0.8F;
            if ((float)i < 8.0F) {
                float f4 = 90.0F * (float)i / 7.0F;
                float f5 = 90.0F * (float)j / 7.0F;
                float f3 = this.getAngle(f4, f5, j, f, 8.0F);
                p_115634_.translate(0.0F, (f2 + 0.2F) * (f3 / 90.0F), 0.0F);
                p_115634_.mulPose(Axis.XP.rotationDegrees(-f3));
            } else if ((float)i < 16.0F) {
                float f14 = ((float)i - 8.0F) / 7.0F;
                float f17 = 90.0F + 90.0F * f14;
                float f6 = 90.0F + 90.0F * ((float)j - 8.0F) / 7.0F;
                float f11 = this.getAngle(f17, f6, j, f, 16.0F);
                p_115634_.translate(0.0F, f2 + 0.2F + (f2 - 0.2F) * (f11 - 90.0F) / 90.0F, 0.0F);
                p_115634_.mulPose(Axis.XP.rotationDegrees(-f11));
            } else if ((float)i < 24.0F) {
                float f15 = ((float)i - 16.0F) / 7.0F;
                float f18 = 180.0F + 90.0F * f15;
                float f20 = 180.0F + 90.0F * ((float)j - 16.0F) / 7.0F;
                float f12 = this.getAngle(f18, f20, j, f, 24.0F);
                p_115634_.translate(0.0F, f2 + f2 * (270.0F - f12) / 90.0F, 0.0F);
                p_115634_.mulPose(Axis.XP.rotationDegrees(-f12));
            } else if (i < 32) {
                float f16 = ((float)i - 24.0F) / 7.0F;
                float f19 = 270.0F + 90.0F * f16;
                float f21 = 270.0F + 90.0F * ((float)j - 24.0F) / 7.0F;
                float f13 = this.getAngle(f19, f21, j, f, 32.0F);
                p_115634_.translate(0.0F, f2 * ((360.0F - f13) / 90.0F), 0.0F);
                p_115634_.mulPose(Axis.XP.rotationDegrees(-f13));
            }
        }

        float f7 = p_362586_.sitAmount;
        if (f7 > 0.0F) {
            p_115634_.translate(0.0F, 0.8F * f7, 0.0F);
            p_115634_.mulPose(Axis.XP.rotationDegrees(Mth.lerp(f7, p_362586_.xRot, p_362586_.xRot + 90.0F)));
            p_115634_.translate(0.0F, -1.0F * f7, 0.0F);
            if (p_362586_.isScared) {
                float f8 = (float)(Math.cos((double)(p_362586_.ageInTicks * 1.25F)) * Math.PI * 0.05F);
                p_115634_.mulPose(Axis.YP.rotationDegrees(f8));
                if (p_362586_.isBaby) {
                    p_115634_.translate(0.0F, 0.8F, 0.55F);
                }
            }
        }

        float f9 = p_362586_.lieOnBackAmount;
        if (f9 > 0.0F) {
            float f10 = p_362586_.isBaby ? 0.5F : 1.3F;
            p_115634_.translate(0.0F, f10 * f9, 0.0F);
            p_115634_.mulPose(Axis.XP.rotationDegrees(Mth.lerp(f9, p_362586_.xRot, p_362586_.xRot + 180.0F)));
        }
    }

    private float getAngle(float p_115625_, float p_115626_, int p_115627_, float p_115628_, float p_115629_) {
        return (float)p_115627_ < p_115629_ ? Mth.lerp(p_115628_, p_115625_, p_115626_) : p_115625_;
    }
}
