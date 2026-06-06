package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public abstract class Model {
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();
    protected final ModelPart root;
    protected final Function<ResourceLocation, RenderType> renderType;
    private final List<ModelPart> allParts;

    public Model(ModelPart p_368583_, Function<ResourceLocation, RenderType> p_103110_) {
        this.root = p_368583_;
        this.renderType = p_103110_;
        this.allParts = p_368583_.getAllParts().toList();
    }

    public final RenderType renderType(ResourceLocation p_103120_) {
        return this.renderType.apply(p_103120_);
    }

    public final void renderToBuffer(PoseStack p_103111_, VertexConsumer p_103112_, int p_103113_, int p_103114_, int p_350308_) {
        this.root().render(p_103111_, p_103112_, p_103113_, p_103114_, p_350308_);
    }

    public final void renderToBuffer(PoseStack p_350804_, VertexConsumer p_350976_, int p_350539_, int p_350374_) {
        this.renderToBuffer(p_350804_, p_350976_, p_350539_, p_350374_, -1);
    }

    public final ModelPart root() {
        return this.root;
    }

    public Optional<ModelPart> getAnyDescendantWithName(String p_365051_) {
        return p_365051_.equals("root")
            ? Optional.of(this.root())
            : this.root().getAllParts().filter(p_365187_ -> p_365187_.hasChild(p_365051_)).findFirst().map(p_363155_ -> p_363155_.getChild(p_365051_));
    }

    public final List<ModelPart> allParts() {
        return this.allParts;
    }

    public final void resetPose() {
        for (ModelPart modelpart : this.allParts) {
            modelpart.resetPose();
        }
    }

    protected void animate(AnimationState p_364820_, AnimationDefinition p_361968_, float p_362503_) {
        this.animate(p_364820_, p_361968_, p_362503_, 1.0F);
    }

    protected void animateWalk(AnimationDefinition p_362453_, float p_365353_, float p_364840_, float p_362983_, float p_361956_) {
        long i = (long)(p_365353_ * 50.0F * p_362983_);
        float f = Math.min(p_364840_ * p_361956_, 1.0F);
        KeyframeAnimations.animate(this, p_362453_, i, f, ANIMATION_VECTOR_CACHE);
    }

    protected void animate(AnimationState p_364413_, AnimationDefinition p_361459_, float p_361947_, float p_362164_) {
        p_364413_.ifStarted(
            p_361743_ -> KeyframeAnimations.animate(
                    this, p_361459_, (long)((float)p_361743_.getTimeInMillis(p_361947_) * p_362164_), 1.0F, ANIMATION_VECTOR_CACHE
                )
        );
    }

    protected void applyStatic(AnimationDefinition p_362055_) {
        KeyframeAnimations.animate(this, p_362055_, 0L, 1.0F, ANIMATION_VECTOR_CACHE);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Simple extends Model {
        public Simple(ModelPart p_364140_, Function<ResourceLocation, RenderType> p_364753_) {
            super(p_364140_, p_364753_);
        }
    }
}
