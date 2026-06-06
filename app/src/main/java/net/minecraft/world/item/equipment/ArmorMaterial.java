package net.minecraft.world.item.equipment;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record ArmorMaterial(
    int durability,
    Map<ArmorType, Integer> defense,
    int enchantmentValue,
    Holder<SoundEvent> equipSound,
    float toughness,
    float knockbackResistance,
    TagKey<Item> repairIngredient,
    ResourceKey<EquipmentAsset> assetId
) {
    public Item.Properties humanoidProperties(Item.Properties p_371701_, ArmorType p_371660_) {
        return p_371701_.durability(p_371660_.getDurability(this.durability))
            .attributes(this.createAttributes(p_371660_))
            .enchantable(this.enchantmentValue)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(p_371660_.getSlot()).setEquipSound(this.equipSound).setAsset(this.assetId).build())
            .repairable(this.repairIngredient);
    }

    public Item.Properties animalProperties(Item.Properties p_371435_, HolderSet<EntityType<?>> p_371268_) {
        return p_371435_.durability(ArmorType.BODY.getDurability(this.durability))
            .attributes(this.createAttributes(ArmorType.BODY))
            .repairable(this.repairIngredient)
            .component(
                DataComponents.EQUIPPABLE,
                Equippable.builder(EquipmentSlot.BODY).setEquipSound(this.equipSound).setAsset(this.assetId).setAllowedEntities(p_371268_).build()
            );
    }

    public Item.Properties animalProperties(Item.Properties p_376672_, Holder<SoundEvent> p_381624_, boolean p_380284_, HolderSet<EntityType<?>> p_376684_) {
        if (p_380284_) {
            p_376672_ = p_376672_.durability(ArmorType.BODY.getDurability(this.durability)).repairable(this.repairIngredient);
        }

        return p_376672_.attributes(this.createAttributes(ArmorType.BODY))
            .component(
                DataComponents.EQUIPPABLE,
                Equippable.builder(EquipmentSlot.BODY)
                    .setEquipSound(p_381624_)
                    .setAsset(this.assetId)
                    .setAllowedEntities(p_376684_)
                    .setDamageOnHurt(p_380284_)
                    .build()
            );
    }

    private ItemAttributeModifiers createAttributes(ArmorType p_371239_) {
        int i = this.defense.getOrDefault(p_371239_, 0);
        ItemAttributeModifiers.Builder itemattributemodifiers$builder = ItemAttributeModifiers.builder();
        EquipmentSlotGroup equipmentslotgroup = EquipmentSlotGroup.bySlot(p_371239_.getSlot());
        ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace("armor." + p_371239_.getName());
        itemattributemodifiers$builder.add(
            Attributes.ARMOR, new AttributeModifier(resourcelocation, (double)i, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup
        );
        itemattributemodifiers$builder.add(
            Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(resourcelocation, (double)this.toughness, AttributeModifier.Operation.ADD_VALUE),
            equipmentslotgroup
        );
        if (this.knockbackResistance > 0.0F) {
            itemattributemodifiers$builder.add(
                Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(resourcelocation, (double)this.knockbackResistance, AttributeModifier.Operation.ADD_VALUE),
                equipmentslotgroup
            );
        }

        return itemattributemodifiers$builder.build();
    }
}
