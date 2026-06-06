package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Node {
    public final me.alphamode.mcbig.math.BigInteger x;
    public final int y;
    public final me.alphamode.mcbig.math.BigInteger z;
    private final int hash;
    public int heapIdx = -1;
    public float g;
    public float h;
    public float f;
    @Nullable
    public Node cameFrom;
    public boolean closed;
    public float walkedDistance;
    public float costMalus;
    public PathType type = PathType.BLOCKED;

    public Node(int p_77285_, int p_77286_, int p_77287_) {
        this.x = me.alphamode.mcbig.math.BigInteger.constant(p_77285_);
        this.y = p_77286_;
        this.z = me.alphamode.mcbig.math.BigInteger.constant(p_77287_);
        this.hash = createHash(x.intValue(), y, z.intValue());
    }

    public Node(me.alphamode.mcbig.math.BigInteger p_77285_, int p_77286_, me.alphamode.mcbig.math.BigInteger p_77287_) {
        this.x = p_77285_;
        this.y = p_77286_;
        this.z = p_77287_;
        this.hash = createHash(p_77285_.intValue(), p_77286_, p_77287_.intValue());
    }


    public Node cloneAndMove(me.alphamode.mcbig.math.BigInteger p_77290_, int p_77291_, me.alphamode.mcbig.math.BigInteger p_77292_) {
        Node node = new Node(p_77290_, p_77291_, p_77292_);
        node.heapIdx = this.heapIdx;
        node.g = this.g;
        node.h = this.h;
        node.f = this.f;
        node.cameFrom = this.cameFrom;
        node.closed = this.closed;
        node.walkedDistance = this.walkedDistance;
        node.costMalus = this.costMalus;
        node.type = this.type;
        return node;
    }

    public static int createHash(int p_77296_, int p_77297_, int p_77298_) {
        return p_77297_ & 0xFF | (p_77296_ & 32767) << 8 | (p_77298_ & 32767) << 24 | (p_77296_ < 0 ? Integer.MIN_VALUE : 0) | (p_77298_ < 0 ? 32768 : 0);
    }

    public float distanceTo(Node p_77294_) {
        float f = (float)(p_77294_.x.subtract(this.x)).floatValue();
        float f1 = (float)(p_77294_.y - this.y);
        float f2 = (float)(p_77294_.z.subtract(this.z)).floatValue();
        return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public float distanceToXZ(Node p_230614_) {
        float f = (float)(p_230614_.x.subtract(this.x)).floatValue();
        float f1 = (float)(p_230614_.z.subtract(this.z)).floatValue();
        return Mth.sqrt(f * f + f1 * f1);
    }

    public float distanceTo(BlockPos p_164698_) {
        float f = (float)(p_164698_.getBigX().subtract(this.x)).floatValue();
        float f1 = (float)(p_164698_.getBigY().subtract(this.y)).floatValue();
        float f2 = (float)(p_164698_.getBigZ().subtract(this.z)).floatValue();
        return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public float distanceToSqr(Node p_77300_) {
        float f = (float)(p_77300_.x.subtract(this.x)).floatValue();
        float f1 = (float)(p_77300_.y - this.y);
        float f2 = (float)(p_77300_.z.subtract(this.z)).floatValue();
        return f * f + f1 * f1 + f2 * f2;
    }

    public float distanceToSqr(BlockPos p_164703_) {
        float f = (float)(p_164703_.getBigX().subtract(this.x)).floatValue();
        float f1 = (float)(p_164703_.getBigY().subtract(this.y)).floatValue();
        float f2 = (float)(p_164703_.getBigZ().subtract(this.z)).floatValue();
        return f * f + f1 * f1 + f2 * f2;
    }

    public float distanceManhattan(Node p_77305_) {
        float f = (float)p_77305_.x.subtract(this.x).abs().floatValue();
        float f1 = (float)Math.abs(p_77305_.y - this.y);
        float f2 = (float)p_77305_.z.subtract(this.z).abs().floatValue();
        return f + f1 + f2;
    }

    public float distanceManhattan(BlockPos p_77307_) {
        float f = (float)p_77307_.getBigX().subtract(this.x).abs().floatValue();
        float f1 = (float)p_77307_.getBigY().subtract(this.y).abs().floatValue();
        float f2 = (float)p_77307_.getBigZ().subtract(this.z).abs().floatValue();
        return f + f1 + f2;
    }

    public BlockPos asBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public Vec3 asVec3() {
        return new Vec3((double)this.x.doubleValue(), (double)this.y, (double)this.z.doubleValue());
    }

    @Override
    public boolean equals(Object p_77309_) {
        return !(p_77309_ instanceof Node node) ? false : this.hash == node.hash && this.x == node.x && this.y == node.y && this.z == node.z;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    public boolean inOpenSet() {
        return this.heapIdx >= 0;
    }

    @Override
    public String toString() {
        return "Node{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
    }

    public void writeToStream(FriendlyByteBuf p_164700_) {
        p_164700_.writeBigInteger(this.x);
        p_164700_.writeInt(this.y);
        p_164700_.writeBigInteger(this.z);
        p_164700_.writeFloat(this.walkedDistance);
        p_164700_.writeFloat(this.costMalus);
        p_164700_.writeBoolean(this.closed);
        p_164700_.writeEnum(this.type);
        p_164700_.writeFloat(this.f);
    }

    public static Node createFromStream(FriendlyByteBuf p_77302_) {
        Node node = new Node(p_77302_.readBigInteger(), p_77302_.readInt(), p_77302_.readBigInteger());
        readContents(p_77302_, node);
        return node;
    }

    protected static void readContents(FriendlyByteBuf p_262984_, Node p_263009_) {
        p_263009_.walkedDistance = p_262984_.readFloat();
        p_263009_.costMalus = p_262984_.readFloat();
        p_263009_.closed = p_262984_.readBoolean();
        p_263009_.type = p_262984_.readEnum(PathType.class);
        p_263009_.f = p_262984_.readFloat();
    }
}
