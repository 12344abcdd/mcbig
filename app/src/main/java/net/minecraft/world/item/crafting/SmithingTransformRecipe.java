package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;

public class SmithingTransformRecipe implements SmithingRecipe {
    final Optional<Ingredient> template;
    final Optional<Ingredient> base;
    final Optional<Ingredient> addition;
    final ItemStack result;
    @Nullable
    private PlacementInfo placementInfo;

    public SmithingTransformRecipe(Optional<Ingredient> p_361033_, Optional<Ingredient> p_365496_, Optional<Ingredient> p_363238_, ItemStack p_267031_) {
        this.template = p_361033_;
        this.base = p_365496_;
        this.addition = p_363238_;
        this.result = p_267031_;
    }

    public ItemStack assemble(SmithingRecipeInput p_345093_, HolderLookup.Provider p_345488_) {
        ItemStack itemstack = p_345093_.base().transmuteCopy(this.result.getItem(), this.result.getCount());
        itemstack.applyComponents(this.result.getComponentsPatch());
        return itemstack;
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return this.template;
    }

    @Override
    public Optional<Ingredient> baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return this.addition;
    }

    @Override
    public RecipeSerializer<SmithingTransformRecipe> getSerializer() {
        return RecipeSerializer.SMITHING_TRANSFORM;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.createFromOptionals(List.of(this.template, this.base, this.addition));
        }

        return this.placementInfo;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
            new SmithingRecipeDisplay(
                Ingredient.optionalIngredientToDisplay(this.template),
                Ingredient.optionalIngredientToDisplay(this.base),
                Ingredient.optionalIngredientToDisplay(this.addition),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
            )
        );
    }

    public static class Serializer implements RecipeSerializer<SmithingTransformRecipe> {
        private static final MapCodec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(
            p_360081_ -> p_360081_.group(
                        Ingredient.CODEC.optionalFieldOf("template").forGetter(p_360080_ -> p_360080_.template),
                        Ingredient.CODEC.optionalFieldOf("base").forGetter(p_360078_ -> p_360078_.base),
                        Ingredient.CODEC.optionalFieldOf("addition").forGetter(p_360077_ -> p_360077_.addition),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_300935_ -> p_300935_.result)
                    )
                    .apply(p_360081_, SmithingTransformRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
            p_360084_ -> p_360084_.template,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
            p_360082_ -> p_360082_.base,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
            p_360083_ -> p_360083_.addition,
            ItemStack.STREAM_CODEC,
            p_360079_ -> p_360079_.result,
            SmithingTransformRecipe::new
        );

        @Override
        public MapCodec<SmithingTransformRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
