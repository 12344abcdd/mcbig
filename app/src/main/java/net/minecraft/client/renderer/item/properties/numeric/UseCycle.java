package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record UseCycle(float period) implements RangeSelectItemModelProperty {
    public static final MapCodec<UseCycle> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_387123_ -> p_387123_.group(ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("period", 1.0F).forGetter(UseCycle::period)).apply(p_387123_, UseCycle::new)
    );

    @Override
    public float get(ItemStack p_387347_, @Nullable ClientLevel p_388372_, @Nullable LivingEntity p_387048_, int p_388681_) {
        return p_387048_ != null && p_387048_.getUseItem() == p_387347_ ? (float)p_387048_.getUseItemRemainingTicks() % this.period : 0.0F;
    }

    @Override
    public MapCodec<UseCycle> type() {
        return MAP_CODEC;
    }
}
