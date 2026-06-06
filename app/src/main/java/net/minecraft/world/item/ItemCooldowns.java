package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.UseCooldown;

public class ItemCooldowns {
    private final Map<ResourceLocation, ItemCooldowns.CooldownInstance> cooldowns = Maps.newHashMap();
    private int tickCount;

    public boolean isOnCooldown(ItemStack p_366432_) {
        return this.getCooldownPercent(p_366432_, 0.0F) > 0.0F;
    }

    public float getCooldownPercent(ItemStack p_366443_, float p_41523_) {
        ResourceLocation resourcelocation = this.getCooldownGroup(p_366443_);
        ItemCooldowns.CooldownInstance itemcooldowns$cooldowninstance = this.cooldowns.get(resourcelocation);
        if (itemcooldowns$cooldowninstance != null) {
            float f = (float)(itemcooldowns$cooldowninstance.endTime - itemcooldowns$cooldowninstance.startTime);
            float f1 = (float)itemcooldowns$cooldowninstance.endTime - ((float)this.tickCount + p_41523_);
            return Mth.clamp(f1 / f, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public void tick() {
        this.tickCount++;
        if (!this.cooldowns.isEmpty()) {
            Iterator<Entry<ResourceLocation, ItemCooldowns.CooldownInstance>> iterator = this.cooldowns.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<ResourceLocation, ItemCooldowns.CooldownInstance> entry = iterator.next();
                if (entry.getValue().endTime <= this.tickCount) {
                    iterator.remove();
                    this.onCooldownEnded(entry.getKey());
                }
            }
        }
    }

    public ResourceLocation getCooldownGroup(ItemStack p_366444_) {
        UseCooldown usecooldown = p_366444_.get(DataComponents.USE_COOLDOWN);
        ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(p_366444_.getItem());
        return usecooldown == null ? resourcelocation : usecooldown.cooldownGroup().orElse(resourcelocation);
    }

    public void addCooldown(ItemStack p_366762_, int p_41526_) {
        this.addCooldown(this.getCooldownGroup(p_366762_), p_41526_);
    }

    public void addCooldown(ResourceLocation p_366429_, int p_366819_) {
        this.cooldowns.put(p_366429_, new ItemCooldowns.CooldownInstance(this.tickCount, this.tickCount + p_366819_));
        this.onCooldownStarted(p_366429_, p_366819_);
    }

    public void removeCooldown(ResourceLocation p_366803_) {
        this.cooldowns.remove(p_366803_);
        this.onCooldownEnded(p_366803_);
    }

    protected void onCooldownStarted(ResourceLocation p_366622_, int p_41530_) {
    }

    protected void onCooldownEnded(ResourceLocation p_366721_) {
    }

    static record CooldownInstance(int startTime, int endTime) {
    }
}
