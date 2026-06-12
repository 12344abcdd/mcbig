package net.minecraft.client.renderer.block.model;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.level.block.state.BlockState;

public interface UnbakedBlockStateModel extends ResolvableModel {
    BakedModel bake(ModelBaker p_388139_);

    Object visualEqualityGroup(BlockState p_360620_);
}
