package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class FallingLeavesParticle extends TextureSheetParticle {
    private static final float ACCELERATION_SCALE = 0.0025F;
    private static final int INITIAL_LIFETIME = 300;
    private static final int CURVE_ENDPOINT_TIME = 300;
    private float rotSpeed;
    private final float particleRandom;
    private final float spinAcceleration;
    private final float windBig;
    private boolean swirl;
    private boolean flowAway;
    private double xaFlowScale;
    private double zaFlowScale;
    private double swirlPeriod;

    protected FallingLeavesParticle(
        ClientLevel p_383228_,
        double p_383201_,
        double p_382929_,
        double p_382848_,
        SpriteSet p_382995_,
        float p_383015_,
        float p_383012_,
        boolean p_383052_,
        boolean p_382926_,
        float p_383165_,
        float p_382942_
    ) {
        super(p_383228_, p_383201_, p_382929_, p_382848_);
        this.setSprite(p_382995_.get(this.random.nextInt(12), 12));
        this.rotSpeed = (float)Math.toRadians(this.random.nextBoolean() ? -30.0 : 30.0);
        this.particleRandom = this.random.nextFloat();
        this.spinAcceleration = (float)Math.toRadians(this.random.nextBoolean() ? -5.0 : 5.0);
        this.windBig = p_383012_;
        this.swirl = p_383052_;
        this.flowAway = p_382926_;
        this.lifetime = 300;
        this.gravity = p_383015_ * 1.2F * 0.0025F;
        float f = p_383165_ * (this.random.nextBoolean() ? 0.05F : 0.075F);
        this.quadSize = f;
        this.setSize(f, f);
        this.friction = 1.0F;
        this.yd = (double)(-p_382942_);
        this.xaFlowScale = Math.cos(Math.toRadians((double)(this.particleRandom * 60.0F))) * (double)this.windBig;
        this.zaFlowScale = Math.sin(Math.toRadians((double)(this.particleRandom * 60.0F))) * (double)this.windBig;
        this.swirlPeriod = Math.toRadians((double)(1000.0F + this.particleRandom * 3000.0F));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        }

        if (!this.removed) {
            float f = (float)(300 - this.lifetime);
            float f1 = Math.min(f / 300.0F, 1.0F);
            double d0 = 0.0;
            double d1 = 0.0;
            if (this.flowAway) {
                d0 += this.xaFlowScale * Math.pow((double)f1, 1.25);
                d1 += this.zaFlowScale * Math.pow((double)f1, 1.25);
            }

            if (this.swirl) {
                d0 += (double)f1 * Math.cos((double)f1 * this.swirlPeriod) * (double)this.windBig;
                d1 += (double)f1 * Math.sin((double)f1 * this.swirlPeriod) * (double)this.windBig;
            }

            this.xd += d0 * 0.0025F;
            this.zd += d1 * 0.0025F;
            this.yd = this.yd - (double)this.gravity;
            this.rotSpeed = this.rotSpeed + this.spinAcceleration / 20.0F;
            this.oRoll = this.roll;
            this.roll = this.roll + this.rotSpeed / 20.0F;
            this.move(this.xd, this.yd, this.zd);
            if (this.onGround || this.lifetime < 299 && (this.xd == 0.0 || this.zd == 0.0)) {
                this.remove();
            }

            if (!this.removed) {
                this.xd = this.xd * (double)this.friction;
                this.yd = this.yd * (double)this.friction;
                this.zd = this.zd * (double)this.friction;
            }
        }
    }

    public static class CherryProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public CherryProvider(SpriteSet p_383119_) {
            this.sprites = p_383119_;
        }

        public Particle createParticle(
            SimpleParticleType p_383195_,
            ClientLevel p_383226_,
            double p_382840_,
            double p_382854_,
            double p_382910_,
            double p_382859_,
            double p_382947_,
            double p_383154_
        ) {
            return new FallingLeavesParticle(p_383226_, p_382840_, p_382854_, p_382910_, this.sprites, 0.25F, 2.0F, false, true, 1.0F, 0.0F);
        }
    }

    public static class PaleOakProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public PaleOakProvider(SpriteSet p_383092_) {
            this.sprites = p_383092_;
        }

        public Particle createParticle(
            SimpleParticleType p_383203_,
            ClientLevel p_383110_,
            double p_383063_,
            double p_382907_,
            double p_383062_,
            double p_382964_,
            double p_382864_,
            double p_382906_
        ) {
            return new FallingLeavesParticle(p_383110_, p_383063_, p_382907_, p_383062_, this.sprites, 0.07F, 10.0F, true, false, 2.0F, 0.021F);
        }
    }
}
