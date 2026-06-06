package net.minecraft.world.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public interface Shearable {
    void shear(ServerLevel p_376429_, SoundSource p_21749_, ItemStack p_372963_);

    boolean readyForShearing();
}
