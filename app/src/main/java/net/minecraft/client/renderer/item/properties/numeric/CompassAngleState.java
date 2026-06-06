package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CompassAngleState extends NeedleDirectionHelper {
    public static final MapCodec<CompassAngleState> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_387422_ -> p_387422_.group(
                    Codec.BOOL.optionalFieldOf("wobble", Boolean.valueOf(true)).forGetter(NeedleDirectionHelper::wobble),
                    CompassAngleState.CompassTarget.CODEC.fieldOf("target").forGetter(CompassAngleState::target)
                )
                .apply(p_387422_, CompassAngleState::new)
    );
    private final NeedleDirectionHelper.Wobbler wobbler;
    private final NeedleDirectionHelper.Wobbler noTargetWobbler;
    private final CompassAngleState.CompassTarget compassTarget;
    private final RandomSource random = RandomSource.create();

    public CompassAngleState(boolean p_388844_, CompassAngleState.CompassTarget p_387613_) {
        super(p_388844_);
        this.wobbler = this.newWobbler(0.8F);
        this.noTargetWobbler = this.newWobbler(0.8F);
        this.compassTarget = p_387613_;
    }

    @Override
    protected float calculate(ItemStack p_388108_, ClientLevel p_387750_, int p_388073_, Entity p_388489_) {
        GlobalPos globalpos = this.compassTarget.get(p_387750_, p_388108_, p_388489_);
        long i = p_387750_.getGameTime();
        return !isValidCompassTargetPos(p_388489_, globalpos)
            ? this.getRandomlySpinningRotation(p_388073_, i)
            : this.getRotationTowardsCompassTarget(p_388489_, i, globalpos.pos());
    }

    private float getRandomlySpinningRotation(int p_388932_, long p_387198_) {
        if (this.noTargetWobbler.shouldUpdate(p_387198_)) {
            this.noTargetWobbler.update(p_387198_, this.random.nextFloat());
        }

        float f = this.noTargetWobbler.rotation() + (float)hash(p_388932_) / 2.1474836E9F;
        return Mth.positiveModulo(f, 1.0F);
    }

    private float getRotationTowardsCompassTarget(Entity p_387599_, long p_387654_, BlockPos p_388263_) {
        float f = (float)getAngleFromEntityToPos(p_387599_, p_388263_);
        float f1 = getWrappedVisualRotationY(p_387599_);
        if (p_387599_ instanceof Player player && player.isLocalPlayer() && player.level().tickRateManager().runsNormally()) {
            if (this.wobbler.shouldUpdate(p_387654_)) {
                this.wobbler.update(p_387654_, 0.5F - (f1 - 0.25F));
            }

            float f3 = f + this.wobbler.rotation();
            return Mth.positiveModulo(f3, 1.0F);
        }

        float f2 = 0.5F - (f1 - 0.25F - f);
        return Mth.positiveModulo(f2, 1.0F);
    }

    private static boolean isValidCompassTargetPos(Entity p_386563_, @Nullable GlobalPos p_387891_) {
        return p_387891_ != null
            && p_387891_.dimension() == p_386563_.level().dimension()
            && !(p_387891_.pos().distToCenterSqr(p_386563_.position()) < 1.0E-5F);
    }

    private static double getAngleFromEntityToPos(Entity p_388327_, BlockPos p_387426_) {
        Vec3 vec3 = Vec3.atCenterOf(p_387426_);
        return Math.atan2(vec3.z() - p_388327_.getZ(), vec3.x() - p_388327_.getX()) / (float) (Math.PI * 2);
    }

    private static float getWrappedVisualRotationY(Entity p_386969_) {
        return Mth.positiveModulo(p_386969_.getVisualRotationYInDegrees() / 360.0F, 1.0F);
    }

    private static int hash(int p_387430_) {
        return p_387430_ * 1327217883;
    }

    protected CompassAngleState.CompassTarget target() {
        return this.compassTarget;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum CompassTarget implements StringRepresentable {
        NONE("none") {
            @Nullable
            @Override
            public GlobalPos get(ClientLevel p_388090_, ItemStack p_388593_, Entity p_388853_) {
                return null;
            }
        },
        LODESTONE("lodestone") {
            @Nullable
            @Override
            public GlobalPos get(ClientLevel p_388590_, ItemStack p_387822_, Entity p_386963_) {
                LodestoneTracker lodestonetracker = p_387822_.get(DataComponents.LODESTONE_TRACKER);
                return lodestonetracker != null ? lodestonetracker.target().orElse(null) : null;
            }
        },
        SPAWN("spawn") {
            @Override
            public GlobalPos get(ClientLevel p_386460_, ItemStack p_387401_, Entity p_387630_) {
                return GlobalPos.of(p_386460_.dimension(), p_386460_.getSharedSpawnPos());
            }
        },
        RECOVERY("recovery") {
            @Nullable
            @Override
            public GlobalPos get(ClientLevel p_390456_, ItemStack p_390419_, Entity p_390489_) {
                return p_390489_ instanceof Player player ? player.getLastDeathLocation().orElse(null) : null;
            }
        };

        public static final Codec<CompassAngleState.CompassTarget> CODEC = StringRepresentable.fromEnum(CompassAngleState.CompassTarget::values);
        private final String name;

        CompassTarget(String p_388295_) {
            this.name = p_388295_;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        @Nullable
        abstract GlobalPos get(ClientLevel p_387000_, ItemStack p_387505_, Entity p_387524_);
    }
}
