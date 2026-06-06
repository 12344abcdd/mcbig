package net.minecraft.core;

public class Cursor3D {
    public static final int TYPE_INSIDE = 0;
    public static final int TYPE_FACE = 1;
    public static final int TYPE_EDGE = 2;
    public static final int TYPE_CORNER = 3;
    private final me.alphamode.mcbig.math.BigInteger originX;
    private final me.alphamode.mcbig.math.BigInteger originY;
    private final me.alphamode.mcbig.math.BigInteger originZ;
    private final int width;
    private final int height;
    private final int depth;
    private final int end;
    private int index;
    private int x;
    private int y;
    private int z;

    public Cursor3D(me.alphamode.mcbig.math.BigInteger p_122298_, me.alphamode.mcbig.math.BigInteger p_122299_, me.alphamode.mcbig.math.BigInteger p_122300_, me.alphamode.mcbig.math.BigInteger p_122301_, me.alphamode.mcbig.math.BigInteger p_122302_, me.alphamode.mcbig.math.BigInteger p_122303_) {
        this.originX = p_122298_;
        this.originY = p_122299_;
        this.originZ = p_122300_;
        this.width = p_122301_.subtract(p_122298_).add().intValue();
        this.height = p_122302_.subtract(p_122299_).add().intValue();
        this.depth = p_122303_.subtract(p_122300_).add().intValue();
        this.end = this.width * this.height * this.depth;
    }

    public boolean advance() {
        if (this.index == this.end) {
            return false;
        } else {
            this.x = this.index % this.width;
            int i = this.index / this.width;
            this.y = i % this.height;
            this.z = i / this.height;
            this.index++;
            return true;
        }
    }

    public me.alphamode.mcbig.math.BigInteger nextX() {
        return this.originX.add(this.x);
    }

    public me.alphamode.mcbig.math.BigInteger nextY() {
        return this.originY.add(this.y);
    }

    public me.alphamode.mcbig.math.BigInteger nextZ() {
        return this.originZ.add(this.z);
    }

    public int getNextType() {
        int type = 0;
        if (this.x == TYPE_INSIDE || this.x == this.width - 1) {
            ++type;
        }

        if (this.y == TYPE_INSIDE || this.y == this.height - 1) {
            ++type;
        }

        if (this.z == TYPE_INSIDE || this.z == this.depth - 1) {
            ++type;
        }

        return type;
    }
}
