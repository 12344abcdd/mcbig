package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BedSpecialRenderer implements NoDataSpecialModelRenderer {
    private final BedRenderer bedRenderer;
    private final Material material;

    public BedSpecialRenderer(BedRenderer p_386864_, Material p_388920_) {
        this.bedRenderer = p_386864_;
        this.material = p_388920_;
    }

    @Override
    public void render(ItemDisplayContext p_387275_, PoseStack p_387960_, MultiBufferSource p_386542_, int p_386921_, int p_387639_, boolean p_387936_) {
        this.bedRenderer.renderInHand(p_387960_, p_386542_, p_386921_, p_387639_, this.material);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(ResourceLocation texture) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<BedSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_388012_ -> p_388012_.group(ResourceLocation.CODEC.fieldOf("texture").forGetter(BedSpecialRenderer.Unbaked::texture))
                    .apply(p_388012_, BedSpecialRenderer.Unbaked::new)
        );

        public Unbaked(DyeColor p_386855_) {
            this(Sheets.colorToResourceMaterial(p_386855_));
        }

        @Override
        public MapCodec<BedSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_386741_) {
            return new BedSpecialRenderer(new BedRenderer(p_386741_), Sheets.createBedMaterial(this.texture));
        }
    }
}
