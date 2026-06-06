package net.minecraft.client.color.item;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ItemTintSource {
    int calculate(ItemStack p_388652_, @Nullable ClientLevel p_390356_, @Nullable LivingEntity p_390510_);

    MapCodec<? extends ItemTintSource> type();
}
