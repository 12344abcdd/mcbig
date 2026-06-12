package net.minecraft.client.gui.screens.recipebook;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class GhostSlots {
    private final Reference2ObjectMap<Slot, GhostSlots.GhostSlot> ingredients = new Reference2ObjectArrayMap<>();
    private final SlotSelectTime slotSelectTime;

    public GhostSlots(SlotSelectTime p_361863_) {
        this.slotSelectTime = p_361863_;
    }

    public void clear() {
        this.ingredients.clear();
    }

    private void setSlot(Slot p_380315_, ContextMap p_380942_, SlotDisplay p_379948_, boolean p_379880_) {
        List<ItemStack> list = p_379948_.resolveForStacks(p_380942_);
        if (!list.isEmpty()) {
            this.ingredients.put(p_380315_, new GhostSlots.GhostSlot(list, p_379880_));
        }
    }

    protected void setInput(Slot p_379329_, ContextMap p_381026_, SlotDisplay p_379367_) {
        this.setSlot(p_379329_, p_381026_, p_379367_, false);
    }

    protected void setResult(Slot p_379733_, ContextMap p_381037_, SlotDisplay p_380406_) {
        this.setSlot(p_379733_, p_381037_, p_380406_, true);
    }

    public void render(GuiGraphics p_360912_, Minecraft p_363356_, boolean p_361600_) {
        this.ingredients.forEach((p_375414_, p_375415_) -> {
            int i = p_375414_.x;
            int j = p_375414_.y;
            if (p_375415_.isResultSlot && p_361600_) {
                p_360912_.fill(i - 4, j - 4, i + 20, j + 20, 822018048);
            } else {
                p_360912_.fill(i, j, i + 16, j + 16, 822018048);
            }

            ItemStack itemstack = p_375415_.getItem(this.slotSelectTime.currentIndex());
            p_360912_.renderFakeItem(itemstack, i, j);
            p_360912_.fill(RenderType.guiGhostRecipeOverlay(), i, j, i + 16, j + 16, 822083583);
            if (p_375415_.isResultSlot) {
                p_360912_.renderItemDecorations(p_363356_.font, itemstack, i, j);
            }
        });
    }

    public void renderTooltip(GuiGraphics p_363721_, Minecraft p_365392_, int p_363797_, int p_363310_, @Nullable Slot p_360907_) {
        if (p_360907_ != null) {
            GhostSlots.GhostSlot ghostslots$ghostslot = this.ingredients.get(p_360907_);
            if (ghostslots$ghostslot != null) {
                ItemStack itemstack = ghostslots$ghostslot.getItem(this.slotSelectTime.currentIndex());
                p_363721_.renderComponentTooltip(
                    p_365392_.font, Screen.getTooltipFromItem(p_365392_, itemstack), p_363797_, p_363310_, itemstack.get(DataComponents.TOOLTIP_STYLE)
                );
            }
        }
    }

    static record GhostSlot(List<ItemStack> items, boolean isResultSlot) {
        public ItemStack getItem(int p_361727_) {
            int i = this.items.size();
            return i == 0 ? ItemStack.EMPTY : this.items.get(p_361727_ % i);
        }
    }
}
