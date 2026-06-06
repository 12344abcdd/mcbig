package net.minecraft.client.resources.model;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.DataResult.Error;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientItemInfoLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter LISTER = FileToIdConverter.json("items");

    public static CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> scheduleLoad(ResourceManager p_390398_, Executor p_390441_) {
        return CompletableFuture.<Map<ResourceLocation, Resource>>supplyAsync(() -> LISTER.listMatchingResources(p_390398_), p_390441_)
            .thenCompose(
                p_390384_ -> {
                    List<CompletableFuture<ClientItemInfoLoader.PendingLoad>> list = new ArrayList<>(p_390384_.size());
                    p_390384_.forEach(
                        (p_390495_, p_390374_) -> list.add(
                                CompletableFuture.supplyAsync(
                                    () -> {
                                        ResourceLocation resourcelocation = LISTER.fileToId(p_390495_);

                                        try {
                                            ClientItemInfoLoader.PendingLoad clientiteminfoloader$pendingload;
                                            try (Reader reader = p_390374_.openAsReader()) {
                                                ClientItem clientitem = ClientItem.CODEC
                                                    .parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                                                    .ifError(
                                                        p_390349_ -> LOGGER.error(
                                                                "Couldn't parse item model '{}' from pack '{}': {}",
                                                                resourcelocation,
                                                                p_390374_.sourcePackId(),
                                                                p_390349_.message()
                                                            )
                                                    )
                                                    .result()
                                                    .orElse(null);
                                                clientiteminfoloader$pendingload = new ClientItemInfoLoader.PendingLoad(resourcelocation, clientitem);
                                            }

                                            return clientiteminfoloader$pendingload;
                                        } catch (Exception exception) {
                                            LOGGER.error("Failed to open item model {} from pack '{}'", p_390495_, p_390374_.sourcePackId(), exception);
                                            return new ClientItemInfoLoader.PendingLoad(resourcelocation, null);
                                        }
                                    },
                                    p_390441_
                                )
                            )
                    );
                    return Util.sequence(list).thenApply(p_390406_ -> {
                        Map<ResourceLocation, ClientItem> map = new HashMap<>();

                        for (ClientItemInfoLoader.PendingLoad clientiteminfoloader$pendingload : p_390406_) {
                            if (clientiteminfoloader$pendingload.clientItemInfo != null) {
                                map.put(clientiteminfoloader$pendingload.id, clientiteminfoloader$pendingload.clientItemInfo);
                            }
                        }

                        return new ClientItemInfoLoader.LoadedClientInfos(map);
                    });
                }
            );
    }

    @OnlyIn(Dist.CLIENT)
    public static record LoadedClientInfos(Map<ResourceLocation, ClientItem> contents) {
    }

    @OnlyIn(Dist.CLIENT)
    static record PendingLoad(ResourceLocation id, @Nullable ClientItem clientItemInfo) {
    }
}
