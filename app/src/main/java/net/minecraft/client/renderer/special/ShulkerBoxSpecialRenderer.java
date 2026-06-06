package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBoxSpecialRenderer implements NoDataSpecialModelRenderer {
    private final ShulkerBoxRenderer shulkerBoxRenderer;
    private final float openness;
    private final Direction orientation;
    private final Material material;

    public ShulkerBoxSpecialRenderer(ShulkerBoxRenderer p_387519_, float p_387173_, Direction p_388269_, Material p_387242_) {
        this.shulkerBoxRenderer = p_387519_;
        this.openness = p_387173_;
        this.orientation = p_388269_;
        this.material = p_387242_;
    }

    @Override
    public void render(ItemDisplayContext p_387436_, PoseStack p_387209_, MultiBufferSource p_388521_, int p_388851_, int p_387596_, boolean p_388474_) {
        this.shulkerBoxRenderer.render(p_387209_, p_388521_, p_388851_, p_387596_, this.orientation, this.openness, this.material);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(ResourceLocation texture, float openness, Direction orientation) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<ShulkerBoxSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_386593_ -> p_386593_.group(
                        ResourceLocation.CODEC.fieldOf("texture").forGetter(ShulkerBoxSpecialRenderer.Unbaked::texture),
                        Codec.FLOAT.optionalFieldOf("openness", Float.valueOf(0.0F)).forGetter(ShulkerBoxSpecialRenderer.Unbaked::openness),
                        Direction.CODEC.optionalFieldOf("orientation", Direction.UP).forGetter(ShulkerBoxSpecialRenderer.Unbaked::orientation)
                    )
                    .apply(p_386593_, ShulkerBoxSpecialRenderer.Unbaked::new)
        );

        public Unbaked() {
            this(ResourceLocation.withDefaultNamespace("shulker"), 0.0F, Direction.UP);
        }

        public Unbaked(DyeColor p_388305_) {
            this(Sheets.colorToShulkerMaterial(p_388305_), 0.0F, Direction.UP);
        }

        @Override
        public MapCodec<ShulkerBoxSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_388396_) {
            return new ShulkerBoxSpecialRenderer(new ShulkerBoxRenderer(p_388396_), this.openness, this.orientation, Sheets.createShulkerMaterial(this.texture));
        }
    }
}
