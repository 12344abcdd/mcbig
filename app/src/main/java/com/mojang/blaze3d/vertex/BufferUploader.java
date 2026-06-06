package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BufferUploader {
    @Nullable
    private static VertexBuffer lastImmediateBuffer;

    public static void reset() {
        if (lastImmediateBuffer != null) {
            invalidate();
            VertexBuffer.unbind();
        }
    }

    public static void invalidate() {
        lastImmediateBuffer = null;
    }

    public static void drawWithShader(MeshData p_350434_) {
        RenderSystem.assertOnRenderThread();
        VertexBuffer vertexbuffer = upload(p_350434_);
        vertexbuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
    }

    public static void draw(MeshData p_350302_) {
        RenderSystem.assertOnRenderThread();
        VertexBuffer vertexbuffer = upload(p_350302_);
        vertexbuffer.draw();
    }

    private static VertexBuffer upload(MeshData p_350970_) {
        VertexBuffer vertexbuffer = bindImmediateBuffer(p_350970_.drawState().format());
        vertexbuffer.upload(p_350970_);
        return vertexbuffer;
    }

    private static VertexBuffer bindImmediateBuffer(VertexFormat p_231207_) {
        VertexBuffer vertexbuffer = p_231207_.getImmediateDrawVertexBuffer();
        bindImmediateBuffer(vertexbuffer);
        return vertexbuffer;
    }

    private static void bindImmediateBuffer(VertexBuffer p_231205_) {
        if (p_231205_ != lastImmediateBuffer) {
            p_231205_.bind();
            lastImmediateBuffer = p_231205_;
        }
    }
}
