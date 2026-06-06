package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends RenderLayer<S, M> {
    private final A innerModel;
    private final A outerModel;
    private final A innerModelBaby;
    private final A outerModelBaby;
    private final EquipmentLayerRenderer equipmentRenderer;

    public HumanoidArmorLayer(RenderLayerParent<S, M> p_267286_, A p_267110_, A p_267150_, EquipmentLayerRenderer p_371362_) {
        this(p_267286_, p_267110_, p_267150_, p_267110_, p_267150_, p_371362_);
    }

    public HumanoidArmorLayer(RenderLayerParent<S, M> p_360748_, A p_361913_, A p_362555_, A p_362321_, A p_362768_, EquipmentLayerRenderer p_371733_) {
        super(p_360748_);
        this.innerModel = p_361913_;
        this.outerModel = p_362555_;
        this.innerModelBaby = p_362321_;
        this.outerModelBaby = p_362768_;
        this.equipmentRenderer = p_371733_;
    }

    public static boolean shouldRender(ItemStack p_371911_, EquipmentSlot p_371669_) {
        Equippable equippable = p_371911_.get(DataComponents.EQUIPPABLE);
        return equippable != null && shouldRender(equippable, p_371669_);
    }

    private static boolean shouldRender(Equippable p_371295_, EquipmentSlot p_371795_) {
        return p_371295_.assetId().isPresent() && p_371295_.slot() == p_371795_;
    }

    public void render(PoseStack p_117096_, MultiBufferSource p_117097_, int p_117098_, S p_363290_, float p_117100_, float p_117101_) {
        this.renderArmorPiece(
            p_117096_, p_117097_, p_363290_.chestEquipment, EquipmentSlot.CHEST, p_117098_, this.getArmorModel(p_363290_, EquipmentSlot.CHEST)
        );
        this.renderArmorPiece(p_117096_, p_117097_, p_363290_.legsEquipment, EquipmentSlot.LEGS, p_117098_, this.getArmorModel(p_363290_, EquipmentSlot.LEGS));
        this.renderArmorPiece(p_117096_, p_117097_, p_363290_.feetEquipment, EquipmentSlot.FEET, p_117098_, this.getArmorModel(p_363290_, EquipmentSlot.FEET));
        this.renderArmorPiece(p_117096_, p_117097_, p_363290_.headEquipment, EquipmentSlot.HEAD, p_117098_, this.getArmorModel(p_363290_, EquipmentSlot.HEAD));
    }

    private void renderArmorPiece(PoseStack p_117119_, MultiBufferSource p_117120_, ItemStack p_362532_, EquipmentSlot p_117122_, int p_117123_, A p_117124_) {
        Equippable equippable = p_362532_.get(DataComponents.EQUIPPABLE);
        if (equippable != null && shouldRender(equippable, p_117122_)) {
            this.getParentModel().copyPropertiesTo(p_117124_);
            this.setPartVisibility(p_117124_, p_117122_);
            EquipmentClientInfo.LayerType equipmentclientinfo$layertype = this.usesInnerModel(p_117122_)
                ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
                : EquipmentClientInfo.LayerType.HUMANOID;
            this.equipmentRenderer
                .renderLayers(equipmentclientinfo$layertype, equippable.assetId().orElseThrow(), p_117124_, p_362532_, p_117119_, p_117120_, p_117123_);
        }
    }

    protected void setPartVisibility(A p_117126_, EquipmentSlot p_117127_) {
        p_117126_.setAllVisible(false);
        switch (p_117127_) {
            case HEAD:
                p_117126_.head.visible = true;
                p_117126_.hat.visible = true;
                break;
            case CHEST:
                p_117126_.body.visible = true;
                p_117126_.rightArm.visible = true;
                p_117126_.leftArm.visible = true;
                break;
            case LEGS:
                p_117126_.body.visible = true;
                p_117126_.rightLeg.visible = true;
                p_117126_.leftLeg.visible = true;
                break;
            case FEET:
                p_117126_.rightLeg.visible = true;
                p_117126_.leftLeg.visible = true;
        }
    }

    private A getArmorModel(S p_362738_, EquipmentSlot p_117079_) {
        if (this.usesInnerModel(p_117079_)) {
            return p_362738_.isBaby ? this.innerModelBaby : this.innerModel;
        } else {
            return p_362738_.isBaby ? this.outerModelBaby : this.outerModel;
        }
    }

    private boolean usesInnerModel(EquipmentSlot p_117129_) {
        return p_117129_ == EquipmentSlot.LEGS;
    }
}
