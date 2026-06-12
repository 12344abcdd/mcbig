package net.minecraft.client.gui.spectator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface SpectatorMenuItem {
    void selectItem(SpectatorMenu p_101842_);

    Component getName();

    void renderIcon(GuiGraphics p_282591_, float p_101840_, float p_361027_);

    boolean isEnabled();
}
