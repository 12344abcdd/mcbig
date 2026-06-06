package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConduitSpecialRenderer implements NoDataSpecialModelRenderer {
    private final ModelPart model;

    public ConduitSpecialRenderer(ModelPart p_387236_) {
        this.model = p_387236_;
    }

    @Override
    public void render(ItemDisplayContext p_387714_, PoseStack p_386873_, MultiBufferSource p_388451_, int p_387407_, int p_387355_, boolean p_386645_) {
        VertexConsumer vertexconsumer = ConduitRenderer.SHELL_TEXTURE.buffer(p_388451_, RenderType::entitySolid);
        p_386873_.pushPose();
        p_386873_.translate(0.5F, 0.5F, 0.5F);
        this.model.render(p_386873_, vertexconsumer, p_387407_, p_387355_);
        p_386873_.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<ConduitSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new ConduitSpecialRenderer.Unbaked());

        @Override
        public MapCodec<ConduitSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_387917_) {
            return new ConduitSpecialRenderer(p_387917_.bakeLayer(ModelLayers.CONDUIT_SHELL));
        }
    }
}
