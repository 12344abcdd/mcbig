package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.WindChargeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WindChargeRenderer extends EntityRenderer<AbstractWindCharge, EntityRenderState> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/wind_charge.png");
    private final WindChargeModel model;

    public WindChargeRenderer(EntityRendererProvider.Context p_312557_) {
        super(p_312557_);
        this.model = new WindChargeModel(p_312557_.bakeLayer(ModelLayers.WIND_CHARGE));
    }

    @Override
    public void render(EntityRenderState p_364131_, PoseStack p_311882_, MultiBufferSource p_312849_, int p_312740_) {
        VertexConsumer vertexconsumer = p_312849_.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(p_364131_.ageInTicks) % 1.0F, 0.0F));
        this.model.setupAnim(p_364131_);
        this.model.renderToBuffer(p_311882_, vertexconsumer, p_312740_, OverlayTexture.NO_OVERLAY);
        super.render(p_364131_, p_311882_, p_312849_, p_312740_);
    }

    protected float xOffset(float p_312655_) {
        return p_312655_ * 0.03F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
