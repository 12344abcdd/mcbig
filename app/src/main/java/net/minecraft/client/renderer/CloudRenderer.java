package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.CloudStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class CloudRenderer extends SimplePreparableReloadListener<Optional<CloudRenderer.TextureData>> implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/clouds.png");
    private static final float CELL_SIZE_IN_BLOCKS = 12.0F;
    private static final float HEIGHT_IN_BLOCKS = 4.0F;
    private static final float BLOCKS_PER_SECOND = 0.6F;
    private static final long EMPTY_CELL = 0L;
    private static final int COLOR_OFFSET = 4;
    private static final int NORTH_OFFSET = 3;
    private static final int EAST_OFFSET = 2;
    private static final int SOUTH_OFFSET = 1;
    private static final int WEST_OFFSET = 0;
    private boolean needsRebuild = true;
    private me.alphamode.mcbig.math.BigInteger prevCellX = me.alphamode.mcbig.math.BigInteger.val(Integer.MIN_VALUE);
    private me.alphamode.mcbig.math.BigInteger prevCellZ = me.alphamode.mcbig.math.BigInteger.val(Integer.MIN_VALUE);
    private CloudRenderer.RelativeCameraPos prevRelativeCameraPos = CloudRenderer.RelativeCameraPos.INSIDE_CLOUDS;
    @Nullable
    private CloudStatus prevType;
    @Nullable
    private CloudRenderer.TextureData texture;
    private final VertexBuffer vertexBuffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
    private boolean vertexBufferEmpty;

    protected Optional<CloudRenderer.TextureData> prepare(ResourceManager p_363181_, ProfilerFiller p_361418_) {
        try {
            Optional optional;
            try (
                InputStream inputstream = p_363181_.open(TEXTURE_LOCATION);
                NativeImage nativeimage = NativeImage.read(inputstream);
            ) {
                int i = nativeimage.getWidth();
                int j = nativeimage.getHeight();
                long[] along = new long[i * j];

                for (int k = 0; k < j; k++) {
                    for (int l = 0; l < i; l++) {
                        int i1 = nativeimage.getPixel(l, k);
                        if (isCellEmpty(i1)) {
                            along[l + k * i] = 0L;
                        } else {
                            boolean flag = isCellEmpty(nativeimage.getPixel(l, Math.floorMod(k - 1, j)));
                            boolean flag1 = isCellEmpty(nativeimage.getPixel(Math.floorMod(l + 1, j), k));
                            boolean flag2 = isCellEmpty(nativeimage.getPixel(l, Math.floorMod(k + 1, j)));
                            boolean flag3 = isCellEmpty(nativeimage.getPixel(Math.floorMod(l - 1, j), k));
                            along[l + k * i] = packCellData(i1, flag, flag1, flag2, flag3);
                        }
                    }
                }

                optional = Optional.of(new CloudRenderer.TextureData(along, i, j));
            }

            return optional;
        } catch (IOException ioexception) {
            LOGGER.error("Failed to load cloud texture", (Throwable)ioexception);
            return Optional.empty();
        }
    }

    protected void apply(Optional<CloudRenderer.TextureData> p_362811_, ResourceManager p_364101_, ProfilerFiller p_360749_) {
        this.texture = p_362811_.orElse(null);
        this.needsRebuild = true;
    }

    private static boolean isCellEmpty(int p_363144_) {
        return ARGB.alpha(p_363144_) < 10;
    }

    private static long packCellData(int p_363244_, boolean p_365018_, boolean p_363077_, boolean p_360343_, boolean p_360813_) {
        return (long)p_363244_ << 4
            | (long)((p_365018_ ? 1 : 0) << 3)
            | (long)((p_363077_ ? 1 : 0) << 2)
            | (long)((p_360343_ ? 1 : 0) << 1)
            | (long)((p_360813_ ? 1 : 0) << 0);
    }

    private static int getColor(long p_361465_) {
        return (int)(p_361465_ >> 4 & 4294967295L);
    }

    private static boolean isNorthEmpty(long p_361438_) {
        return (p_361438_ >> 3 & 1L) != 0L;
    }

    private static boolean isEastEmpty(long p_361625_) {
        return (p_361625_ >> 2 & 1L) != 0L;
    }

    private static boolean isSouthEmpty(long p_361797_) {
        return (p_361797_ >> 1 & 1L) != 0L;
    }

    private static boolean isWestEmpty(long p_363963_) {
        return (p_363963_ >> 0 & 1L) != 0L;
    }

    public void render(int p_363907_, CloudStatus p_364293_, float p_363260_, Matrix4f p_363292_, Matrix4f p_364300_, me.alphamode.mcbig.core.BigVec3 p_363573_, float p_360711_) {
        if (this.texture != null) {
            float f = (float)((double)p_363260_ - p_363573_.y);
            float f1 = f + 4.0F;
            CloudRenderer.RelativeCameraPos cloudrenderer$relativecamerapos;
            if (f1 < 0.0F) {
                cloudrenderer$relativecamerapos = CloudRenderer.RelativeCameraPos.ABOVE_CLOUDS;
            } else if (f > 0.0F) {
                cloudrenderer$relativecamerapos = CloudRenderer.RelativeCameraPos.BELOW_CLOUDS;
            } else {
                cloudrenderer$relativecamerapos = CloudRenderer.RelativeCameraPos.INSIDE_CLOUDS;
            }

            me.alphamode.mcbig.math.BigDecimal d0 = p_363573_.x.add((double)(p_360711_ * 0.030000001F));
            me.alphamode.mcbig.math.BigDecimal d1 = p_363573_.z.add(3.96F);
            double d2 = (double)this.texture.width * 12.0;
            double d3 = (double)this.texture.height * 12.0;
            d0 = d0.subtract(Mth.bigFloor(d0.divide(d2)).toBigDecimal().multiply(d2));
            d1 = d1.subtract(Mth.bigFloor(d1.divide(d3)).toBigDecimal().multiply(d3));
            me.alphamode.mcbig.math.BigInteger i = Mth.bigFloor(d0.divide(me.alphamode.mcbig.core.BigConstants.TWELVE));
            me.alphamode.mcbig.math.BigInteger j = Mth.bigFloor(d1.divide(me.alphamode.mcbig.core.BigConstants.TWELVE));
            float f2 = (float)(d0.floatValue() - (double)((float)i.floatValue() * 12.0F));
            float f3 = (float)(d1.floatValue() - (double)((float)j.floatValue() * 12.0F));
            RenderType rendertype = p_364293_ == CloudStatus.FANCY ? RenderType.clouds() : RenderType.flatClouds();
            this.vertexBuffer.bind();
            if (this.needsRebuild
                || !i.equals(this.prevCellX)
                || !j.equals(this.prevCellZ)
                || cloudrenderer$relativecamerapos != this.prevRelativeCameraPos
                || p_364293_ != this.prevType) {
                this.needsRebuild = false;
                this.prevCellX = i;
                this.prevCellZ = j;
                this.prevRelativeCameraPos = cloudrenderer$relativecamerapos;
                this.prevType = p_364293_;
                MeshData meshdata = this.buildMesh(Tesselator.getInstance(), i, j, p_364293_, cloudrenderer$relativecamerapos, rendertype);
                if (meshdata != null) {
                    this.vertexBuffer.upload(meshdata);
                    this.vertexBufferEmpty = false;
                } else {
                    this.vertexBufferEmpty = true;
                }
            }

            if (!this.vertexBufferEmpty) {
                RenderSystem.setShaderColor(ARGB.redFloat(p_363907_), ARGB.greenFloat(p_363907_), ARGB.blueFloat(p_363907_), 1.0F);
                if (p_364293_ == CloudStatus.FANCY) {
                    this.drawWithRenderType(RenderType.cloudsDepthOnly(), p_363292_, p_364300_, f2, f, f3);
                }

                this.drawWithRenderType(rendertype, p_363292_, p_364300_, f2, f, f3);
                VertexBuffer.unbind();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private void drawWithRenderType(RenderType p_364343_, Matrix4f p_360330_, Matrix4f p_365300_, float p_363561_, float p_362810_, float p_362901_) {
        p_364343_.setupRenderState();
        CompiledShaderProgram compiledshaderprogram = RenderSystem.getShader();
        if (compiledshaderprogram != null && compiledshaderprogram.MODEL_OFFSET != null) {
            compiledshaderprogram.MODEL_OFFSET.set(-p_363561_, p_362810_, -p_362901_);
        }

        this.vertexBuffer.drawWithShader(p_360330_, p_365300_, compiledshaderprogram);
        p_364343_.clearRenderState();
    }

    @Nullable
    private MeshData buildMesh(
        Tesselator p_362597_, me.alphamode.mcbig.math.BigInteger p_362701_, me.alphamode.mcbig.math.BigInteger p_361589_, CloudStatus p_365402_, CloudRenderer.RelativeCameraPos p_364842_, RenderType p_364348_
    ) {
        float f = 0.8F;
        int i = ARGB.colorFromFloat(0.8F, 1.0F, 1.0F, 1.0F);
        int j = ARGB.colorFromFloat(0.8F, 0.9F, 0.9F, 0.9F);
        int k = ARGB.colorFromFloat(0.8F, 0.7F, 0.7F, 0.7F);
        int l = ARGB.colorFromFloat(0.8F, 0.8F, 0.8F, 0.8F);
        BufferBuilder bufferbuilder = p_362597_.begin(p_364348_.mode(), p_364348_.format());
        this.buildMesh(p_364842_, bufferbuilder, p_362701_, p_361589_, k, i, j, l, p_365402_ == CloudStatus.FANCY);
        return bufferbuilder.build();
    }

    private void buildMesh(
        CloudRenderer.RelativeCameraPos p_363221_,
        BufferBuilder p_364486_,
        me.alphamode.mcbig.math.BigInteger p_361006_,
        me.alphamode.mcbig.math.BigInteger p_362674_,
        int p_362100_,
        int p_360889_,
        int p_360776_,
        int p_365003_,
        boolean p_362207_
    ) {
        if (this.texture != null) {
            int i = 32;
            long[] along = this.texture.cells;
            int j = this.texture.width;
            int k = this.texture.height;

            for (int l = -32; l <= 32; l++) {
                for (int i1 = -32; i1 <= 32; i1++) {
                    int j1 = me.alphamode.mcbig.math.BigMath.floorMod(p_361006_.add(i1), j).intValue();
                    int k1 = me.alphamode.mcbig.math.BigMath.floorMod(p_362674_.add(l), k).intValue();
                    long l1 = along[j1 + k1 * j];
                    if (l1 != 0L) {
                        int i2 = getColor(l1);
                        if (p_362207_) {
                            this.buildExtrudedCell(
                                p_363221_,
                                p_364486_,
                                ARGB.multiply(p_362100_, i2),
                                ARGB.multiply(p_360889_, i2),
                                ARGB.multiply(p_360776_, i2),
                                ARGB.multiply(p_365003_, i2),
                                i1,
                                l,
                                l1
                            );
                        } else {
                            this.buildFlatCell(p_364486_, ARGB.multiply(p_360889_, i2), i1, l);
                        }
                    }
                }
            }
        }
    }

    private void buildFlatCell(BufferBuilder p_363638_, int p_364027_, int p_361818_, int p_364671_) {
        float f = (float)p_361818_ * 12.0F;
        float f1 = f + 12.0F;
        float f2 = (float)p_364671_ * 12.0F;
        float f3 = f2 + 12.0F;
        p_363638_.addVertex(f, 0.0F, f2).setColor(p_364027_);
        p_363638_.addVertex(f, 0.0F, f3).setColor(p_364027_);
        p_363638_.addVertex(f1, 0.0F, f3).setColor(p_364027_);
        p_363638_.addVertex(f1, 0.0F, f2).setColor(p_364027_);
    }

    private void buildExtrudedCell(
        CloudRenderer.RelativeCameraPos p_360766_,
        BufferBuilder p_360715_,
        int p_362180_,
        int p_364234_,
        int p_364613_,
        int p_361634_,
        int p_364709_,
        int p_363252_,
        long p_364423_
    ) {
        float f = (float)p_364709_ * 12.0F;
        float f1 = f + 12.0F;
        float f2 = 0.0F;
        float f3 = 4.0F;
        float f4 = (float)p_363252_ * 12.0F;
        float f5 = f4 + 12.0F;
        if (p_360766_ != CloudRenderer.RelativeCameraPos.BELOW_CLOUDS) {
            p_360715_.addVertex(f, 4.0F, f4).setColor(p_364234_);
            p_360715_.addVertex(f, 4.0F, f5).setColor(p_364234_);
            p_360715_.addVertex(f1, 4.0F, f5).setColor(p_364234_);
            p_360715_.addVertex(f1, 4.0F, f4).setColor(p_364234_);
        }

        if (p_360766_ != CloudRenderer.RelativeCameraPos.ABOVE_CLOUDS) {
            p_360715_.addVertex(f1, 0.0F, f4).setColor(p_362180_);
            p_360715_.addVertex(f1, 0.0F, f5).setColor(p_362180_);
            p_360715_.addVertex(f, 0.0F, f5).setColor(p_362180_);
            p_360715_.addVertex(f, 0.0F, f4).setColor(p_362180_);
        }

        if (isNorthEmpty(p_364423_) && p_363252_ > 0) {
            p_360715_.addVertex(f, 0.0F, f4).setColor(p_361634_);
            p_360715_.addVertex(f, 4.0F, f4).setColor(p_361634_);
            p_360715_.addVertex(f1, 4.0F, f4).setColor(p_361634_);
            p_360715_.addVertex(f1, 0.0F, f4).setColor(p_361634_);
        }

        if (isSouthEmpty(p_364423_) && p_363252_ < 0) {
            p_360715_.addVertex(f1, 0.0F, f5).setColor(p_361634_);
            p_360715_.addVertex(f1, 4.0F, f5).setColor(p_361634_);
            p_360715_.addVertex(f, 4.0F, f5).setColor(p_361634_);
            p_360715_.addVertex(f, 0.0F, f5).setColor(p_361634_);
        }

        if (isWestEmpty(p_364423_) && p_364709_ > 0) {
            p_360715_.addVertex(f, 0.0F, f5).setColor(p_364613_);
            p_360715_.addVertex(f, 4.0F, f5).setColor(p_364613_);
            p_360715_.addVertex(f, 4.0F, f4).setColor(p_364613_);
            p_360715_.addVertex(f, 0.0F, f4).setColor(p_364613_);
        }

        if (isEastEmpty(p_364423_) && p_364709_ < 0) {
            p_360715_.addVertex(f1, 0.0F, f4).setColor(p_364613_);
            p_360715_.addVertex(f1, 4.0F, f4).setColor(p_364613_);
            p_360715_.addVertex(f1, 4.0F, f5).setColor(p_364613_);
            p_360715_.addVertex(f1, 0.0F, f5).setColor(p_364613_);
        }

        boolean flag = Math.abs(p_364709_) <= 1 && Math.abs(p_363252_) <= 1;
        if (flag) {
            p_360715_.addVertex(f1, 4.0F, f4).setColor(p_364234_);
            p_360715_.addVertex(f1, 4.0F, f5).setColor(p_364234_);
            p_360715_.addVertex(f, 4.0F, f5).setColor(p_364234_);
            p_360715_.addVertex(f, 4.0F, f4).setColor(p_364234_);
            p_360715_.addVertex(f, 0.0F, f4).setColor(p_362180_);
            p_360715_.addVertex(f, 0.0F, f5).setColor(p_362180_);
            p_360715_.addVertex(f1, 0.0F, f5).setColor(p_362180_);
            p_360715_.addVertex(f1, 0.0F, f4).setColor(p_362180_);
            p_360715_.addVertex(f1, 0.0F, f4).setColor(p_361634_);
            p_360715_.addVertex(f1, 4.0F, f4).setColor(p_361634_);
            p_360715_.addVertex(f, 4.0F, f4).setColor(p_361634_);
            p_360715_.addVertex(f, 0.0F, f4).setColor(p_361634_);
            p_360715_.addVertex(f, 0.0F, f5).setColor(p_361634_);
            p_360715_.addVertex(f, 4.0F, f5).setColor(p_361634_);
            p_360715_.addVertex(f1, 4.0F, f5).setColor(p_361634_);
            p_360715_.addVertex(f1, 0.0F, f5).setColor(p_361634_);
            p_360715_.addVertex(f, 0.0F, f4).setColor(p_364613_);
            p_360715_.addVertex(f, 4.0F, f4).setColor(p_364613_);
            p_360715_.addVertex(f, 4.0F, f5).setColor(p_364613_);
            p_360715_.addVertex(f, 0.0F, f5).setColor(p_364613_);
            p_360715_.addVertex(f1, 0.0F, f5).setColor(p_364613_);
            p_360715_.addVertex(f1, 4.0F, f5).setColor(p_364613_);
            p_360715_.addVertex(f1, 4.0F, f4).setColor(p_364613_);
            p_360715_.addVertex(f1, 0.0F, f4).setColor(p_364613_);
        }
    }

    public void markForRebuild() {
        this.needsRebuild = true;
    }

    @Override
    public void close() {
        this.vertexBuffer.close();
    }

    @OnlyIn(Dist.CLIENT)
    static enum RelativeCameraPos {
        ABOVE_CLOUDS,
        INSIDE_CLOUDS,
        BELOW_CLOUDS;
    }

    @OnlyIn(Dist.CLIENT)
    public static record TextureData(long[] cells, int width, int height) {
    }
}
