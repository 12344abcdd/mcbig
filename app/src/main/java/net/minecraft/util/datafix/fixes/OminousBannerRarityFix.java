package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class OminousBannerRarityFix extends DataFix {
    public OminousBannerRarityFix(Schema p_364363_) {
        super(p_364363_, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type<?> type1 = this.getInputSchema().getType(References.ITEM_STACK);
        TaggedChoiceType<?> taggedchoicetype = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> opticfinder1 = type.findField("components");
        OpticFinder<?> opticfinder2 = type1.findField("components");
        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("Ominous Banner block entity common rarity to uncommon rarity fix", type, p_363323_ -> {
            Object object = p_363323_.get(taggedchoicetype.finder()).getFirst();
            return object.equals("minecraft:banner") ? this.fix(p_363323_, opticfinder1) : p_363323_;
        }), this.fixTypeEverywhereTyped("Ominous Banner item stack common rarity to uncommon rarity fix", type1, p_360501_ -> {
            String s = p_360501_.getOptional(opticfinder).map(Pair::getSecond).orElse("");
            return s.equals("minecraft:white_banner") ? this.fix(p_360501_, opticfinder2) : p_360501_;
        }));
    }

    private Typed<?> fix(Typed<?> p_363320_, OpticFinder<?> p_362991_) {
        return p_363320_.updateTyped(
            p_362991_,
            p_363204_ -> p_363204_.update(
                    DSL.remainderFinder(),
                    p_365422_ -> {
                        boolean flag = p_365422_.get("minecraft:item_name")
                            .asString()
                            .result()
                            .flatMap(ComponentDataFixUtils::extractTranslationString)
                            .filter(p_363017_ -> p_363017_.equals("block.minecraft.ominous_banner"))
                            .isPresent();
                        return flag
                            ? p_365422_.set("minecraft:rarity", p_365422_.createString("uncommon"))
                                .set(
                                    "minecraft:item_name",
                                    ComponentDataFixUtils.createTranslatableComponent(p_365422_.getOps(), "block.minecraft.ominous_banner")
                                )
                            : p_365422_;
                    }
                )
        );
    }
}
