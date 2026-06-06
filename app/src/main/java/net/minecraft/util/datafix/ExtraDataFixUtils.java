package net.minecraft.util.datafix;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.BitSet;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public class ExtraDataFixUtils {
    public static Dynamic<?> fixBlockPos(Dynamic<?> p_326292_) {
        Optional<Number> optional = p_326292_.get("X").asNumber().result();
        Optional<Number> optional1 = p_326292_.get("Y").asNumber().result();
        Optional<Number> optional2 = p_326292_.get("Z").asNumber().result();
        return !optional.isEmpty() && !optional1.isEmpty() && !optional2.isEmpty()
            ? p_326292_.createIntList(IntStream.of(optional.get().intValue(), optional1.get().intValue(), optional2.get().intValue()))
            : p_326292_;
    }

    public static <T, R> Typed<R> cast(Type<R> p_330690_, Typed<T> p_331921_) {
        return new Typed<>(p_330690_, p_331921_.getOps(), (R)p_331921_.getValue());
    }

    public static Type<?> patchSubType(Type<?> p_364210_, Type<?> p_360925_, Type<?> p_365306_) {
        return p_364210_.all(typePatcher(p_360925_, p_365306_), true, false).view().newType();
    }

    private static <A, B> TypeRewriteRule typePatcher(Type<A> p_365338_, Type<B> p_363692_) {
        RewriteResult<A, B> rewriteresult = RewriteResult.create(View.create("Patcher", p_365338_, p_363692_, p_359667_ -> p_359670_ -> {
                throw new UnsupportedOperationException();
            }), new BitSet());
        return TypeRewriteRule.everywhere(TypeRewriteRule.ifSame(p_365338_, rewriteresult), PointFreeRule.nop(), true, true);
    }

    @SafeVarargs
    public static <T> Function<Typed<?>, Typed<?>> chainAllFilters(Function<Typed<?>, Typed<?>>... p_344769_) {
        return p_345927_ -> {
            for (Function<Typed<?>, Typed<?>> function : p_344769_) {
                p_345927_ = function.apply(p_345927_);
            }

            return p_345927_;
        };
    }

    public static Dynamic<?> blockState(String p_361952_, Map<String, String> p_362407_) {
        Dynamic<Tag> dynamic = new Dynamic<>(NbtOps.INSTANCE, new CompoundTag());
        Dynamic<Tag> dynamic1 = dynamic.set("Name", dynamic.createString(p_361952_));
        if (!p_362407_.isEmpty()) {
            dynamic1 = dynamic1.set(
                "Properties",
                dynamic.createMap(
                    p_362407_.entrySet()
                        .stream()
                        .collect(
                            Collectors.toMap(p_359669_ -> dynamic.createString(p_359669_.getKey()), p_359672_ -> dynamic.createString(p_359672_.getValue()))
                        )
                )
            );
        }

        return dynamic1;
    }

    public static Dynamic<?> blockState(String p_363485_) {
        return blockState(p_363485_, Map.of());
    }

    public static Dynamic<?> fixStringField(Dynamic<?> p_365145_, String p_361499_, UnaryOperator<String> p_360789_) {
        return p_365145_.update(
            p_361499_, p_359675_ -> DataFixUtils.orElse(p_359675_.asString().map(p_360789_).map(p_365145_::createString).result(), p_359675_)
        );
    }
}
