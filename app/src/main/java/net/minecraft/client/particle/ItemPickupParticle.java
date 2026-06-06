package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemPickupParticle extends Particle {
    private static final int LIFE_TIME = 3;
    private final Entity itemEntity;
    private final Entity target;
    private int life;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double targetXOld;
    private double targetYOld;
    private double targetZOld;

    public ItemPickupParticle(EntityRenderDispatcher p_107029_, ClientLevel p_107031_, Entity p_107032_, Entity p_107033_) {
        this(p_107029_, p_107031_, p_107032_, p_107033_, p_107032_.getDeltaMovement());
    }

    private ItemPickupParticle(EntityRenderDispatcher p_107023_, ClientLevel p_107025_, Entity p_107026_, Entity p_107027_, Vec3 p_382893_) {
        super(p_107025_, p_107026_.getX(), p_107026_.getY(), p_107026_.getZ(), p_382893_.x, p_382893_.y, p_382893_.z);
        this.itemEntity = this.getSafeCopy(p_107026_);
        this.target = p_107027_;
        this.entityRenderDispatcher = p_107023_;
        this.updatePosition();
        this.saveOldPosition();
    }

    private Entity getSafeCopy(Entity p_107037_) {
        return (Entity)(!(p_107037_ instanceof ItemEntity) ? p_107037_ : ((ItemEntity)p_107037_).copy());
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void renderCustom(PoseStack p_383231_, MultiBufferSource p_382868_, Camera p_382930_, float p_382826_) {
        float f = ((float)this.life + p_382826_) / 3.0F;
        f *= f;
        double d0 = Mth.lerp((double)p_382826_, this.targetXOld, this.targetX);
        double d1 = Mth.lerp((double)p_382826_, this.targetYOld, this.targetY);
        double d2 = Mth.lerp((double)p_382826_, this.targetZOld, this.targetZ);
        double d3 = Mth.lerp((double)f, this.itemEntity.getX(), d0);
        double d4 = Mth.lerp((double)f, this.itemEntity.getY(), d1);
        double d5 = Mth.lerp((double)f, this.itemEntity.getZ(), d2);
        me.alphamode.mcbig.core.BigVec3 vec3 = p_382930_.getPosition();
        this.entityRenderDispatcher
            .render(
                this.itemEntity,
                d3 - vec3.x(),
                d4 - vec3.y(),
                d5 - vec3.z(),
                p_382826_,
                new PoseStack(),
                p_382868_,
                this.entityRenderDispatcher.getPackedLightCoords(this.itemEntity, p_382826_)
            );
    }

    @Override
    public void render(VertexConsumer p_107039_, Camera p_107040_, float p_107041_) {
    }

    @Override
    public void tick() {
        this.life++;
        if (this.life == 3) {
            this.remove();
        }

        this.saveOldPosition();
        this.updatePosition();
    }

    private void updatePosition() {
        this.targetX = this.target.getX();
        this.targetY = (this.target.getY() + this.target.getEyeY()) / 2.0;
        this.targetZ = this.target.getZ();
    }

    private void saveOldPosition() {
        this.targetXOld = this.targetX;
        this.targetYOld = this.targetY;
        this.targetZOld = this.targetZ;
    }
}
