package net.minecraft.client.renderer.texture.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface SpriteResourceLoader {
    Logger LOGGER = LogUtils.getLogger();

    static SpriteResourceLoader create(Collection<MetadataSectionType<?>> p_296204_) {
        return (p_389362_, p_389363_) -> {
            ResourceMetadata resourcemetadata;
            try {
                resourcemetadata = p_389363_.metadata().copySections(p_296204_);
            } catch (Exception exception) {
                LOGGER.error("Unable to parse metadata from {}", p_389362_, exception);
                return null;
            }

            NativeImage nativeimage;
            try (InputStream inputstream = p_389363_.open()) {
                nativeimage = NativeImage.read(inputstream);
            } catch (IOException ioexception) {
                LOGGER.error("Using missing texture, unable to load {}", p_389362_, ioexception);
                return null;
            }

            Optional<AnimationMetadataSection> optional = resourcemetadata.getSection(AnimationMetadataSection.TYPE);
            FrameSize framesize;
            if (optional.isPresent()) {
                framesize = optional.get().calculateFrameSize(nativeimage.getWidth(), nativeimage.getHeight());
                if (!Mth.isMultipleOf(nativeimage.getWidth(), framesize.width()) || !Mth.isMultipleOf(nativeimage.getHeight(), framesize.height())) {
                    LOGGER.error(
                        "Image {} size {},{} is not multiple of frame size {},{}",
                        p_389362_,
                        nativeimage.getWidth(),
                        nativeimage.getHeight(),
                        framesize.width(),
                        framesize.height()
                    );
                    nativeimage.close();
                    return null;
                }
            } else {
                framesize = new FrameSize(nativeimage.getWidth(), nativeimage.getHeight());
            }

            return new SpriteContents(p_389362_, framesize, nativeimage, resourcemetadata);
        };
    }

    @Nullable
    SpriteContents loadSprite(ResourceLocation p_295581_, Resource p_294329_);
}
