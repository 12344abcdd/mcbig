package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class UpdateActivityFromSchedule {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(p_259429_ -> p_259429_.point((p_382734_, p_382735_, p_382736_) -> {
                p_382735_.getBrain().updateActivityFromSchedule(p_382734_.getDayTime(), p_382734_.getGameTime());
                return true;
            }));
    }
}
