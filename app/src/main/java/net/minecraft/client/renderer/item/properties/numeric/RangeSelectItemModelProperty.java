package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface RangeSelectItemModelProperty {
    float get(ItemStack p_388695_, @Nullable ClientLevel p_388363_, @Nullable LivingEntity p_387282_, int p_386614_);

    MapCodec<? extends RangeSelectItemModelProperty> type();
}
