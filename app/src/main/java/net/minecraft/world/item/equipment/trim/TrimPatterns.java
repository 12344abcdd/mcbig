package net.minecraft.world.item.equipment.trim;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TrimPatterns {
    public static final ResourceKey<TrimPattern> SENTRY = registryKey("sentry");
    public static final ResourceKey<TrimPattern> DUNE = registryKey("dune");
    public static final ResourceKey<TrimPattern> COAST = registryKey("coast");
    public static final ResourceKey<TrimPattern> WILD = registryKey("wild");
    public static final ResourceKey<TrimPattern> WARD = registryKey("ward");
    public static final ResourceKey<TrimPattern> EYE = registryKey("eye");
    public static final ResourceKey<TrimPattern> VEX = registryKey("vex");
    public static final ResourceKey<TrimPattern> TIDE = registryKey("tide");
    public static final ResourceKey<TrimPattern> SNOUT = registryKey("snout");
    public static final ResourceKey<TrimPattern> RIB = registryKey("rib");
    public static final ResourceKey<TrimPattern> SPIRE = registryKey("spire");
    public static final ResourceKey<TrimPattern> WAYFINDER = registryKey("wayfinder");
    public static final ResourceKey<TrimPattern> SHAPER = registryKey("shaper");
    public static final ResourceKey<TrimPattern> SILENCE = registryKey("silence");
    public static final ResourceKey<TrimPattern> RAISER = registryKey("raiser");
    public static final ResourceKey<TrimPattern> HOST = registryKey("host");
    public static final ResourceKey<TrimPattern> FLOW = registryKey("flow");
    public static final ResourceKey<TrimPattern> BOLT = registryKey("bolt");

    public static void bootstrap(BootstrapContext<TrimPattern> p_371366_) {
        register(p_371366_, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, SENTRY);
        register(p_371366_, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, DUNE);
        register(p_371366_, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, COAST);
        register(p_371366_, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, WILD);
        register(p_371366_, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, WARD);
        register(p_371366_, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, EYE);
        register(p_371366_, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, VEX);
        register(p_371366_, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, TIDE);
        register(p_371366_, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, SNOUT);
        register(p_371366_, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, RIB);
        register(p_371366_, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, SPIRE);
        register(p_371366_, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, WAYFINDER);
        register(p_371366_, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, SHAPER);
        register(p_371366_, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, SILENCE);
        register(p_371366_, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, RAISER);
        register(p_371366_, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, HOST);
        register(p_371366_, Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, FLOW);
        register(p_371366_, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, BOLT);
    }

    public static Optional<Holder.Reference<TrimPattern>> getFromTemplate(HolderLookup.Provider p_371802_, ItemStack p_371806_) {
        return p_371802_.lookupOrThrow(Registries.TRIM_PATTERN).listElements().filter(p_371279_ -> p_371806_.is(p_371279_.value().templateItem())).findFirst();
    }

    public static void register(BootstrapContext<TrimPattern> p_371430_, Item p_371597_, ResourceKey<TrimPattern> p_371696_) {
        TrimPattern trimpattern = new TrimPattern(
            p_371696_.location(),
            BuiltInRegistries.ITEM.wrapAsHolder(p_371597_),
            Component.translatable(Util.makeDescriptionId("trim_pattern", p_371696_.location())),
            false
        );
        p_371430_.register(p_371696_, trimpattern);
    }

    private static ResourceKey<TrimPattern> registryKey(String p_371622_) {
        return ResourceKey.create(Registries.TRIM_PATTERN, ResourceLocation.withDefaultNamespace(p_371622_));
    }
}
