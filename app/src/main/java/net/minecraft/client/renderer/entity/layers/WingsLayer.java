package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WingsLayer<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    private final ElytraModel elytraModel;
    private final ElytraModel elytraBabyModel;
    private final EquipmentLayerRenderer equipmentRenderer;

    public WingsLayer(RenderLayerParent<S, M> p_371607_, EntityModelSet p_371817_, EquipmentLayerRenderer p_371554_) {
        super(p_371607_);
        this.elytraModel = new ElytraModel(p_371817_.bakeLayer(ModelLayers.ELYTRA));
        this.elytraBabyModel = new ElytraModel(p_371817_.bakeLayer(ModelLayers.ELYTRA_BABY));
        this.equipmentRenderer = p_371554_;
    }

    public void render(PoseStack p_371573_, MultiBufferSource p_371529_, int p_371828_, S p_371273_, float p_371865_, float p_371528_) {
        ItemStack itemstack = p_371273_.chestEquipment;
        Equippable equippable = itemstack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && !equippable.assetId().isEmpty()) {
            ResourceLocation resourcelocation = getPlayerElytraTexture(p_371273_);
            ElytraModel elytramodel = p_371273_.isBaby ? this.elytraBabyModel : this.elytraModel;
            p_371573_.pushPose();
            p_371573_.translate(0.0F, 0.0F, 0.125F);
            elytramodel.setupAnim(p_371273_);
            this.equipmentRenderer
                .renderLayers(
                    EquipmentClientInfo.LayerType.WINGS, equippable.assetId().get(), elytramodel, itemstack, p_371573_, p_371529_, p_371828_, resourcelocation
                );
            p_371573_.popPose();
        }
    }

    @Nullable
    private static ResourceLocation getPlayerElytraTexture(HumanoidRenderState p_371419_) {
        if (p_371419_ instanceof PlayerRenderState playerrenderstate) {
            PlayerSkin playerskin = playerrenderstate.skin;
            if (playerskin.elytraTexture() != null) {
                return playerskin.elytraTexture();
            }

            if (playerskin.capeTexture() != null && playerrenderstate.showCape) {
                return playerskin.capeTexture();
            }
        }

        return null;
    }
}
