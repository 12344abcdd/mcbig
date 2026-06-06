package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkullSpecialRenderer implements SpecialModelRenderer<ResolvableProfile> {
    private final SkullBlock.Type skullType;
    private final SkullModelBase model;
    @Nullable
    private final ResourceLocation textureOverride;
    private final float animation;

    public SkullSpecialRenderer(SkullBlock.Type p_388542_, SkullModelBase p_386930_, @Nullable ResourceLocation p_389591_, float p_390424_) {
        this.skullType = p_388542_;
        this.model = p_386930_;
        this.textureOverride = p_389591_;
        this.animation = p_390424_;
    }

    @Nullable
    public ResolvableProfile extractArgument(ItemStack p_386712_) {
        return p_386712_.get(DataComponents.PROFILE);
    }

    public void render(
        @Nullable ResolvableProfile p_388043_,
        ItemDisplayContext p_387716_,
        PoseStack p_386490_,
        MultiBufferSource p_388080_,
        int p_387930_,
        int p_387838_,
        boolean p_386959_
    ) {
        RenderType rendertype = SkullBlockRenderer.getRenderType(this.skullType, p_388043_, this.textureOverride);
        SkullBlockRenderer.renderSkull(null, 180.0F, this.animation, p_386490_, p_388080_, p_387930_, this.model, rendertype);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(SkullBlock.Type kind, Optional<ResourceLocation> textureOverride, float animation) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<SkullSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_390096_ -> p_390096_.group(
                        SkullBlock.Type.CODEC.fieldOf("kind").forGetter(SkullSpecialRenderer.Unbaked::kind),
                        ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(SkullSpecialRenderer.Unbaked::textureOverride),
                        Codec.FLOAT.optionalFieldOf("animation", Float.valueOf(0.0F)).forGetter(SkullSpecialRenderer.Unbaked::animation)
                    )
                    .apply(p_390096_, SkullSpecialRenderer.Unbaked::new)
        );

        public Unbaked(SkullBlock.Type p_387200_) {
            this(p_387200_, Optional.empty(), 0.0F);
        }

        @Override
        public MapCodec<SkullSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Nullable
        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_388555_) {
            SkullModelBase skullmodelbase = SkullBlockRenderer.createModel(p_388555_, this.kind);
            ResourceLocation resourcelocation = this.textureOverride
                .<ResourceLocation>map(p_389346_ -> p_389346_.withPath(p_389344_ -> "textures/entity/" + p_389344_ + ".png"))
                .orElse(null);
            return skullmodelbase != null ? new SkullSpecialRenderer(this.kind, skullmodelbase, resourcelocation, this.animation) : null;
        }
    }
}
