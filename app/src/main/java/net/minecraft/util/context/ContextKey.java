package net.minecraft.util.context;

import net.minecraft.resources.ResourceLocation;

public class ContextKey<T> {
    private final ResourceLocation name;

    public ContextKey(ResourceLocation p_380935_) {
        this.name = p_380935_;
    }

    public static <T> ContextKey<T> vanilla(String p_381171_) {
        return new ContextKey<>(ResourceLocation.withDefaultNamespace(p_381171_));
    }

    public ResourceLocation name() {
        return this.name;
    }

    @Override
    public String toString() {
        return "<parameter " + this.name + ">";
    }
}
