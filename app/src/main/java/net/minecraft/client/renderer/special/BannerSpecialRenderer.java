package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BannerSpecialRenderer implements SpecialModelRenderer<BannerPatternLayers> {
    private final BannerRenderer bannerRenderer;
    private final DyeColor baseColor;

    public BannerSpecialRenderer(DyeColor p_388643_, BannerRenderer p_386611_) {
        this.bannerRenderer = p_386611_;
        this.baseColor = p_388643_;
    }

    @Nullable
    public BannerPatternLayers extractArgument(ItemStack p_387879_) {
        return p_387879_.get(DataComponents.BANNER_PATTERNS);
    }

    public void render(
        @Nullable BannerPatternLayers p_388526_,
        ItemDisplayContext p_387517_,
        PoseStack p_388611_,
        MultiBufferSource p_388866_,
        int p_387337_,
        int p_386801_,
        boolean p_387745_
    ) {
        this.bannerRenderer
            .renderInHand(p_388611_, p_388866_, p_387337_, p_386801_, this.baseColor, Objects.requireNonNullElse(p_388526_, BannerPatternLayers.EMPTY));
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(DyeColor baseColor) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<BannerSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_386477_ -> p_386477_.group(DyeColor.CODEC.fieldOf("color").forGetter(BannerSpecialRenderer.Unbaked::baseColor))
                    .apply(p_386477_, BannerSpecialRenderer.Unbaked::new)
        );

        @Override
        public MapCodec<BannerSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_388010_) {
            return new BannerSpecialRenderer(this.baseColor, new BannerRenderer(p_388010_));
        }
    }
}
