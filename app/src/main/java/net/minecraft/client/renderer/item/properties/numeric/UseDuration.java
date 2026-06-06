package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record UseDuration(boolean remaining) implements RangeSelectItemModelProperty {
    public static final MapCodec<UseDuration> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_387574_ -> p_387574_.group(Codec.BOOL.optionalFieldOf("remaining", Boolean.valueOf(false)).forGetter(UseDuration::remaining))
                .apply(p_387574_, UseDuration::new)
    );

    @Override
    public float get(ItemStack p_388572_, @Nullable ClientLevel p_387106_, @Nullable LivingEntity p_387276_, int p_386612_) {
        if (p_387276_ != null && p_387276_.getUseItem() == p_388572_) {
            return this.remaining ? (float)p_387276_.getUseItemRemainingTicks() : (float)useDuration(p_388572_, p_387276_);
        } else {
            return 0.0F;
        }
    }

    @Override
    public MapCodec<UseDuration> type() {
        return MAP_CODEC;
    }

    public static int useDuration(ItemStack p_387488_, LivingEntity p_388304_) {
        return p_387488_.getUseDuration(p_388304_) - p_388304_.getUseItemRemainingTicks();
    }
}
