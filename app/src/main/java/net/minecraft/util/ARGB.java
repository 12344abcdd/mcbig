package net.minecraft.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ARGB {
    public static int alpha(int p_360646_) {
        return p_360646_ >>> 24;
    }

    public static int red(int p_365322_) {
        return p_365322_ >> 16 & 0xFF;
    }

    public static int green(int p_364426_) {
        return p_364426_ >> 8 & 0xFF;
    }

    public static int blue(int p_365014_) {
        return p_365014_ & 0xFF;
    }

    public static int color(int p_364075_, int p_361505_, int p_364686_, int p_361326_) {
        return p_364075_ << 24 | p_361505_ << 16 | p_364686_ << 8 | p_361326_;
    }

    public static int color(int p_364209_, int p_361249_, int p_362935_) {
        return color(255, p_364209_, p_361249_, p_362935_);
    }

    public static int color(Vec3 p_362659_) {
        return color(as8BitChannel((float)p_362659_.x()), as8BitChannel((float)p_362659_.y()), as8BitChannel((float)p_362659_.z()));
    }

    public static int multiply(int p_365204_, int p_362101_) {
        if (p_365204_ == -1) {
            return p_362101_;
        } else {
            return p_362101_ == -1
                ? p_365204_
                : color(
                    alpha(p_365204_) * alpha(p_362101_) / 255,
                    red(p_365204_) * red(p_362101_) / 255,
                    green(p_365204_) * green(p_362101_) / 255,
                    blue(p_365204_) * blue(p_362101_) / 255
                );
        }
    }

    public static int scaleRGB(int p_365021_, float p_363883_) {
        return scaleRGB(p_365021_, p_363883_, p_363883_, p_363883_);
    }

    public static int scaleRGB(int p_379627_, float p_379288_, float p_380277_, float p_380015_) {
        return color(
            alpha(p_379627_),
            Math.clamp((long)((int)((float)red(p_379627_) * p_379288_)), 0, 255),
            Math.clamp((long)((int)((float)green(p_379627_) * p_380277_)), 0, 255),
            Math.clamp((long)((int)((float)blue(p_379627_) * p_380015_)), 0, 255)
        );
    }

    public static int scaleRGB(int p_360775_, int p_363230_) {
        return color(
            alpha(p_360775_),
            Math.clamp((long)red(p_360775_) * (long)p_363230_ / 255L, 0, 255),
            Math.clamp((long)green(p_360775_) * (long)p_363230_ / 255L, 0, 255),
            Math.clamp((long)blue(p_360775_) * (long)p_363230_ / 255L, 0, 255)
        );
    }

    public static int greyscale(int p_363419_) {
        int i = (int)((float)red(p_363419_) * 0.3F + (float)green(p_363419_) * 0.59F + (float)blue(p_363419_) * 0.11F);
        return color(i, i, i);
    }

    public static int lerp(float p_361174_, int p_362196_, int p_364079_) {
        int i = Mth.lerpInt(p_361174_, alpha(p_362196_), alpha(p_364079_));
        int j = Mth.lerpInt(p_361174_, red(p_362196_), red(p_364079_));
        int k = Mth.lerpInt(p_361174_, green(p_362196_), green(p_364079_));
        int l = Mth.lerpInt(p_361174_, blue(p_362196_), blue(p_364079_));
        return color(i, j, k, l);
    }

    public static int opaque(int p_363279_) {
        return p_363279_ | 0xFF000000;
    }

    public static int transparent(int p_361588_) {
        return p_361588_ & 16777215;
    }

    public static int color(int p_360598_, int p_363344_) {
        return p_360598_ << 24 | p_363344_ & 16777215;
    }

    public static int white(float p_361517_) {
        return as8BitChannel(p_361517_) << 24 | 16777215;
    }

    public static int colorFromFloat(float p_361263_, float p_361756_, float p_363770_, float p_362317_) {
        return color(as8BitChannel(p_361263_), as8BitChannel(p_361756_), as8BitChannel(p_363770_), as8BitChannel(p_362317_));
    }

    public static Vector3f vector3fFromRGB24(int p_381022_) {
        float f = (float)red(p_381022_) / 255.0F;
        float f1 = (float)green(p_381022_) / 255.0F;
        float f2 = (float)blue(p_381022_) / 255.0F;
        return new Vector3f(f, f1, f2);
    }

    public static int average(int p_363294_, int p_365166_) {
        return color(
            (alpha(p_363294_) + alpha(p_365166_)) / 2,
            (red(p_363294_) + red(p_365166_)) / 2,
            (green(p_363294_) + green(p_365166_)) / 2,
            (blue(p_363294_) + blue(p_365166_)) / 2
        );
    }

    public static int as8BitChannel(float p_362531_) {
        return Mth.floor(p_362531_ * 255.0F);
    }

    public static float alphaFloat(int p_383239_) {
        return from8BitChannel(alpha(p_383239_));
    }

    public static float redFloat(int p_382787_) {
        return from8BitChannel(red(p_382787_));
    }

    public static float greenFloat(int p_382823_) {
        return from8BitChannel(green(p_382823_));
    }

    public static float blueFloat(int p_382858_) {
        return from8BitChannel(blue(p_382858_));
    }

    private static float from8BitChannel(int p_362888_) {
        return (float)p_362888_ / 255.0F;
    }

    public static int toABGR(int p_364127_) {
        return p_364127_ & -16711936 | (p_364127_ & 0xFF0000) >> 16 | (p_364127_ & 0xFF) << 16;
    }

    public static int fromABGR(int p_362222_) {
        return toABGR(p_362222_);
    }
}
