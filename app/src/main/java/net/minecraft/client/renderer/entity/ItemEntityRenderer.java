package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemEntityRenderer extends EntityRenderer<ItemEntity, ItemEntityRenderState> {
    private static final float ITEM_BUNDLE_OFFSET_SCALE = 0.15F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_X = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Y = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Z = 0.09375F;
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();

    public ItemEntityRenderer(EntityRendererProvider.Context p_174198_) {
        super(p_174198_);
        this.itemModelResolver = p_174198_.getItemModelResolver();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    public ItemEntityRenderState createRenderState() {
        return new ItemEntityRenderState();
    }

    public void extractRenderState(ItemEntity p_362393_, ItemEntityRenderState p_361441_, float p_360849_) {
        super.extractRenderState(p_362393_, p_361441_, p_360849_);
        p_361441_.ageInTicks = (float)p_362393_.getAge() + p_360849_;
        p_361441_.bobOffset = p_362393_.bobOffs;
        p_361441_.extractItemGroupRenderState(p_362393_, p_362393_.getItem(), this.itemModelResolver);
    }

    public void render(ItemEntityRenderState p_362172_, PoseStack p_115030_, MultiBufferSource p_115031_, int p_115032_) {
        if (!p_362172_.item.isEmpty()) {
            p_115030_.pushPose();
            float f = 0.25F;
            float f1 = Mth.sin(p_362172_.ageInTicks / 10.0F + p_362172_.bobOffset) * 0.1F + 0.1F;
            float f2 = p_362172_.item.transform().scale.y();
            p_115030_.translate(0.0F, f1 + 0.25F * f2, 0.0F);
            float f3 = ItemEntity.getSpin(p_362172_.ageInTicks, p_362172_.bobOffset);
            p_115030_.mulPose(Axis.YP.rotation(f3));
            renderMultipleFromCount(p_115030_, p_115031_, p_115032_, p_362172_, this.random);
            p_115030_.popPose();
            super.render(p_362172_, p_115030_, p_115031_, p_115032_);
        }
    }

    public static void renderMultipleFromCount(
        PoseStack p_323763_, MultiBufferSource p_324606_, int p_323603_, ItemClusterRenderState p_388704_, RandomSource p_324507_
    ) {
        p_324507_.setSeed((long)p_388704_.seed);
        int i = p_388704_.count;
        ItemStackRenderState itemstackrenderstate = p_388704_.item;
        boolean flag = itemstackrenderstate.isGui3d();
        float f = itemstackrenderstate.transform().scale.x();
        float f1 = itemstackrenderstate.transform().scale.y();
        float f2 = itemstackrenderstate.transform().scale.z();
        if (!flag) {
            float f3 = -0.0F * (float)(i - 1) * 0.5F * f;
            float f4 = -0.0F * (float)(i - 1) * 0.5F * f1;
            float f5 = -0.09375F * (float)(i - 1) * 0.5F * f2;
            p_323763_.translate(f3, f4, f5);
        }

        for (int j = 0; j < i; j++) {
            p_323763_.pushPose();
            if (j > 0) {
                if (flag) {
                    float f7 = (p_324507_.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f9 = (p_324507_.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f6 = (p_324507_.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    p_323763_.translate(f7, f9, f6);
                } else {
                    float f8 = (p_324507_.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float f10 = (p_324507_.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    p_323763_.translate(f8, f10, 0.0F);
                }
            }

            itemstackrenderstate.render(p_323763_, p_324606_, p_323603_, OverlayTexture.NO_OVERLAY);
            p_323763_.popPose();
            if (!flag) {
                p_323763_.translate(0.0F * f, 0.0F * f1, 0.09375F * f2);
            }
        }
    }
}
