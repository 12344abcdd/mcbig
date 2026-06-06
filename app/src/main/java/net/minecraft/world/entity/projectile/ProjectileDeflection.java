package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface ProjectileDeflection {
    ProjectileDeflection NONE = (p_320379_, p_320626_, p_320122_) -> {
    };
    ProjectileDeflection REVERSE = (p_390285_, p_390286_, p_390287_) -> {
        float f = 170.0F + p_390287_.nextFloat() * 20.0F;
        p_390285_.setDeltaMovement(p_390285_.getDeltaMovement().scale(-0.5));
        p_390285_.setYRot(p_390285_.getYRot() + f);
        p_390285_.yRotO += f;
        p_390285_.hasImpulse = true;
    };
    ProjectileDeflection AIM_DEFLECT = (p_390282_, p_390283_, p_390284_) -> {
        if (p_390283_ != null) {
            Vec3 vec3 = p_390283_.getLookAngle().normalize();
            p_390282_.setDeltaMovement(vec3);
            p_390282_.hasImpulse = true;
        }
    };
    ProjectileDeflection MOMENTUM_DEFLECT = (p_390288_, p_390289_, p_390290_) -> {
        if (p_390289_ != null) {
            Vec3 vec3 = p_390289_.getDeltaMovement().normalize();
            p_390288_.setDeltaMovement(vec3);
            p_390288_.hasImpulse = true;
        }
    };

    void deflect(Projectile p_320311_, @Nullable Entity p_320130_, RandomSource p_320125_);
}
