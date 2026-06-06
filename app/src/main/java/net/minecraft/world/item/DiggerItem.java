package net.minecraft.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;

public class DiggerItem extends Item {
    protected DiggerItem(ToolMaterial p_362799_, TagKey<Block> p_204111_, float p_364972_, float p_364718_, Item.Properties p_204112_) {
        super(p_362799_.applyToolProperties(p_204112_, p_204111_, p_364972_, p_364718_));
    }

    @Override
    public boolean hurtEnemy(ItemStack p_40994_, LivingEntity p_40995_, LivingEntity p_40996_) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack p_346200_, LivingEntity p_345855_, LivingEntity p_346191_) {
        p_346200_.hurtAndBreak(2, p_346191_, EquipmentSlot.MAINHAND);
    }
}
