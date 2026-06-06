package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractScrollArea extends AbstractWidget {
    public static final int SCROLLBAR_WIDTH = 6;
    private double scrollAmount;
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
    private static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller_background");
    private boolean scrolling;

    public AbstractScrollArea(int p_386878_, int p_387233_, int p_388234_, int p_386759_, Component p_388945_) {
        super(p_386878_, p_387233_, p_388234_, p_386759_, p_388945_);
    }

    @Override
    public boolean mouseScrolled(double p_388530_, double p_387300_, double p_388604_, double p_386550_) {
        if (!this.visible) {
            return false;
        } else {
            this.setScrollAmount(this.scrollAmount() - p_386550_ * this.scrollRate());
            return true;
        }
    }

    @Override
    public boolean mouseDragged(double p_387876_, double p_387261_, int p_386570_, double p_387432_, double p_386793_) {
        if (this.scrolling) {
            if (p_387261_ < (double)this.getY()) {
                this.setScrollAmount(0.0);
            } else if (p_387261_ > (double)this.getBottom()) {
                this.setScrollAmount((double)this.maxScrollAmount());
            } else {
                double d0 = (double)Math.max(1, this.maxScrollAmount());
                int i = this.scrollerHeight();
                double d1 = Math.max(1.0, d0 / (double)(this.height - i));
                this.setScrollAmount(this.scrollAmount() + p_386793_ * d1);
            }

            return true;
        } else {
            return super.mouseDragged(p_387876_, p_387261_, p_386570_, p_387432_, p_386793_);
        }
    }

    @Override
    public void onRelease(double p_387091_, double p_388483_) {
        this.scrolling = false;
    }

    public double scrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double p_387814_) {
        this.scrollAmount = Mth.clamp(p_387814_, 0.0, (double)this.maxScrollAmount());
    }

    public boolean updateScrolling(double p_388399_, double p_387442_, int p_388346_) {
        this.scrolling = this.scrollbarVisible()
            && this.isValidClickButton(p_388346_)
            && p_388399_ >= (double)this.scrollBarX()
            && p_388399_ <= (double)(this.scrollBarX() + 6)
            && p_387442_ >= (double)this.getY()
            && p_387442_ < (double)this.getBottom();
        return this.scrolling;
    }

    public void refreshScrollAmount() {
        this.setScrollAmount(this.scrollAmount);
    }

    public int maxScrollAmount() {
        return Math.max(0, this.contentHeight() - this.height);
    }

    protected boolean scrollbarVisible() {
        return this.maxScrollAmount() > 0;
    }

    protected int scrollerHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.contentHeight()), 32, this.height - 8);
    }

    protected int scrollBarX() {
        return this.getRight() - 6;
    }

    protected int scrollBarY() {
        return Math.max(this.getY(), (int)this.scrollAmount * (this.height - this.scrollerHeight()) / this.maxScrollAmount() + this.getY());
    }

    protected void renderScrollbar(GuiGraphics p_386501_) {
        if (this.scrollbarVisible()) {
            int i = this.scrollBarX();
            int j = this.scrollerHeight();
            int k = this.scrollBarY();
            p_386501_.blitSprite(RenderType::guiTextured, SCROLLER_BACKGROUND_SPRITE, i, this.getY(), 6, this.getHeight());
            p_386501_.blitSprite(RenderType::guiTextured, SCROLLER_SPRITE, i, k, 6, j);
        }
    }

    protected abstract int contentHeight();

    protected abstract double scrollRate();
}
