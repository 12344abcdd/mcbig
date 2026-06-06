package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class PropertyDispatch {
    private final Map<Selector, List<Variant>> values = Maps.newHashMap();

    protected void putValue(Selector p_388825_, List<Variant> p_387974_) {
        List<Variant> list = this.values.put(p_388825_, p_387974_);
        if (list != null) {
            throw new IllegalStateException("Value " + p_388825_ + " is already defined");
        }
    }

    Map<Selector, List<Variant>> getEntries() {
        this.verifyComplete();
        return ImmutableMap.copyOf(this.values);
    }

    private void verifyComplete() {
        List<Property<?>> list = this.getDefinedProperties();
        Stream<Selector> stream = Stream.of(Selector.empty());

        for (Property<?> property : list) {
            stream = stream.flatMap(p_387875_ -> property.getAllValues().map(p_387875_::extend));
        }

        List<Selector> list1 = stream.filter(p_386514_ -> !this.values.containsKey(p_386514_)).collect(Collectors.toList());
        if (!list1.isEmpty()) {
            throw new IllegalStateException("Missing definition for properties: " + list1);
        }
    }

    abstract List<Property<?>> getDefinedProperties();

    public static <T1 extends Comparable<T1>> PropertyDispatch.C1<T1> property(Property<T1> p_386819_) {
        return new PropertyDispatch.C1<>(p_386819_);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>> PropertyDispatch.C2<T1, T2> properties(Property<T1> p_387480_, Property<T2> p_387328_) {
        return new PropertyDispatch.C2<>(p_387480_, p_387328_);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> PropertyDispatch.C3<T1, T2, T3> properties(
        Property<T1> p_386876_, Property<T2> p_387034_, Property<T3> p_387165_
    ) {
        return new PropertyDispatch.C3<>(p_386876_, p_387034_, p_387165_);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> PropertyDispatch.C4<T1, T2, T3, T4> properties(
        Property<T1> p_387365_, Property<T2> p_388534_, Property<T3> p_388376_, Property<T4> p_388082_
    ) {
        return new PropertyDispatch.C4<>(p_387365_, p_388534_, p_388376_, p_388082_);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>> PropertyDispatch.C5<T1, T2, T3, T4, T5> properties(
        Property<T1> p_387985_, Property<T2> p_386573_, Property<T3> p_388732_, Property<T4> p_388219_, Property<T5> p_387978_
    ) {
        return new PropertyDispatch.C5<>(p_387985_, p_386573_, p_388732_, p_388219_, p_387978_);
    }

    @OnlyIn(Dist.CLIENT)
    public static class C1<T1 extends Comparable<T1>> extends PropertyDispatch {
        private final Property<T1> property1;

        C1(Property<T1> p_386736_) {
            this.property1 = p_386736_;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1);
        }

        public PropertyDispatch.C1<T1> select(T1 p_388287_, List<Variant> p_388075_) {
            Selector selector = Selector.of(this.property1.value(p_388287_));
            this.putValue(selector, p_388075_);
            return this;
        }

        public PropertyDispatch.C1<T1> select(T1 p_387554_, Variant p_388353_) {
            return this.select(p_387554_, Collections.singletonList(p_388353_));
        }

        public PropertyDispatch generate(Function<T1, Variant> p_387942_) {
            this.property1.getPossibleValues().forEach(p_388930_ -> this.select((T1)p_388930_, p_387942_.apply((T1)p_388930_)));
            return this;
        }

        public PropertyDispatch generateList(Function<T1, List<Variant>> p_387024_) {
            this.property1.getPossibleValues().forEach(p_388573_ -> this.select((T1)p_388573_, p_387024_.apply((T1)p_388573_)));
            return this;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class C2<T1 extends Comparable<T1>, T2 extends Comparable<T2>> extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;

        C2(Property<T1> p_387463_, Property<T2> p_388076_) {
            this.property1 = p_387463_;
            this.property2 = p_388076_;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2);
        }

        public PropertyDispatch.C2<T1, T2> select(T1 p_386621_, T2 p_388046_, List<Variant> p_387277_) {
            Selector selector = Selector.of(this.property1.value(p_386621_), this.property2.value(p_388046_));
            this.putValue(selector, p_387277_);
            return this;
        }

        public PropertyDispatch.C2<T1, T2> select(T1 p_386656_, T2 p_387834_, Variant p_387619_) {
            return this.select(p_386656_, p_387834_, Collections.singletonList(p_387619_));
        }

        public PropertyDispatch generate(BiFunction<T1, T2, Variant> p_386812_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_388697_ -> this.property2
                            .getPossibleValues()
                            .forEach(p_386785_ -> this.select((T1)p_388697_, (T2)p_386785_, p_386812_.apply((T1)p_388697_, (T2)p_386785_)))
                );
            return this;
        }

        public PropertyDispatch generateList(BiFunction<T1, T2, List<Variant>> p_387733_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_387035_ -> this.property2
                            .getPossibleValues()
                            .forEach(p_386853_ -> this.select((T1)p_387035_, (T2)p_386853_, p_387733_.apply((T1)p_387035_, (T2)p_386853_)))
                );
            return this;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class C3<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;

        C3(Property<T1> p_387044_, Property<T2> p_388481_, Property<T3> p_386589_) {
            this.property1 = p_387044_;
            this.property2 = p_388481_;
            this.property3 = p_386589_;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2, this.property3);
        }

        public PropertyDispatch.C3<T1, T2, T3> select(T1 p_388711_, T2 p_387656_, T3 p_387096_, List<Variant> p_387405_) {
            Selector selector = Selector.of(this.property1.value(p_388711_), this.property2.value(p_387656_), this.property3.value(p_387096_));
            this.putValue(selector, p_387405_);
            return this;
        }

        public PropertyDispatch.C3<T1, T2, T3> select(T1 p_387229_, T2 p_387080_, T3 p_388837_, Variant p_388682_) {
            return this.select(p_387229_, p_387080_, p_388837_, Collections.singletonList(p_388682_));
        }

        public PropertyDispatch generate(PropertyDispatch.TriFunction<T1, T2, T3, Variant> p_388900_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_388004_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_387043_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_387450_ -> this.select(
                                                    (T1)p_388004_, (T2)p_387043_, (T3)p_387450_, p_388900_.apply((T1)p_388004_, (T2)p_387043_, (T3)p_387450_)
                                                )
                                        )
                            )
                );
            return this;
        }

        public PropertyDispatch generateList(PropertyDispatch.TriFunction<T1, T2, T3, List<Variant>> p_387811_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_387849_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_388797_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_387421_ -> this.select(
                                                    (T1)p_387849_, (T2)p_388797_, (T3)p_387421_, p_387811_.apply((T1)p_387849_, (T2)p_388797_, (T3)p_387421_)
                                                )
                                        )
                            )
                );
            return this;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class C4<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;
        private final Property<T4> property4;

        C4(Property<T1> p_387540_, Property<T2> p_386832_, Property<T3> p_387679_, Property<T4> p_387920_) {
            this.property1 = p_387540_;
            this.property2 = p_386832_;
            this.property3 = p_387679_;
            this.property4 = p_387920_;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2, this.property3, this.property4);
        }

        public PropertyDispatch.C4<T1, T2, T3, T4> select(T1 p_387500_, T2 p_386632_, T3 p_386444_, T4 p_386485_, List<Variant> p_386465_) {
            Selector selector = Selector.of(
                this.property1.value(p_387500_), this.property2.value(p_386632_), this.property3.value(p_386444_), this.property4.value(p_386485_)
            );
            this.putValue(selector, p_386465_);
            return this;
        }

        public PropertyDispatch.C4<T1, T2, T3, T4> select(T1 p_388487_, T2 p_388917_, T3 p_388589_, T4 p_387167_, Variant p_387235_) {
            return this.select(p_388487_, p_388917_, p_388589_, p_387167_, Collections.singletonList(p_387235_));
        }

        public PropertyDispatch generate(PropertyDispatch.QuadFunction<T1, T2, T3, T4, Variant> p_388943_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_386818_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_388830_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_387951_ -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        p_386803_ -> this.select(
                                                                (T1)p_386818_,
                                                                (T2)p_388830_,
                                                                (T3)p_387951_,
                                                                (T4)p_386803_,
                                                                p_388943_.apply((T1)p_386818_, (T2)p_388830_, (T3)p_387951_, (T4)p_386803_)
                                                            )
                                                    )
                                        )
                            )
                );
            return this;
        }

        public PropertyDispatch generateList(PropertyDispatch.QuadFunction<T1, T2, T3, T4, List<Variant>> p_388764_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_387677_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_386868_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_387590_ -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        p_387786_ -> this.select(
                                                                (T1)p_387677_,
                                                                (T2)p_386868_,
                                                                (T3)p_387590_,
                                                                (T4)p_387786_,
                                                                p_388764_.apply((T1)p_387677_, (T2)p_386868_, (T3)p_387590_, (T4)p_387786_)
                                                            )
                                                    )
                                        )
                            )
                );
            return this;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class C5<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>>
        extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;
        private final Property<T4> property4;
        private final Property<T5> property5;

        C5(Property<T1> p_386746_, Property<T2> p_388273_, Property<T3> p_386988_, Property<T4> p_386613_, Property<T5> p_386493_) {
            this.property1 = p_386746_;
            this.property2 = p_388273_;
            this.property3 = p_386988_;
            this.property4 = p_386613_;
            this.property5 = p_386493_;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2, this.property3, this.property4, this.property5);
        }

        public PropertyDispatch.C5<T1, T2, T3, T4, T5> select(T1 p_388392_, T2 p_386456_, T3 p_386640_, T4 p_386474_, T5 p_388406_, List<Variant> p_388463_) {
            Selector selector = Selector.of(
                this.property1.value(p_388392_),
                this.property2.value(p_386456_),
                this.property3.value(p_386640_),
                this.property4.value(p_386474_),
                this.property5.value(p_388406_)
            );
            this.putValue(selector, p_388463_);
            return this;
        }

        public PropertyDispatch.C5<T1, T2, T3, T4, T5> select(T1 p_387944_, T2 p_388683_, T3 p_388773_, T4 p_387929_, T5 p_388713_, Variant p_386466_) {
            return this.select(p_387944_, p_388683_, p_388773_, p_387929_, p_388713_, Collections.singletonList(p_386466_));
        }

        public PropertyDispatch generate(PropertyDispatch.PentaFunction<T1, T2, T3, T4, T5, Variant> p_387016_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_388135_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_388174_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_387752_ -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        p_388362_ -> this.property5
                                                                .getPossibleValues()
                                                                .forEach(
                                                                    p_388281_ -> this.select(
                                                                            (T1)p_388135_,
                                                                            (T2)p_388174_,
                                                                            (T3)p_387752_,
                                                                            (T4)p_388362_,
                                                                            (T5)p_388281_,
                                                                            p_387016_.apply(
                                                                                (T1)p_388135_, (T2)p_388174_, (T3)p_387752_, (T4)p_388362_, (T5)p_388281_
                                                                            )
                                                                        )
                                                                )
                                                    )
                                        )
                            )
                );
            return this;
        }

        public PropertyDispatch generateList(PropertyDispatch.PentaFunction<T1, T2, T3, T4, T5, List<Variant>> p_388365_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_387090_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_388322_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_388641_ -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        p_388001_ -> this.property5
                                                                .getPossibleValues()
                                                                .forEach(
                                                                    p_387883_ -> this.select(
                                                                            (T1)p_387090_,
                                                                            (T2)p_388322_,
                                                                            (T3)p_388641_,
                                                                            (T4)p_388001_,
                                                                            (T5)p_387883_,
                                                                            p_388365_.apply(
                                                                                (T1)p_387090_, (T2)p_388322_, (T3)p_388641_, (T4)p_388001_, (T5)p_387883_
                                                                            )
                                                                        )
                                                                )
                                                    )
                                        )
                            )
                );
            return this;
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface PentaFunction<P1, P2, P3, P4, P5, R> {
        R apply(P1 p_387441_, P2 p_388498_, P3 p_388335_, P4 p_387526_, P5 p_387583_);
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface QuadFunction<P1, P2, P3, P4, R> {
        R apply(P1 p_388149_, P2 p_387160_, P3 p_387565_, P4 p_387690_);
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface TriFunction<P1, P2, P3, R> {
        R apply(P1 p_386859_, P2 p_386986_, P3 p_386583_);
    }
}
