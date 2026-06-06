package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LivingEntityEmissiveLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    private final ResourceLocation texture;
    private final LivingEntityEmissiveLayer.AlphaFunction<S> alphaFunction;
    private final LivingEntityEmissiveLayer.DrawSelector<S, M> drawSelector;
    private final Function<ResourceLocation, RenderType> bufferProvider;
    private final boolean alwaysVisible;

    public LivingEntityEmissiveLayer(
        RenderLayerParent<S, M> p_379642_,
        ResourceLocation p_379818_,
        LivingEntityEmissiveLayer.AlphaFunction<S> p_380124_,
        LivingEntityEmissiveLayer.DrawSelector<S, M> p_379846_,
        Function<ResourceLocation, RenderType> p_380046_,
        boolean p_388482_
    ) {
        super(p_379642_);
        this.texture = p_379818_;
        this.alphaFunction = p_380124_;
        this.drawSelector = p_379846_;
        this.bufferProvider = p_380046_;
        this.alwaysVisible = p_388482_;
    }

    public void render(PoseStack p_380055_, MultiBufferSource p_379364_, int p_380349_, S p_379499_, float p_379823_, float p_380346_) {
        if (!p_379499_.isInvisible || this.alwaysVisible) {
            if (this.onlyDrawSelectedParts(p_379499_)) {
                VertexConsumer vertexconsumer = p_379364_.getBuffer(this.bufferProvider.apply(this.texture));
                float f = this.alphaFunction.apply(p_379499_, p_379499_.ageInTicks);
                int i = ARGB.color(Mth.floor(f * 255.0F), 255, 255, 255);
                this.getParentModel().renderToBuffer(p_380055_, vertexconsumer, p_380349_, LivingEntityRenderer.getOverlayCoords(p_379499_, 0.0F), i);
                this.resetDrawForAllParts();
            }
        }
    }

    private boolean onlyDrawSelectedParts(S p_379774_) {
        List<ModelPart> list = this.drawSelector.getPartsToDraw(this.getParentModel(), p_379774_);
        if (list.isEmpty()) {
            return false;
        } else {
            this.getParentModel().allParts().forEach(p_379465_ -> p_379465_.skipDraw = true);
            list.forEach(p_379767_ -> p_379767_.skipDraw = false);
            return true;
        }
    }

    private void resetDrawForAllParts() {
        this.getParentModel().allParts().forEach(p_379339_ -> p_379339_.skipDraw = false);
    }

    @OnlyIn(Dist.CLIENT)
    public interface AlphaFunction<S extends LivingEntityRenderState> {
        float apply(S p_380400_, float p_379324_);
    }

    @OnlyIn(Dist.CLIENT)
    public interface DrawSelector<S extends LivingEntityRenderState, M extends EntityModel<S>> {
        List<ModelPart> getPartsToDraw(M p_380233_, S p_379592_);
    }
}
