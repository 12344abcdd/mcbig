package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SwordItem extends Item {
    public SwordItem(ToolMaterial p_361460_, float p_362481_, float p_364182_, Item.Properties p_43272_) {
        super(p_361460_.applySwordProperties(p_43272_, p_362481_, p_364182_));
    }

    @Override
    public boolean canAttackBlock(BlockState p_43291_, Level p_43292_, BlockPos p_43293_, Player p_43294_) {
        return !p_43294_.isCreative();
    }

    @Override
    public boolean hurtEnemy(ItemStack p_43278_, LivingEntity p_43279_, LivingEntity p_43280_) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack p_345553_, LivingEntity p_345771_, LivingEntity p_346282_) {
        p_345553_.hurtAndBreak(1, p_346282_, EquipmentSlot.MAINHAND);
    }
}
