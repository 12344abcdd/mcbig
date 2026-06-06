package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DecoratedPotSpecialRenderer implements SpecialModelRenderer<PotDecorations> {
    private final DecoratedPotRenderer decoratedPotRenderer;

    public DecoratedPotSpecialRenderer(DecoratedPotRenderer p_387969_) {
        this.decoratedPotRenderer = p_387969_;
    }

    @Nullable
    public PotDecorations extractArgument(ItemStack p_386678_) {
        return p_386678_.get(DataComponents.POT_DECORATIONS);
    }

    public void render(
        @Nullable PotDecorations p_387830_,
        ItemDisplayContext p_388378_,
        PoseStack p_387712_,
        MultiBufferSource p_386487_,
        int p_388845_,
        int p_388255_,
        boolean p_386498_
    ) {
        this.decoratedPotRenderer.renderInHand(p_387712_, p_386487_, p_388845_, p_388255_, Objects.requireNonNullElse(p_387830_, PotDecorations.EMPTY));
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<DecoratedPotSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new DecoratedPotSpecialRenderer.Unbaked());

        @Override
        public MapCodec<DecoratedPotSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_387186_) {
            return new DecoratedPotSpecialRenderer(new DecoratedPotRenderer(p_387186_));
        }
    }
}
