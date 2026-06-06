package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfArmorLayer extends RenderLayer<WolfRenderState, WolfModel> {
    private final WolfModel adultModel;
    private final WolfModel babyModel;
    private final EquipmentLayerRenderer equipmentRenderer;
    private static final Map<Crackiness.Level, ResourceLocation> ARMOR_CRACK_LOCATIONS = Map.of(
        Crackiness.Level.LOW,
        ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_low.png"),
        Crackiness.Level.MEDIUM,
        ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_medium.png"),
        Crackiness.Level.HIGH,
        ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_high.png")
    );

    public WolfArmorLayer(RenderLayerParent<WolfRenderState, WolfModel> p_316639_, EntityModelSet p_316756_, EquipmentLayerRenderer p_371602_) {
        super(p_316639_);
        this.adultModel = new WolfModel(p_316756_.bakeLayer(ModelLayers.WOLF_ARMOR));
        this.babyModel = new WolfModel(p_316756_.bakeLayer(ModelLayers.WOLF_BABY_ARMOR));
        this.equipmentRenderer = p_371602_;
    }

    public void render(PoseStack p_316890_, MultiBufferSource p_316537_, int p_316674_, WolfRenderState p_360943_, float p_316775_, float p_316264_) {
        ItemStack itemstack = p_360943_.bodyArmorItem;
        Equippable equippable = itemstack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && !equippable.assetId().isEmpty()) {
            WolfModel wolfmodel = p_360943_.isBaby ? this.babyModel : this.adultModel;
            wolfmodel.setupAnim(p_360943_);
            this.equipmentRenderer
                .renderLayers(EquipmentClientInfo.LayerType.WOLF_BODY, equippable.assetId().get(), wolfmodel, itemstack, p_316890_, p_316537_, p_316674_);
            this.maybeRenderCracks(p_316890_, p_316537_, p_316674_, itemstack, wolfmodel);
        }
    }

    private void maybeRenderCracks(PoseStack p_331222_, MultiBufferSource p_331637_, int p_330931_, ItemStack p_331187_, Model p_364428_) {
        Crackiness.Level crackiness$level = Crackiness.WOLF_ARMOR.byDamage(p_331187_);
        if (crackiness$level != Crackiness.Level.NONE) {
            ResourceLocation resourcelocation = ARMOR_CRACK_LOCATIONS.get(crackiness$level);
            VertexConsumer vertexconsumer = p_331637_.getBuffer(RenderType.armorTranslucent(resourcelocation));
            p_364428_.renderToBuffer(p_331222_, vertexconsumer, p_330931_, OverlayTexture.NO_OVERLAY);
        }
    }
}
