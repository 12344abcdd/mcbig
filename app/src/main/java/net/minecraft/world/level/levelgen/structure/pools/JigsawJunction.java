package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class JigsawJunction {
    private final me.alphamode.mcbig.math.BigInteger sourceX;
    private final int sourceGroundY;
    private final me.alphamode.mcbig.math.BigInteger sourceZ;
    private final int deltaY;
    private final StructureTemplatePool.Projection destProjection;

    public JigsawJunction(me.alphamode.mcbig.math.BigInteger p_210247_, int p_210248_, me.alphamode.mcbig.math.BigInteger p_210249_, int p_210250_, StructureTemplatePool.Projection p_210251_) {
        this.sourceX = p_210247_;
        this.sourceGroundY = p_210248_;
        this.sourceZ = p_210249_;
        this.deltaY = p_210250_;
        this.destProjection = p_210251_;
    }

    public me.alphamode.mcbig.math.BigInteger getSourceX() {
        return this.sourceX;
    }

    public int getSourceGroundY() {
        return this.sourceGroundY;
    }

    public me.alphamode.mcbig.math.BigInteger getSourceZ() {
        return this.sourceZ;
    }

    public int getDeltaY() {
        return this.deltaY;
    }

    public StructureTemplatePool.Projection getDestProjection() {
        return this.destProjection;
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> p_210256_) {
        Builder<T, T> builder = ImmutableMap.builder();
        builder.put(p_210256_.createString("source_x"), p_210256_.createString(this.sourceX.toString()))
            .put(p_210256_.createString("source_ground_y"), p_210256_.createInt(this.sourceGroundY))
            .put(p_210256_.createString("source_z"), p_210256_.createString(this.sourceZ.toString()))
            .put(p_210256_.createString("delta_y"), p_210256_.createInt(this.deltaY))
            .put(p_210256_.createString("dest_proj"), p_210256_.createString(this.destProjection.getName()));
        return new Dynamic<>(p_210256_, p_210256_.createMap(builder.build()));
    }

    public static <T> JigsawJunction deserialize(Dynamic<T> p_210254_) {
        return new JigsawJunction(
            me.alphamode.mcbig.math.BigInteger.val(p_210254_.get("source_x").asString("0")),
            p_210254_.get("source_ground_y").asInt(0),
            me.alphamode.mcbig.math.BigInteger.val(p_210254_.get("source_z").asString("0")),
            p_210254_.get("delta_y").asInt(0),
            StructureTemplatePool.Projection.byName(p_210254_.get("dest_proj").asString(""))
        );
    }

    @Override
    public boolean equals(Object p_210262_) {
        if (this == p_210262_) {
            return true;
        } else if (p_210262_ != null && this.getClass() == p_210262_.getClass()) {
            JigsawJunction jigsawjunction = (JigsawJunction)p_210262_;
            if (this.sourceX != jigsawjunction.sourceX) {
                return false;
            } else if (this.sourceZ != jigsawjunction.sourceZ) {
                return false;
            } else {
                return this.deltaY != jigsawjunction.deltaY ? false : this.destProjection == jigsawjunction.destProjection;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int i = this.sourceX.intValue();
        i = 31 * i + this.sourceGroundY;
        i = 31 * i + this.sourceZ.intValue();
        i = 31 * i + this.deltaY;
        return 31 * i + this.destProjection.hashCode();
    }

    @Override
    public String toString() {
        return "JigsawJunction{sourceX="
            + this.sourceX
            + ", sourceGroundY="
            + this.sourceGroundY
            + ", sourceZ="
            + this.sourceZ
            + ", deltaY="
            + this.deltaY
            + ", destProjection="
            + this.destProjection
            + "}";
    }
}
