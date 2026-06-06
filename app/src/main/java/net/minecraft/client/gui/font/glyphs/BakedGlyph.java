package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BakedGlyph {
    public static final float Z_FIGHTER = 0.001F;
    private final GlyphRenderTypes renderTypes;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private final float left;
    private final float right;
    private final float up;
    private final float down;

    public BakedGlyph(
        GlyphRenderTypes p_285527_,
        float p_285271_,
        float p_284970_,
        float p_285098_,
        float p_285023_,
        float p_285242_,
        float p_285043_,
        float p_285100_,
        float p_284948_
    ) {
        this.renderTypes = p_285527_;
        this.u0 = p_285271_;
        this.u1 = p_284970_;
        this.v0 = p_285098_;
        this.v1 = p_285023_;
        this.left = p_285242_;
        this.right = p_285043_;
        this.up = p_285100_;
        this.down = p_284948_;
    }

    public void renderChar(BakedGlyph.GlyphInstance p_380988_, Matrix4f p_381023_, VertexConsumer p_381084_, int p_381113_) {
        Style style = p_380988_.style();
        boolean flag = style.isItalic();
        float f = p_380988_.x();
        float f1 = p_380988_.y();
        int i = p_380988_.color();
        int j = p_380988_.shadowColor();
        boolean flag1 = style.isBold();
        if (p_380988_.hasShadow()) {
            this.render(flag, f + p_380988_.shadowOffset(), f1 + p_380988_.shadowOffset(), p_381023_, p_381084_, j, flag1, p_381113_);
            this.render(flag, f, f1, 0.03F, p_381023_, p_381084_, i, flag1, p_381113_);
        } else {
            this.render(flag, f, f1, p_381023_, p_381084_, i, flag1, p_381113_);
        }

        if (flag1) {
            if (p_380988_.hasShadow()) {
                this.render(
                    flag,
                    f + p_380988_.boldOffset() + p_380988_.shadowOffset(),
                    f1 + p_380988_.shadowOffset(),
                    0.001F,
                    p_381023_,
                    p_381084_,
                    j,
                    true,
                    p_381113_
                );
                this.render(flag, f + p_380988_.boldOffset(), f1, 0.03F, p_381023_, p_381084_, i, true, p_381113_);
            } else {
                this.render(flag, f + p_380988_.boldOffset(), f1, p_381023_, p_381084_, i, true, p_381113_);
            }
        }
    }

    private void render(
        boolean p_95227_, float p_95228_, float p_95229_, Matrix4f p_253706_, VertexConsumer p_95231_, int p_95236_, boolean p_383085_, int p_379666_
    ) {
        this.render(p_95227_, p_95228_, p_95229_, 0.0F, p_253706_, p_95231_, p_95236_, p_383085_, p_379666_);
    }

    private void render(
        boolean p_383040_,
        float p_382836_,
        float p_382914_,
        float p_382815_,
        Matrix4f p_382916_,
        VertexConsumer p_382963_,
        int p_383056_,
        boolean p_382998_,
        int p_382909_
    ) {
        float f = p_382836_ + this.left;
        float f1 = p_382836_ + this.right;
        float f2 = p_382914_ + this.up;
        float f3 = p_382914_ + this.down;
        float f4 = p_383040_ ? 1.0F - 0.25F * this.up : 0.0F;
        float f5 = p_383040_ ? 1.0F - 0.25F * this.down : 0.0F;
        float f6 = p_382998_ ? 0.1F : 0.0F;
        p_382963_.addVertex(p_382916_, f + f4 - f6, f2 - f6, p_382815_).setColor(p_383056_).setUv(this.u0, this.v0).setLight(p_382909_);
        p_382963_.addVertex(p_382916_, f + f5 - f6, f3 + f6, p_382815_).setColor(p_383056_).setUv(this.u0, this.v1).setLight(p_382909_);
        p_382963_.addVertex(p_382916_, f1 + f5 + f6, f3 + f6, p_382815_).setColor(p_383056_).setUv(this.u1, this.v1).setLight(p_382909_);
        p_382963_.addVertex(p_382916_, f1 + f4 + f6, f2 - f6, p_382815_).setColor(p_383056_).setUv(this.u1, this.v0).setLight(p_382909_);
    }

    public void renderEffect(BakedGlyph.Effect p_95221_, Matrix4f p_254370_, VertexConsumer p_95223_, int p_95224_) {
        if (p_95221_.hasShadow()) {
            this.buildEffect(p_95221_, p_95221_.shadowOffset(), 0.0F, p_95221_.shadowColor(), p_95223_, p_95224_, p_254370_);
            this.buildEffect(p_95221_, 0.0F, 0.03F, p_95221_.color, p_95223_, p_95224_, p_254370_);
        } else {
            this.buildEffect(p_95221_, 0.0F, 0.0F, p_95221_.color, p_95223_, p_95224_, p_254370_);
        }
    }

    private void buildEffect(
        BakedGlyph.Effect p_382788_, float p_383227_, float p_382946_, int p_382966_, VertexConsumer p_383218_, int p_382874_, Matrix4f p_383104_
    ) {
        p_383218_.addVertex(p_383104_, p_382788_.x0 + p_383227_, p_382788_.y0 + p_383227_, p_382788_.depth + p_382946_)
            .setColor(p_382966_)
            .setUv(this.u0, this.v0)
            .setLight(p_382874_);
        p_383218_.addVertex(p_383104_, p_382788_.x1 + p_383227_, p_382788_.y0 + p_383227_, p_382788_.depth + p_382946_)
            .setColor(p_382966_)
            .setUv(this.u0, this.v1)
            .setLight(p_382874_);
        p_383218_.addVertex(p_383104_, p_382788_.x1 + p_383227_, p_382788_.y1 + p_383227_, p_382788_.depth + p_382946_)
            .setColor(p_382966_)
            .setUv(this.u1, this.v1)
            .setLight(p_382874_);
        p_383218_.addVertex(p_383104_, p_382788_.x0 + p_383227_, p_382788_.y1 + p_383227_, p_382788_.depth + p_382946_)
            .setColor(p_382966_)
            .setUv(this.u1, this.v0)
            .setLight(p_382874_);
    }

    public RenderType renderType(Font.DisplayMode p_181388_) {
        return this.renderTypes.select(p_181388_);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Effect(float x0, float y0, float x1, float y1, float depth, int color, int shadowColor, float shadowOffset) {
        public Effect(float p_95247_, float p_95248_, float p_95249_, float p_95250_, float p_95251_, int p_379708_) {
            this(p_95247_, p_95248_, p_95249_, p_95250_, p_95251_, p_379708_, 0, 0.0F);
        }

        boolean hasShadow() {
            return this.shadowColor() != 0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record GlyphInstance(float x, float y, int color, int shadowColor, BakedGlyph glyph, Style style, float boldOffset, float shadowOffset) {
        boolean hasShadow() {
            return this.shadowColor() != 0;
        }
    }
}
