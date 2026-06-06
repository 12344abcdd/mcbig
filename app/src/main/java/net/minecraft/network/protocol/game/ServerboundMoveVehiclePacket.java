package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record ServerboundMoveVehiclePacket(Vec3 position, float yRot, float xRot, boolean onGround) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundMoveVehiclePacket> STREAM_CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC,
        ServerboundMoveVehiclePacket::position,
        ByteBufCodecs.FLOAT,
        ServerboundMoveVehiclePacket::yRot,
        ByteBufCodecs.FLOAT,
        ServerboundMoveVehiclePacket::xRot,
        ByteBufCodecs.BOOL,
        ServerboundMoveVehiclePacket::onGround,
        ServerboundMoveVehiclePacket::new
    );

    public static ServerboundMoveVehiclePacket fromEntity(Entity p_387268_) {
        return new ServerboundMoveVehiclePacket(
            new Vec3(p_387268_.lerpTargetX(), p_387268_.lerpTargetY(), p_387268_.lerpTargetZ()), p_387268_.getYRot(), p_387268_.getXRot(), p_387268_.onGround()
        );
    }

    @Override
    public PacketType<ServerboundMoveVehiclePacket> type() {
        return GamePacketTypes.SERVERBOUND_MOVE_VEHICLE;
    }

    public void handle(ServerGamePacketListener p_134198_) {
        p_134198_.handleMoveVehicle(this);
    }
}
