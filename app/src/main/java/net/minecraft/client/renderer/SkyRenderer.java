package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class SkyRenderer implements AutoCloseable {
    private static final ResourceLocation SUN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
    private static final ResourceLocation MOON_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    public static final ResourceLocation END_SKY_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");
    private static final float SKY_DISC_RADIUS = 512.0F;
    private final VertexBuffer starBuffer = VertexBuffer.uploadStatic(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION, this::buildStars);
    private final VertexBuffer topSkyBuffer = VertexBuffer.uploadStatic(
        VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION, p_383202_ -> this.buildSkyDisc(p_383202_, 16.0F)
    );
    private final VertexBuffer bottomSkyBuffer = VertexBuffer.uploadStatic(
        VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION, p_382968_ -> this.buildSkyDisc(p_382968_, -16.0F)
    );
    private final VertexBuffer endSkyBuffer = VertexBuffer.uploadStatic(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, this::buildEndSky);

    private void buildStars(VertexConsumer p_383173_) {
        RandomSource randomsource = RandomSource.create(10842L);
        int i = 1500;
        float f = 100.0F;

        for (int j = 0; j < 1500; j++) {
            float f1 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f2 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f3 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f4 = 0.15F + randomsource.nextFloat() * 0.1F;
            float f5 = Mth.lengthSquared(f1, f2, f3);
            if (!(f5 <= 0.010000001F) && !(f5 >= 1.0F)) {
                Vector3f vector3f = new Vector3f(f1, f2, f3).normalize(100.0F);
                float f6 = (float)(randomsource.nextDouble() * (float) Math.PI * 2.0);
                Matrix3f matrix3f = new Matrix3f().rotateTowards(new Vector3f(vector3f).negate(), new Vector3f(0.0F, 1.0F, 0.0F)).rotateZ(-f6);
                p_383173_.addVertex(new Vector3f(f4, -f4, 0.0F).mul(matrix3f).add(vector3f));
                p_383173_.addVertex(new Vector3f(f4, f4, 0.0F).mul(matrix3f).add(vector3f));
                p_383173_.addVertex(new Vector3f(-f4, f4, 0.0F).mul(matrix3f).add(vector3f));
                p_383173_.addVertex(new Vector3f(-f4, -f4, 0.0F).mul(matrix3f).add(vector3f));
            }
        }
    }

    private void buildSkyDisc(VertexConsumer p_382865_, float p_361373_) {
        float f = Math.signum(p_361373_) * 512.0F;
        p_382865_.addVertex(0.0F, p_361373_, 0.0F);

        for (int i = -180; i <= 180; i += 45) {
            p_382865_.addVertex(f * Mth.cos((float)i * (float) (Math.PI / 180.0)), p_361373_, 512.0F * Mth.sin((float)i * (float) (Math.PI / 180.0)));
        }
    }

    public void renderSkyDisc(float p_362027_, float p_360898_, float p_362939_) {
        RenderSystem.setShaderColor(p_362027_, p_360898_, p_362939_, 1.0F);
        this.topSkyBuffer.drawWithRenderType(RenderType.sky());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void renderDarkDisc(PoseStack p_360284_) {
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        p_360284_.pushPose();
        p_360284_.translate(0.0F, 12.0F, 0.0F);
        this.bottomSkyBuffer.drawWithRenderType(RenderType.sky());
        p_360284_.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void renderSunMoonAndStars(
        PoseStack p_363513_,
        MultiBufferSource.BufferSource p_382843_,
        float p_362201_,
        int p_362572_,
        float p_362569_,
        float p_363542_,
        FogParameters p_361918_
    ) {
        p_363513_.pushPose();
        p_363513_.mulPose(Axis.YP.rotationDegrees(-90.0F));
        p_363513_.mulPose(Axis.XP.rotationDegrees(p_362201_ * 360.0F));
        this.renderSun(p_362569_, p_382843_, p_363513_);
        this.renderMoon(p_362572_, p_362569_, p_382843_, p_363513_);
        p_382843_.endBatch();
        if (p_363542_ > 0.0F) {
            this.renderStars(p_361918_, p_363542_, p_363513_);
        }

        p_363513_.popPose();
    }

    private void renderSun(float p_362331_, MultiBufferSource p_383061_, PoseStack p_361665_) {
        float f = 30.0F;
        float f1 = 100.0F;
        VertexConsumer vertexconsumer = p_383061_.getBuffer(RenderType.celestial(SUN_LOCATION));
        int i = ARGB.white(p_362331_);
        Matrix4f matrix4f = p_361665_.last().pose();
        vertexconsumer.addVertex(matrix4f, -30.0F, 100.0F, -30.0F).setUv(0.0F, 0.0F).setColor(i);
        vertexconsumer.addVertex(matrix4f, 30.0F, 100.0F, -30.0F).setUv(1.0F, 0.0F).setColor(i);
        vertexconsumer.addVertex(matrix4f, 30.0F, 100.0F, 30.0F).setUv(1.0F, 1.0F).setColor(i);
        vertexconsumer.addVertex(matrix4f, -30.0F, 100.0F, 30.0F).setUv(0.0F, 1.0F).setColor(i);
    }

    private void renderMoon(int p_364754_, float p_362497_, MultiBufferSource p_382845_, PoseStack p_362676_) {
        float f = 20.0F;
        int i = p_364754_ % 4;
        int j = p_364754_ / 4 % 2;
        float f1 = (float)(i + 0) / 4.0F;
        float f2 = (float)(j + 0) / 2.0F;
        float f3 = (float)(i + 1) / 4.0F;
        float f4 = (float)(j + 1) / 2.0F;
        float f5 = 100.0F;
        VertexConsumer vertexconsumer = p_382845_.getBuffer(RenderType.celestial(MOON_LOCATION));
        int k = ARGB.white(p_362497_);
        Matrix4f matrix4f = p_362676_.last().pose();
        vertexconsumer.addVertex(matrix4f, -20.0F, -100.0F, 20.0F).setUv(f3, f4).setColor(k);
        vertexconsumer.addVertex(matrix4f, 20.0F, -100.0F, 20.0F).setUv(f1, f4).setColor(k);
        vertexconsumer.addVertex(matrix4f, 20.0F, -100.0F, -20.0F).setUv(f1, f2).setColor(k);
        vertexconsumer.addVertex(matrix4f, -20.0F, -100.0F, -20.0F).setUv(f3, f2).setColor(k);
    }

    private void renderStars(FogParameters p_365253_, float p_361150_, PoseStack p_365236_) {
        Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
        matrix4fstack.pushMatrix();
        matrix4fstack.mul(p_365236_.last().pose());
        RenderSystem.setShaderColor(p_361150_, p_361150_, p_361150_, p_361150_);
        RenderSystem.setShaderFog(FogParameters.NO_FOG);
        this.starBuffer.drawWithRenderType(RenderType.stars());
        RenderSystem.setShaderFog(p_365253_);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrix4fstack.popMatrix();
    }

    public void renderSunriseAndSunset(PoseStack p_362809_, MultiBufferSource.BufferSource p_383216_, float p_364108_, int p_361766_) {
        p_362809_.pushPose();
        p_362809_.mulPose(Axis.XP.rotationDegrees(90.0F));
        float f = Mth.sin(p_364108_) < 0.0F ? 180.0F : 0.0F;
        p_362809_.mulPose(Axis.ZP.rotationDegrees(f));
        p_362809_.mulPose(Axis.ZP.rotationDegrees(90.0F));
        Matrix4f matrix4f = p_362809_.last().pose();
        VertexConsumer vertexconsumer = p_383216_.getBuffer(RenderType.sunriseSunset());
        float f1 = ARGB.alphaFloat(p_361766_);
        vertexconsumer.addVertex(matrix4f, 0.0F, 100.0F, 0.0F).setColor(p_361766_);
        int i = ARGB.transparent(p_361766_);
        int j = 16;

        for (int k = 0; k <= 16; k++) {
            float f2 = (float)k * (float) (Math.PI * 2) / 16.0F;
            float f3 = Mth.sin(f2);
            float f4 = Mth.cos(f2);
            vertexconsumer.addVertex(matrix4f, f3 * 120.0F, f4 * 120.0F, -f4 * 40.0F * f1).setColor(i);
        }

        p_362809_.popPose();
    }

    private void buildEndSky(VertexConsumer p_382981_) {
        for (int i = 0; i < 6; i++) {
            Matrix4f matrix4f = new Matrix4f();
            switch (i) {
                case 1:
                    matrix4f.rotationX((float) (Math.PI / 2));
                    break;
                case 2:
                    matrix4f.rotationX((float) (-Math.PI / 2));
                    break;
                case 3:
                    matrix4f.rotationX((float) Math.PI);
                    break;
                case 4:
                    matrix4f.rotationZ((float) (Math.PI / 2));
                    break;
                case 5:
                    matrix4f.rotationZ((float) (-Math.PI / 2));
            }

            p_382981_.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F).setColor(-14145496);
            p_382981_.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setUv(0.0F, 16.0F).setColor(-14145496);
            p_382981_.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setUv(16.0F, 16.0F).setColor(-14145496);
            p_382981_.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setUv(16.0F, 0.0F).setColor(-14145496);
        }
    }

    public void renderEndSky() {
        this.endSkyBuffer.drawWithRenderType(RenderType.endSky());
    }

    @Override
    public void close() {
        this.starBuffer.close();
        this.topSkyBuffer.close();
        this.bottomSkyBuffer.close();
        this.endSkyBuffer.close();
    }
}
