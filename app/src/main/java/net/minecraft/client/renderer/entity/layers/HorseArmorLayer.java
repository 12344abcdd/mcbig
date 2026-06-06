package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseArmorLayer extends RenderLayer<HorseRenderState, HorseModel> {
    private final HorseModel adultModel;
    private final HorseModel babyModel;
    private final EquipmentLayerRenderer equipmentRenderer;

    public HorseArmorLayer(RenderLayerParent<HorseRenderState, HorseModel> p_174496_, EntityModelSet p_174497_, EquipmentLayerRenderer p_371428_) {
        super(p_174496_);
        this.equipmentRenderer = p_371428_;
        this.adultModel = new HorseModel(p_174497_.bakeLayer(ModelLayers.HORSE_ARMOR));
        this.babyModel = new HorseModel(p_174497_.bakeLayer(ModelLayers.HORSE_BABY_ARMOR));
    }

    public void render(PoseStack p_117032_, MultiBufferSource p_117033_, int p_117034_, HorseRenderState p_364191_, float p_117036_, float p_117037_) {
        ItemStack itemstack = p_364191_.bodyArmorItem;
        Equippable equippable = itemstack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && !equippable.assetId().isEmpty()) {
            HorseModel horsemodel = p_364191_.isBaby ? this.babyModel : this.adultModel;
            horsemodel.setupAnim(p_364191_);
            this.equipmentRenderer
                .renderLayers(EquipmentClientInfo.LayerType.HORSE_BODY, equippable.assetId().get(), horsemodel, itemstack, p_117032_, p_117033_, p_117034_);
        }
    }
}
