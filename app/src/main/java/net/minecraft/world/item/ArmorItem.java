package net.minecraft.world.item;

import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class ArmorItem extends Item {
    public ArmorItem(ArmorMaterial p_371793_, ArmorType p_371848_, Item.Properties p_40388_) {
        super(p_371793_.humanoidProperties(p_40388_, p_371848_));
    }
}
