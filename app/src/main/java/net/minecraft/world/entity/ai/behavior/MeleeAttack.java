package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

public class MeleeAttack {
    public static <T extends Mob> OneShot<T> create(int p_259758_) {
        return create(p_379104_ -> true, p_259758_);
    }

    public static <T extends Mob> OneShot<T> create(Predicate<T> p_379852_, int p_380056_) {
        return BehaviorBuilder.create(
            p_379103_ -> p_379103_.group(
                        p_379103_.registered(MemoryModuleType.LOOK_TARGET),
                        p_379103_.present(MemoryModuleType.ATTACK_TARGET),
                        p_379103_.absent(MemoryModuleType.ATTACK_COOLING_DOWN),
                        p_379103_.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                    )
                    .apply(
                        p_379103_,
                        (p_379118_, p_379119_, p_379120_, p_379121_) -> (p_379112_, p_379113_, p_379114_) -> {
                                LivingEntity livingentity = p_379103_.get(p_379119_);
                                if (p_379852_.test(p_379113_)
                                    && !isHoldingUsableProjectileWeapon(p_379113_)
                                    && p_379113_.isWithinMeleeAttackRange(livingentity)
                                    && p_379103_.<NearestVisibleLivingEntities>get(p_379121_).contains(livingentity)) {
                                    p_379118_.set(new EntityTracker(livingentity, true));
                                    p_379113_.swing(InteractionHand.MAIN_HAND);
                                    p_379113_.doHurtTarget(p_379112_, livingentity);
                                    p_379120_.setWithExpiry(true, (long)p_380056_);
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                    )
        );
    }

    private static boolean isHoldingUsableProjectileWeapon(Mob p_23528_) {
        return p_23528_.isHolding(p_147697_ -> {
            Item item = p_147697_.getItem();
            return item instanceof ProjectileWeaponItem && p_23528_.canFireProjectileWeapon((ProjectileWeaponItem)item);
        });
    }
}
