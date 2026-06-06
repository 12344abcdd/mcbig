package net.minecraft.server.level;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public record ColumnPos(me.alphamode.mcbig.math.BigInteger x, me.alphamode.mcbig.math.BigInteger z) {
    private static final long COORD_BITS = 32L;
    private static final long COORD_MASK = 4294967295L;

    public ChunkPos toChunkPos() {
        return new ChunkPos(SectionPos.blockToSectionCoord(this.x), SectionPos.blockToSectionCoord(this.z));
    }

    public static int getX(long p_214970_) {
        return (int)(p_214970_ & COORD_MASK);
    }

    public static int getZ(long p_214972_) {
        return (int)(p_214972_ >>> COORD_BITS & COORD_MASK);
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.z + "]";
    }
}
