package net.minecraft.client.renderer.texture;

import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class SimpleTexture extends ReloadableTexture {
    public SimpleTexture(ResourceLocation p_118133_) {
        super(p_118133_);
    }

    @Override
    public TextureContents loadContents(ResourceManager p_389644_) throws IOException {
        return TextureContents.load(p_389644_, this.resourceId());
    }
}
