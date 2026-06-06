package com.mojang.blaze3d;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.jtracy.TracyClient;
import javax.annotation.Nullable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TracyFrameCapture implements AutoCloseable {
    private static final int MAX_WIDTH = 320;
    private static final int MAX_HEIGHT = 180;
    private static final int BYTES_PER_PIXEL = 4;
    private int targetWidth;
    private int targetHeight;
    private int width;
    private int height;
    private final RenderTarget frameBuffer = new TextureTarget(320, 180, false);
    private final GpuBuffer pixelbuffer = new GpuBuffer(BufferType.PIXEL_PACK, BufferUsage.STREAM_READ, 0);
    @Nullable
    private GpuFence fence;
    private int lastCaptureDelay;
    private boolean capturedThisFrame;

    private void resize(int p_372813_, int p_372948_) {
        float f = (float)p_372813_ / (float)p_372948_;
        if (p_372813_ > 320) {
            p_372813_ = 320;
            p_372948_ = (int)(320.0F / f);
        }

        if (p_372948_ > 180) {
            p_372813_ = (int)(180.0F * f);
            p_372948_ = 180;
        }

        p_372813_ = p_372813_ / 4 * 4;
        p_372948_ = p_372948_ / 4 * 4;
        if (this.width != p_372813_ || this.height != p_372948_) {
            this.width = p_372813_;
            this.height = p_372948_;
            this.frameBuffer.resize(p_372813_, p_372948_);
            this.pixelbuffer.resize(p_372813_ * p_372948_ * 4);
            if (this.fence != null) {
                this.fence.close();
                this.fence = null;
            }
        }
    }

    public void capture(RenderTarget p_372980_) {
        if (this.fence == null && !this.capturedThisFrame) {
            this.capturedThisFrame = true;
            if (p_372980_.width != this.targetWidth || p_372980_.height != this.targetHeight) {
                this.targetWidth = p_372980_.width;
                this.targetHeight = p_372980_.height;
                this.resize(this.targetWidth, this.targetHeight);
            }

            GlStateManager._glBindFramebuffer(36009, this.frameBuffer.frameBufferId);
            GlStateManager._glBindFramebuffer(36008, p_372980_.frameBufferId);
            GlStateManager._glBlitFrameBuffer(0, 0, p_372980_.width, p_372980_.height, 0, 0, this.width, this.height, 16384, 9729);
            GlStateManager._glBindFramebuffer(36008, 0);
            GlStateManager._glBindFramebuffer(36009, 0);
            this.pixelbuffer.bind();
            GlStateManager._glBindFramebuffer(36008, this.frameBuffer.frameBufferId);
            GlStateManager._readPixels(0, 0, this.width, this.height, 6408, 5121, 0L);
            GlStateManager._glBindFramebuffer(36008, 0);
            this.fence = new GpuFence();
            this.lastCaptureDelay = 0;
        }
    }

    public void upload() {
        if (this.fence != null) {
            if (this.fence.awaitCompletion(0L)) {
                this.fence = null;

                try (GpuBuffer.ReadView gpubuffer$readview = this.pixelbuffer.read()) {
                    if (gpubuffer$readview != null) {
                        TracyClient.frameImage(gpubuffer$readview.data(), this.width, this.height, this.lastCaptureDelay, true);
                    }
                }
            }
        }
    }

    public void endFrame() {
        this.lastCaptureDelay++;
        this.capturedThisFrame = false;
        TracyClient.markFrame();
    }

    @Override
    public void close() {
        if (this.fence != null) {
            this.fence.close();
            this.fence = null;
        }

        this.pixelbuffer.close();
        this.frameBuffer.destroyBuffers();
    }
}
