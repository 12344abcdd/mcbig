package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseModel extends AbstractEquineModel<EquineRenderState> {
    public HorseModel(ModelPart p_170668_) {
        super(p_170668_);
    }
}
