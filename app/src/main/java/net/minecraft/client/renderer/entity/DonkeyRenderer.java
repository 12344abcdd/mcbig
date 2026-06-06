package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.DonkeyModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.state.DonkeyRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DonkeyRenderer<T extends AbstractChestedHorse> extends AbstractHorseRenderer<T, DonkeyRenderState, DonkeyModel> {
    public static final ResourceLocation DONKEY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/horse/donkey.png");
    public static final ResourceLocation MULE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/horse/mule.png");
    private final ResourceLocation texture;

    public DonkeyRenderer(EntityRendererProvider.Context p_363633_, ModelLayerLocation p_363360_, ModelLayerLocation p_361901_, boolean p_360812_) {
        super(p_363633_, new DonkeyModel(p_363633_.bakeLayer(p_363360_)), new DonkeyModel(p_363633_.bakeLayer(p_361901_)));
        this.texture = p_360812_ ? MULE_TEXTURE : DONKEY_TEXTURE;
    }

    public ResourceLocation getTextureLocation(DonkeyRenderState p_363990_) {
        return this.texture;
    }

    public DonkeyRenderState createRenderState() {
        return new DonkeyRenderState();
    }

    public void extractRenderState(T p_365085_, DonkeyRenderState p_363620_, float p_363304_) {
        super.extractRenderState(p_365085_, p_363620_, p_363304_);
        p_363620_.hasChest = p_365085_.hasChest();
    }
}
