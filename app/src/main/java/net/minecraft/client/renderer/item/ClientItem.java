package net.minecraft.client.renderer.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ClientItem(ItemModel.Unbaked model, ClientItem.Properties properties) {
    public static final Codec<ClientItem> CODEC = RecordCodecBuilder.create(
        p_390086_ -> p_390086_.group(
                    ItemModels.CODEC.fieldOf("model").forGetter(ClientItem::model), ClientItem.Properties.MAP_CODEC.forGetter(ClientItem::properties)
                )
                .apply(p_390086_, ClientItem::new)
    );

    @OnlyIn(Dist.CLIENT)
    public static record Properties(boolean handAnimationOnSwap) {
        public static final ClientItem.Properties DEFAULT = new ClientItem.Properties(true);
        public static final MapCodec<ClientItem.Properties> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_390483_ -> p_390483_.group(
                        Codec.BOOL.optionalFieldOf("hand_animation_on_swap", Boolean.valueOf(true)).forGetter(ClientItem.Properties::handAnimationOnSwap)
                    )
                    .apply(p_390483_, ClientItem.Properties::new)
        );
    }
}
