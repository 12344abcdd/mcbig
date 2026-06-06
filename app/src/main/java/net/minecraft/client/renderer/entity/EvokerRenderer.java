package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvokerRenderer<T extends SpellcasterIllager> extends IllagerRenderer<T, EvokerRenderState> {
    private static final ResourceLocation EVOKER_ILLAGER = ResourceLocation.withDefaultNamespace("textures/entity/illager/evoker.png");

    public EvokerRenderer(EntityRendererProvider.Context p_174108_) {
        super(p_174108_, new IllagerModel<>(p_174108_.bakeLayer(ModelLayers.EVOKER)), 0.5F);
        this.addLayer(new ItemInHandLayer<EvokerRenderState, IllagerModel<EvokerRenderState>>(this) {
            public void render(PoseStack p_114547_, MultiBufferSource p_114548_, int p_114549_, EvokerRenderState p_363943_, float p_114551_, float p_114552_) {
                if (p_363943_.isCastingSpell) {
                    super.render(p_114547_, p_114548_, p_114549_, p_363943_, p_114551_, p_114552_);
                }
            }
        });
    }

    public ResourceLocation getTextureLocation(EvokerRenderState p_361448_) {
        return EVOKER_ILLAGER;
    }

    public EvokerRenderState createRenderState() {
        return new EvokerRenderState();
    }

    public void extractRenderState(T p_362708_, EvokerRenderState p_364639_, float p_364860_) {
        super.extractRenderState(p_362708_, p_364639_, p_364860_);
        p_364639_.isCastingSpell = p_362708_.isCastingSpell();
    }
}
