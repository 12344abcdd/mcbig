package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Condition extends Supplier<JsonElement> {
    void validate(StateDefinition<?, ?> p_387380_);

    static Condition.TerminalCondition condition() {
        return new Condition.TerminalCondition();
    }

    static Condition and(Condition... p_387124_) {
        return new Condition.CompositeCondition(Condition.Operation.AND, Arrays.asList(p_387124_));
    }

    static Condition or(Condition... p_386471_) {
        return new Condition.CompositeCondition(Condition.Operation.OR, Arrays.asList(p_386471_));
    }

    @OnlyIn(Dist.CLIENT)
    public static class CompositeCondition implements Condition {
        private final Condition.Operation operation;
        private final List<Condition> subconditions;

        CompositeCondition(Condition.Operation p_388743_, List<Condition> p_387495_) {
            this.operation = p_388743_;
            this.subconditions = p_387495_;
        }

        @Override
        public void validate(StateDefinition<?, ?> p_386533_) {
            this.subconditions.forEach(p_386836_ -> p_386836_.validate(p_386533_));
        }

        public JsonElement get() {
            JsonArray jsonarray = new JsonArray();
            this.subconditions.stream().map(Supplier::get).forEach(jsonarray::add);
            JsonObject jsonobject = new JsonObject();
            jsonobject.add(this.operation.id, jsonarray);
            return jsonobject;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Operation {
        AND("AND"),
        OR("OR");

        final String id;

        private Operation(String p_386506_) {
            this.id = p_386506_;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TerminalCondition implements Condition {
        private final Map<Property<?>, String> terms = Maps.newHashMap();

        private static <T extends Comparable<T>> String joinValues(Property<T> p_387081_, Stream<T> p_388457_) {
            return p_388457_.<CharSequence>map(p_387081_::getName).collect(Collectors.joining("|"));
        }

        private static <T extends Comparable<T>> String getTerm(Property<T> p_388052_, T p_388165_, T[] p_386600_) {
            return joinValues(p_388052_, Stream.concat(Stream.of(p_388165_), Stream.of(p_386600_)));
        }

        private <T extends Comparable<T>> void putValue(Property<T> p_387312_, String p_387643_) {
            String s = this.terms.put(p_387312_, p_387643_);
            if (s != null) {
                throw new IllegalStateException("Tried to replace " + p_387312_ + " value from " + s + " to " + p_387643_);
            }
        }

        public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> p_388262_, T p_387671_) {
            this.putValue(p_388262_, p_388262_.getName(p_387671_));
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> p_387046_, T p_388209_, T... p_388298_) {
            this.putValue(p_387046_, getTerm(p_387046_, p_388209_, p_388298_));
            return this;
        }

        public final <T extends Comparable<T>> Condition.TerminalCondition negatedTerm(Property<T> p_387919_, T p_388646_) {
            this.putValue(p_387919_, "!" + p_387919_.getName(p_388646_));
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> Condition.TerminalCondition negatedTerm(Property<T> p_387468_, T p_387801_, T... p_387397_) {
            this.putValue(p_387468_, "!" + getTerm(p_387468_, p_387801_, p_387397_));
            return this;
        }

        public JsonElement get() {
            JsonObject jsonobject = new JsonObject();
            this.terms.forEach((p_386685_, p_388469_) -> jsonobject.addProperty(p_386685_.getName(), p_388469_));
            return jsonobject;
        }

        @Override
        public void validate(StateDefinition<?, ?> p_388691_) {
            List<Property<?>> list = this.terms
                .keySet()
                .stream()
                .filter(p_388807_ -> p_388691_.getProperty(p_388807_.getName()) != p_388807_)
                .collect(Collectors.toList());
            if (!list.isEmpty()) {
                throw new IllegalStateException("Properties " + list + " are missing from " + p_388691_);
            }
        }
    }
}
