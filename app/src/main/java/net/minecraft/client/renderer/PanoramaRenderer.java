package net.minecraft.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PanoramaRenderer {
    public static final ResourceLocation PANORAMA_OVERLAY = ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama_overlay.png");
    private final Minecraft minecraft;
    private final CubeMap cubeMap;
    private float spin;

    public PanoramaRenderer(CubeMap p_110002_) {
        this.cubeMap = p_110002_;
        this.minecraft = Minecraft.getInstance();
    }

    public void render(GuiGraphics p_334063_, int p_333839_, int p_333923_, float p_110004_, float p_110005_) {
        float f = this.minecraft.getDeltaTracker().getRealtimeDeltaTicks();
        float f1 = (float)((double)f * this.minecraft.options.panoramaSpeed().get());
        this.spin = wrap(this.spin + f1 * 0.1F, 360.0F);
        p_334063_.flush();
        this.cubeMap.render(this.minecraft, 10.0F, -this.spin, p_110004_);
        p_334063_.flush();
        p_334063_.blit(RenderType::guiTextured, PANORAMA_OVERLAY, 0, 0, 0.0F, 0.0F, p_333839_, p_333923_, 16, 128, 16, 128, ARGB.white(p_110004_));
    }

    private static float wrap(float p_249058_, float p_249548_) {
        return p_249058_ > p_249548_ ? p_249058_ - p_249548_ : p_249058_;
    }
}
