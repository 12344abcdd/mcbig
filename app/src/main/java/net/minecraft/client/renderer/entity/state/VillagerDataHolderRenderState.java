package net.minecraft.client.renderer.entity.state;

import net.minecraft.world.entity.npc.VillagerData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface VillagerDataHolderRenderState {
    VillagerData getVillagerData();
}
