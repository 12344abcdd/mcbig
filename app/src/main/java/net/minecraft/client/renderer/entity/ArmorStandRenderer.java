package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandRenderer extends LivingEntityRenderer<ArmorStand, ArmorStandRenderState, ArmorStandArmorModel> {
    public static final ResourceLocation DEFAULT_SKIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/armorstand/wood.png");
    private final ArmorStandArmorModel bigModel = this.getModel();
    private final ArmorStandArmorModel smallModel;

    public ArmorStandRenderer(EntityRendererProvider.Context p_173915_) {
        super(p_173915_, new ArmorStandModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND)), 0.0F);
        this.smallModel = new ArmorStandModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_SMALL));
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new ArmorStandArmorModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)),
                new ArmorStandArmorModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR)),
                new ArmorStandArmorModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_SMALL_INNER_ARMOR)),
                new ArmorStandArmorModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_SMALL_OUTER_ARMOR)),
                p_173915_.getEquipmentRenderer()
            )
        );
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new WingsLayer<>(this, p_173915_.getModelSet(), p_173915_.getEquipmentRenderer()));
        this.addLayer(new CustomHeadLayer<>(this, p_173915_.getModelSet()));
    }

    public ResourceLocation getTextureLocation(ArmorStandRenderState p_364197_) {
        return DEFAULT_SKIN_LOCATION;
    }

    public ArmorStandRenderState createRenderState() {
        return new ArmorStandRenderState();
    }

    public void extractRenderState(ArmorStand p_361199_, ArmorStandRenderState p_363932_, float p_365059_) {
        super.extractRenderState(p_361199_, p_363932_, p_365059_);
        HumanoidMobRenderer.extractHumanoidRenderState(p_361199_, p_363932_, p_365059_, this.itemModelResolver);
        p_363932_.yRot = Mth.rotLerp(p_365059_, p_361199_.yRotO, p_361199_.getYRot());
        p_363932_.isMarker = p_361199_.isMarker();
        p_363932_.isSmall = p_361199_.isSmall();
        p_363932_.showArms = p_361199_.showArms();
        p_363932_.showBasePlate = p_361199_.showBasePlate();
        p_363932_.bodyPose = p_361199_.getBodyPose();
        p_363932_.headPose = p_361199_.getHeadPose();
        p_363932_.leftArmPose = p_361199_.getLeftArmPose();
        p_363932_.rightArmPose = p_361199_.getRightArmPose();
        p_363932_.leftLegPose = p_361199_.getLeftLegPose();
        p_363932_.rightLegPose = p_361199_.getRightLegPose();
        p_363932_.wiggle = (float)(p_361199_.level().getGameTime() - p_361199_.lastHit) + p_365059_;
    }

    public void render(ArmorStandRenderState p_360777_, PoseStack p_362088_, MultiBufferSource p_364152_, int p_361217_) {
        this.model = p_360777_.isSmall ? this.smallModel : this.bigModel;
        super.render(p_360777_, p_362088_, p_364152_, p_361217_);
    }

    protected void setupRotations(ArmorStandRenderState p_364765_, PoseStack p_113801_, float p_113802_, float p_113803_) {
        p_113801_.mulPose(Axis.YP.rotationDegrees(180.0F - p_113802_));
        if (p_364765_.wiggle < 5.0F) {
            p_113801_.mulPose(Axis.YP.rotationDegrees(Mth.sin(p_364765_.wiggle / 1.5F * (float) Math.PI) * 3.0F));
        }
    }

    protected boolean shouldShowName(ArmorStand p_360644_, double p_362585_) {
        return p_360644_.isCustomNameVisible();
    }

    @Nullable
    protected RenderType getRenderType(ArmorStandRenderState p_364565_, boolean p_113794_, boolean p_113795_, boolean p_113796_) {
        if (!p_364565_.isMarker) {
            return super.getRenderType(p_364565_, p_113794_, p_113795_, p_113796_);
        } else {
            ResourceLocation resourcelocation = this.getTextureLocation(p_364565_);
            if (p_113795_) {
                return RenderType.entityTranslucent(resourcelocation, false);
            } else {
                return p_113794_ ? RenderType.entityCutoutNoCull(resourcelocation, false) : null;
            }
        }
    }
}
