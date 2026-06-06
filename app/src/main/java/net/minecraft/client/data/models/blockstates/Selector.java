package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class Selector {
    private static final Selector EMPTY = new Selector(ImmutableList.of());
    private static final Comparator<Property.Value<?>> COMPARE_BY_NAME = Comparator.comparing(p_387747_ -> p_387747_.property().getName());
    private final List<Property.Value<?>> values;

    public Selector extend(Property.Value<?> p_386584_) {
        return new Selector(ImmutableList.<Property.Value<?>>builder().addAll(this.values).add(p_386584_).build());
    }

    public Selector extend(Selector p_388421_) {
        return new Selector(ImmutableList.<Property.Value<?>>builder().addAll(this.values).addAll(p_388421_.values).build());
    }

    private Selector(List<Property.Value<?>> p_388438_) {
        this.values = p_388438_;
    }

    public static Selector empty() {
        return EMPTY;
    }

    public static Selector of(Property.Value<?>... p_388186_) {
        return new Selector(ImmutableList.copyOf(p_388186_));
    }

    @Override
    public boolean equals(Object p_387038_) {
        return this == p_387038_ || p_387038_ instanceof Selector && this.values.equals(((Selector)p_387038_).values);
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    public String getKey() {
        return this.values.stream().sorted(COMPARE_BY_NAME).map(Property.Value::toString).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return this.getKey();
    }
}
