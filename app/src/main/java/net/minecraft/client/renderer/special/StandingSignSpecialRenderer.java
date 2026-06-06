package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StandingSignSpecialRenderer implements NoDataSpecialModelRenderer {
    private final Model model;
    private final Material material;

    public StandingSignSpecialRenderer(Model p_389670_, Material p_389667_) {
        this.model = p_389670_;
        this.material = p_389667_;
    }

    @Override
    public void render(ItemDisplayContext p_389518_, PoseStack p_389707_, MultiBufferSource p_389673_, int p_389471_, int p_389604_, boolean p_389437_) {
        SignRenderer.renderInHand(p_389707_, p_389673_, p_389471_, p_389604_, this.model, this.material);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(WoodType woodType, Optional<ResourceLocation> texture) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<StandingSignSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_389433_ -> p_389433_.group(
                        WoodType.CODEC.fieldOf("wood_type").forGetter(StandingSignSpecialRenderer.Unbaked::woodType),
                        ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(StandingSignSpecialRenderer.Unbaked::texture)
                    )
                    .apply(p_389433_, StandingSignSpecialRenderer.Unbaked::new)
        );

        public Unbaked(WoodType p_389712_) {
            this(p_389712_, Optional.empty());
        }

        @Override
        public MapCodec<StandingSignSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_389399_) {
            Model model = SignRenderer.createSignModel(p_389399_, this.woodType, true);
            Material material = this.texture.<Material>map(Sheets::createSignMaterial).orElseGet(() -> Sheets.getSignMaterial(this.woodType));
            return new StandingSignSpecialRenderer(model, material);
        }
    }
}
