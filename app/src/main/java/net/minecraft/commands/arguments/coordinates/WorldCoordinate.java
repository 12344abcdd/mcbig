package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.Component;

public class WorldCoordinate {
    private static final char PREFIX_RELATIVE = '~';
    public static final SimpleCommandExceptionType ERROR_EXPECTED_DOUBLE = new SimpleCommandExceptionType(Component.translatable("argument.pos.missing.double"));
    public static final SimpleCommandExceptionType ERROR_EXPECTED_INT = new SimpleCommandExceptionType(Component.translatable("argument.pos.missing.int"));
    private final boolean relative;
    private final me.alphamode.mcbig.math.BigDecimal value;

    public WorldCoordinate(boolean p_120864_, me.alphamode.mcbig.math.BigDecimal p_120865_) {
        this.relative = p_120864_;
        this.value = p_120865_;
    }

    public WorldCoordinate(boolean p_120864_, double p_120865_) {
        this(p_120864_, me.alphamode.mcbig.math.BigDecimal.val(p_120865_));
    }

    public me.alphamode.mcbig.math.BigDecimal get(me.alphamode.mcbig.math.BigDecimal p_120868_) {
        return this.relative ? this.value.add(p_120868_) : this.value;
    }

    public static WorldCoordinate parseDouble(StringReader p_120872_, boolean p_120873_) throws CommandSyntaxException {
        if (p_120872_.canRead() && p_120872_.peek() == '^') {
            throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(p_120872_);
        } else if (!p_120872_.canRead()) {
            throw ERROR_EXPECTED_DOUBLE.createWithContext(p_120872_);
        } else {
            boolean flag = isRelative(p_120872_);
            int i = p_120872_.getCursor();
            me.alphamode.mcbig.math.BigDecimal d0 = p_120872_.canRead() && p_120872_.peek() != ' ' ? me.alphamode.mcbig.math.BigDecimal.command(p_120872_) : me.alphamode.mcbig.math.BigDecimal.ZERO;
            String s = p_120872_.getString().substring(i, p_120872_.getCursor());
            if (flag && s.isEmpty()) {
                return new WorldCoordinate(true, me.alphamode.mcbig.math.BigDecimal.ZERO);
            } else {
                if (!s.contains(".") && !flag && p_120873_) {
                    d0 = d0.add(me.alphamode.mcbig.core.BigConstants.AABB);
                }

                return new WorldCoordinate(flag, d0);
            }
        }
    }

    public static WorldCoordinate parseInt(StringReader p_120870_) throws CommandSyntaxException {
        if (p_120870_.canRead() && p_120870_.peek() == '^') {
            throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(p_120870_);
        } else if (!p_120870_.canRead()) {
            throw ERROR_EXPECTED_INT.createWithContext(p_120870_);
        } else {
            boolean flag = isRelative(p_120870_);
            me.alphamode.mcbig.math.BigDecimal d0;
            if (p_120870_.canRead() && p_120870_.peek() != ' ') {
                d0 = flag ? me.alphamode.mcbig.math.BigDecimal.command(p_120870_) : me.alphamode.mcbig.math.BigInteger.command(p_120870_).toBigDecimal();
            } else {
                d0 = me.alphamode.mcbig.math.BigDecimal.ZERO;
            }

            return new WorldCoordinate(flag, d0);
        }
    }

    public static boolean isRelative(StringReader p_120875_) {
        boolean flag;
        if (p_120875_.peek() == '~') {
            flag = true;
            p_120875_.skip();
        } else {
            flag = false;
        }

        return flag;
    }

    @Override
    public boolean equals(Object p_120877_) {
        if (this == p_120877_) {
            return true;
        } else if (!(p_120877_ instanceof WorldCoordinate worldcoordinate)) {
            return false;
        } else {
            return this.relative != worldcoordinate.relative ? false : worldcoordinate.value.compareTo(this.value) == 0;
        }
    }

    @Override
    public int hashCode() {
        int i = this.relative ? 1 : 0;
        long j = Double.doubleToLongBits(this.value.doubleValue());
        return 31 * i + (int)(j ^ j >>> 32);
    }

    public boolean isRelative() {
        return this.relative;
    }
}
