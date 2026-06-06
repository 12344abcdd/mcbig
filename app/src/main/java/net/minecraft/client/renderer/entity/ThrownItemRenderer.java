package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownItemRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T, ThrownItemRenderState> {
    private final ItemModelResolver itemModelResolver;
    private final float scale;
    private final boolean fullBright;

    public ThrownItemRenderer(EntityRendererProvider.Context p_174416_, float p_174417_, boolean p_174418_) {
        super(p_174416_);
        this.itemModelResolver = p_174416_.getItemModelResolver();
        this.scale = p_174417_;
        this.fullBright = p_174418_;
    }

    public ThrownItemRenderer(EntityRendererProvider.Context p_174414_) {
        this(p_174414_, 1.0F, false);
    }

    @Override
    protected int getBlockLightLevel(T p_116092_, BlockPos p_116093_) {
        return this.fullBright ? 15 : super.getBlockLightLevel(p_116092_, p_116093_);
    }

    public void render(ThrownItemRenderState p_363985_, PoseStack p_362764_, MultiBufferSource p_361995_, int p_362009_) {
        p_362764_.pushPose();
        p_362764_.scale(this.scale, this.scale, this.scale);
        p_362764_.mulPose(this.entityRenderDispatcher.cameraOrientation());
        p_363985_.item.render(p_362764_, p_361995_, p_362009_, OverlayTexture.NO_OVERLAY);
        p_362764_.popPose();
        super.render(p_363985_, p_362764_, p_361995_, p_362009_);
    }

    public ThrownItemRenderState createRenderState() {
        return new ThrownItemRenderState();
    }

    public void extractRenderState(T p_364505_, ThrownItemRenderState p_363251_, float p_362608_) {
        super.extractRenderState(p_364505_, p_363251_, p_362608_);
        this.itemModelResolver.updateForNonLiving(p_363251_.item, p_364505_.getItem(), ItemDisplayContext.GROUND, p_364505_);
    }
}
