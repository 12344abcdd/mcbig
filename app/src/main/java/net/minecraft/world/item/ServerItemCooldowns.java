package net.minecraft.world.item;

import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ServerItemCooldowns extends ItemCooldowns {
    private final ServerPlayer player;

    public ServerItemCooldowns(ServerPlayer p_43067_) {
        this.player = p_43067_;
    }

    @Override
    protected void onCooldownStarted(ResourceLocation p_366780_, int p_43070_) {
        super.onCooldownStarted(p_366780_, p_43070_);
        this.player.connection.send(new ClientboundCooldownPacket(p_366780_, p_43070_));
    }

    @Override
    protected void onCooldownEnded(ResourceLocation p_366791_) {
        super.onCooldownEnded(p_366791_);
        this.player.connection.send(new ClientboundCooldownPacket(p_366791_, 0));
    }
}
