package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerFaceRenderer {
    public static final int SKIN_HEAD_U = 8;
    public static final int SKIN_HEAD_V = 8;
    public static final int SKIN_HEAD_WIDTH = 8;
    public static final int SKIN_HEAD_HEIGHT = 8;
    public static final int SKIN_HAT_U = 40;
    public static final int SKIN_HAT_V = 8;
    public static final int SKIN_HAT_WIDTH = 8;
    public static final int SKIN_HAT_HEIGHT = 8;
    public static final int SKIN_TEX_WIDTH = 64;
    public static final int SKIN_TEX_HEIGHT = 64;

    public static void draw(GuiGraphics p_296230_, PlayerSkin p_295435_, int p_294736_, int p_295624_, int p_295191_) {
        draw(p_296230_, p_295435_, p_294736_, p_295624_, p_295191_, -1);
    }

    public static void draw(GuiGraphics p_281827_, PlayerSkin p_365093_, int p_282126_, int p_281693_, int p_281565_, int p_364021_) {
        draw(p_281827_, p_365093_.texture(), p_282126_, p_281693_, p_281565_, true, false, p_364021_);
    }

    public static void draw(
        GuiGraphics p_283244_, ResourceLocation p_281495_, int p_282035_, int p_282441_, int p_281801_, boolean p_283149_, boolean p_283555_, int p_362543_
    ) {
        int i = 8 + (p_283555_ ? 8 : 0);
        int j = 8 * (p_283555_ ? -1 : 1);
        p_283244_.blit(RenderType::guiTextured, p_281495_, p_282035_, p_282441_, 8.0F, (float)i, p_281801_, p_281801_, 8, j, 64, 64, p_362543_);
        if (p_283149_) {
            drawHat(p_283244_, p_281495_, p_282035_, p_282441_, p_281801_, p_283555_, p_362543_);
        }
    }

    private static void drawHat(
        GuiGraphics p_282228_, ResourceLocation p_282835_, int p_282585_, int p_282234_, int p_282576_, boolean p_281523_, int p_363508_
    ) {
        int i = 8 + (p_281523_ ? 8 : 0);
        int j = 8 * (p_281523_ ? -1 : 1);
        p_282228_.blit(RenderType::guiTextured, p_282835_, p_282585_, p_282234_, 40.0F, (float)i, p_282576_, p_282576_, 8, j, 64, 64, p_363508_);
    }
}
