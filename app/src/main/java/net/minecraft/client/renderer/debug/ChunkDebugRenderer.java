package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private final int radius = 12;
    @Nullable
    private ChunkDebugRenderer.ChunkData data;

    public ChunkDebugRenderer(Minecraft p_113368_) {
        this.minecraft = p_113368_;
    }

    @Override
    public void render(PoseStack p_113370_, MultiBufferSource p_113371_, double p_113372_, double p_113373_, double p_113374_) {
        double d0 = (double)Util.getNanos();
        if (d0 - this.lastUpdateTime > 3.0E9) {
            this.lastUpdateTime = d0;
            IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
            if (integratedserver != null) {
                this.data = new ChunkDebugRenderer.ChunkData(integratedserver, p_113372_, p_113374_);
            } else {
                this.data = null;
            }
        }

        if (this.data != null) {
            Map<ChunkPos, String> map = this.data.serverData.getNow(null);
            double d1 = this.minecraft.gameRenderer.getMainCamera().getPosition().y * 0.85;

            for (Entry<ChunkPos, String> entry : this.data.clientData.entrySet()) {
                ChunkPos chunkpos = entry.getKey();
                String s = entry.getValue();
                if (map != null) {
                    s = s + map.get(chunkpos);
                }

                String[] astring = s.split("\n");
                int i = 0;

                for (String s1 : astring) {
                    DebugRenderer.renderFloatingText(
                        p_113370_,
                        p_113371_,
                        s1,
                        (double)SectionPos.sectionToBlockCoord(chunkpos.x(), 8).intValue(),
                        d1 + (double)i,
                        (double)SectionPos.sectionToBlockCoord(chunkpos.z(), 8).intValue(),
                        -1,
                        0.15F,
                        true,
                        0.0F,
                        true
                    );
                    i -= 2;
                }
            }
        }
    }

    final class ChunkData {
        final Map<ChunkPos, String> clientData;
        final CompletableFuture<Map<ChunkPos, String>> serverData;

        ChunkData(IntegratedServer p_113382_, double p_113383_, double p_113384_) {
            ClientLevel clientlevel = ChunkDebugRenderer.this.minecraft.level;
            ResourceKey<Level> resourcekey = clientlevel.dimension();
            me.alphamode.mcbig.math.BigInteger i = SectionPos.posToSectionCoord(p_113383_);
            me.alphamode.mcbig.math.BigInteger j = SectionPos.posToSectionCoord(p_113384_);
            Builder<ChunkPos, String> builder = ImmutableMap.builder();
            ClientChunkCache clientchunkcache = clientlevel.getChunkSource();

            for(me.alphamode.mcbig.math.BigInteger k = i.subtract(me.alphamode.mcbig.core.BigConstants.Ints.TWELVE); k.compareTo(i.add(me.alphamode.mcbig.core.BigConstants.Ints.TWELVE)) <= 0; k = k.add()) {
                for(me.alphamode.mcbig.math.BigInteger l = j.subtract(me.alphamode.mcbig.core.BigConstants.Ints.TWELVE); l.compareTo(j.add(me.alphamode.mcbig.core.BigConstants.Ints.TWELVE)) <= 0; l = l.add()) {
                    ChunkPos chunkpos = new ChunkPos(k, l);
                    String s = "";
                    LevelChunk levelchunk = clientchunkcache.getChunk(k, l, false);
                    s = s + "Client: ";
                    if (levelchunk == null) {
                        s = s + "0n/a\n";
                    } else {
                        s = s + (levelchunk.isEmpty() ? " E" : "");
                        s = s + "\n";
                    }

                    builder.put(chunkpos, s);
                }
            }

            this.clientData = builder.build();
            this.serverData = p_113382_.submit(() -> {
                ServerLevel serverlevel = p_113382_.getLevel(resourcekey);
                if (serverlevel == null) {
                    return ImmutableMap.of();
                } else {
                    Builder<ChunkPos, String> builder1 = ImmutableMap.builder();
                    ServerChunkCache serverchunkcache = serverlevel.getChunkSource();

                    for(me.alphamode.mcbig.math.BigInteger i1 = i.subtract(me.alphamode.mcbig.core.BigConstants.Ints.TWELVE); i1.compareTo(i.add(me.alphamode.mcbig.core.BigConstants.Ints.TWELVE)) <= 0; i1 = i1.add()) {
                        for(me.alphamode.mcbig.math.BigInteger j1 = j.subtract(me.alphamode.mcbig.core.BigConstants.Ints.TWELVE); j1.compareTo(j.add(me.alphamode.mcbig.core.BigConstants.Ints.TWELVE)) <= 0; j1 = j1.add()) {
                            ChunkPos chunkpos1 = new ChunkPos(i1, j1);
                            builder1.put(chunkpos1, "Server: " + serverchunkcache.getChunkDebugData(chunkpos1));
                        }
                    }

                    return builder1.build();
                }
            });
        }
    }
}
