package com.mojang.realmsclient.client.worldupload;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.util.task.RealmCreationTask;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsCreateWorldFlow {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void createWorld(
        Minecraft p_374166_, Screen p_374334_, Screen p_374049_, int p_376611_, RealmsServer p_374154_, @Nullable RealmCreationTask p_374383_
    ) {
        CreateWorldScreen.openFresh(
            p_374166_,
            p_374334_,
            (p_375396_, p_375397_, p_375398_, p_375399_) -> {
                Path path;
                try {
                    path = createTemporaryWorldFolder(p_375397_, p_375398_, p_375399_);
                } catch (IOException ioexception) {
                    LOGGER.warn("Failed to create temporary world folder.");
                    p_374166_.setScreen(new RealmsGenericErrorScreen(Component.translatable("mco.create.world.failed"), p_374049_));
                    return true;
                }

                RealmsWorldOptions realmsworldoptions = RealmsWorldOptions.createFromSettings(
                    p_375398_.getLevelSettings(), SharedConstants.getCurrentVersion().getName()
                );
                RealmsWorldUpload realmsworldupload = new RealmsWorldUpload(
                    path, realmsworldoptions, p_374166_.getUser(), p_374154_.id, p_376611_, RealmsWorldUploadStatusTracker.noOp()
                );
                p_374166_.forceSetScreen(
                    new AlertScreen(
                        realmsworldupload::cancel,
                        Component.translatable("mco.create.world.reset.title"),
                        Component.empty(),
                        CommonComponents.GUI_CANCEL,
                        false
                    )
                );
                if (p_374383_ != null) {
                    p_374383_.run();
                }

                realmsworldupload.packAndUpload().handleAsync((p_378750_, p_378751_) -> {
                    if (p_378751_ != null) {
                        if (p_378751_ instanceof CompletionException completionexception) {
                            p_378751_ = completionexception.getCause();
                        }

                        if (p_378751_ instanceof RealmsUploadCanceledException) {
                            p_374166_.forceSetScreen(p_374049_);
                        } else {
                            if (p_378751_ instanceof RealmsUploadFailedException realmsuploadfailedexception) {
                                LOGGER.warn("Failed to create realms world {}", realmsuploadfailedexception.getStatusMessage());
                            } else {
                                LOGGER.warn("Failed to create realms world {}", p_378751_.getMessage());
                            }

                            p_374166_.forceSetScreen(new RealmsGenericErrorScreen(Component.translatable("mco.create.world.failed"), p_374049_));
                        }
                    } else {
                        if (p_374334_ instanceof RealmsConfigureWorldScreen realmsconfigureworldscreen) {
                            realmsconfigureworldscreen.fetchServerData(p_374154_.id);
                        }

                        if (p_374383_ != null) {
                            RealmsMainScreen.play(p_374154_, p_374334_, true);
                        } else {
                            p_374166_.forceSetScreen(p_374334_);
                        }

                        RealmsMainScreen.refreshServerList();
                    }

                    return null;
                }, p_374166_);
                return true;
            }
        );
    }

    private static Path createTemporaryWorldFolder(LayeredRegistryAccess<RegistryLayer> p_374264_, PrimaryLevelData p_374355_, @Nullable Path p_374356_) throws IOException {
        Path path = Files.createTempDirectory("minecraft_realms_world_upload");
        if (p_374356_ != null) {
            Files.move(p_374356_, path.resolve("datapacks"));
        }

        CompoundTag compoundtag = p_374355_.createTag(p_374264_.compositeAccess(), null);
        CompoundTag compoundtag1 = new CompoundTag();
        compoundtag1.put("Data", compoundtag);
        Path path1 = Files.createFile(path.resolve("level.dat"));
        NbtIo.writeCompressed(compoundtag1, path1);
        return path;
    }
}
