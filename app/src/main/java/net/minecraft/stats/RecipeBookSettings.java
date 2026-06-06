package net.minecraft.stats;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookSettings {
    public static final StreamCodec<FriendlyByteBuf, RecipeBookSettings> STREAM_CODEC = StreamCodec.ofMember(
        RecipeBookSettings::write, RecipeBookSettings::read
    );
    private static final Map<RecipeBookType, Pair<String, String>> TAG_FIELDS = ImmutableMap.of(
        RecipeBookType.CRAFTING,
        Pair.of("isGuiOpen", "isFilteringCraftable"),
        RecipeBookType.FURNACE,
        Pair.of("isFurnaceGuiOpen", "isFurnaceFilteringCraftable"),
        RecipeBookType.BLAST_FURNACE,
        Pair.of("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"),
        RecipeBookType.SMOKER,
        Pair.of("isSmokerGuiOpen", "isSmokerFilteringCraftable")
    );
    private final Map<RecipeBookType, RecipeBookSettings.TypeSettings> states;

    private RecipeBookSettings(Map<RecipeBookType, RecipeBookSettings.TypeSettings> p_12730_) {
        this.states = p_12730_;
    }

    public RecipeBookSettings() {
        this(new EnumMap<>(RecipeBookType.class));
    }

    private RecipeBookSettings.TypeSettings getSettings(RecipeBookType p_380129_) {
        return this.states.getOrDefault(p_380129_, RecipeBookSettings.TypeSettings.DEFAULT);
    }

    private void updateSettings(RecipeBookType p_379381_, UnaryOperator<RecipeBookSettings.TypeSettings> p_379491_) {
        this.states.compute(p_379381_, (p_379060_, p_379061_) -> {
            if (p_379061_ == null) {
                p_379061_ = RecipeBookSettings.TypeSettings.DEFAULT;
            }

            p_379061_ = p_379491_.apply(p_379061_);
            if (p_379061_.equals(RecipeBookSettings.TypeSettings.DEFAULT)) {
                p_379061_ = null;
            }

            return p_379061_;
        });
    }

    public boolean isOpen(RecipeBookType p_12735_) {
        return this.getSettings(p_12735_).open;
    }

    public void setOpen(RecipeBookType p_12737_, boolean p_12738_) {
        this.updateSettings(p_12737_, p_379063_ -> p_379063_.setOpen(p_12738_));
    }

    public boolean isFiltering(RecipeBookType p_12755_) {
        return this.getSettings(p_12755_).filtering;
    }

    public void setFiltering(RecipeBookType p_12757_, boolean p_12758_) {
        this.updateSettings(p_12757_, p_379058_ -> p_379058_.setFiltering(p_12758_));
    }

    private static RecipeBookSettings read(FriendlyByteBuf p_12753_) {
        Map<RecipeBookType, RecipeBookSettings.TypeSettings> map = new EnumMap<>(RecipeBookType.class);

        for (RecipeBookType recipebooktype : RecipeBookType.values()) {
            boolean flag = p_12753_.readBoolean();
            boolean flag1 = p_12753_.readBoolean();
            if (flag || flag1) {
                map.put(recipebooktype, new RecipeBookSettings.TypeSettings(flag, flag1));
            }
        }

        return new RecipeBookSettings(map);
    }

    private void write(FriendlyByteBuf p_12762_) {
        for (RecipeBookType recipebooktype : RecipeBookType.values()) {
            RecipeBookSettings.TypeSettings recipebooksettings$typesettings = this.states.getOrDefault(recipebooktype, RecipeBookSettings.TypeSettings.DEFAULT);
            p_12762_.writeBoolean(recipebooksettings$typesettings.open);
            p_12762_.writeBoolean(recipebooksettings$typesettings.filtering);
        }
    }

    public static RecipeBookSettings read(CompoundTag p_12742_) {
        Map<RecipeBookType, RecipeBookSettings.TypeSettings> map = new EnumMap<>(RecipeBookType.class);
        TAG_FIELDS.forEach((p_379055_, p_379056_) -> {
            boolean flag = p_12742_.getBoolean(p_379056_.getFirst());
            boolean flag1 = p_12742_.getBoolean(p_379056_.getSecond());
            if (flag || flag1) {
                map.put(p_379055_, new RecipeBookSettings.TypeSettings(flag, flag1));
            }
        });
        return new RecipeBookSettings(map);
    }

    public void write(CompoundTag p_12760_) {
        TAG_FIELDS.forEach((p_379051_, p_379052_) -> {
            RecipeBookSettings.TypeSettings recipebooksettings$typesettings = this.states.getOrDefault(p_379051_, RecipeBookSettings.TypeSettings.DEFAULT);
            p_12760_.putBoolean(p_379052_.getFirst(), recipebooksettings$typesettings.open);
            p_12760_.putBoolean(p_379052_.getSecond(), recipebooksettings$typesettings.filtering);
        });
    }

    public RecipeBookSettings copy() {
        return new RecipeBookSettings(new EnumMap<>(this.states));
    }

    public void replaceFrom(RecipeBookSettings p_12733_) {
        this.states.clear();
        this.states.putAll(p_12733_.states);
    }

    @Override
    public boolean equals(Object p_12764_) {
        return this == p_12764_ || p_12764_ instanceof RecipeBookSettings && this.states.equals(((RecipeBookSettings)p_12764_).states);
    }

    @Override
    public int hashCode() {
        return this.states.hashCode();
    }

    static record TypeSettings(boolean open, boolean filtering) {
        public static final RecipeBookSettings.TypeSettings DEFAULT = new RecipeBookSettings.TypeSettings(false, false);

        @Override
        public String toString() {
            return "[open=" + this.open + ", filtering=" + this.filtering + "]";
        }

        public RecipeBookSettings.TypeSettings setOpen(boolean p_379785_) {
            return new RecipeBookSettings.TypeSettings(p_379785_, this.filtering);
        }

        public RecipeBookSettings.TypeSettings setFiltering(boolean p_380039_) {
            return new RecipeBookSettings.TypeSettings(this.open, p_380039_);
        }
    }
}
