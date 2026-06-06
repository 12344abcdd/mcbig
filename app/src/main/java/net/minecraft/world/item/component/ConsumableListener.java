package net.minecraft.world.item.component;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ConsumableListener {
    void onConsume(Level p_366714_, LivingEntity p_366581_, ItemStack p_366512_, Consumable p_366630_);
}
