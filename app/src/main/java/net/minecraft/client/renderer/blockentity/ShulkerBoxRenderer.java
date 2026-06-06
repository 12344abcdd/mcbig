package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBoxRenderer implements BlockEntityRenderer<ShulkerBoxBlockEntity> {
    private final ShulkerBoxRenderer.ShulkerBoxModel model;

    public ShulkerBoxRenderer(BlockEntityRendererProvider.Context p_173626_) {
        this(p_173626_.getModelSet());
    }

    public ShulkerBoxRenderer(EntityModelSet p_388849_) {
        this.model = new ShulkerBoxRenderer.ShulkerBoxModel(p_388849_.bakeLayer(ModelLayers.SHULKER_BOX));
    }

    public void render(ShulkerBoxBlockEntity p_112478_, float p_112479_, PoseStack p_112480_, MultiBufferSource p_112481_, int p_112482_, int p_112483_) {
        Direction direction = p_112478_.getBlockState().getValueOrElse(ShulkerBoxBlock.FACING, Direction.UP);
        DyeColor dyecolor = p_112478_.getColor();
        Material material;
        if (dyecolor == null) {
            material = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION;
        } else {
            material = Sheets.getShulkerBoxMaterial(dyecolor);
        }

        float f = p_112478_.getProgress(p_112479_);
        this.render(p_112480_, p_112481_, p_112482_, p_112483_, direction, f, material);
    }

    public void render(PoseStack p_388735_, MultiBufferSource p_386677_, int p_388822_, int p_386683_, Direction p_387528_, float p_387342_, Material p_387103_) {
        p_388735_.pushPose();
        p_388735_.translate(0.5F, 0.5F, 0.5F);
        float f = 0.9995F;
        p_388735_.scale(0.9995F, 0.9995F, 0.9995F);
        p_388735_.mulPose(p_387528_.getRotation());
        p_388735_.scale(1.0F, -1.0F, -1.0F);
        p_388735_.translate(0.0F, -1.0F, 0.0F);
        this.model.animate(p_387342_);
        VertexConsumer vertexconsumer = p_387103_.buffer(p_386677_, this.model::renderType);
        this.model.renderToBuffer(p_388735_, vertexconsumer, p_388822_, p_386683_);
        p_388735_.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    static class ShulkerBoxModel extends Model {
        private final ModelPart lid;

        public ShulkerBoxModel(ModelPart p_361240_) {
            super(p_361240_, RenderType::entityCutoutNoCull);
            this.lid = p_361240_.getChild("lid");
        }

        public void animate(float p_365330_) {
            this.lid.setPos(0.0F, 24.0F - p_365330_ * 0.5F * 16.0F, 0.0F);
            this.lid.yRot = 270.0F * p_365330_ * (float) (Math.PI / 180.0);
        }
    }
}
