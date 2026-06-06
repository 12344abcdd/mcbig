package net.minecraft.world.entity.vehicle;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NewMinecartBehavior extends MinecartBehavior {
    public static final int POS_ROT_LERP_TICKS = 3;
    public static final double ON_RAIL_Y_OFFSET = 0.1;
    public static final double OPPOSING_SLOPES_REST_AT_SPEED_THRESHOLD = 0.005;
    @Nullable
    private NewMinecartBehavior.StepPartialTicks cacheIndexAlpha;
    private int cachedLerpDelay;
    private float cachedPartialTick;
    private int lerpDelay = 0;
    public final List<NewMinecartBehavior.MinecartStep> lerpSteps = new LinkedList<>();
    public final List<NewMinecartBehavior.MinecartStep> currentLerpSteps = new LinkedList<>();
    public double currentLerpStepsTotalWeight = 0.0;
    public NewMinecartBehavior.MinecartStep oldLerp = NewMinecartBehavior.MinecartStep.ZERO;

    public NewMinecartBehavior(AbstractMinecart p_360977_) {
        super(p_360977_);
    }

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel serverlevel) {
            BlockPos blockpos = this.minecart.getCurrentBlockPosOrRailBelow();
            BlockState $$4 = this.level().getBlockState(blockpos);
            if (this.minecart.isFirstTick()) {
                this.minecart.setOnRails(BaseRailBlock.isRail($$4));
                this.adjustToRails(blockpos, $$4, true);
            }

            this.minecart.applyGravity();
            this.minecart.moveAlongTrack(serverlevel);
        } else {
            this.lerpClientPositionAndRotation();
            boolean flag = BaseRailBlock.isRail(this.level().getBlockState(this.minecart.getCurrentBlockPosOrRailBelow()));
            this.minecart.setOnRails(flag);
        }
    }

    private void lerpClientPositionAndRotation() {
        if (--this.lerpDelay <= 0) {
            this.setOldLerpValues();
            this.currentLerpSteps.clear();
            if (!this.lerpSteps.isEmpty()) {
                this.currentLerpSteps.addAll(this.lerpSteps);
                this.lerpSteps.clear();
                this.currentLerpStepsTotalWeight = 0.0;

                for (NewMinecartBehavior.MinecartStep newminecartbehavior$minecartstep : this.currentLerpSteps) {
                    this.currentLerpStepsTotalWeight = this.currentLerpStepsTotalWeight + (double)newminecartbehavior$minecartstep.weight;
                }

                this.lerpDelay = this.currentLerpStepsTotalWeight == 0.0 ? 0 : 3;
            }
        }

        if (this.cartHasPosRotLerp()) {
            this.setPos(this.getCartLerpPosition(1.0F));
            this.setDeltaMovement(this.getCartLerpMovements(1.0F));
            this.setXRot(this.getCartLerpXRot(1.0F));
            this.setYRot(this.getCartLerpYRot(1.0F));
        }
    }

    public void setOldLerpValues() {
        this.oldLerp = new NewMinecartBehavior.MinecartStep(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), 0.0F);
    }

    public boolean cartHasPosRotLerp() {
        return !this.currentLerpSteps.isEmpty();
    }

    public float getCartLerpXRot(float p_364019_) {
        NewMinecartBehavior.StepPartialTicks newminecartbehavior$steppartialticks = this.getCurrentLerpStep(p_364019_);
        return Mth.rotLerp(
            newminecartbehavior$steppartialticks.partialTicksInStep,
            newminecartbehavior$steppartialticks.previousStep.xRot,
            newminecartbehavior$steppartialticks.currentStep.xRot
        );
    }

    public float getCartLerpYRot(float p_362660_) {
        NewMinecartBehavior.StepPartialTicks newminecartbehavior$steppartialticks = this.getCurrentLerpStep(p_362660_);
        return Mth.rotLerp(
            newminecartbehavior$steppartialticks.partialTicksInStep,
            newminecartbehavior$steppartialticks.previousStep.yRot,
            newminecartbehavior$steppartialticks.currentStep.yRot
        );
    }

    public Vec3 getCartLerpPosition(float p_363111_) {
        NewMinecartBehavior.StepPartialTicks newminecartbehavior$steppartialticks = this.getCurrentLerpStep(p_363111_);
        return Mth.lerp(
            (double)newminecartbehavior$steppartialticks.partialTicksInStep,
            newminecartbehavior$steppartialticks.previousStep.position,
            newminecartbehavior$steppartialticks.currentStep.position
        );
    }

    public Vec3 getCartLerpMovements(float p_365356_) {
        NewMinecartBehavior.StepPartialTicks newminecartbehavior$steppartialticks = this.getCurrentLerpStep(p_365356_);
        return Mth.lerp(
            (double)newminecartbehavior$steppartialticks.partialTicksInStep,
            newminecartbehavior$steppartialticks.previousStep.movement,
            newminecartbehavior$steppartialticks.currentStep.movement
        );
    }

    private NewMinecartBehavior.StepPartialTicks getCurrentLerpStep(float p_364383_) {
        if (p_364383_ == this.cachedPartialTick && this.lerpDelay == this.cachedLerpDelay && this.cacheIndexAlpha != null) {
            return this.cacheIndexAlpha;
        } else {
            float f = ((float)(3 - this.lerpDelay) + p_364383_) / 3.0F;
            float f1 = 0.0F;
            float f2 = 1.0F;
            boolean flag = false;

            int i;
            for (i = 0; i < this.currentLerpSteps.size(); i++) {
                float f3 = this.currentLerpSteps.get(i).weight;
                if (!(f3 <= 0.0F)) {
                    f1 += f3;
                    if ((double)f1 >= this.currentLerpStepsTotalWeight * (double)f) {
                        float f4 = f1 - f3;
                        f2 = (float)(((double)f * this.currentLerpStepsTotalWeight - (double)f4) / (double)f3);
                        flag = true;
                        break;
                    }
                }
            }

            if (!flag) {
                i = this.currentLerpSteps.size() - 1;
            }

            NewMinecartBehavior.MinecartStep newminecartbehavior$minecartstep = this.currentLerpSteps.get(i);
            NewMinecartBehavior.MinecartStep newminecartbehavior$minecartstep1 = i > 0 ? this.currentLerpSteps.get(i - 1) : this.oldLerp;
            this.cacheIndexAlpha = new NewMinecartBehavior.StepPartialTicks(f2, newminecartbehavior$minecartstep, newminecartbehavior$minecartstep1);
            this.cachedLerpDelay = this.lerpDelay;
            this.cachedPartialTick = p_364383_;
            return this.cacheIndexAlpha;
        }
    }

    public void adjustToRails(BlockPos p_360495_, BlockState p_362772_, boolean p_366683_) {
        if (BaseRailBlock.isRail(p_362772_)) {
            RailShape railshape = p_362772_.getValue(((BaseRailBlock)p_362772_.getBlock()).getShapeProperty());
            Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railshape);
            Vec3 vec3 = new Vec3(pair.getFirst()).scale(0.5);
            Vec3 vec31 = new Vec3(pair.getSecond()).scale(0.5);
            Vec3 vec32 = vec3.horizontal();
            Vec3 vec33 = vec31.horizontal();
            if (this.getDeltaMovement().length() > 1.0E-5F && this.getDeltaMovement().dot(vec32) < this.getDeltaMovement().dot(vec33)
                || this.isDecending(vec33, railshape)) {
                Vec3 vec34 = vec32;
                vec32 = vec33;
                vec33 = vec34;
            }

            float f = 180.0F - (float)(Math.atan2(vec32.z, vec32.x) * 180.0 / Math.PI);
            f += this.minecart.isFlipped() ? 180.0F : 0.0F;
            Vec3 vec35 = this.position();
            boolean flag = vec3.x() != vec31.x() && vec3.z() != vec31.z();
            Vec3 vec36;
            if (flag) {
                Vec3 vec37 = vec31.subtract(vec3);
                Vec3 vec38 = vec35.subtract(p_360495_.getBottomCenter()).subtract(vec3);
                Vec3 vec39 = vec37.scale(vec37.dot(vec38) / vec37.dot(vec37));
                vec36 = p_360495_.getBottomCenter().add(vec3).add(vec39);
                f = 180.0F - (float)(Math.atan2(vec39.z, vec39.x) * 180.0 / Math.PI);
                f += this.minecart.isFlipped() ? 180.0F : 0.0F;
            } else {
                boolean flag1 = vec3.subtract(vec31).x != 0.0;
                boolean flag2 = vec3.subtract(vec31).z != 0.0;
                vec36 = new Vec3(flag2 ? p_360495_.getCenter().x : vec35.x, (double)p_360495_.getY(), flag1 ? p_360495_.getCenter().z : vec35.z);
            }

            Vec3 vec311 = vec36.subtract(vec35);
            this.setPos(vec35.add(vec311));
            float f1 = 0.0F;
            boolean flag3 = vec3.y() != vec31.y();
            if (flag3) {
                Vec3 vec310 = p_360495_.getBottomCenter().add(vec33);
                double d0 = vec310.distanceTo(this.position());
                this.setPos(this.position().add(0.0, d0 + 0.1, 0.0));
                f1 = this.minecart.isFlipped() ? 45.0F : -45.0F;
            } else {
                this.setPos(this.position().add(0.0, 0.1, 0.0));
            }

            this.setRotation(f, f1);
            double d1 = vec35.distanceTo(this.position());
            if (d1 > 0.0) {
                this.lerpSteps
                    .add(
                        new NewMinecartBehavior.MinecartStep(
                            this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), p_366683_ ? 0.0F : (float)d1
                        )
                    );
            }
        }
    }

    private void setRotation(float p_366672_, float p_366480_) {
        double d0 = (double)Math.abs(p_366672_ - this.getYRot());
        if (d0 >= 175.0 && d0 <= 185.0) {
            this.minecart.setFlipped(!this.minecart.isFlipped());
            p_366672_ -= 180.0F;
            p_366480_ *= -1.0F;
        }

        p_366480_ = Math.clamp(p_366480_, -45.0F, 45.0F);
        this.setXRot(p_366480_ % 360.0F);
        this.setYRot(p_366672_ % 360.0F);
    }

    @Override
    public void moveAlongTrack(ServerLevel p_376236_) {
        for (NewMinecartBehavior.TrackIteration newminecartbehavior$trackiteration = new NewMinecartBehavior.TrackIteration();
            newminecartbehavior$trackiteration.shouldIterate() && this.minecart.isAlive();
            newminecartbehavior$trackiteration.firstIteration = false
        ) {
            Vec3 vec3 = this.getDeltaMovement();
            BlockPos blockpos = this.minecart.getCurrentBlockPosOrRailBelow();
            BlockState blockstate = this.level().getBlockState(blockpos);
            boolean flag = BaseRailBlock.isRail(blockstate);
            if (this.minecart.isOnRails() != flag) {
                this.minecart.setOnRails(flag);
                this.adjustToRails(blockpos, blockstate, false);
            }

            if (flag) {
                this.minecart.resetFallDistance();
                this.minecart.setOldPosAndRot();
                if (blockstate.is(Blocks.ACTIVATOR_RAIL)) {
                    this.minecart.activateMinecart(blockpos.getBigX(), blockpos.getBigY(), blockpos.getBigZ(), blockstate.getValue(PoweredRailBlock.POWERED));
                }

                RailShape railshape = blockstate.getValue(((BaseRailBlock)blockstate.getBlock()).getShapeProperty());
                Vec3 vec31 = this.calculateTrackSpeed(p_376236_, vec3.horizontal(), newminecartbehavior$trackiteration, blockpos, blockstate, railshape);
                if (newminecartbehavior$trackiteration.firstIteration) {
                    newminecartbehavior$trackiteration.movementLeft = vec31.horizontalDistance();
                } else {
                    newminecartbehavior$trackiteration.movementLeft = newminecartbehavior$trackiteration.movementLeft
                        + (vec31.horizontalDistance() - vec3.horizontalDistance());
                }

                this.setDeltaMovement(vec31);
                newminecartbehavior$trackiteration.movementLeft = this.minecart
                    .makeStepAlongTrack(blockpos, railshape, newminecartbehavior$trackiteration.movementLeft);
            } else {
                this.minecart.comeOffTrack(p_376236_);
                newminecartbehavior$trackiteration.movementLeft = 0.0;
            }

            Vec3 vec32 = this.position();
            Vec3 vec33 = vec32.subtract(this.minecart.oldPosition());
            double d0 = vec33.length();
            if (d0 > 1.0E-5F) {
                if (!(vec33.horizontalDistanceSqr() > 1.0E-5F)) {
                    if (!this.minecart.isOnRails()) {
                        this.setXRot(this.minecart.onGround() ? 0.0F : Mth.rotLerp(0.2F, this.getXRot(), 0.0F));
                    }
                } else {
                    float f = 180.0F - (float)(Math.atan2(vec33.z, vec33.x) * 180.0 / Math.PI);
                    float f1 = this.minecart.onGround() && !this.minecart.isOnRails()
                        ? 0.0F
                        : 90.0F - (float)(Math.atan2(vec33.horizontalDistance(), vec33.y) * 180.0 / Math.PI);
                    f += this.minecart.isFlipped() ? 180.0F : 0.0F;
                    f1 *= this.minecart.isFlipped() ? -1.0F : 1.0F;
                    this.setRotation(f, f1);
                }

                this.lerpSteps
                    .add(
                        new NewMinecartBehavior.MinecartStep(
                            vec32, this.getDeltaMovement(), this.getYRot(), this.getXRot(), (float)Math.min(d0, this.getMaxSpeed(p_376236_))
                        )
                    );
            } else if (vec3.horizontalDistanceSqr() > 0.0) {
                this.lerpSteps.add(new NewMinecartBehavior.MinecartStep(vec32, this.getDeltaMovement(), this.getYRot(), this.getXRot(), 1.0F));
            }

            if (d0 > 1.0E-5F || newminecartbehavior$trackiteration.firstIteration) {
                this.minecart.applyEffectsFromBlocks();
                this.minecart.applyEffectsFromBlocks();
            }
        }
    }

    private Vec3 calculateTrackSpeed(
        ServerLevel p_376196_, Vec3 p_364530_, NewMinecartBehavior.TrackIteration p_364155_, BlockPos p_364264_, BlockState p_363628_, RailShape p_364548_
    ) {
        Vec3 vec3 = p_364530_;
        if (!p_364155_.hasGainedSlopeSpeed) {
            Vec3 vec31 = this.calculateSlopeSpeed(p_364530_, p_364548_);
            if (vec31.horizontalDistanceSqr() != p_364530_.horizontalDistanceSqr()) {
                p_364155_.hasGainedSlopeSpeed = true;
                vec3 = vec31;
            }
        }

        if (p_364155_.firstIteration) {
            Vec3 vec32 = this.calculatePlayerInputSpeed(vec3);
            if (vec32.horizontalDistanceSqr() != vec3.horizontalDistanceSqr()) {
                p_364155_.hasHalted = true;
                vec3 = vec32;
            }
        }

        if (!p_364155_.hasHalted) {
            Vec3 vec33 = this.calculateHaltTrackSpeed(vec3, p_363628_);
            if (vec33.horizontalDistanceSqr() != vec3.horizontalDistanceSqr()) {
                p_364155_.hasHalted = true;
                vec3 = vec33;
            }
        }

        if (p_364155_.firstIteration) {
            vec3 = this.minecart.applyNaturalSlowdown(vec3);
            if (vec3.lengthSqr() > 0.0) {
                double d0 = Math.min(vec3.length(), this.minecart.getMaxSpeed(p_376196_));
                vec3 = vec3.normalize().scale(d0);
            }
        }

        if (!p_364155_.hasBoosted) {
            Vec3 vec34 = this.calculateBoostTrackSpeed(vec3, p_364264_, p_363628_);
            if (vec34.horizontalDistanceSqr() != vec3.horizontalDistanceSqr()) {
                p_364155_.hasBoosted = true;
                vec3 = vec34;
            }
        }

        return vec3;
    }

    private Vec3 calculateSlopeSpeed(Vec3 p_361946_, RailShape p_361759_) {
        double d0 = Math.max(0.0078125, p_361946_.horizontalDistance() * 0.02);
        if (this.minecart.isInWater()) {
            d0 *= 0.2;
        }
        return switch (p_361759_) {
            case ASCENDING_EAST -> p_361946_.add(-d0, 0.0, 0.0);
            case ASCENDING_WEST -> p_361946_.add(d0, 0.0, 0.0);
            case ASCENDING_NORTH -> p_361946_.add(0.0, 0.0, d0);
            case ASCENDING_SOUTH -> p_361946_.add(0.0, 0.0, -d0);
            default -> p_361946_;
        };
    }

    private Vec3 calculatePlayerInputSpeed(Vec3 p_362005_) {
        if (this.minecart.getFirstPassenger() instanceof ServerPlayer serverplayer) {
            Vec3 vec31 = serverplayer.getLastClientMoveIntent();
            if (vec31.lengthSqr() > 0.0) {
                Vec3 vec3 = vec31.normalize();
                double d0 = p_362005_.horizontalDistanceSqr();
                if (vec3.lengthSqr() > 0.0 && d0 < 0.01) {
                    return p_362005_.add(new Vec3(vec3.x, 0.0, vec3.z).normalize().scale(0.001));
                }
            }

            return p_362005_;
        } else {
            return p_362005_;
        }
    }

    private Vec3 calculateHaltTrackSpeed(Vec3 p_360517_, BlockState p_362923_) {
        if (p_362923_.is(Blocks.POWERED_RAIL) && !p_362923_.getValue(PoweredRailBlock.POWERED)) {
            return p_360517_.length() < 0.03 ? Vec3.ZERO : p_360517_.scale(0.5);
        } else {
            return p_360517_;
        }
    }

    private Vec3 calculateBoostTrackSpeed(Vec3 p_363053_, BlockPos p_361792_, BlockState p_361859_) {
        if (p_361859_.is(Blocks.POWERED_RAIL) && p_361859_.getValue(PoweredRailBlock.POWERED)) {
            if (p_363053_.length() > 0.01) {
                return p_363053_.normalize().scale(p_363053_.length() + 0.06);
            } else {
                Vec3 vec3 = this.minecart.getRedstoneDirection(p_361792_);
                return vec3.lengthSqr() <= 0.0 ? p_363053_ : vec3.scale(p_363053_.length() + 0.2);
            }
        } else {
            return p_363053_;
        }
    }

    @Override
    public double stepAlongTrack(BlockPos p_362592_, RailShape p_361660_, double p_361219_) {
        if (p_361219_ < 1.0E-5F) {
            return 0.0;
        } else {
            Vec3 vec3 = this.position();
            Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(p_361660_);
            Vec3i vec3i = pair.getFirst();
            Vec3i vec3i1 = pair.getSecond();
            Vec3 vec31 = this.getDeltaMovement().horizontal();
            if (vec31.length() < 1.0E-5F) {
                this.setDeltaMovement(Vec3.ZERO);
                return 0.0;
            } else {
                boolean flag = vec3i.getY() != vec3i1.getY();
                Vec3 vec32 = new Vec3(vec3i1).scale(0.5).horizontal();
                Vec3 vec33 = new Vec3(vec3i).scale(0.5).horizontal();
                if (vec31.dot(vec33) < vec31.dot(vec32)) {
                    vec33 = vec32;
                }

                Vec3 vec34 = p_362592_.getBottomCenter().add(vec33).add(0.0, 0.1, 0.0).add(vec33.normalize().scale(1.0E-5F));
                if (flag && !this.isDecending(vec31, p_361660_)) {
                    vec34 = vec34.add(0.0, 1.0, 0.0);
                }

                Vec3 vec35 = vec34.subtract(this.position()).normalize();
                vec31 = vec35.scale(vec31.length() / vec35.horizontalDistance());
                Vec3 vec36 = vec3.add(vec31.normalize().scale(p_361219_ * (double)(flag ? Mth.SQRT_OF_TWO : 1.0F)));
                if (vec3.distanceToSqr(vec34) <= vec3.distanceToSqr(vec36)) {
                    p_361219_ = vec34.subtract(vec36).horizontalDistance();
                    vec36 = vec34;
                } else {
                    p_361219_ = 0.0;
                }

                this.minecart.move(MoverType.SELF, vec36.subtract(vec3));
                BlockState blockstate = this.level().getBlockState(BlockPos.containing(vec36));
                if (flag) {
                    if (BaseRailBlock.isRail(blockstate)) {
                        RailShape railshape = blockstate.getValue(((BaseRailBlock)blockstate.getBlock()).getShapeProperty());
                        if (this.restAtVShape(p_361660_, railshape)) {
                            return 0.0;
                        }
                    }

                    double d1 = vec34.horizontal().distanceTo(this.position().horizontal());
                    double d0 = vec34.y + (this.isDecending(vec31, p_361660_) ? d1 : -d1);
                    if (this.position().y < d0) {
                        this.setPos(this.position().x, d0, this.position().z);
                    }
                }

                if (this.position().distanceTo(vec3) < 1.0E-5F && vec36.distanceTo(vec3) > 1.0E-5F) {
                    this.setDeltaMovement(Vec3.ZERO);
                    return 0.0;
                } else {
                    this.setDeltaMovement(vec31);
                    return p_361219_;
                }
            }
        }
    }

    private boolean restAtVShape(RailShape p_366540_, RailShape p_366691_) {
        if (this.getDeltaMovement().lengthSqr() < 0.005
            && p_366691_.isSlope()
            && this.isDecending(this.getDeltaMovement(), p_366540_)
            && !this.isDecending(this.getDeltaMovement(), p_366691_)) {
            this.setDeltaMovement(Vec3.ZERO);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public double getMaxSpeed(ServerLevel p_376456_) {
        return (double)p_376456_.getGameRules().getInt(GameRules.RULE_MINECART_MAX_SPEED) * (this.minecart.isInWater() ? 0.5 : 1.0) / 20.0;
    }

    private boolean isDecending(Vec3 p_363895_, RailShape p_363541_) {
        return switch (p_363541_) {
            case ASCENDING_EAST -> p_363895_.x < 0.0;
            case ASCENDING_WEST -> p_363895_.x > 0.0;
            case ASCENDING_NORTH -> p_363895_.z > 0.0;
            case ASCENDING_SOUTH -> p_363895_.z < 0.0;
            default -> false;
        };
    }

    @Override
    public double getSlowdownFactor() {
        return this.minecart.isVehicle() ? 0.997 : 0.975;
    }

    @Override
    public boolean pushAndPickupEntities() {
        boolean flag = this.pickupEntities(this.minecart.getBoundingBox().inflate(0.2, 0.0, 0.2));
        if (!this.minecart.horizontalCollision && !this.minecart.verticalCollision) {
            return false;
        } else {
            boolean flag1 = this.pushEntities(this.minecart.getBoundingBox().inflate(1.0E-7));
            return flag && !flag1;
        }
    }

    public boolean pickupEntities(AABB p_366509_) {
        if (this.minecart.isRideable() && !this.minecart.isVehicle()) {
            List<Entity> list = this.level().getEntities(this.minecart, p_366509_, EntitySelector.pushableBy(this.minecart));
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (!(entity instanceof Player)
                        && !(entity instanceof IronGolem)
                        && !(entity instanceof AbstractMinecart)
                        && !this.minecart.isVehicle()
                        && !entity.isPassenger()) {
                        boolean flag = entity.startRiding(this.minecart);
                        if (flag) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean pushEntities(AABB p_366897_) {
        boolean flag = false;
        if (this.minecart.isRideable()) {
            List<Entity> list = this.level().getEntities(this.minecart, p_366897_, EntitySelector.pushableBy(this.minecart));
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity instanceof Player
                        || entity instanceof IronGolem
                        || entity instanceof AbstractMinecart
                        || this.minecart.isVehicle()
                        || entity.isPassenger()) {
                        entity.push(this.minecart);
                        flag = true;
                    }
                }
            }
        } else {
            for (Entity entity1 : this.level().getEntities(this.minecart, p_366897_)) {
                if (!this.minecart.hasPassenger(entity1) && entity1.isPushable() && entity1 instanceof AbstractMinecart) {
                    entity1.push(this.minecart);
                    flag = true;
                }
            }
        }

        return flag;
    }

    public static record MinecartStep(Vec3 position, Vec3 movement, float yRot, float xRot, float weight) {
        public static final StreamCodec<ByteBuf, NewMinecartBehavior.MinecartStep> STREAM_CODEC = StreamCodec.composite(
            Vec3.STREAM_CODEC,
            NewMinecartBehavior.MinecartStep::position,
            Vec3.STREAM_CODEC,
            NewMinecartBehavior.MinecartStep::movement,
            ByteBufCodecs.ROTATION_BYTE,
            NewMinecartBehavior.MinecartStep::yRot,
            ByteBufCodecs.ROTATION_BYTE,
            NewMinecartBehavior.MinecartStep::xRot,
            ByteBufCodecs.FLOAT,
            NewMinecartBehavior.MinecartStep::weight,
            NewMinecartBehavior.MinecartStep::new
        );
        public static NewMinecartBehavior.MinecartStep ZERO = new NewMinecartBehavior.MinecartStep(Vec3.ZERO, Vec3.ZERO, 0.0F, 0.0F, 0.0F);
    }

    static record StepPartialTicks(float partialTicksInStep, NewMinecartBehavior.MinecartStep currentStep, NewMinecartBehavior.MinecartStep previousStep) {
    }

    static class TrackIteration {
        double movementLeft = 0.0;
        boolean firstIteration = true;
        boolean hasGainedSlopeSpeed = false;
        boolean hasHalted = false;
        boolean hasBoosted = false;

        public boolean shouldIterate() {
            return this.firstIteration || this.movementLeft > 1.0E-5F;
        }
    }
}
