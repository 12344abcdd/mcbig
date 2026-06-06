package com.mojang.blaze3d.buffers;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum BufferType {
    VERTICES(34962),
    INDICES(34963),
    PIXEL_PACK(35051),
    COPY_READ(36662),
    COPY_WRITE(36663),
    PIXEL_UNPACK(35052),
    UNIFORM(35345);

    final int id;

    private BufferType(int p_374137_) {
        this.id = p_374137_;
    }
}
