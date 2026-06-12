package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.MatrixUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemRenderer {
    public static final ResourceLocation ENCHANTED_GLINT_ENTITY = ResourceLocation.withDefaultNamespace("textures/misc/enchanted_glint_entity.png");
    public static final ResourceLocation ENCHANTED_GLINT_ITEM = ResourceLocation.withDefaultNamespace("textures/misc/enchanted_glint_item.png");
    public static final int GUI_SLOT_CENTER_X = 8;
    public static final int GUI_SLOT_CENTER_Y = 8;
    public static final int ITEM_DECORATION_BLIT_OFFSET = 200;
    public static final float COMPASS_FOIL_UI_SCALE = 0.5F;
    public static final float COMPASS_FOIL_FIRST_PERSON_SCALE = 0.75F;
    public static final float COMPASS_FOIL_TEXTURE_SCALE = 0.0078125F;
    public static final int NO_TINT = -1;
    private final ItemModelResolver resolver;
    private final ItemStackRenderState scratchItemStackRenderState = new ItemStackRenderState();

    public ItemRenderer(ItemModelResolver p_387475_) {
        this.resolver = p_387475_;
    }

    private static void renderModelLists(BakedModel p_115190_, int[] p_387364_, int p_115192_, int p_115193_, PoseStack p_115194_, VertexConsumer p_115195_) {
        RandomSource randomsource = RandomSource.create();
        long i = 42L;

        for (Direction direction : Direction.values()) {
            randomsource.setSeed(42L);
            renderQuadList(p_115194_, p_115195_, p_115190_.getQuads(null, direction, randomsource), p_387364_, p_115192_, p_115193_);
        }

        randomsource.setSeed(42L);
        renderQuadList(p_115194_, p_115195_, p_115190_.getQuads(null, null, randomsource), p_387364_, p_115192_, p_115193_);
    }

    public static void renderItem(
        ItemDisplayContext p_361627_,
        PoseStack p_360423_,
        MultiBufferSource p_360415_,
        int p_361265_,
        int p_364771_,
        int[] p_386517_,
        BakedModel p_363970_,
        RenderType p_388877_,
        ItemStackRenderState.FoilType p_387026_
    ) {
        VertexConsumer vertexconsumer;
        if (p_387026_ == ItemStackRenderState.FoilType.SPECIAL) {
            PoseStack.Pose posestack$pose = p_360423_.last().copy();
            if (p_361627_ == ItemDisplayContext.GUI) {
                MatrixUtil.mulComponentWise(posestack$pose.pose(), 0.5F);
            } else if (p_361627_.firstPerson()) {
                MatrixUtil.mulComponentWise(posestack$pose.pose(), 0.75F);
            }

            vertexconsumer = getCompassFoilBuffer(p_360415_, p_388877_, posestack$pose);
        } else {
            vertexconsumer = getFoilBuffer(p_360415_, p_388877_, true, p_387026_ != ItemStackRenderState.FoilType.NONE);
        }

        renderModelLists(p_363970_, p_386517_, p_361265_, p_364771_, p_360423_, vertexconsumer);
    }

    public static VertexConsumer getArmorFoilBuffer(MultiBufferSource p_115185_, RenderType p_115186_, boolean p_115187_) {
        return p_115187_
            ? VertexMultiConsumer.create(p_115185_.getBuffer(RenderType.armorEntityGlint()), p_115185_.getBuffer(p_115186_))
            : p_115185_.getBuffer(p_115186_);
    }

    private static VertexConsumer getCompassFoilBuffer(MultiBufferSource p_115181_, RenderType p_115182_, PoseStack.Pose p_115183_) {
        return VertexMultiConsumer.create(
            new SheetedDecalTextureGenerator(p_115181_.getBuffer(RenderType.glint()), p_115183_, 0.0078125F), p_115181_.getBuffer(p_115182_)
        );
    }

    public static VertexConsumer getFoilBuffer(MultiBufferSource p_115212_, RenderType p_115213_, boolean p_115214_, boolean p_115215_) {
        if (p_115215_) {
            return Minecraft.useShaderTransparency() && p_115213_ == Sheets.translucentItemSheet()
                ? VertexMultiConsumer.create(p_115212_.getBuffer(RenderType.glintTranslucent()), p_115212_.getBuffer(p_115213_))
                : VertexMultiConsumer.create(p_115212_.getBuffer(p_115214_ ? RenderType.glint() : RenderType.entityGlint()), p_115212_.getBuffer(p_115213_));
        } else {
            return p_115212_.getBuffer(p_115213_);
        }
    }

    private static int getLayerColorSafe(int[] p_387884_, int p_388524_) {
        return p_388524_ >= p_387884_.length ? -1 : p_387884_[p_388524_];
    }

    private static void renderQuadList(PoseStack p_115163_, VertexConsumer p_115164_, List<BakedQuad> p_115165_, int[] p_387305_, int p_115167_, int p_115168_) {
        PoseStack.Pose posestack$pose = p_115163_.last();

        for (BakedQuad bakedquad : p_115165_) {
            float f;
            float f1;
            float f2;
            float f3;
            if (bakedquad.isTinted()) {
                int i = getLayerColorSafe(p_387305_, bakedquad.getTintIndex());
                f = (float)ARGB.alpha(i) / 255.0F;
                f1 = (float)ARGB.red(i) / 255.0F;
                f2 = (float)ARGB.green(i) / 255.0F;
                f3 = (float)ARGB.blue(i) / 255.0F;
            } else {
                f = 1.0F;
                f1 = 1.0F;
                f2 = 1.0F;
                f3 = 1.0F;
            }

            p_115164_.putBulkData(posestack$pose, bakedquad, f1, f2, f3, f, p_115167_, p_115168_);
        }
    }

    public void renderStatic(
        ItemStack p_270761_,
        ItemDisplayContext p_270648_,
        int p_270410_,
        int p_270894_,
        PoseStack p_270430_,
        MultiBufferSource p_270457_,
        @Nullable Level p_270149_,
        int p_270509_
    ) {
        this.renderStatic(null, p_270761_, p_270648_, false, p_270430_, p_270457_, p_270149_, p_270410_, p_270894_, p_270509_);
    }

    public void renderStatic(
        @Nullable LivingEntity p_270101_,
        ItemStack p_270637_,
        ItemDisplayContext p_270437_,
        boolean p_270434_,
        PoseStack p_270230_,
        MultiBufferSource p_270411_,
        @Nullable Level p_270641_,
        int p_270595_,
        int p_270927_,
        int p_270845_
    ) {
        this.resolver.updateForTopItem(this.scratchItemStackRenderState, p_270637_, p_270437_, p_270434_, p_270641_, p_270101_, p_270845_);
        this.scratchItemStackRenderState.render(p_270230_, p_270411_, p_270595_, p_270927_);
    }
}
