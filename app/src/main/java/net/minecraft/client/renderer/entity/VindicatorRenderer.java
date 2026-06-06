package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VindicatorRenderer extends IllagerRenderer<Vindicator, IllagerRenderState> {
    private static final ResourceLocation VINDICATOR = ResourceLocation.withDefaultNamespace("textures/entity/illager/vindicator.png");

    public VindicatorRenderer(EntityRendererProvider.Context p_174439_) {
        super(p_174439_, new IllagerModel<>(p_174439_.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        this.addLayer(
            new ItemInHandLayer<IllagerRenderState, IllagerModel<IllagerRenderState>>(this) {
                public void render(
                    PoseStack p_116341_, MultiBufferSource p_116342_, int p_116343_, IllagerRenderState p_362981_, float p_116345_, float p_116346_
                ) {
                    if (p_362981_.isAggressive) {
                        super.render(p_116341_, p_116342_, p_116343_, p_362981_, p_116345_, p_116346_);
                    }
                }
            }
        );
    }

    public ResourceLocation getTextureLocation(IllagerRenderState p_364813_) {
        return VINDICATOR;
    }

    public IllagerRenderState createRenderState() {
        return new IllagerRenderState();
    }
}
