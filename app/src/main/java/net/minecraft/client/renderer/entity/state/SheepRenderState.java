package net.minecraft.client.renderer.entity.state;

import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepRenderState extends LivingEntityRenderState {
    public float headEatPositionScale;
    public float headEatAngleScale;
    public boolean isSheared;
    public DyeColor woolColor = DyeColor.WHITE;
    public int id;
}
