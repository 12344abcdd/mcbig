package net.minecraft.core;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface WritableRegistry<T> extends Registry<T> {
    Holder.Reference<T> register(ResourceKey<T> p_256320_, T p_255978_, RegistrationInfo p_326122_);

    void bindTag(TagKey<T> p_362404_, List<Holder<T>> p_365163_);

    boolean isEmpty();

    HolderGetter<T> createRegistrationLookup();
}
