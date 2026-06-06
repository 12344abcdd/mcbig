package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ConditionalItemModelProperty {
    boolean get(ItemStack p_387265_, @Nullable ClientLevel p_386652_, @Nullable LivingEntity p_388843_, int p_388885_, ItemDisplayContext p_389576_);

    MapCodec<? extends ConditionalItemModelProperty> type();
}
