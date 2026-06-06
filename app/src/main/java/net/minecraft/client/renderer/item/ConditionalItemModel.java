package net.minecraft.client.renderer.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConditionalItemModel implements ItemModel {
    private final ConditionalItemModelProperty property;
    private final ItemModel onTrue;
    private final ItemModel onFalse;

    public ConditionalItemModel(ConditionalItemModelProperty p_387648_, ItemModel p_387738_, ItemModel p_388649_) {
        this.property = p_387648_;
        this.onTrue = p_387738_;
        this.onFalse = p_388649_;
    }

    @Override
    public void update(
        ItemStackRenderState p_387756_,
        ItemStack p_387286_,
        ItemModelResolver p_386644_,
        ItemDisplayContext p_387754_,
        @Nullable ClientLevel p_388301_,
        @Nullable LivingEntity p_387078_,
        int p_387025_
    ) {
        (this.property.get(p_387286_, p_388301_, p_387078_, p_387025_, p_387754_) ? this.onTrue : this.onFalse)
            .update(p_387756_, p_387286_, p_386644_, p_387754_, p_388301_, p_387078_, p_387025_);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(ConditionalItemModelProperty property, ItemModel.Unbaked onTrue, ItemModel.Unbaked onFalse) implements ItemModel.Unbaked {
        public static final MapCodec<ConditionalItemModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_388916_ -> p_388916_.group(
                        ConditionalItemModelProperties.MAP_CODEC.forGetter(ConditionalItemModel.Unbaked::property),
                        ItemModels.CODEC.fieldOf("on_true").forGetter(ConditionalItemModel.Unbaked::onTrue),
                        ItemModels.CODEC.fieldOf("on_false").forGetter(ConditionalItemModel.Unbaked::onFalse)
                    )
                    .apply(p_388916_, ConditionalItemModel.Unbaked::new)
        );

        @Override
        public MapCodec<ConditionalItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext p_388309_) {
            return new ConditionalItemModel(this.property, this.onTrue.bake(p_388309_), this.onFalse.bake(p_388309_));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver p_388796_) {
            this.onTrue.resolveDependencies(p_388796_);
            this.onFalse.resolveDependencies(p_388796_);
        }
    }
}
