package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractRecipeBookScreen<T extends RecipeBookMenu> extends AbstractContainerScreen<T> implements RecipeUpdateListener {
    private final RecipeBookComponent<?> recipeBookComponent;
    private boolean widthTooNarrow;

    public AbstractRecipeBookScreen(T p_376876_, RecipeBookComponent<?> p_376668_, Inventory p_376880_, Component p_376410_) {
        super(p_376876_, p_376880_, p_376410_);
        this.recipeBookComponent = p_376668_;
    }

    @Override
    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.initButton();
    }

    protected abstract ScreenPosition getRecipeBookButtonPosition();

    private void initButton() {
        ScreenPosition screenposition = this.getRecipeBookButtonPosition();
        this.addRenderableWidget(new ImageButton(screenposition.x(), screenposition.y(), 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, p_376441_ -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            ScreenPosition screenposition1 = this.getRecipeBookButtonPosition();
            p_376441_.setPosition(screenposition1.x(), screenposition1.y());
            this.onRecipeBookButtonClick();
        }));
        this.addWidget(this.recipeBookComponent);
    }

    protected void onRecipeBookButtonClick() {
    }

    @Override
    public void render(GuiGraphics p_376146_, int p_376361_, int p_376653_, float p_376448_) {
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBackground(p_376146_, p_376361_, p_376653_, p_376448_);
        } else {
            super.render(p_376146_, p_376361_, p_376653_, p_376448_);
        }

        this.recipeBookComponent.render(p_376146_, p_376361_, p_376653_, p_376448_);
        this.renderTooltip(p_376146_, p_376361_, p_376653_);
        this.recipeBookComponent.renderTooltip(p_376146_, p_376361_, p_376653_, this.hoveredSlot);
    }

    @Override
    protected void renderSlots(GuiGraphics p_376566_) {
        super.renderSlots(p_376566_);
        this.recipeBookComponent.renderGhostRecipe(p_376566_, this.isBiggerResultSlot());
    }

    protected boolean isBiggerResultSlot() {
        return true;
    }

    @Override
    public boolean charTyped(char p_376170_, int p_376511_) {
        return this.recipeBookComponent.charTyped(p_376170_, p_376511_) ? true : super.charTyped(p_376170_, p_376511_);
    }

    @Override
    public boolean keyPressed(int p_376379_, int p_376138_, int p_376148_) {
        return this.recipeBookComponent.keyPressed(p_376379_, p_376138_, p_376148_) ? true : super.keyPressed(p_376379_, p_376138_, p_376148_);
    }

    @Override
    public boolean mouseClicked(double p_376773_, double p_376144_, int p_376496_) {
        if (this.recipeBookComponent.mouseClicked(p_376773_, p_376144_, p_376496_)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(p_376773_, p_376144_, p_376496_);
        }
    }

    @Override
    protected boolean isHovering(int p_376913_, int p_376169_, int p_376269_, int p_376237_, double p_376730_, double p_376230_) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible())
            && super.isHovering(p_376913_, p_376169_, p_376269_, p_376237_, p_376730_, p_376230_);
    }

    @Override
    protected boolean hasClickedOutside(double p_376729_, double p_376497_, int p_376612_, int p_376679_, int p_376937_) {
        boolean flag = p_376729_ < (double)p_376612_
            || p_376497_ < (double)p_376679_
            || p_376729_ >= (double)(p_376612_ + this.imageWidth)
            || p_376497_ >= (double)(p_376679_ + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(p_376729_, p_376497_, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, p_376937_)
            && flag;
    }

    @Override
    protected void slotClicked(Slot p_376636_, int p_376122_, int p_376346_, ClickType p_376809_) {
        super.slotClicked(p_376636_, p_376122_, p_376346_, p_376809_);
        this.recipeBookComponent.slotClicked(p_376636_);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public void fillGhostRecipe(RecipeDisplay p_379988_) {
        this.recipeBookComponent.fillGhostRecipe(p_379988_);
    }
}
