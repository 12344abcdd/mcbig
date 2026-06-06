package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaRenderer extends AgeableMobRenderer<Llama, LlamaRenderState, LlamaModel> {
    private static final ResourceLocation CREAMY = ResourceLocation.withDefaultNamespace("textures/entity/llama/creamy.png");
    private static final ResourceLocation WHITE = ResourceLocation.withDefaultNamespace("textures/entity/llama/white.png");
    private static final ResourceLocation BROWN = ResourceLocation.withDefaultNamespace("textures/entity/llama/brown.png");
    private static final ResourceLocation GRAY = ResourceLocation.withDefaultNamespace("textures/entity/llama/gray.png");

    public LlamaRenderer(EntityRendererProvider.Context p_174293_, ModelLayerLocation p_174294_, ModelLayerLocation p_364000_) {
        super(p_174293_, new LlamaModel(p_174293_.bakeLayer(p_174294_)), new LlamaModel(p_174293_.bakeLayer(p_364000_)), 0.7F);
        this.addLayer(new LlamaDecorLayer(this, p_174293_.getModelSet(), p_174293_.getEquipmentRenderer()));
    }

    public ResourceLocation getTextureLocation(LlamaRenderState p_361005_) {
        return switch (p_361005_.variant) {
            case CREAMY -> CREAMY;
            case WHITE -> WHITE;
            case BROWN -> BROWN;
            case GRAY -> GRAY;
        };
    }

    public LlamaRenderState createRenderState() {
        return new LlamaRenderState();
    }

    public void extractRenderState(Llama p_363790_, LlamaRenderState p_363082_, float p_361575_) {
        super.extractRenderState(p_363790_, p_363082_, p_361575_);
        p_363082_.variant = p_363790_.getVariant();
        p_363082_.hasChest = !p_363790_.isBaby() && p_363790_.hasChest();
        p_363082_.bodyItem = p_363790_.getBodyArmorItem();
        p_363082_.isTraderLlama = p_363790_.isTraderLlama();
    }
}
