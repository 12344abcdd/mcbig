package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.BannerFlagModel;
import net.minecraft.client.model.BannerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BannerRenderer implements BlockEntityRenderer<BannerBlockEntity> {
    private static final int MAX_PATTERNS = 16;
    private static final float SIZE = 0.6666667F;
    private final BannerModel standingModel;
    private final BannerModel wallModel;
    private final BannerFlagModel standingFlagModel;
    private final BannerFlagModel wallFlagModel;

    public BannerRenderer(BlockEntityRendererProvider.Context p_173521_) {
        this(p_173521_.getModelSet());
    }

    public BannerRenderer(EntityModelSet p_387622_) {
        this.standingModel = new BannerModel(p_387622_.bakeLayer(ModelLayers.STANDING_BANNER));
        this.wallModel = new BannerModel(p_387622_.bakeLayer(ModelLayers.WALL_BANNER));
        this.standingFlagModel = new BannerFlagModel(p_387622_.bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
        this.wallFlagModel = new BannerFlagModel(p_387622_.bakeLayer(ModelLayers.WALL_BANNER_FLAG));
    }

    public void render(BannerBlockEntity p_112052_, float p_112053_, PoseStack p_112054_, MultiBufferSource p_112055_, int p_112056_, int p_112057_) {
        BlockState blockstate = p_112052_.getBlockState();
        BannerModel bannermodel;
        BannerFlagModel bannerflagmodel;
        float f;
        if (blockstate.getBlock() instanceof BannerBlock) {
            f = -RotationSegment.convertToDegrees(blockstate.getValue(BannerBlock.ROTATION));
            bannermodel = this.standingModel;
            bannerflagmodel = this.standingFlagModel;
        } else {
            f = -blockstate.getValue(WallBannerBlock.FACING).toYRot();
            bannermodel = this.wallModel;
            bannerflagmodel = this.wallFlagModel;
        }

        long i = p_112052_.getLevel().getGameTime();
        BlockPos blockpos = p_112052_.getBlockPos();
        float f1 = ((float)Math.floorMod((long)(blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + i, 100L) + p_112053_) / 100.0F;
        renderBanner(p_112054_, p_112055_, p_112056_, p_112057_, f, bannermodel, bannerflagmodel, f1, p_112052_.getBaseColor(), p_112052_.getPatterns());
    }

    public void renderInHand(PoseStack p_387128_, MultiBufferSource p_387289_, int p_386943_, int p_387938_, DyeColor p_388253_, BannerPatternLayers p_388428_) {
        renderBanner(p_387128_, p_387289_, p_386943_, p_387938_, 0.0F, this.standingModel, this.standingFlagModel, 0.0F, p_388253_, p_388428_);
    }

    private static void renderBanner(
        PoseStack p_388373_,
        MultiBufferSource p_388314_,
        int p_386449_,
        int p_388689_,
        float p_387344_,
        BannerModel p_386469_,
        BannerFlagModel p_387874_,
        float p_387444_,
        DyeColor p_388367_,
        BannerPatternLayers p_388938_
    ) {
        p_388373_.pushPose();
        p_388373_.translate(0.5F, 0.0F, 0.5F);
        p_388373_.mulPose(Axis.YP.rotationDegrees(p_387344_));
        p_388373_.scale(0.6666667F, -0.6666667F, -0.6666667F);
        p_386469_.renderToBuffer(p_388373_, ModelBakery.BANNER_BASE.buffer(p_388314_, RenderType::entitySolid), p_386449_, p_388689_);
        p_387874_.setupAnim(p_387444_);
        renderPatterns(p_388373_, p_388314_, p_386449_, p_388689_, p_387874_.root(), ModelBakery.BANNER_BASE, true, p_388367_, p_388938_);
        p_388373_.popPose();
    }

    public static void renderPatterns(
        PoseStack p_112066_,
        MultiBufferSource p_112067_,
        int p_112068_,
        int p_112069_,
        ModelPart p_112070_,
        Material p_112071_,
        boolean p_112072_,
        DyeColor p_332660_,
        BannerPatternLayers p_330986_
    ) {
        renderPatterns(p_112066_, p_112067_, p_112068_, p_112069_, p_112070_, p_112071_, p_112072_, p_332660_, p_330986_, false, true);
    }

    public static void renderPatterns(
        PoseStack p_112075_,
        MultiBufferSource p_112076_,
        int p_112077_,
        int p_112078_,
        ModelPart p_112079_,
        Material p_112080_,
        boolean p_112081_,
        DyeColor p_332652_,
        BannerPatternLayers p_331851_,
        boolean p_332823_,
        boolean p_364162_
    ) {
        p_112079_.render(p_112075_, p_112080_.buffer(p_112076_, RenderType::entitySolid, p_364162_, p_332823_), p_112077_, p_112078_);
        renderPatternLayer(p_112075_, p_112076_, p_112077_, p_112078_, p_112079_, p_112081_ ? Sheets.BANNER_BASE : Sheets.SHIELD_BASE, p_332652_);

        for (int i = 0; i < 16 && i < p_331851_.layers().size(); i++) {
            BannerPatternLayers.Layer bannerpatternlayers$layer = p_331851_.layers().get(i);
            Material material = p_112081_
                ? Sheets.getBannerMaterial(bannerpatternlayers$layer.pattern())
                : Sheets.getShieldMaterial(bannerpatternlayers$layer.pattern());
            renderPatternLayer(p_112075_, p_112076_, p_112077_, p_112078_, p_112079_, material, bannerpatternlayers$layer.color());
        }
    }

    private static void renderPatternLayer(
        PoseStack p_332737_, MultiBufferSource p_332758_, int p_332821_, int p_332828_, ModelPart p_332732_, Material p_332704_, DyeColor p_332728_
    ) {
        int i = p_332728_.getTextureDiffuseColor();
        p_332732_.render(p_332737_, p_332704_.buffer(p_332758_, RenderType::entityNoOutline), p_332821_, p_332828_, i);
    }
}
