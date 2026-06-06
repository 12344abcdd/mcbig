package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
    @Override
    public Codec<RecipeUnlockedTrigger.TriggerInstance> codec() {
        return RecipeUnlockedTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_63719_, RecipeHolder<?> p_301160_) {
        this.trigger(p_63719_, p_300666_ -> p_300666_.matches(p_301160_));
    }

    public static Criterion<RecipeUnlockedTrigger.TriggerInstance> unlocked(ResourceKey<Recipe<?>> p_379611_) {
        return CriteriaTriggers.RECIPE_UNLOCKED.createCriterion(new RecipeUnlockedTrigger.TriggerInstance(Optional.empty(), p_379611_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Recipe<?>> recipe)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<RecipeUnlockedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_378761_ -> p_378761_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RecipeUnlockedTrigger.TriggerInstance::player),
                        ResourceKey.codec(Registries.RECIPE).fieldOf("recipe").forGetter(RecipeUnlockedTrigger.TriggerInstance::recipe)
                    )
                    .apply(p_378761_, RecipeUnlockedTrigger.TriggerInstance::new)
        );

        public boolean matches(RecipeHolder<?> p_301295_) {
            return this.recipe == p_301295_.id();
        }
    }
}
