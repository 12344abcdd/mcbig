package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@Deprecated
@OnlyIn(Dist.CLIENT)
public abstract class AgeableMobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends MobRenderer<T, S, M> {
    private final M adultModel;
    private final M babyModel;

    public AgeableMobRenderer(EntityRendererProvider.Context p_360562_, M p_363747_, M p_361867_, float p_360815_) {
        super(p_360562_, p_363747_, p_360815_);
        this.adultModel = p_363747_;
        this.babyModel = p_361867_;
    }

    @Override
    public void render(S p_362004_, PoseStack p_361118_, MultiBufferSource p_363184_, int p_361276_) {
        this.model = p_362004_.isBaby ? this.babyModel : this.adultModel;
        super.render(p_362004_, p_361118_, p_363184_, p_361276_);
    }
}
