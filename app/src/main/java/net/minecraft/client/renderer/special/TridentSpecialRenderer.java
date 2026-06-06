package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TridentSpecialRenderer implements NoDataSpecialModelRenderer {
    private final TridentModel model;

    public TridentSpecialRenderer(TridentModel p_386794_) {
        this.model = p_386794_;
    }

    @Override
    public void render(ItemDisplayContext p_387547_, PoseStack p_386661_, MultiBufferSource p_386541_, int p_387525_, int p_388595_, boolean p_388045_) {
        p_386661_.pushPose();
        p_386661_.scale(1.0F, -1.0F, -1.0F);
        VertexConsumer vertexconsumer = ItemRenderer.getFoilBuffer(p_386541_, this.model.renderType(TridentModel.TEXTURE), false, p_388045_);
        this.model.renderToBuffer(p_386661_, vertexconsumer, p_387525_, p_388595_);
        p_386661_.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<TridentSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new TridentSpecialRenderer.Unbaked());

        @Override
        public MapCodec<TridentSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_386553_) {
            return new TridentSpecialRenderer(new TridentModel(p_386553_.bakeLayer(ModelLayers.TRIDENT)));
        }
    }
}
