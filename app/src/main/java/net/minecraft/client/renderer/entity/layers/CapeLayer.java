package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CapeLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
    private final HumanoidModel<PlayerRenderState> model;
    private final EquipmentAssetManager equipmentAssets;

    public CapeLayer(RenderLayerParent<PlayerRenderState, PlayerModel> p_116602_, EntityModelSet p_365418_, EquipmentAssetManager p_387093_) {
        super(p_116602_);
        this.model = new PlayerCapeModel<>(p_365418_.bakeLayer(ModelLayers.PLAYER_CAPE));
        this.equipmentAssets = p_387093_;
    }

    private boolean hasLayer(ItemStack p_372809_, EquipmentClientInfo.LayerType p_388089_) {
        Equippable equippable = p_372809_.get(DataComponents.EQUIPPABLE);
        if (equippable != null && !equippable.assetId().isEmpty()) {
            EquipmentClientInfo equipmentclientinfo = this.equipmentAssets.get(equippable.assetId().get());
            return !equipmentclientinfo.getLayers(p_388089_).isEmpty();
        } else {
            return false;
        }
    }

    public void render(PoseStack p_116615_, MultiBufferSource p_116616_, int p_116617_, PlayerRenderState p_360337_, float p_116619_, float p_116620_) {
        if (!p_360337_.isInvisible && p_360337_.showCape) {
            PlayerSkin playerskin = p_360337_.skin;
            if (playerskin.capeTexture() != null) {
                if (!this.hasLayer(p_360337_.chestEquipment, EquipmentClientInfo.LayerType.WINGS)) {
                    p_116615_.pushPose();
                    if (this.hasLayer(p_360337_.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
                        p_116615_.translate(0.0F, -0.053125F, 0.06875F);
                    }

                    VertexConsumer vertexconsumer = p_116616_.getBuffer(RenderType.entitySolid(playerskin.capeTexture()));
                    this.getParentModel().copyPropertiesTo(this.model);
                    this.model.setupAnim(p_360337_);
                    this.model.renderToBuffer(p_116615_, vertexconsumer, p_116617_, OverlayTexture.NO_OVERLAY);
                    p_116615_.popPose();
                }
            }
        }
    }
}
