package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShieldSpecialRenderer implements SpecialModelRenderer<DataComponentMap> {
    private final ShieldModel model;

    public ShieldSpecialRenderer(ShieldModel p_386724_) {
        this.model = p_386724_;
    }

    @Nullable
    public DataComponentMap extractArgument(ItemStack p_387204_) {
        return p_387204_.immutableComponents();
    }

    public void render(
        @Nullable DataComponentMap p_386991_,
        ItemDisplayContext p_388730_,
        PoseStack p_387961_,
        MultiBufferSource p_388686_,
        int p_387382_,
        int p_387013_,
        boolean p_387902_
    ) {
        BannerPatternLayers bannerpatternlayers = p_386991_ != null
            ? p_386991_.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
            : BannerPatternLayers.EMPTY;
        DyeColor dyecolor = p_386991_ != null ? p_386991_.get(DataComponents.BASE_COLOR) : null;
        boolean flag = !bannerpatternlayers.layers().isEmpty() || dyecolor != null;
        p_387961_.pushPose();
        p_387961_.scale(1.0F, -1.0F, -1.0F);
        Material material = flag ? ModelBakery.SHIELD_BASE : ModelBakery.NO_PATTERN_SHIELD;
        VertexConsumer vertexconsumer = material.sprite()
            .wrap(ItemRenderer.getFoilBuffer(p_388686_, this.model.renderType(material.atlasLocation()), p_388730_ == ItemDisplayContext.GUI, p_387902_));
        this.model.handle().render(p_387961_, vertexconsumer, p_387382_, p_387013_);
        if (flag) {
            BannerRenderer.renderPatterns(
                p_387961_,
                p_388686_,
                p_387382_,
                p_387013_,
                this.model.plate(),
                material,
                false,
                Objects.requireNonNullElse(dyecolor, DyeColor.WHITE),
                bannerpatternlayers,
                p_387902_,
                false
            );
        } else {
            this.model.plate().render(p_387961_, vertexconsumer, p_387382_, p_387013_);
        }

        p_387961_.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final ShieldSpecialRenderer.Unbaked INSTANCE = new ShieldSpecialRenderer.Unbaked();
        public static final MapCodec<ShieldSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public MapCodec<ShieldSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_387269_) {
            return new ShieldSpecialRenderer(new ShieldModel(p_387269_.bakeLayer(ModelLayers.SHIELD)));
        }
    }
}
