package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public abstract class MinecartBehavior {
    protected final AbstractMinecart minecart;

    protected MinecartBehavior(AbstractMinecart p_364358_) {
        this.minecart = p_364358_;
    }

    public void cancelLerp() {
    }

    public void lerpTo(double p_365519_, double p_364497_, double p_364457_, float p_364946_, float p_363413_, int p_361328_) {
        this.setPos(p_365519_, p_364497_, p_364457_);
        this.setYRot(p_364946_ % 360.0F);
        this.setXRot(p_363413_ % 360.0F);
    }

    public double lerpTargetX() {
        return this.getX();
    }

    public double lerpTargetY() {
        return this.getY();
    }

    public double lerpTargetZ() {
        return this.getZ();
    }

    public float lerpTargetXRot() {
        return this.getXRot();
    }

    public float lerpTargetYRot() {
        return this.getYRot();
    }

    public void lerpMotion(double p_363918_, double p_364438_, double p_365344_) {
        this.setDeltaMovement(p_363918_, p_364438_, p_365344_);
    }

    public abstract void tick();

    public Level level() {
        return this.minecart.level();
    }

    public abstract void moveAlongTrack(ServerLevel p_376849_);

    public abstract double stepAlongTrack(BlockPos p_360466_, RailShape p_361422_, double p_363822_);

    public abstract boolean pushAndPickupEntities();

    public Vec3 getDeltaMovement() {
        return this.minecart.getDeltaMovement();
    }

    public void setDeltaMovement(Vec3 p_362692_) {
        this.minecart.setDeltaMovement(p_362692_);
    }

    public void setDeltaMovement(double p_364114_, double p_362878_, double p_362138_) {
        this.minecart.setDeltaMovement(p_364114_, p_362878_, p_362138_);
    }

    public Vec3 position() {
        return this.minecart.position();
    }

    public double getX() {
        return this.minecart.getX();
    }

    public double getY() {
        return this.minecart.getY();
    }

    public double getZ() {
        return this.minecart.getZ();
    }

    public void setPos(Vec3 p_364392_) {
        this.minecart.setPos(p_364392_);
    }

    public void setPos(double p_361747_, double p_363801_, double p_363761_) {
        this.minecart.setPos(p_361747_, p_363801_, p_363761_);
    }

    public float getXRot() {
        return this.minecart.getXRot();
    }

    public void setXRot(float p_361403_) {
        this.minecart.setXRot(p_361403_);
    }

    public float getYRot() {
        return this.minecart.getYRot();
    }

    public void setYRot(float p_361334_) {
        this.minecart.setYRot(p_361334_);
    }

    public Direction getMotionDirection() {
        return this.minecart.getDirection();
    }

    public Vec3 getKnownMovement(Vec3 p_361961_) {
        return p_361961_;
    }

    public abstract double getMaxSpeed(ServerLevel p_376563_);

    public abstract double getSlowdownFactor();
}
