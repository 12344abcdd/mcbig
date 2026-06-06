package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class HorseRenderer extends AbstractHorseRenderer<Horse, HorseRenderState, HorseModel> {
    private static final Map<Variant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(Variant.class), p_349902_ -> {
        p_349902_.put(Variant.WHITE, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_white.png"));
        p_349902_.put(Variant.CREAMY, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_creamy.png"));
        p_349902_.put(Variant.CHESTNUT, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_chestnut.png"));
        p_349902_.put(Variant.BROWN, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_brown.png"));
        p_349902_.put(Variant.BLACK, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_black.png"));
        p_349902_.put(Variant.GRAY, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_gray.png"));
        p_349902_.put(Variant.DARK_BROWN, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_darkbrown.png"));
    });

    public HorseRenderer(EntityRendererProvider.Context p_174167_) {
        super(p_174167_, new HorseModel(p_174167_.bakeLayer(ModelLayers.HORSE)), new HorseModel(p_174167_.bakeLayer(ModelLayers.HORSE_BABY)));
        this.addLayer(new HorseMarkingLayer(this));
        this.addLayer(new HorseArmorLayer(this, p_174167_.getModelSet(), p_174167_.getEquipmentRenderer()));
    }

    public ResourceLocation getTextureLocation(HorseRenderState p_365094_) {
        return LOCATION_BY_VARIANT.get(p_365094_.variant);
    }

    public HorseRenderState createRenderState() {
        return new HorseRenderState();
    }

    public void extractRenderState(Horse p_362522_, HorseRenderState p_363732_, float p_362557_) {
        super.extractRenderState(p_362522_, p_363732_, p_362557_);
        p_363732_.variant = p_362522_.getVariant();
        p_363732_.markings = p_362522_.getMarkings();
        p_363732_.bodyArmorItem = p_362522_.getBodyArmorItem().copy();
    }
}
