package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BreakingItemParticle extends TextureSheetParticle {
    private final float uo;
    private final float vo;

    BreakingItemParticle(
        ClientLevel p_105646_,
        double p_105647_,
        double p_105648_,
        double p_105649_,
        double p_105650_,
        double p_105651_,
        double p_105652_,
        ItemStackRenderState p_386668_
    ) {
        this(p_105646_, p_105647_, p_105648_, p_105649_, p_386668_);
        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
        this.xd += p_105650_;
        this.yd += p_105651_;
        this.zd += p_105652_;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    protected BreakingItemParticle(ClientLevel p_105665_, double p_105666_, double p_105667_, double p_105668_, ItemStackRenderState p_386446_) {
        super(p_105665_, p_105666_, p_105667_, p_105668_, 0.0, 0.0, 0.0);
        TextureAtlasSprite textureatlassprite = p_386446_.pickParticleIcon(this.random);
        if (textureatlassprite != null) {
            this.setSprite(textureatlassprite);
        } else {
            this.setSprite(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation()));
        }

        this.gravity = 1.0F;
        this.quadSize /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uo / 4.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0F) / 4.0F);
    }

    public static class CobwebProvider extends BreakingItemParticle.ItemParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType p_338579_,
            ClientLevel p_338749_,
            double p_338877_,
            double p_338362_,
            double p_338343_,
            double p_338303_,
            double p_338217_,
            double p_338683_
        ) {
            return new BreakingItemParticle(p_338749_, p_338877_, p_338362_, p_338343_, this.calculateState(new ItemStack(Items.COBWEB), p_338749_));
        }
    }

    public abstract static class ItemParticleProvider<T extends ParticleOptions> implements ParticleProvider<T> {
        private final ItemStackRenderState scratchRenderState = new ItemStackRenderState();

        protected ItemStackRenderState calculateState(ItemStack p_387481_, ClientLevel p_388432_) {
            Minecraft.getInstance()
                .getItemModelResolver()
                .updateForTopItem(this.scratchRenderState, p_387481_, ItemDisplayContext.GROUND, false, p_388432_, null, 0);
            return this.scratchRenderState;
        }
    }

    public static class Provider extends BreakingItemParticle.ItemParticleProvider<ItemParticleOption> {
        public Particle createParticle(
            ItemParticleOption p_105677_,
            ClientLevel p_105678_,
            double p_105679_,
            double p_105680_,
            double p_105681_,
            double p_105682_,
            double p_105683_,
            double p_105684_
        ) {
            return new BreakingItemParticle(
                p_105678_, p_105679_, p_105680_, p_105681_, p_105682_, p_105683_, p_105684_, this.calculateState(p_105677_.getItem(), p_105678_)
            );
        }
    }

    public static class SlimeProvider extends BreakingItemParticle.ItemParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType p_105705_,
            ClientLevel p_105706_,
            double p_105707_,
            double p_105708_,
            double p_105709_,
            double p_105710_,
            double p_105711_,
            double p_105712_
        ) {
            return new BreakingItemParticle(p_105706_, p_105707_, p_105708_, p_105709_, this.calculateState(new ItemStack(Items.SLIME_BALL), p_105706_));
        }
    }

    public static class SnowballProvider extends BreakingItemParticle.ItemParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType p_105724_,
            ClientLevel p_105725_,
            double p_105726_,
            double p_105727_,
            double p_105728_,
            double p_105729_,
            double p_105730_,
            double p_105731_
        ) {
            return new BreakingItemParticle(p_105725_, p_105726_, p_105727_, p_105728_, this.calculateState(new ItemStack(Items.SNOWBALL), p_105725_));
        }
    }
}
