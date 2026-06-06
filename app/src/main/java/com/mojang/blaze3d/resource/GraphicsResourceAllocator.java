package com.mojang.blaze3d.resource;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface GraphicsResourceAllocator {
    GraphicsResourceAllocator UNPOOLED = new GraphicsResourceAllocator() {
        @Override
        public <T> T acquire(ResourceDescriptor<T> p_362261_) {
            return p_362261_.allocate();
        }

        @Override
        public <T> void release(ResourceDescriptor<T> p_363699_, T p_364295_) {
            p_363699_.free(p_364295_);
        }
    };

    <T> T acquire(ResourceDescriptor<T> p_362669_);

    <T> void release(ResourceDescriptor<T> p_361831_, T p_363754_);
}
