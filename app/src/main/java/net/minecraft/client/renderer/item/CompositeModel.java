package net.minecraft.client.renderer.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CompositeModel implements ItemModel {
    private final List<ItemModel> models;

    public CompositeModel(List<ItemModel> p_388744_) {
        this.models = p_388744_;
    }

    @Override
    public void update(
        ItemStackRenderState p_386893_,
        ItemStack p_387281_,
        ItemModelResolver p_388412_,
        ItemDisplayContext p_388059_,
        @Nullable ClientLevel p_388484_,
        @Nullable LivingEntity p_388840_,
        int p_387330_
    ) {
        p_386893_.ensureCapacity(this.models.size());

        for (ItemModel itemmodel : this.models) {
            itemmodel.update(p_386893_, p_387281_, p_388412_, p_388059_, p_388484_, p_388840_, p_387330_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(List<ItemModel.Unbaked> models) implements ItemModel.Unbaked {
        public static final MapCodec<CompositeModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_388702_ -> p_388702_.group(ItemModels.CODEC.listOf().fieldOf("models").forGetter(CompositeModel.Unbaked::models))
                    .apply(p_388702_, CompositeModel.Unbaked::new)
        );

        @Override
        public MapCodec<CompositeModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver p_387836_) {
            for (ItemModel.Unbaked itemmodel$unbaked : this.models) {
                itemmodel$unbaked.resolveDependencies(p_387836_);
            }
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext p_388240_) {
            return new CompositeModel(this.models.stream().map(p_387424_ -> p_387424_.bake(p_388240_)).toList());
        }
    }
}
