package net.minecraft.client.renderer.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BlockModelWrapper implements ItemModel {
    private final BakedModel model;
    private final List<ItemTintSource> tints;

    BlockModelWrapper(BakedModel p_387113_, List<ItemTintSource> p_388185_) {
        this.model = p_387113_;
        this.tints = p_388185_;
    }

    @Override
    public void update(
        ItemStackRenderState p_386488_,
        ItemStack p_386443_,
        ItemModelResolver p_388726_,
        ItemDisplayContext p_388231_,
        @Nullable ClientLevel p_387522_,
        @Nullable LivingEntity p_387263_,
        int p_388300_
    ) {
        ItemStackRenderState.LayerRenderState itemstackrenderstate$layerrenderstate = p_386488_.newLayer();
        if (p_386443_.hasFoil()) {
            itemstackrenderstate$layerrenderstate.setFoilType(
                hasSpecialAnimatedTexture(p_386443_) ? ItemStackRenderState.FoilType.SPECIAL : ItemStackRenderState.FoilType.STANDARD
            );
        }

        int i = this.tints.size();
        int[] aint = itemstackrenderstate$layerrenderstate.prepareTintLayers(i);

        for (int j = 0; j < i; j++) {
            aint[j] = this.tints.get(j).calculate(p_386443_, p_387522_, p_387263_);
        }

        RenderType rendertype = ItemBlockRenderTypes.getRenderType(p_386443_);
        itemstackrenderstate$layerrenderstate.setupBlockModel(this.model, rendertype);
    }

    private static boolean hasSpecialAnimatedTexture(ItemStack p_387217_) {
        return p_387217_.is(ItemTags.COMPASSES) || p_387217_.is(Items.CLOCK);
    }

    public static record Unbaked(ResourceLocation model, List<ItemTintSource> tints) implements ItemModel.Unbaked {
        public static final MapCodec<BlockModelWrapper.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_387684_ -> p_387684_.group(
                        ResourceLocation.CODEC.fieldOf("model").forGetter(BlockModelWrapper.Unbaked::model),
                        ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(BlockModelWrapper.Unbaked::tints)
                    )
                    .apply(p_387684_, BlockModelWrapper.Unbaked::new)
        );

        @Override
        public void resolveDependencies(ResolvableModel.Resolver p_387532_) {
            p_387532_.resolve(this.model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext p_388226_) {
            BakedModel bakedmodel = p_388226_.bake(this.model);
            return new BlockModelWrapper(bakedmodel, this.tints);
        }

        @Override
        public MapCodec<BlockModelWrapper.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
