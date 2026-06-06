package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishRenderer extends MobRenderer<TropicalFish, TropicalFishRenderState, EntityModel<TropicalFishRenderState>> {
    private final EntityModel<TropicalFishRenderState> modelA = this.getModel();
    private final EntityModel<TropicalFishRenderState> modelB;
    private static final ResourceLocation MODEL_A_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a.png");
    private static final ResourceLocation MODEL_B_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_b.png");

    public TropicalFishRenderer(EntityRendererProvider.Context p_174428_) {
        super(p_174428_, new TropicalFishModelA(p_174428_.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15F);
        this.modelB = new TropicalFishModelB(p_174428_.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE));
        this.addLayer(new TropicalFishPatternLayer(this, p_174428_.getModelSet()));
    }

    public ResourceLocation getTextureLocation(TropicalFishRenderState p_363704_) {
        return switch (p_363704_.variant.base()) {
            case SMALL -> MODEL_A_TEXTURE;
            case LARGE -> MODEL_B_TEXTURE;
        };
    }

    public TropicalFishRenderState createRenderState() {
        return new TropicalFishRenderState();
    }

    public void extractRenderState(TropicalFish p_364725_, TropicalFishRenderState p_363671_, float p_361595_) {
        super.extractRenderState(p_364725_, p_363671_, p_361595_);
        p_363671_.variant = p_364725_.getVariant();
        p_363671_.baseColor = p_364725_.getBaseColor().getTextureDiffuseColor();
        p_363671_.patternColor = p_364725_.getPatternColor().getTextureDiffuseColor();
    }

    public void render(TropicalFishRenderState p_360880_, PoseStack p_116200_, MultiBufferSource p_116201_, int p_116202_) {
        this.model = switch (p_360880_.variant.base()) {
            case SMALL -> this.modelA;
            case LARGE -> this.modelB;
        };
        super.render(p_360880_, p_116200_, p_116201_, p_116202_);
    }

    protected int getModelTint(TropicalFishRenderState p_364711_) {
        return p_364711_.baseColor;
    }

    protected void setupRotations(TropicalFishRenderState p_365512_, PoseStack p_116227_, float p_116228_, float p_116229_) {
        super.setupRotations(p_365512_, p_116227_, p_116228_, p_116229_);
        float f = 4.3F * Mth.sin(0.6F * p_365512_.ageInTicks);
        p_116227_.mulPose(Axis.YP.rotationDegrees(f));
        if (!p_365512_.isInWater) {
            p_116227_.translate(0.2F, 0.1F, 0.0F);
            p_116227_.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }
    }
}
