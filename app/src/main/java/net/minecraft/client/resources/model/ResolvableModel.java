package net.minecraft.client.resources.model;

import net.minecraft.resources.ResourceLocation;

public interface ResolvableModel {
    void resolveDependencies(ResolvableModel.Resolver p_387087_);

    public interface Resolver {
        UnbakedModel resolve(ResourceLocation p_388329_);
    }
}
