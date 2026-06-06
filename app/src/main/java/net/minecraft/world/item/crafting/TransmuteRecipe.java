package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public class TransmuteRecipe implements CraftingRecipe {
    final String group;
    final CraftingBookCategory category;
    final Ingredient input;
    final Ingredient material;
    final Holder<Item> result;
    @Nullable
    private PlacementInfo placementInfo;

    public TransmuteRecipe(String p_374115_, CraftingBookCategory p_374513_, Ingredient p_374507_, Ingredient p_374190_, Holder<Item> p_374204_) {
        this.group = p_374115_;
        this.category = p_374513_;
        this.input = p_374507_;
        this.material = p_374190_;
        this.result = p_374204_;
    }

    public boolean matches(CraftingInput p_374402_, Level p_374075_) {
        if (p_374402_.ingredientCount() != 2) {
            return false;
        } else {
            boolean flag = false;
            boolean flag1 = false;

            for (int i = 0; i < p_374402_.size(); i++) {
                ItemStack itemstack = p_374402_.getItem(i);
                if (!itemstack.isEmpty()) {
                    if (!flag && this.input.test(itemstack) && itemstack.getItem() != this.result.value()) {
                        flag = true;
                    } else {
                        if (flag1 || !this.material.test(itemstack)) {
                            return false;
                        }

                        flag1 = true;
                    }
                }
            }

            return flag && flag1;
        }
    }

    public ItemStack assemble(CraftingInput p_374148_, HolderLookup.Provider p_374579_) {
        ItemStack itemstack = ItemStack.EMPTY;

        for (int i = 0; i < p_374148_.size(); i++) {
            ItemStack itemstack1 = p_374148_.getItem(i);
            if (!itemstack1.isEmpty() && this.input.test(itemstack1) && itemstack1.getItem() != this.result.value()) {
                itemstack = itemstack1;
            }
        }

        return itemstack.transmuteCopy(this.result.value(), 1);
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
            new ShapelessCraftingRecipeDisplay(
                List.of(this.input.display(), this.material.display()),
                new SlotDisplay.ItemSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
            )
        );
    }

    @Override
    public RecipeSerializer<TransmuteRecipe> getSerializer() {
        return RecipeSerializer.TRANSMUTE;
    }

    @Override
    public String group() {
        return this.group;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(List.of(this.input, this.material));
        }

        return this.placementInfo;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    public static class Serializer implements RecipeSerializer<TransmuteRecipe> {
        private static final MapCodec<TransmuteRecipe> CODEC = RecordCodecBuilder.mapCodec(
            p_381583_ -> p_381583_.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(p_374051_ -> p_374051_.group),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(p_374281_ -> p_374281_.category),
                        Ingredient.CODEC.fieldOf("input").forGetter(p_374483_ -> p_374483_.input),
                        Ingredient.CODEC.fieldOf("material").forGetter(p_374399_ -> p_374399_.material),
                        Item.CODEC.fieldOf("result").forGetter(p_374517_ -> p_374517_.result)
                    )
                    .apply(p_381583_, TransmuteRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, TransmuteRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            p_374373_ -> p_374373_.group,
            CraftingBookCategory.STREAM_CODEC,
            p_374482_ -> p_374482_.category,
            Ingredient.CONTENTS_STREAM_CODEC,
            p_374160_ -> p_374160_.input,
            Ingredient.CONTENTS_STREAM_CODEC,
            p_374370_ -> p_374370_.material,
            ByteBufCodecs.holderRegistry(Registries.ITEM),
            p_374224_ -> p_374224_.result,
            TransmuteRecipe::new
        );

        @Override
        public MapCodec<TransmuteRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TransmuteRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
