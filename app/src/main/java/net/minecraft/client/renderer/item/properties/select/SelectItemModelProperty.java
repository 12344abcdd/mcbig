package net.minecraft.client.renderer.item.properties.select;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface SelectItemModelProperty<T> {
    @Nullable
    T get(ItemStack p_387845_, @Nullable ClientLevel p_387945_, @Nullable LivingEntity p_388349_, int p_388630_, ItemDisplayContext p_388902_);

    SelectItemModelProperty.Type<? extends SelectItemModelProperty<T>, T> type();

    @OnlyIn(Dist.CLIENT)
    public static record Type<P extends SelectItemModelProperty<T>, T>(MapCodec<SelectItemModel.UnbakedSwitch<P, T>> switchCodec) {
        public static <P extends SelectItemModelProperty<T>, T> SelectItemModelProperty.Type<P, T> create(MapCodec<P> p_386816_, Codec<T> p_387850_) {
            Codec<List<SelectItemModel.SwitchCase<T>>> codec = SelectItemModel.SwitchCase.codec(p_387850_)
                .listOf()
                .validate(
                    p_388099_ -> {
                        if (p_388099_.isEmpty()) {
                            return DataResult.error(() -> "Empty case list");
                        } else {
                            Multiset<T> multiset = HashMultiset.create();

                            for (SelectItemModel.SwitchCase<T> switchcase : p_388099_) {
                                multiset.addAll(switchcase.values());
                            }

                            return multiset.size() != multiset.entrySet().size()
                                ? DataResult.error(
                                    () -> "Duplicate case conditions: "
                                            + multiset.entrySet()
                                                .stream()
                                                .filter(p_388701_ -> p_388701_.getCount() > 1)
                                                .map(p_387521_ -> p_387521_.getElement().toString())
                                                .collect(Collectors.joining(", "))
                                )
                                : DataResult.success(p_388099_);
                        }
                    }
                );
            MapCodec<SelectItemModel.UnbakedSwitch<P, T>> mapcodec = RecordCodecBuilder.mapCodec(
                p_386797_ -> p_386797_.group(
                            p_386816_.forGetter(SelectItemModel.UnbakedSwitch::property),
                            codec.fieldOf("cases").forGetter(SelectItemModel.UnbakedSwitch::cases)
                        )
                        .apply(p_386797_, SelectItemModel.UnbakedSwitch::new)
            );
            return new SelectItemModelProperty.Type<>(mapcodec);
        }
    }
}
