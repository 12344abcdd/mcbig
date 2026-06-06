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
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HangingSignSpecialRenderer implements NoDataSpecialModelRenderer {
    private final Model model;
    private final Material material;

    public HangingSignSpecialRenderer(Model p_389506_, Material p_389504_) {
        this.model = p_389506_;
        this.material = p_389504_;
    }

    @Override
    public void render(ItemDisplayContext p_389655_, PoseStack p_389572_, MultiBufferSource p_389478_, int p_389663_, int p_389449_, boolean p_389652_) {
        HangingSignRenderer.renderInHand(p_389572_, p_389478_, p_389663_, p_389449_, this.model, this.material);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(WoodType woodType, Optional<ResourceLocation> texture) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<HangingSignSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_389524_ -> p_389524_.group(
                        WoodType.CODEC.fieldOf("wood_type").forGetter(HangingSignSpecialRenderer.Unbaked::woodType),
                        ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(HangingSignSpecialRenderer.Unbaked::texture)
                    )
                    .apply(p_389524_, HangingSignSpecialRenderer.Unbaked::new)
        );

        public Unbaked(WoodType p_389680_) {
            this(p_389680_, Optional.empty());
        }

        @Override
        public MapCodec<HangingSignSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_389408_) {
            Model model = HangingSignRenderer.createSignModel(p_389408_, this.woodType, HangingSignRenderer.AttachmentType.CEILING_MIDDLE);
            Material material = this.texture.<Material>map(Sheets::createHangingSignMaterial).orElseGet(() -> Sheets.getHangingSignMaterial(this.woodType));
            return new HangingSignSpecialRenderer(model, material);
        }
    }
}
