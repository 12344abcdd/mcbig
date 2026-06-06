package net.minecraft.client.gui.screens.inventory.tooltip;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TooltipRenderUtil {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/background");
    private static final ResourceLocation FRAME_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/frame");
    public static final int MOUSE_OFFSET = 12;
    private static final int PADDING = 3;
    public static final int PADDING_LEFT = 3;
    public static final int PADDING_RIGHT = 3;
    public static final int PADDING_TOP = 3;
    public static final int PADDING_BOTTOM = 3;
    private static final int MARGIN = 9;

    public static void renderTooltipBackground(
        GuiGraphics p_282666_, int p_281901_, int p_281846_, int p_281559_, int p_283336_, int p_283422_, @Nullable ResourceLocation p_371259_
    ) {
        int i = p_281901_ - 3 - 9;
        int j = p_281846_ - 3 - 9;
        int k = p_281559_ + 3 + 3 + 18;
        int l = p_283336_ + 3 + 3 + 18;
        p_282666_.pose().pushPose();
        p_282666_.pose().translate(0.0F, 0.0F, (float)p_283422_);
        p_282666_.blitSprite(RenderType::guiTextured, getBackgroundSprite(p_371259_), i, j, k, l);
        p_282666_.blitSprite(RenderType::guiTextured, getFrameSprite(p_371259_), i, j, k, l);
        p_282666_.pose().popPose();
    }

    private static ResourceLocation getBackgroundSprite(@Nullable ResourceLocation p_371705_) {
        return p_371705_ == null ? BACKGROUND_SPRITE : p_371705_.withPath(p_371425_ -> "tooltip/" + p_371425_ + "_background");
    }

    private static ResourceLocation getFrameSprite(@Nullable ResourceLocation p_371297_) {
        return p_371297_ == null ? FRAME_SPRITE : p_371297_.withPath(p_371467_ -> "tooltip/" + p_371467_ + "_frame");
    }
}
