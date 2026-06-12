package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public class EquipmentLayerRenderer {
    private static final int NO_LAYER_COLOR = 0;
    private final EquipmentAssetManager equipmentAssets;
    private final Function<EquipmentLayerRenderer.LayerTextureKey, ResourceLocation> layerTextureLookup;
    private final Function<EquipmentLayerRenderer.TrimSpriteKey, TextureAtlasSprite> trimSpriteLookup;

    public EquipmentLayerRenderer(EquipmentAssetManager p_388621_, TextureAtlas p_371221_) {
        this.equipmentAssets = p_388621_;
        this.layerTextureLookup = Util.memoize(p_386235_ -> p_386235_.layer.getTextureLocation(p_386235_.layerType));
        this.trimSpriteLookup = Util.memoize(p_386234_ -> p_371221_.getSprite(p_386234_.textureId()));
    }

    public void renderLayers(
        EquipmentClientInfo.LayerType p_388694_,
        ResourceKey<EquipmentAsset> p_386937_,
        Model p_371498_,
        ItemStack p_371902_,
        PoseStack p_371937_,
        MultiBufferSource p_371457_,
        int p_371495_
    ) {
        this.renderLayers(p_388694_, p_386937_, p_371498_, p_371902_, p_371937_, p_371457_, p_371495_, null);
    }

    public void renderLayers(
        EquipmentClientInfo.LayerType p_387484_,
        ResourceKey<EquipmentAsset> p_387603_,
        Model p_371731_,
        ItemStack p_371670_,
        PoseStack p_371767_,
        MultiBufferSource p_371286_,
        int p_371309_,
        @Nullable ResourceLocation p_371639_
    ) {
        List<EquipmentClientInfo.Layer> list = this.equipmentAssets.get(p_387603_).getLayers(p_387484_);
        if (!list.isEmpty()) {
            int i = p_371670_.is(ItemTags.DYEABLE) ? DyedItemColor.getOrDefault(p_371670_, 0) : 0;
            boolean flag = p_371670_.hasFoil();

            for (EquipmentClientInfo.Layer equipmentclientinfo$layer : list) {
                int j = getColorForLayer(equipmentclientinfo$layer, i);
                if (j != 0) {
                    ResourceLocation resourcelocation = equipmentclientinfo$layer.usePlayerTexture() && p_371639_ != null
                        ? p_371639_
                        : this.layerTextureLookup.apply(new EquipmentLayerRenderer.LayerTextureKey(p_387484_, equipmentclientinfo$layer));
                    VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(p_371286_, RenderType.armorCutoutNoCull(resourcelocation), flag);
                    p_371731_.renderToBuffer(p_371767_, vertexconsumer, p_371309_, OverlayTexture.NO_OVERLAY, j);
                    flag = false;
                }
            }

            ArmorTrim armortrim = p_371670_.get(DataComponents.TRIM);
            if (armortrim != null) {
                TextureAtlasSprite textureatlassprite = this.trimSpriteLookup.apply(new EquipmentLayerRenderer.TrimSpriteKey(armortrim, p_387484_, p_387603_));
                VertexConsumer vertexconsumer1 = textureatlassprite.wrap(p_371286_.getBuffer(Sheets.armorTrimsSheet(armortrim.pattern().value().decal())));
                p_371731_.renderToBuffer(p_371767_, vertexconsumer1, p_371309_, OverlayTexture.NO_OVERLAY);
            }
        }
    }

    private static int getColorForLayer(EquipmentClientInfo.Layer p_386482_, int p_371443_) {
        Optional<EquipmentClientInfo.Dyeable> optional = p_386482_.dyeable();
        if (optional.isPresent()) {
            int i = optional.get().colorWhenUndyed().map(ARGB::opaque).orElse(0);
            return p_371443_ != 0 ? p_371443_ : i;
        } else {
            return -1;
        }
    }

    static record LayerTextureKey(EquipmentClientInfo.LayerType layerType, EquipmentClientInfo.Layer layer) {
    }

    static record TrimSpriteKey(ArmorTrim trim, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> equipmentAssetId) {
        private static String getColorPaletteSuffix(Holder<TrimMaterial> p_387117_, ResourceKey<EquipmentAsset> p_386860_) {
            String s = p_387117_.value().overrideArmorAssets().get(p_386860_);
            return s != null ? s : p_387117_.value().assetName();
        }

        public ResourceLocation textureId() {
            ResourceLocation resourcelocation = this.trim.pattern().value().assetId();
            String s = getColorPaletteSuffix(this.trim.material(), this.equipmentAssetId);
            return resourcelocation.withPath(p_387008_ -> "trims/entity/" + this.layerType.getSerializedName() + "/" + p_387008_ + "_" + s);
        }
    }
}
