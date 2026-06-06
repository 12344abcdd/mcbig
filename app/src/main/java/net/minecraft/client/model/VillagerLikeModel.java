package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface VillagerLikeModel {
    void hatVisible(boolean p_382812_);

    void translateToArms(PoseStack p_383014_);
}
