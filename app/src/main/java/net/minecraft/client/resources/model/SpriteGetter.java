package net.minecraft.client.resources.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface SpriteGetter {
    TextureAtlasSprite get(Material p_387809_);

    TextureAtlasSprite reportMissingReference(String p_387031_);
}
