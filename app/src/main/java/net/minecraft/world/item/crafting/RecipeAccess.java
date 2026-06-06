package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceKey;

public interface RecipeAccess {
    RecipePropertySet propertySet(ResourceKey<RecipePropertySet> p_379923_);

    SelectableRecipe.SingleInputSet<StonecutterRecipe> stonecutterRecipes();
}
