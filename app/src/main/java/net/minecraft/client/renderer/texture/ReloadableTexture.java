package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ReloadableTexture extends AbstractTexture {
    private final ResourceLocation resourceId;

    public ReloadableTexture(ResourceLocation p_389687_) {
        this.resourceId = p_389687_;
    }

    public ResourceLocation resourceId() {
        return this.resourceId;
    }

    public void apply(TextureContents p_389491_) {
        boolean flag = p_389491_.clamp();
        boolean flag1 = p_389491_.blur();
        this.defaultBlur = flag1;
        NativeImage nativeimage = p_389491_.image();
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this.doLoad(nativeimage, flag1, flag));
        } else {
            this.doLoad(nativeimage, flag1, flag);
        }
    }

    private void doLoad(NativeImage p_389473_, boolean p_389455_, boolean p_389621_) {
        TextureUtil.prepareImage(this.getId(), 0, p_389473_.getWidth(), p_389473_.getHeight());
        this.setFilter(p_389455_, false);
        this.setClamp(p_389621_);
        p_389473_.upload(0, 0, 0, 0, 0, p_389473_.getWidth(), p_389473_.getHeight(), true);
    }

    public abstract TextureContents loadContents(ResourceManager p_389520_) throws IOException;
}
