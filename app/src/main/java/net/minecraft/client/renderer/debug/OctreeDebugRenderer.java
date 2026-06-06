package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Octree;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;

@OnlyIn(Dist.CLIENT)
public class OctreeDebugRenderer {
    private final Minecraft minecraft;

    public OctreeDebugRenderer(Minecraft p_366882_) {
        this.minecraft = p_366882_;
    }

    public void render(PoseStack p_366656_, Frustum p_366635_, MultiBufferSource p_366910_, double p_366642_, double p_366743_, double p_366744_) {
        Octree octree = this.minecraft.levelRenderer.getSectionOcclusionGraph().getOctree();
        MutableInt mutableint = new MutableInt(0);
        octree.visitNodes(
            (p_370341_, p_370342_, p_370343_, p_370344_) -> this.renderNode(
                    p_370341_, p_366656_, p_366910_, p_366642_, p_366743_, p_366744_, p_370343_, p_370342_, mutableint, p_370344_
                ),
            p_366635_,
            32
        );
    }

    private void renderNode(
        Octree.Node p_366527_,
        PoseStack p_366585_,
        MultiBufferSource p_366909_,
        double p_366814_,
        double p_366595_,
        double p_366414_,
        int p_366591_,
        boolean p_366876_,
        MutableInt p_366741_,
        boolean p_371203_
    ) {
        me.alphamode.mcbig.phys.BigAABB aabb = p_366527_.getAABB();
        double d0 = aabb.getXsize();
        long i = Math.round(d0 / 16.0);
        if (i == 1L) {
            p_366741_.add(1);
            double d1 = aabb.getCenter().x.doubleValue();
            double d2 = aabb.getCenter().y;
            double d3 = aabb.getCenter().z.doubleValue();
            int k = p_371203_ ? -16711936 : -1;
            DebugRenderer.renderFloatingText(p_366585_, p_366909_, String.valueOf(p_366741_.getValue()), d1, d2, d3, k, 0.3F);
        }

        VertexConsumer vertexconsumer = p_366909_.getBuffer(RenderType.lines());
        long j = i + 5L;
        ShapeRenderer.renderLineBox(
            p_366585_,
            vertexconsumer,
            aabb.deflate(0.1 * (double)p_366591_).move(-p_366814_, -p_366595_, -p_366414_).toVanilla(),
            getColorComponent(j, 0.3F),
            getColorComponent(j, 0.8F),
            getColorComponent(j, 0.5F),
            p_366876_ ? 0.4F : 1.0F
        );
    }

    private static float getColorComponent(long p_366878_, float p_366682_) {
        float f = 0.1F;
        return Mth.frac(p_366682_ * (float)p_366878_) * 0.9F + 0.1F;
    }
}
