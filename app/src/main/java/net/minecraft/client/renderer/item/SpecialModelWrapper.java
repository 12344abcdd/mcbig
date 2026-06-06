package net.minecraft.client.renderer.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpecialModelWrapper<T> implements ItemModel {
    private final SpecialModelRenderer<T> specialRenderer;
    private final BakedModel baseModel;

    public SpecialModelWrapper(SpecialModelRenderer<T> p_387041_, BakedModel p_387735_) {
        this.specialRenderer = p_387041_;
        this.baseModel = p_387735_;
    }

    @Override
    public void update(
        ItemStackRenderState p_388134_,
        ItemStack p_387781_,
        ItemModelResolver p_387931_,
        ItemDisplayContext p_388057_,
        @Nullable ClientLevel p_388213_,
        @Nullable LivingEntity p_388020_,
        int p_387759_
    ) {
        ItemStackRenderState.LayerRenderState itemstackrenderstate$layerrenderstate = p_388134_.newLayer();
        if (p_387781_.hasFoil()) {
            itemstackrenderstate$layerrenderstate.setFoilType(ItemStackRenderState.FoilType.STANDARD);
        }

        itemstackrenderstate$layerrenderstate.setupSpecialModel(this.specialRenderer, this.specialRenderer.extractArgument(p_387781_), this.baseModel);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(ResourceLocation base, SpecialModelRenderer.Unbaked specialModel) implements ItemModel.Unbaked {
        public static final MapCodec<SpecialModelWrapper.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_386693_ -> p_386693_.group(
                        ResourceLocation.CODEC.fieldOf("base").forGetter(SpecialModelWrapper.Unbaked::base),
                        SpecialModelRenderers.CODEC.fieldOf("model").forGetter(SpecialModelWrapper.Unbaked::specialModel)
                    )
                    .apply(p_386693_, SpecialModelWrapper.Unbaked::new)
        );

        @Override
        public void resolveDependencies(ResolvableModel.Resolver p_387171_) {
            p_387171_.resolve(this.base);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext p_388760_) {
            BakedModel bakedmodel = p_388760_.bake(this.base);
            SpecialModelRenderer<?> specialmodelrenderer = this.specialModel.bake(p_388760_.entityModelSet());
            return (ItemModel)(specialmodelrenderer == null ? p_388760_.missingItemModel() : new SpecialModelWrapper<>(specialmodelrenderer, bakedmodel));
        }

        @Override
        public MapCodec<SpecialModelWrapper.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
