package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignRenderer extends AbstractSignRenderer {
    private static final float RENDER_SCALE = 0.6666667F;
    private static final Vec3 TEXT_OFFSET = new Vec3(0.0, 0.33333334F, 0.046666667F);
    private final Map<WoodType, SignRenderer.Models> signModels;

    public SignRenderer(BlockEntityRendererProvider.Context p_173636_) {
        super(p_173636_);
        this.signModels = WoodType.values()
            .collect(
                ImmutableMap.toImmutableMap(
                    p_173645_ -> (WoodType)p_173645_,
                    p_359254_ -> new SignRenderer.Models(
                            createSignModel(p_173636_.getModelSet(), p_359254_, true), createSignModel(p_173636_.getModelSet(), p_359254_, false)
                        )
                )
            );
    }

    @Override
    protected Model getSignModel(BlockState p_389425_, WoodType p_389581_) {
        SignRenderer.Models signrenderer$models = this.signModels.get(p_389581_);
        return p_389425_.getBlock() instanceof StandingSignBlock ? signrenderer$models.standing() : signrenderer$models.wall();
    }

    @Override
    protected Material getSignMaterial(WoodType p_251961_) {
        return Sheets.getSignMaterial(p_251961_);
    }

    @Override
    protected float getSignModelRenderScale() {
        return 0.6666667F;
    }

    @Override
    protected float getSignTextRenderScale() {
        return 0.6666667F;
    }

    private static void translateBase(PoseStack p_389606_, float p_389676_) {
        p_389606_.translate(0.5F, 0.5F, 0.5F);
        p_389606_.mulPose(Axis.YP.rotationDegrees(p_389676_));
    }

    @Override
    protected void translateSign(PoseStack p_278074_, float p_277875_, BlockState p_277559_) {
        translateBase(p_278074_, p_277875_);
        if (!(p_277559_.getBlock() instanceof StandingSignBlock)) {
            p_278074_.translate(0.0F, -0.3125F, -0.4375F);
        }
    }

    @Override
    protected Vec3 getTextOffset() {
        return TEXT_OFFSET;
    }

    public static void renderInHand(PoseStack p_389553_, MultiBufferSource p_389643_, int p_389568_, int p_389710_, Model p_389561_, Material p_389713_) {
        p_389553_.pushPose();
        translateBase(p_389553_, 0.0F);
        p_389553_.scale(0.6666667F, -0.6666667F, -0.6666667F);
        VertexConsumer vertexconsumer = p_389713_.buffer(p_389643_, p_389561_::renderType);
        p_389561_.renderToBuffer(p_389553_, vertexconsumer, p_389568_, p_389710_);
        p_389553_.popPose();
    }

    public static Model createSignModel(EntityModelSet p_173647_, WoodType p_173648_, boolean p_363591_) {
        ModelLayerLocation modellayerlocation = p_363591_ ? ModelLayers.createStandingSignModelName(p_173648_) : ModelLayers.createWallSignModelName(p_173648_);
        return new Model.Simple(p_173647_.bakeLayer(modellayerlocation), RenderType::entityCutoutNoCull);
    }

    public static LayerDefinition createSignLayer(boolean p_365434_) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
        if (p_365434_) {
            partdefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.ZERO);
        }

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @OnlyIn(Dist.CLIENT)
    static record Models(Model standing, Model wall) {
    }
}
