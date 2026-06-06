package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiVariantGenerator implements BlockStateGenerator {
    private final Block block;
    private final List<Variant> baseVariants;
    private final Set<Property<?>> seenProperties = Sets.newHashSet();
    private final List<PropertyDispatch> declaredPropertySets = Lists.newArrayList();

    private MultiVariantGenerator(Block p_388671_, List<Variant> p_386519_) {
        this.block = p_388671_;
        this.baseVariants = p_386519_;
    }

    public MultiVariantGenerator with(PropertyDispatch p_388256_) {
        p_388256_.getDefinedProperties().forEach(p_388710_ -> {
            if (this.block.getStateDefinition().getProperty(p_388710_.getName()) != p_388710_) {
                throw new IllegalStateException("Property " + p_388710_ + " is not defined for block " + this.block);
            } else if (!this.seenProperties.add((Property<?>)p_388710_)) {
                throw new IllegalStateException("Values of property " + p_388710_ + " already defined for block " + this.block);
            }
        });
        this.declaredPropertySets.add(p_388256_);
        return this;
    }

    public JsonElement get() {
        Stream<Pair<Selector, List<Variant>>> stream = Stream.of(Pair.of(Selector.empty(), this.baseVariants));

        for (PropertyDispatch propertydispatch : this.declaredPropertySets) {
            Map<Selector, List<Variant>> map = propertydispatch.getEntries();
            stream = stream.flatMap(p_388549_ -> map.entrySet().stream().map(p_388868_ -> {
                    Selector selector = ((Selector)p_388549_.getFirst()).extend(p_388868_.getKey());
                    List<Variant> list = mergeVariants((List<Variant>)p_388549_.getSecond(), p_388868_.getValue());
                    return Pair.of(selector, list);
                }));
        }

        Map<String, JsonElement> map1 = new TreeMap<>();
        stream.forEach(p_387851_ -> map1.put(p_387851_.getFirst().getKey(), Variant.convertList(p_387851_.getSecond())));
        JsonObject jsonobject = new JsonObject();
        jsonobject.add("variants", Util.make(new JsonObject(), p_388876_ -> map1.forEach(p_388876_::add)));
        return jsonobject;
    }

    private static List<Variant> mergeVariants(List<Variant> p_388459_, List<Variant> p_387914_) {
        Builder<Variant> builder = ImmutableList.builder();
        p_388459_.forEach(p_388613_ -> p_387914_.forEach(p_386653_ -> builder.add(Variant.merge(p_388613_, p_386653_))));
        return builder.build();
    }

    @Override
    public Block getBlock() {
        return this.block;
    }

    public static MultiVariantGenerator multiVariant(Block p_387605_) {
        return new MultiVariantGenerator(p_387605_, ImmutableList.of(Variant.variant()));
    }

    public static MultiVariantGenerator multiVariant(Block p_387802_, Variant p_387637_) {
        return new MultiVariantGenerator(p_387802_, ImmutableList.of(p_387637_));
    }

    public static MultiVariantGenerator multiVariant(Block p_388316_, Variant... p_388452_) {
        return new MultiVariantGenerator(p_388316_, ImmutableList.copyOf(p_388452_));
    }
}
