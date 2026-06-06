package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WardenRenderer extends MobRenderer<Warden, WardenRenderState, WardenModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden.png");
    private static final ResourceLocation BIOLUMINESCENT_LAYER_TEXTURE = ResourceLocation.withDefaultNamespace(
        "textures/entity/warden/warden_bioluminescent_layer.png"
    );
    private static final ResourceLocation HEART_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_heart.png");
    private static final ResourceLocation PULSATING_SPOTS_TEXTURE_1 = ResourceLocation.withDefaultNamespace(
        "textures/entity/warden/warden_pulsating_spots_1.png"
    );
    private static final ResourceLocation PULSATING_SPOTS_TEXTURE_2 = ResourceLocation.withDefaultNamespace(
        "textures/entity/warden/warden_pulsating_spots_2.png"
    );

    public WardenRenderer(EntityRendererProvider.Context p_234787_) {
        super(p_234787_, new WardenModel(p_234787_.bakeLayer(ModelLayers.WARDEN)), 0.9F);
        this.addLayer(
            new LivingEntityEmissiveLayer<>(
                this,
                BIOLUMINESCENT_LAYER_TEXTURE,
                (p_360647_, p_234810_) -> 1.0F,
                WardenModel::getBioluminescentLayerModelParts,
                RenderType::entityTranslucentEmissive,
                false
            )
        );
        this.addLayer(
            new LivingEntityEmissiveLayer<>(
                this,
                PULSATING_SPOTS_TEXTURE_1,
                (p_360462_, p_234806_) -> Math.max(0.0F, Mth.cos(p_234806_ * 0.045F) * 0.25F),
                WardenModel::getPulsatingSpotsLayerModelParts,
                RenderType::entityTranslucentEmissive,
                false
            )
        );
        this.addLayer(
            new LivingEntityEmissiveLayer<>(
                this,
                PULSATING_SPOTS_TEXTURE_2,
                (p_364045_, p_234802_) -> Math.max(0.0F, Mth.cos(p_234802_ * 0.045F + (float) Math.PI) * 0.25F),
                WardenModel::getPulsatingSpotsLayerModelParts,
                RenderType::entityTranslucentEmissive,
                false
            )
        );
        this.addLayer(
            new LivingEntityEmissiveLayer<>(
                this,
                TEXTURE,
                (p_359288_, p_359289_) -> p_359288_.tendrilAnimation,
                WardenModel::getTendrilsLayerModelParts,
                RenderType::entityTranslucentEmissive,
                false
            )
        );
        this.addLayer(
            new LivingEntityEmissiveLayer<>(
                this,
                HEART_TEXTURE,
                (p_359290_, p_359291_) -> p_359290_.heartAnimation,
                WardenModel::getHeartLayerModelParts,
                RenderType::entityTranslucentEmissive,
                false
            )
        );
    }

    public ResourceLocation getTextureLocation(WardenRenderState p_362254_) {
        return TEXTURE;
    }

    public WardenRenderState createRenderState() {
        return new WardenRenderState();
    }

    public void extractRenderState(Warden p_362437_, WardenRenderState p_362253_, float p_360809_) {
        super.extractRenderState(p_362437_, p_362253_, p_360809_);
        p_362253_.tendrilAnimation = p_362437_.getTendrilAnimation(p_360809_);
        p_362253_.heartAnimation = p_362437_.getHeartAnimation(p_360809_);
        p_362253_.roarAnimationState.copyFrom(p_362437_.roarAnimationState);
        p_362253_.sniffAnimationState.copyFrom(p_362437_.sniffAnimationState);
        p_362253_.emergeAnimationState.copyFrom(p_362437_.emergeAnimationState);
        p_362253_.diggingAnimationState.copyFrom(p_362437_.diggingAnimationState);
        p_362253_.attackAnimationState.copyFrom(p_362437_.attackAnimationState);
        p_362253_.sonicBoomAnimationState.copyFrom(p_362437_.sonicBoomAnimationState);
    }
}
