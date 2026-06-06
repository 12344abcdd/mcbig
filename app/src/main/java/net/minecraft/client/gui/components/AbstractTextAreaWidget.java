package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractTextAreaWidget extends AbstractScrollArea {
    private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("widget/text_field"), ResourceLocation.withDefaultNamespace("widget/text_field_highlighted")
    );
    private static final int INNER_PADDING = 4;

    public AbstractTextAreaWidget(int p_388859_, int p_387520_, int p_387683_, int p_387659_, Component p_386737_) {
        super(p_388859_, p_387520_, p_387683_, p_387659_, p_386737_);
    }

    @Override
    public boolean mouseClicked(double p_387557_, double p_388283_, int p_387326_) {
        boolean flag = this.updateScrolling(p_387557_, p_388283_, p_387326_);
        return super.mouseClicked(p_387557_, p_388283_, p_387326_) || flag;
    }

    @Override
    public boolean keyPressed(int p_388331_, int p_388254_, int p_388465_) {
        boolean flag = p_388331_ == 265;
        boolean flag1 = p_388331_ == 264;
        if (flag || flag1) {
            double d0 = this.scrollAmount();
            this.setScrollAmount(this.scrollAmount() + (double)(flag ? -1 : 1) * this.scrollRate());
            if (d0 != this.scrollAmount()) {
                return true;
            }
        }

        return super.keyPressed(p_388331_, p_388254_, p_388465_);
    }

    @Override
    public void renderWidget(GuiGraphics p_386672_, int p_387901_, int p_387577_, float p_387259_) {
        if (this.visible) {
            this.renderBackground(p_386672_);
            p_386672_.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
            p_386672_.pose().pushPose();
            p_386672_.pose().translate(0.0, -this.scrollAmount(), 0.0);
            this.renderContents(p_386672_, p_387901_, p_387577_, p_387259_);
            p_386672_.pose().popPose();
            p_386672_.disableScissor();
            this.renderDecorations(p_386672_);
        }
    }

    protected void renderDecorations(GuiGraphics p_387032_) {
        this.renderScrollbar(p_387032_);
    }

    protected int innerPadding() {
        return 4;
    }

    protected int totalInnerPadding() {
        return this.innerPadding() * 2;
    }

    @Override
    public boolean isMouseOver(double p_386839_, double p_388246_) {
        return this.active
            && this.visible
            && p_386839_ >= (double)this.getX()
            && p_388246_ >= (double)this.getY()
            && p_386839_ < (double)(this.getRight() + 6)
            && p_388246_ < (double)this.getBottom();
    }

    @Override
    protected int scrollBarX() {
        return this.getRight();
    }

    @Override
    protected int contentHeight() {
        return this.getInnerHeight() + this.totalInnerPadding();
    }

    protected void renderBackground(GuiGraphics p_387240_) {
        this.renderBorder(p_387240_, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    protected void renderBorder(GuiGraphics p_388574_, int p_387898_, int p_387393_, int p_388028_, int p_387158_) {
        ResourceLocation resourcelocation = BACKGROUND_SPRITES.get(this.isActive(), this.isFocused());
        p_388574_.blitSprite(RenderType::guiTextured, resourcelocation, p_387898_, p_387393_, p_388028_, p_387158_);
    }

    protected boolean withinContentAreaTopBottom(int p_386967_, int p_387417_) {
        return (double)p_387417_ - this.scrollAmount() >= (double)this.getY() && (double)p_386967_ - this.scrollAmount() <= (double)(this.getY() + this.height);
    }

    protected abstract int getInnerHeight();

    protected abstract void renderContents(GuiGraphics p_386674_, int p_387663_, int p_388111_, float p_387720_);

    protected int getInnerLeft() {
        return this.getX() + this.innerPadding();
    }

    protected int getInnerTop() {
        return this.getY() + this.innerPadding();
    }

    @Override
    public void playDownSound(SoundManager p_386774_) {
    }
}
