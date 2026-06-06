package net.minecraft.client.renderer.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SelectItemModel<T> implements ItemModel {
    private final SelectItemModelProperty<T> property;
    private final Object2ObjectMap<T, ItemModel> models;

    public SelectItemModel(SelectItemModelProperty<T> p_387418_, Object2ObjectMap<T, ItemModel> p_387958_) {
        this.property = p_387418_;
        this.models = p_387958_;
    }

    @Override
    public void update(
        ItemStackRenderState p_387349_,
        ItemStack p_386764_,
        ItemModelResolver p_388842_,
        ItemDisplayContext p_386961_,
        @Nullable ClientLevel p_386907_,
        @Nullable LivingEntity p_387755_,
        int p_386608_
    ) {
        T t = this.property.get(p_386764_, p_386907_, p_387755_, p_386608_, p_386961_);
        ItemModel itemmodel = this.models.get(t);
        if (itemmodel != null) {
            itemmodel.update(p_387349_, p_386764_, p_388842_, p_386961_, p_386907_, p_387755_, p_386608_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record SwitchCase<T>(List<T> values, ItemModel.Unbaked model) {
        public static <T> Codec<SelectItemModel.SwitchCase<T>> codec(Codec<T> p_387015_) {
            return RecordCodecBuilder.create(
                p_387815_ -> p_387815_.group(
                            ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(p_387015_)).fieldOf("when").forGetter(SelectItemModel.SwitchCase::values),
                            ItemModels.CODEC.fieldOf("model").forGetter(SelectItemModel.SwitchCase::model)
                        )
                        .apply(p_387815_, SelectItemModel.SwitchCase::new)
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(SelectItemModel.UnbakedSwitch<?, ?> unbakedSwitch, Optional<ItemModel.Unbaked> fallback) implements ItemModel.Unbaked {
        public static final MapCodec<SelectItemModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_386872_ -> p_386872_.group(
                        SelectItemModel.UnbakedSwitch.MAP_CODEC.forGetter(SelectItemModel.Unbaked::unbakedSwitch),
                        ItemModels.CODEC.optionalFieldOf("fallback").forGetter(SelectItemModel.Unbaked::fallback)
                    )
                    .apply(p_386872_, SelectItemModel.Unbaked::new)
        );

        @Override
        public MapCodec<SelectItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext p_388757_) {
            ItemModel itemmodel = this.fallback.<ItemModel>map(p_387332_ -> p_387332_.bake(p_388757_)).orElse(p_388757_.missingItemModel());
            return this.unbakedSwitch.bake(p_388757_, itemmodel);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver p_386548_) {
            this.unbakedSwitch.resolveDependencies(p_386548_);
            this.fallback.ifPresent(p_388441_ -> p_388441_.resolveDependencies(p_386548_));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record UnbakedSwitch<P extends SelectItemModelProperty<T>, T>(P property, List<SelectItemModel.SwitchCase<T>> cases) {
        public static final MapCodec<SelectItemModel.UnbakedSwitch<?, ?>> MAP_CODEC = SelectItemModelProperties.CODEC
            .dispatchMap("property", p_387573_ -> p_387573_.property().type(), SelectItemModelProperty.Type::switchCodec);

        public ItemModel bake(ItemModel.BakingContext p_386650_, ItemModel p_388617_) {
            Object2ObjectMap<T, ItemModel> object2objectmap = new Object2ObjectOpenHashMap<>();

            for (SelectItemModel.SwitchCase<T> switchcase : this.cases) {
                ItemModel.Unbaked itemmodel$unbaked = switchcase.model;
                ItemModel itemmodel = itemmodel$unbaked.bake(p_386650_);

                for (T t : switchcase.values) {
                    object2objectmap.put(t, itemmodel);
                }
            }

            object2objectmap.defaultReturnValue(p_388617_);
            return new SelectItemModel<>(this.property, object2objectmap);
        }

        public void resolveDependencies(ResolvableModel.Resolver p_388258_) {
            for (SelectItemModel.SwitchCase<?> switchcase : this.cases) {
                switchcase.model.resolveDependencies(p_388258_);
            }
        }
    }
}
