package net.minecraft.client.renderer.block.model;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface UnbakedBlockStateModel extends ResolvableModel {
    BakedModel bake(ModelBaker p_388139_);

    Object visualEqualityGroup(BlockState p_360620_);
}
