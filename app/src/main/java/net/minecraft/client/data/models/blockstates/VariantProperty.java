package net.minecraft.client.data.models.blockstates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Function;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VariantProperty<T> {
    final String key;
    final Function<T, JsonElement> serializer;

    public VariantProperty(String p_387446_, Function<T, JsonElement> p_388157_) {
        this.key = p_387446_;
        this.serializer = p_388157_;
    }

    public VariantProperty<T>.Value withValue(T p_388129_) {
        return new VariantProperty.Value(p_388129_);
    }

    @Override
    public String toString() {
        return this.key;
    }

    @OnlyIn(Dist.CLIENT)
    public class Value {
        private final T value;

        public Value(T p_387190_) {
            this.value = p_387190_;
        }

        public VariantProperty<T> getKey() {
            return VariantProperty.this;
        }

        public void addToVariant(JsonObject p_386895_) {
            p_386895_.add(VariantProperty.this.key, VariantProperty.this.serializer.apply(this.value));
        }

        @Override
        public String toString() {
            return VariantProperty.this.key + "=" + this.value;
        }
    }
}
