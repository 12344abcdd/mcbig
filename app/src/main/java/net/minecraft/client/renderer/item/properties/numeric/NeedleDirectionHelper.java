package net.minecraft.client.renderer.item.properties.numeric;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class NeedleDirectionHelper {
    private final boolean wobble;

    protected NeedleDirectionHelper(boolean p_388777_) {
        this.wobble = p_388777_;
    }

    public float get(ItemStack p_386635_, @Nullable ClientLevel p_387256_, @Nullable LivingEntity p_388745_, int p_387873_) {
        Entity entity = (Entity)(p_388745_ != null ? p_388745_ : p_386635_.getEntityRepresentation());
        if (entity == null) {
            return 0.0F;
        } else {
            if (p_387256_ == null && entity.level() instanceof ClientLevel clientlevel) {
                p_387256_ = clientlevel;
            }

            return p_387256_ == null ? 0.0F : this.calculate(p_386635_, p_387256_, p_387873_, entity);
        }
    }

    protected abstract float calculate(ItemStack p_386601_, ClientLevel p_387798_, int p_387568_, Entity p_388687_);

    protected boolean wobble() {
        return this.wobble;
    }

    protected NeedleDirectionHelper.Wobbler newWobbler(float p_387658_) {
        return this.wobble ? standardWobbler(p_387658_) : nonWobbler();
    }

    public static NeedleDirectionHelper.Wobbler standardWobbler(final float p_388906_) {
        return new NeedleDirectionHelper.Wobbler() {
            private float rotation;
            private float deltaRotation;
            private long lastUpdateTick;

            @Override
            public float rotation() {
                return this.rotation;
            }

            @Override
            public boolean shouldUpdate(long p_387925_) {
                return this.lastUpdateTick != p_387925_;
            }

            @Override
            public void update(long p_387682_, float p_388243_) {
                this.lastUpdateTick = p_387682_;
                float f = Mth.positiveModulo(p_388243_ - this.rotation + 0.5F, 1.0F) - 0.5F;
                this.deltaRotation += f * 0.1F;
                this.deltaRotation = this.deltaRotation * p_388906_;
                this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0F);
            }
        };
    }

    public static NeedleDirectionHelper.Wobbler nonWobbler() {
        return new NeedleDirectionHelper.Wobbler() {
            private float targetValue;

            @Override
            public float rotation() {
                return this.targetValue;
            }

            @Override
            public boolean shouldUpdate(long p_388006_) {
                return true;
            }

            @Override
            public void update(long p_388810_, float p_387609_) {
                this.targetValue = p_387609_;
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public interface Wobbler {
        float rotation();

        boolean shouldUpdate(long p_386830_);

        void update(long p_388155_, float p_388782_);
    }
}
