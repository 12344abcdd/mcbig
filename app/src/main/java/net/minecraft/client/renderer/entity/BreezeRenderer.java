package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.BreezeEyesLayer;
import net.minecraft.client.renderer.entity.layers.BreezeWindLayer;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreezeRenderer extends MobRenderer<Breeze, BreezeRenderState, BreezeModel> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze.png");

    public BreezeRenderer(EntityRendererProvider.Context p_312679_) {
        super(p_312679_, new BreezeModel(p_312679_.bakeLayer(ModelLayers.BREEZE)), 0.5F);
        this.addLayer(new BreezeWindLayer(p_312679_, this));
        this.addLayer(new BreezeEyesLayer(this));
    }

    public void render(BreezeRenderState p_364514_, PoseStack p_316442_, MultiBufferSource p_316679_, int p_316262_) {
        BreezeModel breezemodel = this.getModel();
        enable(breezemodel, breezemodel.head(), breezemodel.rods());
        super.render(p_364514_, p_316442_, p_316679_, p_316262_);
    }

    public ResourceLocation getTextureLocation(BreezeRenderState p_365503_) {
        return TEXTURE_LOCATION;
    }

    public BreezeRenderState createRenderState() {
        return new BreezeRenderState();
    }

    public void extractRenderState(Breeze p_362109_, BreezeRenderState p_361497_, float p_365263_) {
        super.extractRenderState(p_362109_, p_361497_, p_365263_);
        p_361497_.idle.copyFrom(p_362109_.idle);
        p_361497_.shoot.copyFrom(p_362109_.shoot);
        p_361497_.slide.copyFrom(p_362109_.slide);
        p_361497_.slideBack.copyFrom(p_362109_.slideBack);
        p_361497_.inhale.copyFrom(p_362109_.inhale);
        p_361497_.longJump.copyFrom(p_362109_.longJump);
    }

    public static BreezeModel enable(BreezeModel p_316245_, ModelPart... p_316382_) {
        p_316245_.head().visible = false;
        p_316245_.eyes().visible = false;
        p_316245_.rods().visible = false;
        p_316245_.wind().visible = false;

        for (ModelPart modelpart : p_316382_) {
            modelpart.visible = true;
        }

        return p_316245_;
    }
}
