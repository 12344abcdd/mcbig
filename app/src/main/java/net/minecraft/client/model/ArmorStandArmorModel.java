package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandArmorModel extends HumanoidModel<ArmorStandRenderState> {
    public ArmorStandArmorModel(ModelPart p_170346_) {
        super(p_170346_);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation p_170348_) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(p_170348_, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, p_170348_), PartPose.offset(0.0F, 1.0F, 0.0F)
        );
        partdefinition1.addOrReplaceChild(
            "hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, p_170348_.extend(0.5F)), PartPose.ZERO
        );
        partdefinition.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170348_.extend(-0.1F)),
            PartPose.offset(-1.9F, 11.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170348_.extend(-0.1F)),
            PartPose.offset(1.9F, 11.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public void setupAnim(ArmorStandRenderState p_364632_) {
        super.setupAnim(p_364632_);
        this.head.xRot = (float) (Math.PI / 180.0) * p_364632_.headPose.getX();
        this.head.yRot = (float) (Math.PI / 180.0) * p_364632_.headPose.getY();
        this.head.zRot = (float) (Math.PI / 180.0) * p_364632_.headPose.getZ();
        this.body.xRot = (float) (Math.PI / 180.0) * p_364632_.bodyPose.getX();
        this.body.yRot = (float) (Math.PI / 180.0) * p_364632_.bodyPose.getY();
        this.body.zRot = (float) (Math.PI / 180.0) * p_364632_.bodyPose.getZ();
        this.leftArm.xRot = (float) (Math.PI / 180.0) * p_364632_.leftArmPose.getX();
        this.leftArm.yRot = (float) (Math.PI / 180.0) * p_364632_.leftArmPose.getY();
        this.leftArm.zRot = (float) (Math.PI / 180.0) * p_364632_.leftArmPose.getZ();
        this.rightArm.xRot = (float) (Math.PI / 180.0) * p_364632_.rightArmPose.getX();
        this.rightArm.yRot = (float) (Math.PI / 180.0) * p_364632_.rightArmPose.getY();
        this.rightArm.zRot = (float) (Math.PI / 180.0) * p_364632_.rightArmPose.getZ();
        this.leftLeg.xRot = (float) (Math.PI / 180.0) * p_364632_.leftLegPose.getX();
        this.leftLeg.yRot = (float) (Math.PI / 180.0) * p_364632_.leftLegPose.getY();
        this.leftLeg.zRot = (float) (Math.PI / 180.0) * p_364632_.leftLegPose.getZ();
        this.rightLeg.xRot = (float) (Math.PI / 180.0) * p_364632_.rightLegPose.getX();
        this.rightLeg.yRot = (float) (Math.PI / 180.0) * p_364632_.rightLegPose.getY();
        this.rightLeg.zRot = (float) (Math.PI / 180.0) * p_364632_.rightLegPose.getZ();
    }
}
