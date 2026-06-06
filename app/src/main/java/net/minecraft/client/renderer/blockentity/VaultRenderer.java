package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultClientData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VaultRenderer implements BlockEntityRenderer<VaultBlockEntity> {
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();
    private final ItemClusterRenderState renderState = new ItemClusterRenderState();

    public VaultRenderer(BlockEntityRendererProvider.Context p_324525_) {
        this.itemModelResolver = p_324525_.getItemModelResolver();
    }

    public void render(VaultBlockEntity p_323921_, float p_324166_, PoseStack p_324316_, MultiBufferSource p_323716_, int p_324311_, int p_324178_) {
        if (VaultBlockEntity.Client.shouldDisplayActiveEffects(p_323921_.getSharedData())) {
            Level level = p_323921_.getLevel();
            if (level != null) {
                ItemStack itemstack = p_323921_.getSharedData().getDisplayItem();
                if (!itemstack.isEmpty()) {
                    this.itemModelResolver.updateForTopItem(this.renderState.item, itemstack, ItemDisplayContext.GROUND, false, level, null, 0);
                    this.renderState.count = ItemClusterRenderState.getRenderedAmount(itemstack.getCount());
                    this.renderState.seed = ItemClusterRenderState.getSeedForItemStack(itemstack);
                    VaultClientData vaultclientdata = p_323921_.getClientData();
                    p_324316_.pushPose();
                    p_324316_.translate(0.5F, 0.4F, 0.5F);
                    p_324316_.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(p_324166_, vaultclientdata.previousSpin(), vaultclientdata.currentSpin())));
                    ItemEntityRenderer.renderMultipleFromCount(p_324316_, p_323716_, p_324311_, this.renderState, this.random);
                    p_324316_.popPose();
                }
            }
        }
    }
}
