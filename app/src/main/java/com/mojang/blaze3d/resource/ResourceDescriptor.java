package com.mojang.blaze3d.resource;


public interface ResourceDescriptor<T> {
    T allocate();

    void free(T p_363676_);
}
