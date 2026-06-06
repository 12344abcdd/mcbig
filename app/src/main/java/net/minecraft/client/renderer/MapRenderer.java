package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class MapRenderer {
    private static final float MAP_Z_OFFSET = -0.01F;
    private static final float DECORATION_Z_OFFSET = -0.001F;
    private static final int WIDTH = 128;
    private static final int HEIGHT = 128;
    private final MapTextureManager mapTextureManager;
    private final MapDecorationTextureManager decorationTextures;

    public MapRenderer(MapDecorationTextureManager p_364983_, MapTextureManager p_360823_) {
        this.decorationTextures = p_364983_;
        this.mapTextureManager = p_360823_;
    }

    public void render(MapRenderState p_360365_, PoseStack p_362483_, MultiBufferSource p_363581_, boolean p_362262_, int p_364872_) {
        Matrix4f matrix4f = p_362483_.last().pose();
        VertexConsumer vertexconsumer = p_363581_.getBuffer(RenderType.text(p_360365_.texture));
        vertexconsumer.addVertex(matrix4f, 0.0F, 128.0F, -0.01F).setColor(-1).setUv(0.0F, 1.0F).setLight(p_364872_);
        vertexconsumer.addVertex(matrix4f, 128.0F, 128.0F, -0.01F).setColor(-1).setUv(1.0F, 1.0F).setLight(p_364872_);
        vertexconsumer.addVertex(matrix4f, 128.0F, 0.0F, -0.01F).setColor(-1).setUv(1.0F, 0.0F).setLight(p_364872_);
        vertexconsumer.addVertex(matrix4f, 0.0F, 0.0F, -0.01F).setColor(-1).setUv(0.0F, 0.0F).setLight(p_364872_);
        int i = 0;

        for (MapRenderState.MapDecorationRenderState maprenderstate$mapdecorationrenderstate : p_360365_.decorations) {
            if (!p_362262_ || maprenderstate$mapdecorationrenderstate.renderOnFrame) {
                p_362483_.pushPose();
                p_362483_.translate(
                    (float)maprenderstate$mapdecorationrenderstate.x / 2.0F + 64.0F, (float)maprenderstate$mapdecorationrenderstate.y / 2.0F + 64.0F, -0.02F
                );
                p_362483_.mulPose(Axis.ZP.rotationDegrees((float)(maprenderstate$mapdecorationrenderstate.rot * 360) / 16.0F));
                p_362483_.scale(4.0F, 4.0F, 3.0F);
                p_362483_.translate(-0.125F, 0.125F, 0.0F);
                Matrix4f matrix4f1 = p_362483_.last().pose();
                TextureAtlasSprite textureatlassprite = maprenderstate$mapdecorationrenderstate.atlasSprite;
                if (textureatlassprite != null) {
                    VertexConsumer vertexconsumer1 = p_363581_.getBuffer(RenderType.text(textureatlassprite.atlasLocation()));
                    vertexconsumer1.addVertex(matrix4f1, -1.0F, 1.0F, (float)i * -0.001F)
                        .setColor(-1)
                        .setUv(textureatlassprite.getU0(), textureatlassprite.getV0())
                        .setLight(p_364872_);
                    vertexconsumer1.addVertex(matrix4f1, 1.0F, 1.0F, (float)i * -0.001F)
                        .setColor(-1)
                        .setUv(textureatlassprite.getU1(), textureatlassprite.getV0())
                        .setLight(p_364872_);
                    vertexconsumer1.addVertex(matrix4f1, 1.0F, -1.0F, (float)i * -0.001F)
                        .setColor(-1)
                        .setUv(textureatlassprite.getU1(), textureatlassprite.getV1())
                        .setLight(p_364872_);
                    vertexconsumer1.addVertex(matrix4f1, -1.0F, -1.0F, (float)i * -0.001F)
                        .setColor(-1)
                        .setUv(textureatlassprite.getU0(), textureatlassprite.getV1())
                        .setLight(p_364872_);
                    p_362483_.popPose();
                }

                if (maprenderstate$mapdecorationrenderstate.name != null) {
                    Font font = Minecraft.getInstance().font;
                    float f = (float)font.width(maprenderstate$mapdecorationrenderstate.name);
                    float f1 = Mth.clamp(25.0F / f, 0.0F, 6.0F / 9.0F);
                    p_362483_.pushPose();
                    p_362483_.translate(
                        (float)maprenderstate$mapdecorationrenderstate.x / 2.0F + 64.0F - f * f1 / 2.0F,
                        (float)maprenderstate$mapdecorationrenderstate.y / 2.0F + 64.0F + 4.0F,
                        -0.025F
                    );
                    p_362483_.scale(f1, f1, 1.0F);
                    p_362483_.translate(0.0F, 0.0F, -0.1F);
                    font.drawInBatch(
                        maprenderstate$mapdecorationrenderstate.name,
                        0.0F,
                        0.0F,
                        -1,
                        false,
                        p_362483_.last().pose(),
                        p_363581_,
                        Font.DisplayMode.NORMAL,
                        Integer.MIN_VALUE,
                        p_364872_,
                        false
                    );
                    p_362483_.popPose();
                }

                i++;
            }
        }
    }

    public void extractRenderState(MapId p_361383_, MapItemSavedData p_363500_, MapRenderState p_364922_) {
        p_364922_.texture = this.mapTextureManager.prepareMapTexture(p_361383_, p_363500_);
        p_364922_.decorations.clear();

        for (MapDecoration mapdecoration : p_363500_.getDecorations()) {
            p_364922_.decorations.add(this.extractDecorationRenderState(mapdecoration));
        }
    }

    private MapRenderState.MapDecorationRenderState extractDecorationRenderState(MapDecoration p_364175_) {
        MapRenderState.MapDecorationRenderState maprenderstate$mapdecorationrenderstate = new MapRenderState.MapDecorationRenderState();
        maprenderstate$mapdecorationrenderstate.atlasSprite = this.decorationTextures.get(p_364175_);
        maprenderstate$mapdecorationrenderstate.x = p_364175_.x();
        maprenderstate$mapdecorationrenderstate.y = p_364175_.y();
        maprenderstate$mapdecorationrenderstate.rot = p_364175_.rot();
        maprenderstate$mapdecorationrenderstate.name = p_364175_.name().orElse(null);
        maprenderstate$mapdecorationrenderstate.renderOnFrame = p_364175_.renderOnFrame();
        return maprenderstate$mapdecorationrenderstate;
    }
}
