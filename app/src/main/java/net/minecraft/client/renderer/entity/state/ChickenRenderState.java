package net.minecraft.client.renderer.entity.state;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChickenRenderState extends LivingEntityRenderState {
    public float flap;
    public float flapSpeed;
}
