package net.minecraft.client.resources.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.VisibleForDebug;

public interface ModelBaker {
    BakedModel bake(ResourceLocation p_250776_, ModelState p_251280_);

    SpriteGetter sprites();

    @VisibleForDebug
    ModelDebugName rootName();
}
