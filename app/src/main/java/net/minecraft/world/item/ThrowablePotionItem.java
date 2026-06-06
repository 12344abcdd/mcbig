package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;

public class ThrowablePotionItem extends PotionItem implements ProjectileItem {
    public static float PROJECTILE_SHOOT_POWER = 0.5F;

    public ThrowablePotionItem(Item.Properties p_43301_) {
        super(p_43301_);
    }

    @Override
    public InteractionResult use(Level p_43303_, Player p_43304_, InteractionHand p_43305_) {
        ItemStack itemstack = p_43304_.getItemInHand(p_43305_);
        if (p_43303_ instanceof ServerLevel serverlevel) {
            Projectile.spawnProjectileFromRotation(ThrownPotion::new, serverlevel, itemstack, p_43304_, -20.0F, PROJECTILE_SHOOT_POWER, 1.0F);
        }

        p_43304_.awardStat(Stats.ITEM_USED.get(this));
        itemstack.consume(1, p_43304_);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level p_338465_, Position p_338661_, ItemStack p_338506_, Direction p_338517_) {
        return new ThrownPotion(p_338465_, p_338661_.x(), p_338661_.y(), p_338661_.z(), p_338506_);
    }

    @Override
    public ProjectileItem.DispenseConfig createDispenseConfig() {
        return ProjectileItem.DispenseConfig.builder()
            .uncertainty(ProjectileItem.DispenseConfig.DEFAULT.uncertainty() * 0.5F)
            .power(ProjectileItem.DispenseConfig.DEFAULT.power() * 1.25F)
            .build();
    }
}
