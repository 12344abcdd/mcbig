package net.minecraft.core;

public final class QuartPos {
    public static final int BITS = 2;
    public static final int SIZE = 4;
    public static final int MASK = 3;
    private static final int SECTION_TO_QUARTS_BITS = 2;

    private QuartPos() {
    }

    public static int fromBlock(int p_175401_) {
        return p_175401_ >> BITS;
    }

    public static me.alphamode.mcbig.math.BigInteger fromBlock(me.alphamode.mcbig.math.BigInteger p_175401_) {
        return p_175401_.shiftRight(BITS);
    }

    public static int quartLocal(int p_198377_) {
        return p_198377_ & MASK;
    }

    public static me.alphamode.mcbig.math.BigInteger toBlock(me.alphamode.mcbig.math.BigInteger p_175403_) {
        return p_175403_.shiftLeft(BITS);
    }

    public static int toBlock(int p_175403_) {
        return p_175403_ << BITS;
    }

    public static int fromSection(int p_175405_) {
        return p_175405_ << SECTION_TO_QUARTS_BITS;
    }

    public static me.alphamode.mcbig.math.BigInteger fromSection(me.alphamode.mcbig.math.BigInteger p_175405_) {
        return p_175405_.shiftLeft(SECTION_TO_QUARTS_BITS);
    }

    public static int toSection(int p_175407_) {
        return p_175407_ >> SECTION_TO_QUARTS_BITS;
    }

    public static me.alphamode.mcbig.math.BigInteger toSection(me.alphamode.mcbig.math.BigInteger p_175407_) {
        return p_175407_.shiftRight(SECTION_TO_QUARTS_BITS);
    }
}
