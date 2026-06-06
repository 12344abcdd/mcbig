package net.minecraft.world.item.equipment.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record ArmorTrim(Holder<TrimMaterial> material, Holder<TrimPattern> pattern, boolean showInTooltip) implements TooltipProvider {
    public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create(
        p_371800_ -> p_371800_.group(
                    TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material),
                    TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern),
                    Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(p_371680_ -> p_371680_.showInTooltip)
                )
                .apply(p_371800_, ArmorTrim::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorTrim> STREAM_CODEC = StreamCodec.composite(
        TrimMaterial.STREAM_CODEC,
        ArmorTrim::material,
        TrimPattern.STREAM_CODEC,
        ArmorTrim::pattern,
        ByteBufCodecs.BOOL,
        p_371487_ -> p_371487_.showInTooltip,
        ArmorTrim::new
    );
    private static final Component UPGRADE_TITLE = Component.translatable(
            Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.upgrade"))
        )
        .withStyle(ChatFormatting.GRAY);

    public ArmorTrim(Holder<TrimMaterial> p_371301_, Holder<TrimPattern> p_371695_) {
        this(p_371301_, p_371695_, true);
    }

    public boolean hasPatternAndMaterial(Holder<TrimPattern> p_371564_, Holder<TrimMaterial> p_371503_) {
        return p_371564_.equals(this.pattern) && p_371503_.equals(this.material);
    }

    @Override
    public void addToTooltip(Item.TooltipContext p_371689_, Consumer<Component> p_371352_, TooltipFlag p_371517_) {
        if (this.showInTooltip) {
            p_371352_.accept(UPGRADE_TITLE);
            p_371352_.accept(CommonComponents.space().append(this.pattern.value().copyWithStyle(this.material)));
            p_371352_.accept(CommonComponents.space().append(this.material.value().description()));
        }
    }

    public ArmorTrim withTooltip(boolean p_371568_) {
        return new ArmorTrim(this.material, this.pattern, p_371568_);
    }
}
