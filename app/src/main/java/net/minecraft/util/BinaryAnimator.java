package net.minecraft.util;

public class BinaryAnimator {
    private final int animationLength;
    private final BinaryAnimator.EasingFunction easingFunction;
    private int ticks;
    private int ticksOld;

    public BinaryAnimator(int p_363393_, BinaryAnimator.EasingFunction p_361668_) {
        this.animationLength = p_363393_;
        this.easingFunction = p_361668_;
    }

    public BinaryAnimator(int p_361095_) {
        this(p_361095_, p_362335_ -> p_362335_);
    }

    public void tick(boolean p_363738_) {
        this.ticksOld = this.ticks;
        if (p_363738_) {
            if (this.ticks < this.animationLength) {
                this.ticks++;
            }
        } else if (this.ticks > 0) {
            this.ticks--;
        }
    }

    public float getFactor(float p_360599_) {
        float f = Mth.lerp(p_360599_, (float)this.ticksOld, (float)this.ticks) / (float)this.animationLength;
        return this.easingFunction.apply(f);
    }

    public interface EasingFunction {
        float apply(float p_363887_);
    }
}
