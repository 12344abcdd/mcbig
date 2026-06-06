package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.FireworkRocketRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FireworkEntityRenderer extends EntityRenderer<FireworkRocketEntity, FireworkRocketRenderState> {
    private final ItemModelResolver itemModelResolver;

    public FireworkEntityRenderer(EntityRendererProvider.Context p_174114_) {
        super(p_174114_);
        this.itemModelResolver = p_174114_.getItemModelResolver();
    }

    public void render(FireworkRocketRenderState p_362873_, PoseStack p_114659_, MultiBufferSource p_114660_, int p_114661_) {
        p_114659_.pushPose();
        p_114659_.mulPose(this.entityRenderDispatcher.cameraOrientation());
        if (p_362873_.isShotAtAngle) {
            p_114659_.mulPose(Axis.ZP.rotationDegrees(180.0F));
            p_114659_.mulPose(Axis.YP.rotationDegrees(180.0F));
            p_114659_.mulPose(Axis.XP.rotationDegrees(90.0F));
        }

        p_362873_.item.render(p_114659_, p_114660_, p_114661_, OverlayTexture.NO_OVERLAY);
        p_114659_.popPose();
        super.render(p_362873_, p_114659_, p_114660_, p_114661_);
    }

    public FireworkRocketRenderState createRenderState() {
        return new FireworkRocketRenderState();
    }

    public void extractRenderState(FireworkRocketEntity p_363409_, FireworkRocketRenderState p_360980_, float p_365252_) {
        super.extractRenderState(p_363409_, p_360980_, p_365252_);
        p_360980_.isShotAtAngle = p_363409_.isShotAtAngle();
        this.itemModelResolver.updateForNonLiving(p_360980_.item, p_363409_.getItem(), ItemDisplayContext.GROUND, p_363409_);
    }
}
