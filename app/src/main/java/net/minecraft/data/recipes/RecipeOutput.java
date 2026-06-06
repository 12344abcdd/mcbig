package net.minecraft.data.recipes;

import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public interface RecipeOutput {
    void accept(ResourceKey<Recipe<?>> p_380042_, Recipe<?> p_312328_, @Nullable AdvancementHolder p_312176_);

    Advancement.Builder advancement();

    void includeRootAdvancement();
}
