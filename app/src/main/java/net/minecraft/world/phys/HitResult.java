package net.minecraft.world.phys;

import net.minecraft.world.entity.Entity;

public abstract class HitResult {
    protected final me.alphamode.mcbig.core.BigVec3 location;

    protected HitResult(me.alphamode.mcbig.core.BigVec3 p_82447_) {
        this.location = p_82447_;
    }

    public double distanceTo(Entity p_82449_) {
        double d0 = this.location.x.subtract(p_82449_.getX()).doubleValue();
        double d1 = this.location.y - p_82449_.getY();
        double d2 = this.location.z.subtract(p_82449_.getZ()).doubleValue();
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public abstract HitResult.Type getType();

    public me.alphamode.mcbig.core.BigVec3 getLocation() {
        return this.location;
    }

    public static enum Type {
        MISS,
        BLOCK,
        ENTITY;
    }
}
