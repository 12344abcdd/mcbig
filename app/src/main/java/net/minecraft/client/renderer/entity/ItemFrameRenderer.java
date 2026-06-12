package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

public class ItemFrameRenderer<T extends ItemFrame> extends EntityRenderer<T, ItemFrameRenderState> {
    public static final int GLOW_FRAME_BRIGHTNESS = 5;
    public static final int BRIGHT_MAP_LIGHT_ADJUSTMENT = 30;
    private final ItemModelResolver itemModelResolver;
    private final MapRenderer mapRenderer;
    private final BlockRenderDispatcher blockRenderer;

    public ItemFrameRenderer(EntityRendererProvider.Context p_174204_) {
        super(p_174204_);
        this.itemModelResolver = p_174204_.getItemModelResolver();
        this.mapRenderer = p_174204_.getMapRenderer();
        this.blockRenderer = p_174204_.getBlockRenderDispatcher();
    }

    protected int getBlockLightLevel(T p_174216_, BlockPos p_174217_) {
        return p_174216_.getType() == EntityType.GLOW_ITEM_FRAME
            ? Math.max(5, super.getBlockLightLevel(p_174216_, p_174217_))
            : super.getBlockLightLevel(p_174216_, p_174217_);
    }

    public void render(ItemFrameRenderState p_364723_, PoseStack p_115079_, MultiBufferSource p_115080_, int p_115081_) {
        super.render(p_364723_, p_115079_, p_115080_, p_115081_);
        p_115079_.pushPose();
        Direction direction = p_364723_.direction;
        Vec3 vec3 = this.getRenderOffset(p_364723_);
        p_115079_.translate(-vec3.x(), -vec3.y(), -vec3.z());
        double d0 = 0.46875;
        p_115079_.translate((double)direction.getStepX() * 0.46875, (double)direction.getStepY() * 0.46875, (double)direction.getStepZ() * 0.46875);
        float f;
        float f1;
        if (direction.getAxis().isHorizontal()) {
            f = 0.0F;
            f1 = 180.0F - direction.toYRot();
        } else {
            f = (float)(-90 * direction.getAxisDirection().getStep());
            f1 = 180.0F;
        }

        p_115079_.mulPose(Axis.XP.rotationDegrees(f));
        p_115079_.mulPose(Axis.YP.rotationDegrees(f1));
        if (!p_364723_.isInvisible) {
            ModelManager modelmanager = this.blockRenderer.getBlockModelShaper().getModelManager();
            ModelResourceLocation modelresourcelocation = getFrameModelResourceLocation(p_364723_);
            p_115079_.pushPose();
            p_115079_.translate(-0.5F, -0.5F, -0.5F);
            this.blockRenderer
                .getModelRenderer()
                .renderModel(
                    p_115079_.last(),
                    p_115080_.getBuffer(RenderType.entitySolidZOffsetForward(TextureAtlas.LOCATION_BLOCKS)),
                    null,
                    modelmanager.getModel(modelresourcelocation),
                    1.0F,
                    1.0F,
                    1.0F,
                    p_115081_,
                    OverlayTexture.NO_OVERLAY
                );
            p_115079_.popPose();
        }

        if (p_364723_.isInvisible) {
            p_115079_.translate(0.0F, 0.0F, 0.5F);
        } else {
            p_115079_.translate(0.0F, 0.0F, 0.4375F);
        }

        if (p_364723_.mapId != null) {
            int j = p_364723_.rotation % 4 * 2;
            p_115079_.mulPose(Axis.ZP.rotationDegrees((float)j * 360.0F / 8.0F));
            p_115079_.mulPose(Axis.ZP.rotationDegrees(180.0F));
            float f2 = 0.0078125F;
            p_115079_.scale(0.0078125F, 0.0078125F, 0.0078125F);
            p_115079_.translate(-64.0F, -64.0F, 0.0F);
            p_115079_.translate(0.0F, 0.0F, -1.0F);
            int i = this.getLightCoords(p_364723_.isGlowFrame, 15728850, p_115081_);
            this.mapRenderer.render(p_364723_.mapRenderState, p_115079_, p_115080_, true, i);
        } else if (!p_364723_.item.isEmpty()) {
            p_115079_.mulPose(Axis.ZP.rotationDegrees((float)p_364723_.rotation * 360.0F / 8.0F));
            int k = this.getLightCoords(p_364723_.isGlowFrame, 15728880, p_115081_);
            p_115079_.scale(0.5F, 0.5F, 0.5F);
            p_364723_.item.render(p_115079_, p_115080_, k, OverlayTexture.NO_OVERLAY);
        }

        p_115079_.popPose();
    }

    private int getLightCoords(boolean p_364675_, int p_174210_, int p_174211_) {
        return p_364675_ ? p_174210_ : p_174211_;
    }

    private static ModelResourceLocation getFrameModelResourceLocation(ItemFrameRenderState p_388245_) {
        if (p_388245_.mapId != null) {
            return p_388245_.isGlowFrame ? BlockStateModelLoader.GLOW_MAP_FRAME_LOCATION : BlockStateModelLoader.MAP_FRAME_LOCATION;
        } else {
            return p_388245_.isGlowFrame ? BlockStateModelLoader.GLOW_FRAME_LOCATION : BlockStateModelLoader.FRAME_LOCATION;
        }
    }

    public Vec3 getRenderOffset(ItemFrameRenderState p_361106_) {
        return new Vec3((double)((float)p_361106_.direction.getStepX() * 0.3F), -0.25, (double)((float)p_361106_.direction.getStepZ() * 0.3F));
    }

    protected boolean shouldShowName(T p_115091_, double p_361664_) {
        return Minecraft.renderNames() && this.entityRenderDispatcher.crosshairPickEntity == p_115091_ && p_115091_.getItem().getCustomName() != null;
    }

    protected Component getNameTag(T p_365048_) {
        return p_365048_.getItem().getHoverName();
    }

    public ItemFrameRenderState createRenderState() {
        return new ItemFrameRenderState();
    }

    public void extractRenderState(T p_363125_, ItemFrameRenderState p_362907_, float p_364471_) {
        super.extractRenderState(p_363125_, p_362907_, p_364471_);
        p_362907_.direction = p_363125_.getDirection();
        ItemStack itemstack = p_363125_.getItem();
        this.itemModelResolver.updateForNonLiving(p_362907_.item, itemstack, ItemDisplayContext.FIXED, p_363125_);
        p_362907_.rotation = p_363125_.getRotation();
        p_362907_.isGlowFrame = p_363125_.getType() == EntityType.GLOW_ITEM_FRAME;
        p_362907_.mapId = null;
        if (!itemstack.isEmpty()) {
            MapId mapid = p_363125_.getFramedMapId(itemstack);
            if (mapid != null) {
                MapItemSavedData mapitemsaveddata = p_363125_.level().getMapData(mapid);
                if (mapitemsaveddata != null) {
                    this.mapRenderer.extractRenderState(mapid, mapitemsaveddata, p_362907_.mapRenderState);
                    p_362907_.mapId = mapid;
                }
            }
        }
    }
}
