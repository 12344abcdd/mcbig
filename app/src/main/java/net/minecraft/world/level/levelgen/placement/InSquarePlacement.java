package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class InSquarePlacement extends PlacementModifier {
    private static final InSquarePlacement INSTANCE = new InSquarePlacement();
    public static final MapCodec<InSquarePlacement> CODEC = MapCodec.unit(() -> INSTANCE);

    public static InSquarePlacement spread() {
        return INSTANCE;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext p_226348_, RandomSource p_226349_, BlockPos p_226350_) {
        me.alphamode.mcbig.math.BigInteger i = p_226350_.getBigX().add(p_226349_.nextInt(16));
        me.alphamode.mcbig.math.BigInteger j = p_226350_.getBigZ().add(p_226349_.nextInt(16));
        return Stream.of(new BlockPos(i, p_226350_.getY(), j));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.IN_SQUARE;
    }
}
