package net.minecraft.network.protocol.game;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.VisibleForTesting;

public class VecDeltaCodec {
    private static final me.alphamode.mcbig.math.BigDecimal TRUNCATION_STEPS = me.alphamode.mcbig.math.BigDecimal.val(4096.0);
    private me.alphamode.mcbig.core.BigVec3 base = me.alphamode.mcbig.core.BigVec3.ZERO;

    @VisibleForTesting
    static me.alphamode.mcbig.math.BigInteger encode(me.alphamode.mcbig.math.BigDecimal p_238018_) {
        return p_238018_.multiply(TRUNCATION_STEPS).round().toBigInteger();
    }

    @VisibleForTesting
    static me.alphamode.mcbig.math.BigDecimal decode(me.alphamode.mcbig.math.BigInteger p_238020_) {
        return p_238020_.toBigDecimal().divide(TRUNCATION_STEPS);
    }

    @VisibleForTesting
    static long encode(double p_238018_) {
        return Math.round(p_238018_ * 4096.0);
    }

    @VisibleForTesting
    static double decode(long p_238020_) {
        return (double)p_238020_ / 4096.0;
    }

    public me.alphamode.mcbig.core.BigVec3 decode(long p_238022_, long p_238023_, long p_238024_) {
        if (p_238022_ == 0L && p_238023_ == 0L && p_238024_ == 0L) {
            return this.base;
        } else {
            me.alphamode.mcbig.math.BigDecimal d0 = p_238022_ == 0L ? this.base.x : decode(encode(this.base.x).add(p_238022_));
            double d1 = p_238023_ == 0L ? this.base.y : decode(encode(this.base.y) + p_238023_);
            me.alphamode.mcbig.math.BigDecimal d2 = p_238024_ == 0L ? this.base.z : decode(encode(this.base.z).add(p_238024_));
            return new me.alphamode.mcbig.core.BigVec3(d0, d1, d2);
        }
    }

    public me.alphamode.mcbig.math.BigInteger encodeX(me.alphamode.mcbig.core.BigVec3 p_238026_) {
        return encode(p_238026_.x).subtract(encode(this.base.x));
    }

    public long encodeY(me.alphamode.mcbig.core.BigVec3 p_238028_) {
        return encode(p_238028_.y()) - encode(this.base.y());
    }

    public me.alphamode.mcbig.math.BigInteger encodeZ(me.alphamode.mcbig.core.BigVec3 p_238030_) {
        return encode(p_238030_.z).subtract(encode(this.base.z));
    }

    public Vec3 delta(me.alphamode.mcbig.core.BigVec3 p_238032_) {
        return p_238032_.subtract(this.base).toVanilla();
    }

    public void setBase(me.alphamode.mcbig.core.BigVec3 p_238034_) {
        this.base = p_238034_;
    }

    public me.alphamode.mcbig.core.BigVec3 getBase() {
        return this.base;
    }
}
