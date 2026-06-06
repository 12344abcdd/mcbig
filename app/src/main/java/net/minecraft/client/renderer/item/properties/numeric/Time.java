package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Time extends NeedleDirectionHelper implements RangeSelectItemModelProperty {
    public static final MapCodec<Time> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_390087_ -> p_390087_.group(
                    Codec.BOOL.optionalFieldOf("wobble", Boolean.valueOf(true)).forGetter(NeedleDirectionHelper::wobble),
                    Time.TimeSource.CODEC.fieldOf("source").forGetter(p_390088_ -> p_390088_.source)
                )
                .apply(p_390087_, Time::new)
    );
    private final Time.TimeSource source;
    private final RandomSource randomSource = RandomSource.create();
    private final NeedleDirectionHelper.Wobbler wobbler;

    public Time(boolean p_387033_, Time.TimeSource p_390432_) {
        super(p_387033_);
        this.source = p_390432_;
        this.wobbler = this.newWobbler(0.9F);
    }

    @Override
    protected float calculate(ItemStack p_387493_, ClientLevel p_387362_, int p_388783_, Entity p_388056_) {
        float f = this.source.get(p_387362_, p_387493_, p_388056_, this.randomSource);
        long i = p_387362_.getGameTime();
        if (this.wobbler.shouldUpdate(i)) {
            this.wobbler.update(i, f);
        }

        return this.wobbler.rotation();
    }

    @Override
    public MapCodec<Time> type() {
        return MAP_CODEC;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum TimeSource implements StringRepresentable {
        RANDOM("random") {
            @Override
            public float get(ClientLevel p_390411_, ItemStack p_390382_, Entity p_390443_, RandomSource p_390409_) {
                return p_390409_.nextFloat();
            }
        },
        DAYTIME("daytime") {
            @Override
            public float get(ClientLevel p_390440_, ItemStack p_390494_, Entity p_390360_, RandomSource p_390488_) {
                return p_390440_.getTimeOfDay(1.0F);
            }
        },
        MOON_PHASE("moon_phase") {
            @Override
            public float get(ClientLevel p_390465_, ItemStack p_390476_, Entity p_390363_, RandomSource p_390375_) {
                return (float)p_390465_.getMoonPhase() / 8.0F;
            }
        };

        public static final Codec<Time.TimeSource> CODEC = StringRepresentable.fromEnum(Time.TimeSource::values);
        private final String name;

        TimeSource(String p_390521_) {
            this.name = p_390521_;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        abstract float get(ClientLevel p_390505_, ItemStack p_390478_, Entity p_390420_, RandomSource p_390390_);
    }
}
