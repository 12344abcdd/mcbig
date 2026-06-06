package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.DonkeyRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DonkeyModel extends AbstractEquineModel<DonkeyRenderState> {
    public static final float DONKEY_SCALE = 0.87F;
    public static final float MULE_SCALE = 0.92F;
    private final ModelPart leftChest = this.body.getChild("left_chest");
    private final ModelPart rightChest = this.body.getChild("right_chest");

    public DonkeyModel(ModelPart p_363073_) {
        super(p_363073_);
    }

    public static LayerDefinition createBodyLayer(float p_382961_) {
        MeshDefinition meshdefinition = AbstractEquineModel.createBodyMesh(CubeDeformation.NONE);
        modifyMesh(meshdefinition.getRoot());
        return LayerDefinition.create(meshdefinition, 64, 64).apply(MeshTransformer.scaling(p_382961_));
    }

    public static LayerDefinition createBabyLayer(float p_382985_) {
        MeshDefinition meshdefinition = AbstractEquineModel.createFullScaleBabyMesh(CubeDeformation.NONE);
        modifyMesh(meshdefinition.getRoot());
        return LayerDefinition.create(AbstractEquineModel.BABY_TRANSFORMER.apply(meshdefinition), 64, 64).apply(MeshTransformer.scaling(p_382985_));
    }

    private static void modifyMesh(PartDefinition p_363183_) {
        PartDefinition partdefinition = p_363183_.getChild("body");
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(26, 21).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
        partdefinition.addOrReplaceChild("left_chest", cubelistbuilder, PartPose.offsetAndRotation(6.0F, -8.0F, 0.0F, 0.0F, (float) (-Math.PI / 2), 0.0F));
        partdefinition.addOrReplaceChild("right_chest", cubelistbuilder, PartPose.offsetAndRotation(-6.0F, -8.0F, 0.0F, 0.0F, (float) (Math.PI / 2), 0.0F));
        PartDefinition partdefinition1 = p_363183_.getChild("head_parts").getChild("head");
        CubeListBuilder cubelistbuilder1 = CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -7.0F, 0.0F, 2.0F, 7.0F, 1.0F);
        partdefinition1.addOrReplaceChild(
            "left_ear", cubelistbuilder1, PartPose.offsetAndRotation(1.25F, -10.0F, 4.0F, (float) (Math.PI / 12), 0.0F, (float) (Math.PI / 12))
        );
        partdefinition1.addOrReplaceChild(
            "right_ear", cubelistbuilder1, PartPose.offsetAndRotation(-1.25F, -10.0F, 4.0F, (float) (Math.PI / 12), 0.0F, (float) (-Math.PI / 12))
        );
    }

    public void setupAnim(DonkeyRenderState p_361197_) {
        super.setupAnim(p_361197_);
        this.leftChest.visible = p_361197_.hasChest;
        this.rightChest.visible = p_361197_.hasChest;
    }
}
