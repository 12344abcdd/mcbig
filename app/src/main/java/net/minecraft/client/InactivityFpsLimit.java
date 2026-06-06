package net.minecraft.client;

import com.mojang.serialization.Codec;
import net.minecraft.util.OptionEnum;
import net.minecraft.util.StringRepresentable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum InactivityFpsLimit implements OptionEnum, StringRepresentable {
    MINIMIZED(0, "minimized", "options.inactivityFpsLimit.minimized"),
    AFK(1, "afk", "options.inactivityFpsLimit.afk");

    public static final Codec<InactivityFpsLimit> CODEC = StringRepresentable.fromEnum(InactivityFpsLimit::values);
    private final int id;
    private final String serializedName;
    private final String key;

    private InactivityFpsLimit(int p_362220_, String p_363546_, String p_360831_) {
        this.id = p_362220_;
        this.serializedName = p_363546_;
        this.key = p_360831_;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
