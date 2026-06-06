package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.screens.AddRealmPopupScreen;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureManager implements PreparableReloadListener, Tickable, AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = ResourceLocation.withDefaultNamespace("");
    private final Map<ResourceLocation, AbstractTexture> byPath = new HashMap<>();
    private final Set<Tickable> tickableTextures = new HashSet<>();
    private final ResourceManager resourceManager;

    public TextureManager(ResourceManager p_118474_) {
        this.resourceManager = p_118474_;
        NativeImage nativeimage = MissingTextureAtlasSprite.generateMissingImage();
        this.register(MissingTextureAtlasSprite.getLocation(), new DynamicTexture(nativeimage));
    }

    public void registerAndLoad(ResourceLocation p_389410_, ReloadableTexture p_389421_) {
        try {
            p_389421_.apply(this.loadContentsSafe(p_389410_, p_389421_));
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Uploading texture");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Uploaded texture");
            crashreportcategory.setDetail("Resource location", p_389421_.resourceId());
            crashreportcategory.setDetail("Texture id", p_389410_);
            throw new ReportedException(crashreport);
        }

        this.register(p_389410_, p_389421_);
    }

    private TextureContents loadContentsSafe(ResourceLocation p_390395_, ReloadableTexture p_390396_) {
        try {
            return loadContents(this.resourceManager, p_390395_, p_390396_);
        } catch (Exception exception) {
            LOGGER.error("Failed to load texture {} into slot {}", p_390396_.resourceId(), p_390395_, exception);
            return TextureContents.createMissing();
        }
    }

    public void registerForNextReload(ResourceLocation p_389634_) {
        this.register(p_389634_, new SimpleTexture(p_389634_));
    }

    public void register(ResourceLocation p_118496_, AbstractTexture p_118497_) {
        AbstractTexture abstracttexture = this.byPath.put(p_118496_, p_118497_);
        if (abstracttexture != p_118497_) {
            if (abstracttexture != null) {
                this.safeClose(p_118496_, abstracttexture);
            }

            if (p_118497_ instanceof Tickable tickable) {
                this.tickableTextures.add(tickable);
            }
        }
    }

    private void safeClose(ResourceLocation p_118509_, AbstractTexture p_118510_) {
        this.tickableTextures.remove(p_118510_);

        try {
            p_118510_.close();
        } catch (Exception exception) {
            LOGGER.warn("Failed to close texture {}", p_118509_, exception);
        }

        p_118510_.releaseId();
    }

    public AbstractTexture getTexture(ResourceLocation p_118507_) {
        AbstractTexture abstracttexture = this.byPath.get(p_118507_);
        if (abstracttexture != null) {
            return abstracttexture;
        } else {
            SimpleTexture simpletexture = new SimpleTexture(p_118507_);
            this.registerAndLoad(p_118507_, simpletexture);
            return simpletexture;
        }
    }

    @Override
    public void tick() {
        for (Tickable tickable : this.tickableTextures) {
            tickable.tick();
        }
    }

    public void release(ResourceLocation p_118514_) {
        AbstractTexture abstracttexture = this.byPath.remove(p_118514_);
        if (abstracttexture != null) {
            this.safeClose(p_118514_, abstracttexture);
        }
    }

    @Override
    public void close() {
        this.byPath.forEach(this::safeClose);
        this.byPath.clear();
        this.tickableTextures.clear();
    }

    @Override
    public CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier p_118476_, ResourceManager p_118477_, Executor p_118480_, Executor p_118481_
    ) {
        List<TextureManager.PendingReload> list = new ArrayList<>();
        this.byPath.forEach((p_389356_, p_389357_) -> {
            if (p_389357_ instanceof ReloadableTexture reloadabletexture) {
                list.add(scheduleLoad(p_118477_, p_389356_, reloadabletexture, p_118480_));
            }
        });
        return CompletableFuture.allOf(list.stream().map(TextureManager.PendingReload::newContents).toArray(CompletableFuture[]::new))
            .thenCompose(p_118476_::wait)
            .thenAcceptAsync(p_389351_ -> {
                AddRealmPopupScreen.updateCarouselImages(this.resourceManager);

                for (TextureManager.PendingReload texturemanager$pendingreload : list) {
                    texturemanager$pendingreload.texture.apply(texturemanager$pendingreload.newContents.join());
                }
            }, p_118481_);
    }

    public void dumpAllSheets(Path p_276129_) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._dumpAllSheets(p_276129_));
        } else {
            this._dumpAllSheets(p_276129_);
        }
    }

    private void _dumpAllSheets(Path p_276128_) {
        try {
            Files.createDirectories(p_276128_);
        } catch (IOException ioexception) {
            LOGGER.error("Failed to create directory {}", p_276128_, ioexception);
            return;
        }

        this.byPath.forEach((p_276101_, p_276102_) -> {
            if (p_276102_ instanceof Dumpable dumpable) {
                try {
                    dumpable.dumpContents(p_276101_, p_276128_);
                } catch (IOException ioexception1) {
                    LOGGER.error("Failed to dump texture {}", p_276101_, ioexception1);
                }
            }
        });
    }

    private static TextureContents loadContents(ResourceManager p_389428_, ResourceLocation p_389405_, ReloadableTexture p_389480_) throws IOException {
        try {
            return p_389480_.loadContents(p_389428_);
        } catch (FileNotFoundException filenotfoundexception) {
            if (p_389405_ != INTENTIONAL_MISSING_TEXTURE) {
                LOGGER.warn("Missing resource {} referenced from {}", p_389480_.resourceId(), p_389405_);
            }

            return TextureContents.createMissing();
        }
    }

    private static TextureManager.PendingReload scheduleLoad(
        ResourceManager p_389696_, ResourceLocation p_389555_, ReloadableTexture p_389691_, Executor p_389430_
    ) {
        return new TextureManager.PendingReload(p_389691_, CompletableFuture.supplyAsync(() -> {
            try {
                return loadContents(p_389696_, p_389555_, p_389691_);
            } catch (IOException ioexception) {
                throw new UncheckedIOException(ioexception);
            }
        }, p_389430_));
    }

    @OnlyIn(Dist.CLIENT)
    static record PendingReload(ReloadableTexture texture, CompletableFuture<TextureContents> newContents) {
    }
}
