package net.minecraft.client.gui;

import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ItemSlotMouseAction {
    boolean matches(Slot p_362913_);

    boolean onMouseScrolled(double p_360659_, double p_365477_, int p_363499_, ItemStack p_365261_);

    void onStopHovering(Slot p_360941_);

    void onSlotClicked(Slot p_372974_, ClickType p_372997_);
}
