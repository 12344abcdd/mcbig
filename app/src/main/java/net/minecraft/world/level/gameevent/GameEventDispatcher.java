package net.minecraft.world.level.gameevent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;

public class GameEventDispatcher {
    private final ServerLevel level;

    public GameEventDispatcher(ServerLevel p_251921_) {
        this.level = p_251921_;
    }

    public void post(Holder<GameEvent> p_316151_, me.alphamode.mcbig.core.BigVec3 p_250613_, GameEvent.Context p_251777_) {
        me.alphamode.mcbig.math.BigInteger i = me.alphamode.mcbig.math.BigInteger.constant(p_316151_.value().notificationRadius());
        BlockPos blockpos = BlockPos.containing(p_250613_);
        me.alphamode.mcbig.math.BigInteger j = SectionPos.blockToSectionCoord(blockpos.getBigX().subtract(i));
        me.alphamode.mcbig.math.BigInteger k = SectionPos.blockToSectionCoord(blockpos.getBigY().subtract(i));
        me.alphamode.mcbig.math.BigInteger l = SectionPos.blockToSectionCoord(blockpos.getBigZ().subtract(i));
        me.alphamode.mcbig.math.BigInteger i1 = SectionPos.blockToSectionCoord(blockpos.getBigX().add(i));
        me.alphamode.mcbig.math.BigInteger j1 = SectionPos.blockToSectionCoord(blockpos.getBigY().add(i));
        me.alphamode.mcbig.math.BigInteger k1 = SectionPos.blockToSectionCoord(blockpos.getBigZ().add(i));
        List<GameEvent.ListenerInfo> list = new ArrayList<>();
        GameEventListenerRegistry.ListenerVisitor gameeventlistenerregistry$listenervisitor = (p_316091_, p_316092_) -> {
            if (p_316091_.getDeliveryMode() == GameEventListener.DeliveryMode.BY_DISTANCE) {
                list.add(new GameEvent.ListenerInfo(p_316151_, p_250613_, p_251777_, p_316091_, p_316092_));
            } else {
                p_316091_.handleGameEvent(this.level, p_316151_, p_251777_, p_250613_);
            }
        };
        boolean flag = false;

        for (me.alphamode.mcbig.math.BigInteger l1 = j; l1.compareTo(i1) <= 0; l1 = l1.add()) {
            for (me.alphamode.mcbig.math.BigInteger i2 = l; i2.compareTo(k1) <= 0; i2 = i2.add()) {
                ChunkAccess chunkaccess = this.level.getChunkSource().getChunkNow(l1, i2);
                if (chunkaccess != null) {
                    for (me.alphamode.mcbig.math.BigInteger j2 = k; j2.compareTo(j1) <= 0; j2 = j2.add()) {
                        flag |= chunkaccess.getListenerRegistry(j2.intValue())
                                .visitInRangeListeners(p_316151_, p_250613_, p_251777_, gameeventlistenerregistry$listenervisitor);
                    }
                }
            }
        }

        if (!list.isEmpty()) {
            this.handleGameEventMessagesInQueue(list);
        }

        if (flag) {
            DebugPackets.sendGameEventInfo(this.level, p_316151_, p_250613_);
        }
    }

    private void handleGameEventMessagesInQueue(List<GameEvent.ListenerInfo> p_251433_) {
        Collections.sort(p_251433_);

        for (GameEvent.ListenerInfo gameevent$listenerinfo : p_251433_) {
            GameEventListener gameeventlistener = gameevent$listenerinfo.recipient();
            gameeventlistener.handleGameEvent(this.level, gameevent$listenerinfo.gameEvent(), gameevent$listenerinfo.context(), gameevent$listenerinfo.source());
        }
    }
}
