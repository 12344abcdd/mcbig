package net.minecraft.client.resources.model;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ResolvableModel {
    void resolveDependencies(ResolvableModel.Resolver p_387087_);

    @OnlyIn(Dist.CLIENT)
    public interface Resolver {
        UnbakedModel resolve(ResourceLocation p_388329_);
    }
}
