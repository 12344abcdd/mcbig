package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Markings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseMarkingLayer extends RenderLayer<HorseRenderState, HorseModel> {
    private static final Map<Markings, ResourceLocation> LOCATION_BY_MARKINGS = Util.make(Maps.newEnumMap(Markings.class), p_349908_ -> {
        p_349908_.put(Markings.NONE, null);
        p_349908_.put(Markings.WHITE, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_white.png"));
        p_349908_.put(Markings.WHITE_FIELD, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_whitefield.png"));
        p_349908_.put(Markings.WHITE_DOTS, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_whitedots.png"));
        p_349908_.put(Markings.BLACK_DOTS, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_blackdots.png"));
    });

    public HorseMarkingLayer(RenderLayerParent<HorseRenderState, HorseModel> p_117045_) {
        super(p_117045_);
    }

    public void render(PoseStack p_117047_, MultiBufferSource p_117048_, int p_117049_, HorseRenderState p_365122_, float p_117051_, float p_117052_) {
        ResourceLocation resourcelocation = LOCATION_BY_MARKINGS.get(p_365122_.markings);
        if (resourcelocation != null && !p_365122_.isInvisible) {
            VertexConsumer vertexconsumer = p_117048_.getBuffer(RenderType.entityTranslucent(resourcelocation));
            this.getParentModel().renderToBuffer(p_117047_, vertexconsumer, p_117049_, LivingEntityRenderer.getOverlayCoords(p_365122_, 0.0F));
        }
    }
}
