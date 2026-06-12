package net.minecraft.client.renderer.entity.state;


public class PigRenderState extends LivingEntityRenderState implements SaddleableRenderState {
    public boolean isSaddled;

    @Override
    public boolean isSaddled() {
        return this.isSaddled;
    }
}
