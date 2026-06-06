package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByArrowTrigger extends SimpleCriterionTrigger<KilledByArrowTrigger.TriggerInstance> {
    @Override
    public Codec<KilledByArrowTrigger.TriggerInstance> codec() {
        return KilledByArrowTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_373039_, Collection<Entity> p_373041_, @Nullable ItemStack p_373082_) {
        List<LootContext> list = Lists.newArrayList();
        Set<EntityType<?>> set = Sets.newHashSet();

        for (Entity entity : p_373041_) {
            set.add(entity.getType());
            list.add(EntityPredicate.createContext(p_373039_, entity));
        }

        this.trigger(p_373039_, p_372912_ -> p_372912_.matches(list, set.size(), p_373082_));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        List<ContextAwarePredicate> victims,
        MinMaxBounds.Ints uniqueEntityTypes,
        Optional<ItemPredicate> firedFromWeapon
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<KilledByArrowTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_373084_ -> p_373084_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(KilledByArrowTrigger.TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC
                            .listOf()
                            .optionalFieldOf("victims", List.of())
                            .forGetter(KilledByArrowTrigger.TriggerInstance::victims),
                        MinMaxBounds.Ints.CODEC
                            .optionalFieldOf("unique_entity_types", MinMaxBounds.Ints.ANY)
                            .forGetter(KilledByArrowTrigger.TriggerInstance::uniqueEntityTypes),
                        ItemPredicate.CODEC.optionalFieldOf("fired_from_weapon").forGetter(KilledByArrowTrigger.TriggerInstance::firedFromWeapon)
                    )
                    .apply(p_373084_, KilledByArrowTrigger.TriggerInstance::new)
        );

        public static Criterion<KilledByArrowTrigger.TriggerInstance> crossbowKilled(HolderGetter<Item> p_373005_, EntityPredicate.Builder... p_373068_) {
            return CriteriaTriggers.KILLED_BY_ARROW
                .createCriterion(
                    new KilledByArrowTrigger.TriggerInstance(
                        Optional.empty(),
                        EntityPredicate.wrap(p_373068_),
                        MinMaxBounds.Ints.ANY,
                        Optional.of(ItemPredicate.Builder.item().of(p_373005_, Items.CROSSBOW).build())
                    )
                );
        }

        public static Criterion<KilledByArrowTrigger.TriggerInstance> crossbowKilled(HolderGetter<Item> p_373076_, MinMaxBounds.Ints p_372922_) {
            return CriteriaTriggers.KILLED_BY_ARROW
                .createCriterion(
                    new KilledByArrowTrigger.TriggerInstance(
                        Optional.empty(), List.of(), p_372922_, Optional.of(ItemPredicate.Builder.item().of(p_373076_, Items.CROSSBOW).build())
                    )
                );
        }

        public boolean matches(Collection<LootContext> p_372887_, int p_372892_, @Nullable ItemStack p_372931_) {
            if (!this.firedFromWeapon.isPresent() || p_372931_ != null && this.firedFromWeapon.get().test(p_372931_)) {
                if (!this.victims.isEmpty()) {
                    List<LootContext> list = Lists.newArrayList(p_372887_);

                    for (ContextAwarePredicate contextawarepredicate : this.victims) {
                        boolean flag = false;
                        Iterator<LootContext> iterator = list.iterator();

                        while (iterator.hasNext()) {
                            LootContext lootcontext = iterator.next();
                            if (contextawarepredicate.matches(lootcontext)) {
                                iterator.remove();
                                flag = true;
                                break;
                            }
                        }

                        if (!flag) {
                            return false;
                        }
                    }
                }

                return this.uniqueEntityTypes.matches(p_372892_);
            } else {
                return false;
            }
        }

        @Override
        public void validate(CriterionValidator p_373032_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_373032_);
            p_373032_.validateEntities(this.victims, ".victims");
        }
    }
}
