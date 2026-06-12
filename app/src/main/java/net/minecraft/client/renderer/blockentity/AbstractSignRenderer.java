package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractSignRenderer implements BlockEntityRenderer<SignBlockEntity> {
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private final Font font;

    public AbstractSignRenderer(BlockEntityRendererProvider.Context p_389447_) {
        this.font = p_389447_.getFont();
    }

    protected abstract Model getSignModel(BlockState p_389441_, WoodType p_389675_);

    protected abstract Material getSignMaterial(WoodType p_389677_);

    protected abstract float getSignModelRenderScale();

    protected abstract float getSignTextRenderScale();

    protected abstract Vec3 getTextOffset();

    protected abstract void translateSign(PoseStack p_389625_, float p_389558_, BlockState p_389427_);

    public void render(SignBlockEntity p_389499_, float p_389653_, PoseStack p_389702_, MultiBufferSource p_389632_, int p_389452_, int p_389727_) {
        BlockState blockstate = p_389499_.getBlockState();
        SignBlock signblock = (SignBlock)blockstate.getBlock();
        Model model = this.getSignModel(blockstate, signblock.type());
        this.renderSignWithText(p_389499_, p_389702_, p_389632_, p_389452_, p_389727_, blockstate, signblock, signblock.type(), model);
    }

    private void renderSignWithText(
        SignBlockEntity p_389660_,
        PoseStack p_389494_,
        MultiBufferSource p_389603_,
        int p_389637_,
        int p_389601_,
        BlockState p_389608_,
        SignBlock p_389508_,
        WoodType p_389639_,
        Model p_389728_
    ) {
        p_389494_.pushPose();
        this.translateSign(p_389494_, -p_389508_.getYRotationDegrees(p_389608_), p_389608_);
        this.renderSign(p_389494_, p_389603_, p_389637_, p_389601_, p_389639_, p_389728_);
        this.renderSignText(
            p_389660_.getBlockPos(),
            p_389660_.getFrontText(),
            p_389494_,
            p_389603_,
            p_389637_,
            p_389660_.getTextLineHeight(),
            p_389660_.getMaxTextLineWidth(),
            true
        );
        this.renderSignText(
            p_389660_.getBlockPos(),
            p_389660_.getBackText(),
            p_389494_,
            p_389603_,
            p_389637_,
            p_389660_.getTextLineHeight(),
            p_389660_.getMaxTextLineWidth(),
            false
        );
        p_389494_.popPose();
    }

    protected void renderSign(PoseStack p_389545_, MultiBufferSource p_389532_, int p_389592_, int p_389671_, WoodType p_389459_, Model p_389397_) {
        p_389545_.pushPose();
        float f = this.getSignModelRenderScale();
        p_389545_.scale(f, -f, -f);
        Material material = this.getSignMaterial(p_389459_);
        VertexConsumer vertexconsumer = material.buffer(p_389532_, p_389397_::renderType);
        p_389397_.renderToBuffer(p_389545_, vertexconsumer, p_389592_, p_389671_);
        p_389545_.popPose();
    }

    private void renderSignText(
        BlockPos p_389704_,
        SignText p_389539_,
        PoseStack p_389662_,
        MultiBufferSource p_389505_,
        int p_389571_,
        int p_389500_,
        int p_389640_,
        boolean p_389595_
    ) {
        p_389662_.pushPose();
        this.translateSignText(p_389662_, p_389595_, this.getTextOffset());
        int i = getDarkColor(p_389539_);
        int j = 4 * p_389500_ / 2;
        FormattedCharSequence[] aformattedcharsequence = p_389539_.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), p_389418_ -> {
            List<FormattedCharSequence> list = this.font.split(p_389418_, p_389640_);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        });
        int k;
        boolean flag;
        int l;
        if (p_389539_.hasGlowingText()) {
            k = p_389539_.getColor().getTextColor();
            flag = isOutlineVisible(p_389704_, k);
            l = 15728880;
        } else {
            k = i;
            flag = false;
            l = p_389571_;
        }

        for (int i1 = 0; i1 < 4; i1++) {
            FormattedCharSequence formattedcharsequence = aformattedcharsequence[i1];
            float f = (float)(-this.font.width(formattedcharsequence) / 2);
            if (flag) {
                this.font.drawInBatch8xOutline(formattedcharsequence, f, (float)(i1 * p_389500_ - j), k, i, p_389662_.last().pose(), p_389505_, l);
            } else {
                this.font
                    .drawInBatch(
                        formattedcharsequence,
                        f,
                        (float)(i1 * p_389500_ - j),
                        k,
                        false,
                        p_389662_.last().pose(),
                        p_389505_,
                        Font.DisplayMode.POLYGON_OFFSET,
                        0,
                        l
                    );
            }
        }

        p_389662_.popPose();
    }

    private void translateSignText(PoseStack p_389535_, boolean p_389461_, Vec3 p_389589_) {
        if (!p_389461_) {
            p_389535_.mulPose(Axis.YP.rotationDegrees(180.0F));
        }

        float f = 0.015625F * this.getSignTextRenderScale();
        p_389535_.translate(p_389589_);
        p_389535_.scale(f, -f, f);
    }

    private static boolean isOutlineVisible(BlockPos p_389438_, int p_389526_) {
        if (p_389526_ == DyeColor.BLACK.getTextColor()) {
            return true;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localplayer = minecraft.player;
            if (localplayer != null && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping()) {
                return true;
            } else {
                Entity entity = minecraft.getCameraEntity();
                return entity != null && entity.distanceToSqr(Vec3.atCenterOf(p_389438_)) < (double)OUTLINE_RENDER_DISTANCE;
            }
        }
    }

    public static int getDarkColor(SignText p_389470_) {
        int i = p_389470_.getColor().getTextColor();
        if (i == DyeColor.BLACK.getTextColor() && p_389470_.hasGlowingText()) {
            return -988212;
        } else {
            double d0 = 0.4;
            int j = (int)((double)ARGB.red(i) * 0.4);
            int k = (int)((double)ARGB.green(i) * 0.4);
            int l = (int)((double)ARGB.blue(i) * 0.4);
            return ARGB.color(0, j, k, l);
        }
    }
}
