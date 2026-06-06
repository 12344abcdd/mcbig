package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface NoDataSpecialModelRenderer extends SpecialModelRenderer<Void> {
    @Nullable
    default Void extractArgument(ItemStack p_386451_) {
        return null;
    }

    default void render(
        @Nullable Void p_388741_,
        ItemDisplayContext p_388330_,
        PoseStack p_386566_,
        MultiBufferSource p_387064_,
        int p_386544_,
        int p_388169_,
        boolean p_387813_
    ) {
        this.render(p_388330_, p_386566_, p_387064_, p_386544_, p_388169_, p_387813_);
    }

    void render(ItemDisplayContext p_388455_, PoseStack p_387697_, MultiBufferSource p_386694_, int p_388874_, int p_388252_, boolean p_387131_);
}
