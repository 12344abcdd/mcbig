package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetChunkCacheCenterPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetChunkCacheCenterPacket> STREAM_CODEC = Packet.codec(
        ClientboundSetChunkCacheCenterPacket::write, ClientboundSetChunkCacheCenterPacket::new
    );
    private final me.alphamode.mcbig.math.BigInteger x;
    private final me.alphamode.mcbig.math.BigInteger z;

    public ClientboundSetChunkCacheCenterPacket(me.alphamode.mcbig.math.BigInteger p_133086_, me.alphamode.mcbig.math.BigInteger p_133087_) {
        this.x = p_133086_;
        this.z = p_133087_;
    }

    private ClientboundSetChunkCacheCenterPacket(FriendlyByteBuf p_179282_) {
        this.x = p_179282_.readBigInteger();
        this.z = p_179282_.readBigInteger();
    }

    private void write(FriendlyByteBuf p_133096_) {
        p_133096_.writeBigInteger(this.x);
        p_133096_.writeBigInteger(this.z);
    }

    @Override
    public PacketType<ClientboundSetChunkCacheCenterPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_CENTER;
    }

    public void handle(ClientGamePacketListener p_133093_) {
        p_133093_.handleSetChunkCacheCenter(this);
    }

    public me.alphamode.mcbig.math.BigInteger getX() {
        return this.x;
    }

    public me.alphamode.mcbig.math.BigInteger getZ() {
        return this.z;
    }
}
