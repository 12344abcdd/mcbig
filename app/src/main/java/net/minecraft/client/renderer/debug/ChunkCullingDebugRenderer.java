package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class ChunkCullingDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    public static final Direction[] DIRECTIONS = Direction.values();
    private final Minecraft minecraft;

    public ChunkCullingDebugRenderer(Minecraft p_362015_) {
        this.minecraft = p_362015_;
    }

    @Override
    public void render(PoseStack p_362623_, MultiBufferSource p_361977_, double p_364318_, double p_361586_, double p_365009_) {
        LevelRenderer levelrenderer = this.minecraft.levelRenderer;
        if (this.minecraft.sectionPath || this.minecraft.sectionVisibility) {
            SectionOcclusionGraph sectionocclusiongraph = levelrenderer.getSectionOcclusionGraph();

            for (SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection : levelrenderer.getVisibleSections()) {
                SectionOcclusionGraph.Node sectionocclusiongraph$node = sectionocclusiongraph.getNode(sectionrenderdispatcher$rendersection);
                if (sectionocclusiongraph$node != null) {
                    BlockPos blockpos = sectionrenderdispatcher$rendersection.getOrigin();
                    p_362623_.pushPose();
                    p_362623_.translate((double)blockpos.getX() - p_364318_, (double)blockpos.getY() - p_361586_, (double)blockpos.getZ() - p_365009_);
                    Matrix4f matrix4f = p_362623_.last().pose();
                    if (this.minecraft.sectionPath) {
                        VertexConsumer vertexconsumer = p_361977_.getBuffer(RenderType.lines());
                        int i = sectionocclusiongraph$node.step == 0 ? 0 : Mth.hsvToRgb((float)sectionocclusiongraph$node.step / 50.0F, 0.9F, 0.9F);
                        int j = i >> 16 & 0xFF;
                        int k = i >> 8 & 0xFF;
                        int l = i & 0xFF;

                        for (int i1 = 0; i1 < DIRECTIONS.length; i1++) {
                            if (sectionocclusiongraph$node.hasSourceDirection(i1)) {
                                Direction direction = DIRECTIONS[i1];
                                vertexconsumer.addVertex(matrix4f, 8.0F, 8.0F, 8.0F)
                                    .setColor(j, k, l, 255)
                                    .setNormal((float)direction.getStepX(), (float)direction.getStepY(), (float)direction.getStepZ());
                                vertexconsumer.addVertex(
                                        matrix4f,
                                        (float)(8 - 16 * direction.getStepX()),
                                        (float)(8 - 16 * direction.getStepY()),
                                        (float)(8 - 16 * direction.getStepZ())
                                    )
                                    .setColor(j, k, l, 255)
                                    .setNormal((float)direction.getStepX(), (float)direction.getStepY(), (float)direction.getStepZ());
                            }
                        }
                    }

                    if (this.minecraft.sectionVisibility && sectionrenderdispatcher$rendersection.getCompiled().hasRenderableLayers()) {
                        VertexConsumer vertexconsumer3 = p_361977_.getBuffer(RenderType.lines());
                        int j1 = 0;

                        for (Direction direction2 : DIRECTIONS) {
                            for (Direction direction1 : DIRECTIONS) {
                                boolean flag = sectionrenderdispatcher$rendersection.getCompiled().facesCanSeeEachother(direction2, direction1);
                                if (!flag) {
                                    j1++;
                                    vertexconsumer3.addVertex(
                                            matrix4f,
                                            (float)(8 + 8 * direction2.getStepX()),
                                            (float)(8 + 8 * direction2.getStepY()),
                                            (float)(8 + 8 * direction2.getStepZ())
                                        )
                                        .setColor(255, 0, 0, 255)
                                        .setNormal((float)direction2.getStepX(), (float)direction2.getStepY(), (float)direction2.getStepZ());
                                    vertexconsumer3.addVertex(
                                            matrix4f,
                                            (float)(8 + 8 * direction1.getStepX()),
                                            (float)(8 + 8 * direction1.getStepY()),
                                            (float)(8 + 8 * direction1.getStepZ())
                                        )
                                        .setColor(255, 0, 0, 255)
                                        .setNormal((float)direction1.getStepX(), (float)direction1.getStepY(), (float)direction1.getStepZ());
                                }
                            }
                        }

                        if (j1 > 0) {
                            VertexConsumer vertexconsumer4 = p_361977_.getBuffer(RenderType.debugQuads());
                            float f = 0.5F;
                            float f1 = 0.2F;
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                        }
                    }

                    p_362623_.popPose();
                }
            }
        }

        Frustum frustum = levelrenderer.getCapturedFrustum();
        if (frustum != null) {
            p_362623_.pushPose();
            p_362623_.translate((float)(frustum.getCamX() - p_364318_), (float)(frustum.getCamY() - p_361586_), (float)(frustum.getCamZ() - p_365009_));
            Matrix4f matrix4f1 = p_362623_.last().pose();
            Vector4f[] avector4f = frustum.getFrustumPoints();
            VertexConsumer vertexconsumer1 = p_361977_.getBuffer(RenderType.debugQuads());
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 0, 1, 2, 3, 0, 1, 1);
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 4, 5, 6, 7, 1, 0, 0);
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 0, 1, 5, 4, 1, 1, 0);
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 2, 3, 7, 6, 0, 0, 1);
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 0, 4, 7, 3, 0, 1, 0);
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 1, 5, 6, 2, 1, 0, 1);
            VertexConsumer vertexconsumer2 = p_361977_.getBuffer(RenderType.lines());
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[0]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[1]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[1]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[2]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[2]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[3]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[3]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[0]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[4]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[5]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[5]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[6]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[6]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[7]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[7]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[4]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[0]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[4]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[1]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[5]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[2]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[6]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[3]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[7]);
            p_362623_.popPose();
        }
    }

    private void addFrustumVertex(VertexConsumer p_360922_, Matrix4f p_363048_, Vector4f p_362450_) {
        p_360922_.addVertex(p_363048_, p_362450_.x(), p_362450_.y(), p_362450_.z()).setColor(-16777216).setNormal(0.0F, 0.0F, -1.0F);
    }

    private void addFrustumQuad(
        VertexConsumer p_360778_,
        Matrix4f p_364409_,
        Vector4f[] p_360544_,
        int p_363131_,
        int p_364933_,
        int p_363857_,
        int p_360531_,
        int p_365365_,
        int p_360692_,
        int p_363466_
    ) {
        float f = 0.25F;
        p_360778_.addVertex(p_364409_, p_360544_[p_363131_].x(), p_360544_[p_363131_].y(), p_360544_[p_363131_].z())
            .setColor((float)p_365365_, (float)p_360692_, (float)p_363466_, 0.25F);
        p_360778_.addVertex(p_364409_, p_360544_[p_364933_].x(), p_360544_[p_364933_].y(), p_360544_[p_364933_].z())
            .setColor((float)p_365365_, (float)p_360692_, (float)p_363466_, 0.25F);
        p_360778_.addVertex(p_364409_, p_360544_[p_363857_].x(), p_360544_[p_363857_].y(), p_360544_[p_363857_].z())
            .setColor((float)p_365365_, (float)p_360692_, (float)p_363466_, 0.25F);
        p_360778_.addVertex(p_364409_, p_360544_[p_360531_].x(), p_360544_[p_360531_].y(), p_360544_[p_360531_].z())
            .setColor((float)p_365365_, (float)p_360692_, (float)p_363466_, 0.25F);
    }
}
