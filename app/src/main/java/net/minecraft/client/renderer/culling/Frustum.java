package net.minecraft.client.renderer.culling;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class Frustum {
    public static final int OFFSET_STEP = 4;
    private final FrustumIntersection intersection = new FrustumIntersection();
    private final Matrix4f matrix = new Matrix4f();
    private Vector4f viewVector;
    private me.alphamode.mcbig.math.BigDecimal camX = me.alphamode.mcbig.math.BigDecimal.ZERO;
    private double camY;
    private me.alphamode.mcbig.math.BigDecimal camZ = me.alphamode.mcbig.math.BigDecimal.ZERO;

    public Frustum(Matrix4f p_254207_, Matrix4f p_254535_) {
        this.calculateFrustum(p_254207_, p_254535_);
    }

    public Frustum(Frustum p_194440_) {
        this.intersection.set(p_194440_.matrix);
        this.matrix.set(p_194440_.matrix);
        this.camX = p_194440_.camX;
        this.camY = p_194440_.camY;
        this.camZ = p_194440_.camZ;
        this.viewVector = p_194440_.viewVector;
    }

    public Frustum offsetToFullyIncludeCameraCube(int p_194442_) {
        me.alphamode.mcbig.math.BigDecimal d0 = this.camX.divide((double)p_194442_).floor().multiply((double)p_194442_);
        double d1 = Math.floor(this.camY / (double)p_194442_) * (double)p_194442_;
        me.alphamode.mcbig.math.BigDecimal d2 = this.camZ.divide((double)p_194442_).floor().multiply((double)p_194442_);
        me.alphamode.mcbig.math.BigDecimal d3 = this.camX.divide((double)p_194442_).ceil().multiply((double)p_194442_);
        double d4 = Math.ceil(this.camY / (double)p_194442_) * (double)p_194442_;

        for(me.alphamode.mcbig.math.BigDecimal d5 = this.camZ.divide((double)p_194442_).ceil().multiply((double)p_194442_);
            this.intersection
                    .intersectAab(
                        (float)(d0.subtract(this.camX).floatValue()),
                        (float)(d1 - this.camY),
                        (float)(d2.subtract(this.camZ).floatValue()),
                        (float)(d3.subtract(this.camX).floatValue()),
                        (float)(d4 - this.camY),
                        (float)(d5.subtract(this.camZ).floatValue())
                    )
                != -2;
            this.camZ = this.camZ.subtract((double)(this.viewVector.z() * 4.0F))
        ) {
            this.camX = this.camX.subtract((double)(this.viewVector.x() * 4.0F));
            this.camY = this.camY - (double)(this.viewVector.y() * 4.0F);
        }

        return this;
    }

    public void prepare(me.alphamode.mcbig.math.BigDecimal p_113003_, double p_113004_, me.alphamode.mcbig.math.BigDecimal p_113005_) {
        this.camX = p_113003_;
        this.camY = p_113004_;
        this.camZ = p_113005_;
    }

    private void calculateFrustum(Matrix4f p_253909_, Matrix4f p_254521_) {
        p_254521_.mul(p_253909_, this.matrix);
        this.intersection.set(this.matrix);
        this.viewVector = this.matrix.transformTranspose(new Vector4f(0.0F, 0.0F, 1.0F, 0.0F));
    }

    public boolean isVisible(AABB p_113030_) {
        /*int i = this.cubeInFrustum(p_113030_.minX, p_113030_.minY, p_113030_.minZ, p_113030_.maxX, p_113030_.maxY, p_113030_.maxZ);
        return i == -2 || i == -1;*/
        return true;
    }

    public boolean isVisible(me.alphamode.mcbig.phys.BigAABB p_113030_) {
        /*int i = this.cubeInFrustum(p_113030_.minX, p_113030_.minY, p_113030_.minZ, p_113030_.maxX, p_113030_.maxY, p_113030_.maxZ);
        return i == -2 || i == -1;*/
        return true;
    }

    public int cubeInFrustum(BoundingBox p_366406_) {
        return this.cubeInFrustum(
            (double)p_366406_.minX().doubleValue(),
            (double)p_366406_.minY().doubleValue(),
            (double)p_366406_.minZ().doubleValue(),
            (double)(p_366406_.maxX().add().doubleValue()),
            (double)(p_366406_.maxY().add().doubleValue()),
            (double)(p_366406_.maxZ().add().doubleValue())
        );
    }

    private int cubeInFrustum(double p_113007_, double p_113008_, double p_113009_, double p_113010_, double p_113011_, double p_113012_) {
        float f = (float)(p_113007_ - this.camX.doubleValue());
        float f1 = (float)(p_113008_ - this.camY);
        float f2 = (float)(p_113009_ - this.camZ.doubleValue());
        float f3 = (float)(p_113010_ - this.camX.doubleValue());
        float f4 = (float)(p_113011_ - this.camY);
        float f5 = (float)(p_113012_ - this.camZ.doubleValue());
        return this.intersection.intersectAab(f, f1, f2, f3, f4, f5);
    }

    private int cubeInFrustum(me.alphamode.mcbig.math.BigDecimal p_113007_, double p_113008_, me.alphamode.mcbig.math.BigDecimal p_113009_, me.alphamode.mcbig.math.BigDecimal p_113010_, double p_113011_, me.alphamode.mcbig.math.BigDecimal p_113012_) {
        float f = (float)(p_113007_.subtract(this.camX).floatValue());
        float f1 = (float)(p_113008_ - this.camY);
        float f2 = (float)(p_113009_.subtract(this.camZ).floatValue());
        float f3 = (float)(p_113010_.subtract(this.camX).floatValue());
        float f4 = (float)(p_113011_ - this.camY);
        float f5 = (float)(p_113012_.subtract(this.camZ).floatValue());
        return this.intersection.intersectAab(f, f1, f2, f3, f4, f5);
    }

    public Vector4f[] getFrustumPoints() {
        Vector4f[] avector4f = new Vector4f[]{
            new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F),
            new Vector4f(1.0F, -1.0F, -1.0F, 1.0F),
            new Vector4f(1.0F, 1.0F, -1.0F, 1.0F),
            new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F),
            new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F),
            new Vector4f(1.0F, -1.0F, 1.0F, 1.0F),
            new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
            new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F)
        };
        Matrix4f matrix4f = this.matrix.invert(new Matrix4f());

        for (int i = 0; i < 8; i++) {
            matrix4f.transform(avector4f[i]);
            avector4f[i].div(avector4f[i].w());
        }

        return avector4f;
    }

    public double getCamX() {
        return this.camX.doubleValue();
    }

    public double getCamY() {
        return this.camY;
    }

    public double getCamZ() {
        return this.camZ.doubleValue();
    }
}
