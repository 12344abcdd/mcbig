package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.state.BlockDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public abstract class DisplayRenderer<T extends Display, S, ST extends DisplayEntityRenderState> extends EntityRenderer<T, ST> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    protected DisplayRenderer(EntityRendererProvider.Context p_270168_) {
        super(p_270168_);
        this.entityRenderDispatcher = p_270168_.getEntityRenderDispatcher();
    }

    protected AABB getBoundingBoxForCulling(T p_363062_) {
        return p_363062_.getBoundingBoxForCulling();
    }

    protected boolean affectedByCulling(T p_362589_) {
        return p_362589_.affectedByCulling();
    }

    private static int getBrightnessOverride(Display p_368691_) {
        Display.RenderState display$renderstate = p_368691_.renderState();
        return display$renderstate != null ? display$renderstate.brightnessOverride() : -1;
    }

    protected int getSkyLightLevel(T p_368549_, BlockPos p_368562_) {
        int i = getBrightnessOverride(p_368549_);
        return i != -1 ? LightTexture.sky(i) : super.getSkyLightLevel(p_368549_, p_368562_);
    }

    protected int getBlockLightLevel(T p_368656_, BlockPos p_368591_) {
        int i = getBrightnessOverride(p_368656_);
        return i != -1 ? LightTexture.block(i) : super.getBlockLightLevel(p_368656_, p_368591_);
    }

    protected float getShadowRadius(ST p_382866_) {
        Display.RenderState display$renderstate = p_382866_.renderState;
        return display$renderstate == null ? 0.0F : display$renderstate.shadowRadius().get(p_382866_.interpolationProgress);
    }

    protected float getShadowStrength(ST p_383074_) {
        Display.RenderState display$renderstate = p_383074_.renderState;
        return display$renderstate == null ? 0.0F : display$renderstate.shadowStrength().get(p_383074_.interpolationProgress);
    }

    public void render(ST p_360624_, PoseStack p_270117_, MultiBufferSource p_270319_, int p_270659_) {
        Display.RenderState display$renderstate = p_360624_.renderState;
        if (display$renderstate != null && p_360624_.hasSubState()) {
            float f = p_360624_.interpolationProgress;
            super.render(p_360624_, p_270117_, p_270319_, p_270659_);
            p_270117_.pushPose();
            p_270117_.mulPose(this.calculateOrientation(display$renderstate, p_360624_, new Quaternionf()));
            Transformation transformation = display$renderstate.transformation().get(f);
            p_270117_.mulPose(transformation.getMatrix());
            this.renderInner(p_360624_, p_270117_, p_270319_, p_270659_, f);
            p_270117_.popPose();
        }
    }

    private Quaternionf calculateOrientation(Display.RenderState p_277846_, ST p_364909_, Quaternionf p_295809_) {
        Camera camera = this.entityRenderDispatcher.camera;

        return switch (p_277846_.billboardConstraints()) {
            case FIXED -> p_295809_.rotationYXZ((float) (-Math.PI / 180.0) * p_364909_.entityYRot, (float) (Math.PI / 180.0) * p_364909_.entityXRot, 0.0F);
            case HORIZONTAL -> p_295809_.rotationYXZ((float) (-Math.PI / 180.0) * p_364909_.entityYRot, (float) (Math.PI / 180.0) * cameraXRot(camera), 0.0F);
            case VERTICAL -> p_295809_.rotationYXZ((float) (-Math.PI / 180.0) * cameraYrot(camera), (float) (Math.PI / 180.0) * p_364909_.entityXRot, 0.0F);
            case CENTER -> p_295809_.rotationYXZ((float) (-Math.PI / 180.0) * cameraYrot(camera), (float) (Math.PI / 180.0) * cameraXRot(camera), 0.0F);
        };
    }

    private static float cameraYrot(Camera p_295988_) {
        return p_295988_.getYRot() - 180.0F;
    }

    private static float cameraXRot(Camera p_295299_) {
        return -p_295299_.getXRot();
    }

    private static <T extends Display> float entityYRot(T p_295109_, float p_295760_) {
        return p_295109_.getYRot(p_295760_);
    }

    private static <T extends Display> float entityXRot(T p_295884_, float p_294935_) {
        return p_295884_.getXRot(p_294935_);
    }

    protected abstract void renderInner(ST p_364455_, PoseStack p_277686_, MultiBufferSource p_277429_, int p_278023_, float p_277453_);

    public void extractRenderState(T p_362672_, ST p_361329_, float p_365301_) {
        super.extractRenderState(p_362672_, p_361329_, p_365301_);
        p_361329_.renderState = p_362672_.renderState();
        p_361329_.interpolationProgress = p_362672_.calculateInterpolationProgress(p_365301_);
        p_361329_.entityYRot = entityYRot(p_362672_, p_365301_);
        p_361329_.entityXRot = entityXRot(p_362672_, p_365301_);
    }

    @OnlyIn(Dist.CLIENT)
    public static class BlockDisplayRenderer
        extends DisplayRenderer<Display.BlockDisplay, Display.BlockDisplay.BlockRenderState, BlockDisplayEntityRenderState> {
        private final BlockRenderDispatcher blockRenderer;

        protected BlockDisplayRenderer(EntityRendererProvider.Context p_270283_) {
            super(p_270283_);
            this.blockRenderer = p_270283_.getBlockRenderDispatcher();
        }

        public BlockDisplayEntityRenderState createRenderState() {
            return new BlockDisplayEntityRenderState();
        }

        public void extractRenderState(Display.BlockDisplay p_362697_, BlockDisplayEntityRenderState p_363759_, float p_360854_) {
            super.extractRenderState(p_362697_, p_363759_, p_360854_);
            p_363759_.blockRenderState = p_362697_.blockRenderState();
        }

        public void renderInner(BlockDisplayEntityRenderState p_362193_, PoseStack p_277831_, MultiBufferSource p_277554_, int p_278071_, float p_277847_) {
            this.blockRenderer.renderSingleBlock(p_362193_.blockRenderState.blockState(), p_277831_, p_277554_, p_278071_, OverlayTexture.NO_OVERLAY);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ItemDisplayRenderer extends DisplayRenderer<Display.ItemDisplay, Display.ItemDisplay.ItemRenderState, ItemDisplayEntityRenderState> {
        private final ItemModelResolver itemModelResolver;

        protected ItemDisplayRenderer(EntityRendererProvider.Context p_270110_) {
            super(p_270110_);
            this.itemModelResolver = p_270110_.getItemModelResolver();
        }

        public ItemDisplayEntityRenderState createRenderState() {
            return new ItemDisplayEntityRenderState();
        }

        public void extractRenderState(Display.ItemDisplay p_360671_, ItemDisplayEntityRenderState p_361611_, float p_361257_) {
            super.extractRenderState(p_360671_, p_361611_, p_361257_);
            Display.ItemDisplay.ItemRenderState display$itemdisplay$itemrenderstate = p_360671_.itemRenderState();
            if (display$itemdisplay$itemrenderstate != null) {
                this.itemModelResolver
                    .updateForNonLiving(
                        p_361611_.item, display$itemdisplay$itemrenderstate.itemStack(), display$itemdisplay$itemrenderstate.itemTransform(), p_360671_
                    );
            } else {
                p_361611_.item.clear();
            }
        }

        public void renderInner(ItemDisplayEntityRenderState p_364899_, PoseStack p_277361_, MultiBufferSource p_277912_, int p_277474_, float p_278032_) {
            if (!p_364899_.item.isEmpty()) {
                p_277361_.mulPose(Axis.YP.rotation((float) Math.PI));
                p_364899_.item.render(p_277361_, p_277912_, p_277474_, OverlayTexture.NO_OVERLAY);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TextDisplayRenderer extends DisplayRenderer<Display.TextDisplay, Display.TextDisplay.TextRenderState, TextDisplayEntityRenderState> {
        private final Font font;

        protected TextDisplayRenderer(EntityRendererProvider.Context p_271012_) {
            super(p_271012_);
            this.font = p_271012_.getFont();
        }

        public TextDisplayEntityRenderState createRenderState() {
            return new TextDisplayEntityRenderState();
        }

        public void extractRenderState(Display.TextDisplay p_362264_, TextDisplayEntityRenderState p_365023_, float p_365012_) {
            super.extractRenderState(p_362264_, p_365023_, p_365012_);
            p_365023_.textRenderState = p_362264_.textRenderState();
            p_365023_.cachedInfo = p_362264_.cacheDisplay(this::splitLines);
        }

        private Display.TextDisplay.CachedInfo splitLines(Component p_270823_, int p_270893_) {
            List<FormattedCharSequence> list = this.font.split(p_270823_, p_270893_);
            List<Display.TextDisplay.CachedLine> list1 = new ArrayList<>(list.size());
            int i = 0;

            for (FormattedCharSequence formattedcharsequence : list) {
                int j = this.font.width(formattedcharsequence);
                i = Math.max(i, j);
                list1.add(new Display.TextDisplay.CachedLine(formattedcharsequence, j));
            }

            return new Display.TextDisplay.CachedInfo(list1, i);
        }

        public void renderInner(TextDisplayEntityRenderState p_363505_, PoseStack p_277503_, MultiBufferSource p_278036_, int p_278079_, float p_277784_) {
            Display.TextDisplay.TextRenderState display$textdisplay$textrenderstate = p_363505_.textRenderState;
            byte b0 = display$textdisplay$textrenderstate.flags();
            boolean flag = (b0 & 2) != 0;
            boolean flag1 = (b0 & 4) != 0;
            boolean flag2 = (b0 & 1) != 0;
            Display.TextDisplay.Align display$textdisplay$align = Display.TextDisplay.getAlign(b0);
            byte b1 = (byte)display$textdisplay$textrenderstate.textOpacity().get(p_277784_);
            int i;
            if (flag1) {
                float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                i = (int)(f * 255.0F) << 24;
            } else {
                i = display$textdisplay$textrenderstate.backgroundColor().get(p_277784_);
            }

            float f2 = 0.0F;
            Matrix4f matrix4f = p_277503_.last().pose();
            matrix4f.rotate((float) Math.PI, 0.0F, 1.0F, 0.0F);
            matrix4f.scale(-0.025F, -0.025F, -0.025F);
            Display.TextDisplay.CachedInfo display$textdisplay$cachedinfo = p_363505_.cachedInfo;
            int j = 1;
            int k = 9 + 1;
            int l = display$textdisplay$cachedinfo.width();
            int i1 = display$textdisplay$cachedinfo.lines().size() * k - 1;
            matrix4f.translate(1.0F - (float)l / 2.0F, (float)(-i1), 0.0F);
            if (i != 0) {
                VertexConsumer vertexconsumer = p_278036_.getBuffer(flag ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
                vertexconsumer.addVertex(matrix4f, -1.0F, -1.0F, 0.0F).setColor(i).setLight(p_278079_);
                vertexconsumer.addVertex(matrix4f, -1.0F, (float)i1, 0.0F).setColor(i).setLight(p_278079_);
                vertexconsumer.addVertex(matrix4f, (float)l, (float)i1, 0.0F).setColor(i).setLight(p_278079_);
                vertexconsumer.addVertex(matrix4f, (float)l, -1.0F, 0.0F).setColor(i).setLight(p_278079_);
            }

            for (Display.TextDisplay.CachedLine display$textdisplay$cachedline : display$textdisplay$cachedinfo.lines()) {
                float f1 = switch (display$textdisplay$align) {
                    case LEFT -> 0.0F;
                    case RIGHT -> (float)(l - display$textdisplay$cachedline.width());
                    case CENTER -> (float)l / 2.0F - (float)display$textdisplay$cachedline.width() / 2.0F;
                };
                this.font
                    .drawInBatch(
                        display$textdisplay$cachedline.contents(),
                        f1,
                        f2,
                        b1 << 24 | 16777215,
                        flag2,
                        matrix4f,
                        p_278036_,
                        flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
                        0,
                        p_278079_
                    );
                f2 += (float)k;
            }
        }
    }
}
