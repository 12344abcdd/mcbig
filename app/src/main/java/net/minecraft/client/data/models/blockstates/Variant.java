package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Variant implements Supplier<JsonElement> {
    private final Map<VariantProperty<?>, VariantProperty<?>.Value> values = Maps.newLinkedHashMap();

    public <T> Variant with(VariantProperty<T> p_387154_, T p_387177_) {
        VariantProperty<?>.Value variantproperty = this.values.put(p_387154_, p_387154_.withValue(p_387177_));
        if (variantproperty != null) {
            throw new IllegalStateException("Replacing value of " + variantproperty + " with " + p_387177_);
        } else {
            return this;
        }
    }

    public static Variant variant() {
        return new Variant();
    }

    public static Variant merge(Variant p_386537_, Variant p_388369_) {
        Variant variant = new Variant();
        variant.values.putAll(p_386537_.values);
        variant.values.putAll(p_388369_.values);
        return variant;
    }

    public JsonElement get() {
        JsonObject jsonobject = new JsonObject();
        this.values.values().forEach(p_387061_ -> p_387061_.addToVariant(jsonobject));
        return jsonobject;
    }

    public static JsonElement convertList(List<Variant> p_388085_) {
        if (p_388085_.size() == 1) {
            return p_388085_.get(0).get();
        } else {
            JsonArray jsonarray = new JsonArray();
            p_388085_.forEach(p_386684_ -> jsonarray.add(p_386684_.get()));
            return jsonarray;
        }
    }
}
