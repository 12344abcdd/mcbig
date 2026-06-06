package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TntRenderer extends EntityRenderer<PrimedTnt, TntRenderState> {
    private final BlockRenderDispatcher blockRenderer;

    public TntRenderer(EntityRendererProvider.Context p_174426_) {
        super(p_174426_);
        this.shadowRadius = 0.5F;
        this.blockRenderer = p_174426_.getBlockRenderDispatcher();
    }

    public void render(TntRenderState p_364695_, PoseStack p_116180_, MultiBufferSource p_116181_, int p_116182_) {
        p_116180_.pushPose();
        p_116180_.translate(0.0F, 0.5F, 0.0F);
        float f = p_364695_.fuseRemainingInTicks;
        if (p_364695_.fuseRemainingInTicks < 10.0F) {
            float f1 = 1.0F - p_364695_.fuseRemainingInTicks / 10.0F;
            f1 = Mth.clamp(f1, 0.0F, 1.0F);
            f1 *= f1;
            f1 *= f1;
            float f2 = 1.0F + f1 * 0.3F;
            p_116180_.scale(f2, f2, f2);
        }

        p_116180_.mulPose(Axis.YP.rotationDegrees(-90.0F));
        p_116180_.translate(-0.5F, -0.5F, 0.5F);
        p_116180_.mulPose(Axis.YP.rotationDegrees(90.0F));
        if (p_364695_.blockState != null) {
            TntMinecartRenderer.renderWhiteSolidBlock(this.blockRenderer, p_364695_.blockState, p_116180_, p_116181_, p_116182_, (int)f / 5 % 2 == 0);
        }

        p_116180_.popPose();
        super.render(p_364695_, p_116180_, p_116181_, p_116182_);
    }

    public TntRenderState createRenderState() {
        return new TntRenderState();
    }

    public void extractRenderState(PrimedTnt p_361380_, TntRenderState p_364625_, float p_360472_) {
        super.extractRenderState(p_361380_, p_364625_, p_360472_);
        p_364625_.fuseRemainingInTicks = (float)p_361380_.getFuse() - p_360472_ + 1.0F;
        p_364625_.blockState = p_361380_.getBlockState();
    }
}
