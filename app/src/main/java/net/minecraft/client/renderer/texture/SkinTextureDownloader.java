package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SkinTextureDownloader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SKIN_WIDTH = 64;
    private static final int SKIN_HEIGHT = 64;
    private static final int LEGACY_SKIN_HEIGHT = 32;

    public static CompletableFuture<ResourceLocation> downloadAndRegisterSkin(ResourceLocation p_389586_, Path p_389600_, String p_389645_, boolean p_389477_) {
        return CompletableFuture.<NativeImage>supplyAsync(() -> {
            NativeImage nativeimage;
            try {
                nativeimage = downloadSkin(p_389600_, p_389645_);
            } catch (IOException ioexception) {
                throw new UncheckedIOException(ioexception);
            }

            return p_389477_ ? processLegacySkin(nativeimage, p_389645_) : nativeimage;
        }, Util.nonCriticalIoPool().forName("downloadTexture")).thenCompose(p_389457_ -> registerTextureInManager(p_389586_, p_389457_));
    }

    private static NativeImage downloadSkin(Path p_389493_, String p_389422_) throws IOException {
        if (Files.isRegularFile(p_389493_)) {
            LOGGER.debug("Loading HTTP texture from local cache ({})", p_389493_);

            NativeImage nativeimage1;
            try (InputStream inputstream = Files.newInputStream(p_389493_)) {
                nativeimage1 = NativeImage.read(inputstream);
            }

            return nativeimage1;
        } else {
            HttpURLConnection httpurlconnection = null;
            LOGGER.debug("Downloading HTTP texture from {} to {}", p_389422_, p_389493_);
            URI uri = URI.create(p_389422_);

            NativeImage $$7;
            try {
                httpurlconnection = (HttpURLConnection)uri.toURL().openConnection(Minecraft.getInstance().getProxy());
                httpurlconnection.setDoInput(true);
                httpurlconnection.setDoOutput(false);
                httpurlconnection.connect();
                int i = httpurlconnection.getResponseCode();
                if (i / 100 != 2) {
                    throw new IOException("Failed to open " + uri + ", HTTP error code: " + i);
                }

                byte[] abyte = httpurlconnection.getInputStream().readAllBytes();

                try {
                    FileUtil.createDirectoriesSafe(p_389493_.getParent());
                    Files.write(p_389493_, abyte);
                } catch (IOException ioexception) {
                    LOGGER.warn("Failed to cache texture {} in {}", p_389422_, p_389493_);
                }

                $$7 = NativeImage.read(abyte);
            } finally {
                if (httpurlconnection != null) {
                    httpurlconnection.disconnect();
                }
            }

            return $$7;
        }
    }

    private static CompletableFuture<ResourceLocation> registerTextureInManager(ResourceLocation p_389574_, NativeImage p_389628_) {
        Minecraft minecraft = Minecraft.getInstance();
        return CompletableFuture.supplyAsync(() -> {
            minecraft.getTextureManager().register(p_389574_, new DynamicTexture(p_389628_));
            return p_389574_;
        }, minecraft);
    }

    private static NativeImage processLegacySkin(NativeImage p_389593_, String p_389622_) {
        int i = p_389593_.getHeight();
        int j = p_389593_.getWidth();
        if (j == 64 && (i == 32 || i == 64)) {
            boolean flag = i == 32;
            if (flag) {
                NativeImage nativeimage = new NativeImage(64, 64, true);
                nativeimage.copyFrom(p_389593_);
                p_389593_.close();
                p_389593_ = nativeimage;
                nativeimage.fillRect(0, 32, 64, 32, 0);
                nativeimage.copyRect(4, 16, 16, 32, 4, 4, true, false);
                nativeimage.copyRect(8, 16, 16, 32, 4, 4, true, false);
                nativeimage.copyRect(0, 20, 24, 32, 4, 12, true, false);
                nativeimage.copyRect(4, 20, 16, 32, 4, 12, true, false);
                nativeimage.copyRect(8, 20, 8, 32, 4, 12, true, false);
                nativeimage.copyRect(12, 20, 16, 32, 4, 12, true, false);
                nativeimage.copyRect(44, 16, -8, 32, 4, 4, true, false);
                nativeimage.copyRect(48, 16, -8, 32, 4, 4, true, false);
                nativeimage.copyRect(40, 20, 0, 32, 4, 12, true, false);
                nativeimage.copyRect(44, 20, -8, 32, 4, 12, true, false);
                nativeimage.copyRect(48, 20, -16, 32, 4, 12, true, false);
                nativeimage.copyRect(52, 20, -8, 32, 4, 12, true, false);
            }

            setNoAlpha(p_389593_, 0, 0, 32, 16);
            if (flag) {
                doNotchTransparencyHack(p_389593_, 32, 0, 64, 32);
            }

            setNoAlpha(p_389593_, 0, 16, 64, 32);
            setNoAlpha(p_389593_, 16, 48, 48, 64);
            return p_389593_;
        } else {
            p_389593_.close();
            throw new IllegalStateException("Discarding incorrectly sized (" + j + "x" + i + ") skin texture from " + p_389622_);
        }
    }

    private static void doNotchTransparencyHack(NativeImage p_389443_, int p_389442_, int p_389419_, int p_389424_, int p_389559_) {
        for (int i = p_389442_; i < p_389424_; i++) {
            for (int j = p_389419_; j < p_389559_; j++) {
                int k = p_389443_.getPixel(i, j);
                if (ARGB.alpha(k) < 128) {
                    return;
                }
            }
        }

        for (int l = p_389442_; l < p_389424_; l++) {
            for (int i1 = p_389419_; i1 < p_389559_; i1++) {
                p_389443_.setPixel(l, i1, p_389443_.getPixel(l, i1) & 16777215);
            }
        }
    }

    private static void setNoAlpha(NativeImage p_389456_, int p_389475_, int p_389579_, int p_389725_, int p_389657_) {
        for (int i = p_389475_; i < p_389725_; i++) {
            for (int j = p_389579_; j < p_389657_; j++) {
                p_389456_.setPixel(i, j, ARGB.opaque(p_389456_.getPixel(i, j)));
            }
        }
    }
}
