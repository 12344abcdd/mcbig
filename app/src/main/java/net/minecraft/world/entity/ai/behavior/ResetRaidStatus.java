package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ResetRaidStatus {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(p_259870_ -> p_259870_.point((p_382722_, p_382723_, p_382724_) -> {
                if (p_382722_.random.nextInt(20) != 0) {
                    return false;
                } else {
                    Brain<?> brain = p_382723_.getBrain();
                    Raid raid = p_382722_.getRaidAt(p_382723_.blockPosition());
                    if (raid == null || raid.isStopped() || raid.isLoss()) {
                        brain.setDefaultActivity(Activity.IDLE);
                        brain.updateActivityFromSchedule(p_382722_.getDayTime(), p_382722_.getGameTime());
                    }

                    return true;
                }
            }));
    }
}
