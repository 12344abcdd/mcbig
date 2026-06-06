package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MagmaCube;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MagmaCubeRenderer extends MobRenderer<MagmaCube, SlimeRenderState, LavaSlimeModel> {
    private static final ResourceLocation MAGMACUBE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/slime/magmacube.png");

    public MagmaCubeRenderer(EntityRendererProvider.Context p_174298_) {
        super(p_174298_, new LavaSlimeModel(p_174298_.bakeLayer(ModelLayers.MAGMA_CUBE)), 0.25F);
    }

    protected int getBlockLightLevel(MagmaCube p_115399_, BlockPos p_115400_) {
        return 15;
    }

    public ResourceLocation getTextureLocation(SlimeRenderState p_361835_) {
        return MAGMACUBE_LOCATION;
    }

    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    public void extractRenderState(MagmaCube p_362519_, SlimeRenderState p_361851_, float p_361242_) {
        super.extractRenderState(p_362519_, p_361851_, p_361242_);
        p_361851_.squish = Mth.lerp(p_361242_, p_362519_.oSquish, p_362519_.squish);
        p_361851_.size = p_362519_.getSize();
    }

    protected float getShadowRadius(SlimeRenderState p_382806_) {
        return (float)p_382806_.size * 0.25F;
    }

    protected void scale(SlimeRenderState p_362807_, PoseStack p_115390_) {
        int i = p_362807_.size;
        float f = p_362807_.squish / ((float)i * 0.5F + 1.0F);
        float f1 = 1.0F / (f + 1.0F);
        p_115390_.scale(f1 * (float)i, 1.0F / f1 * (float)i, f1 * (float)i);
    }
}
