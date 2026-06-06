package com.mojang.blaze3d.framegraph;

import com.mojang.blaze3d.resource.ResourceDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface FramePass {
    <T> ResourceHandle<T> createsInternal(String p_361080_, ResourceDescriptor<T> p_361202_);

    <T> void reads(ResourceHandle<T> p_363657_);

    <T> ResourceHandle<T> readsAndWrites(ResourceHandle<T> p_364074_);

    void requires(FramePass p_364258_);

    void disableCulling();

    void executes(Runnable p_363152_);
}
