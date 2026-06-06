package net.minecraft.world.level.biome;

import net.minecraft.core.Holder;

public interface BiomeResolver {
    Holder<Biome> getNoiseBiome(me.alphamode.mcbig.math.BigInteger p_204221_, me.alphamode.mcbig.math.BigInteger p_204222_, me.alphamode.mcbig.math.BigInteger p_204223_, Climate.Sampler p_204224_);
}
