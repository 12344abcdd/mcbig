package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface SpecialModelRenderer<T> {
    void render(
        @Nullable T p_387335_, ItemDisplayContext p_388635_, PoseStack p_387508_, MultiBufferSource p_387513_, int p_386748_, int p_388858_, boolean p_387642_
    );

    @Nullable
    T extractArgument(ItemStack p_387212_);

    @OnlyIn(Dist.CLIENT)
    public interface Unbaked {
        @Nullable
        SpecialModelRenderer<?> bake(EntityModelSet p_388631_);

        MapCodec<? extends SpecialModelRenderer.Unbaked> type();
    }
}
