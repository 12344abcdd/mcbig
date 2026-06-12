package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Display;

public abstract class DisplayEntityRenderState extends EntityRenderState {
    @Nullable
    public Display.RenderState renderState;
    public float interpolationProgress;
    public float entityYRot;
    public float entityXRot;

    public abstract boolean hasSubState();
}
