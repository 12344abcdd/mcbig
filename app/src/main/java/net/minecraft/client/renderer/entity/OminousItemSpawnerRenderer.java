package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OminousItemSpawnerRenderer extends EntityRenderer<OminousItemSpawner, ItemClusterRenderState> {
    private static final float ROTATION_SPEED = 40.0F;
    private static final int TICKS_SCALING = 50;
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();

    protected OminousItemSpawnerRenderer(EntityRendererProvider.Context p_338603_) {
        super(p_338603_);
        this.itemModelResolver = p_338603_.getItemModelResolver();
    }

    public ItemClusterRenderState createRenderState() {
        return new ItemClusterRenderState();
    }

    public void extractRenderState(OminousItemSpawner p_361782_, ItemClusterRenderState p_388307_, float p_361351_) {
        super.extractRenderState(p_361782_, p_388307_, p_361351_);
        ItemStack itemstack = p_361782_.getItem();
        p_388307_.extractItemGroupRenderState(p_361782_, itemstack, this.itemModelResolver);
    }

    public void render(ItemClusterRenderState p_386695_, PoseStack p_338440_, MultiBufferSource p_338413_, int p_338541_) {
        if (!p_386695_.item.isEmpty()) {
            p_338440_.pushPose();
            if (p_386695_.ageInTicks <= 50.0F) {
                float f = Math.min(p_386695_.ageInTicks, 50.0F) / 50.0F;
                p_338440_.scale(f, f, f);
            }

            float f1 = Mth.wrapDegrees(p_386695_.ageInTicks * 40.0F);
            p_338440_.mulPose(Axis.YP.rotationDegrees(f1));
            ItemEntityRenderer.renderMultipleFromCount(p_338440_, p_338413_, 15728880, p_386695_, this.random);
            p_338440_.popPose();
        }
    }
}
