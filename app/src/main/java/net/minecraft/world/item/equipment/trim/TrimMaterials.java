package net.minecraft.world.item.equipment.trim;

import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class TrimMaterials {
    public static final ResourceKey<TrimMaterial> QUARTZ = registryKey("quartz");
    public static final ResourceKey<TrimMaterial> IRON = registryKey("iron");
    public static final ResourceKey<TrimMaterial> NETHERITE = registryKey("netherite");
    public static final ResourceKey<TrimMaterial> REDSTONE = registryKey("redstone");
    public static final ResourceKey<TrimMaterial> COPPER = registryKey("copper");
    public static final ResourceKey<TrimMaterial> GOLD = registryKey("gold");
    public static final ResourceKey<TrimMaterial> EMERALD = registryKey("emerald");
    public static final ResourceKey<TrimMaterial> DIAMOND = registryKey("diamond");
    public static final ResourceKey<TrimMaterial> LAPIS = registryKey("lapis");
    public static final ResourceKey<TrimMaterial> AMETHYST = registryKey("amethyst");
    public static final ResourceKey<TrimMaterial> RESIN = registryKey("resin");

    public static void bootstrap(BootstrapContext<TrimMaterial> p_371627_) {
        register(p_371627_, QUARTZ, Items.QUARTZ, Style.EMPTY.withColor(14931140));
        register(p_371627_, IRON, Items.IRON_INGOT, Style.EMPTY.withColor(15527148), Map.of(EquipmentAssets.IRON, "iron_darker"));
        register(p_371627_, NETHERITE, Items.NETHERITE_INGOT, Style.EMPTY.withColor(6445145), Map.of(EquipmentAssets.NETHERITE, "netherite_darker"));
        register(p_371627_, REDSTONE, Items.REDSTONE, Style.EMPTY.withColor(9901575));
        register(p_371627_, COPPER, Items.COPPER_INGOT, Style.EMPTY.withColor(11823181));
        register(p_371627_, GOLD, Items.GOLD_INGOT, Style.EMPTY.withColor(14594349), Map.of(EquipmentAssets.GOLD, "gold_darker"));
        register(p_371627_, EMERALD, Items.EMERALD, Style.EMPTY.withColor(1155126));
        register(p_371627_, DIAMOND, Items.DIAMOND, Style.EMPTY.withColor(7269586), Map.of(EquipmentAssets.DIAMOND, "diamond_darker"));
        register(p_371627_, LAPIS, Items.LAPIS_LAZULI, Style.EMPTY.withColor(4288151));
        register(p_371627_, AMETHYST, Items.AMETHYST_SHARD, Style.EMPTY.withColor(10116294));
        register(p_371627_, RESIN, Items.RESIN_BRICK, Style.EMPTY.withColor(16545810));
    }

    public static Optional<Holder.Reference<TrimMaterial>> getFromIngredient(HolderLookup.Provider p_371665_, ItemStack p_371700_) {
        return p_371665_.lookupOrThrow(Registries.TRIM_MATERIAL).listElements().filter(p_371615_ -> p_371700_.is(p_371615_.value().ingredient())).findFirst();
    }

    private static void register(BootstrapContext<TrimMaterial> p_371580_, ResourceKey<TrimMaterial> p_371417_, Item p_371230_, Style p_371405_) {
        register(p_371580_, p_371417_, p_371230_, p_371405_, Map.of());
    }

    private static void register(
        BootstrapContext<TrimMaterial> p_371763_,
        ResourceKey<TrimMaterial> p_371867_,
        Item p_371472_,
        Style p_371730_,
        Map<ResourceKey<EquipmentAsset>, String> p_388368_
    ) {
        TrimMaterial trimmaterial = TrimMaterial.create(
            p_371867_.location().getPath(),
            p_371472_,
            Component.translatable(Util.makeDescriptionId("trim_material", p_371867_.location())).withStyle(p_371730_),
            p_388368_
        );
        p_371763_.register(p_371867_, trimmaterial);
    }

    private static ResourceKey<TrimMaterial> registryKey(String p_371483_) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.withDefaultNamespace(p_371483_));
    }
}
