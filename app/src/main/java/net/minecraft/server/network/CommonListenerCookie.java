package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ClientInformation;

public record CommonListenerCookie(GameProfile gameProfile, int latency, ClientInformation clientInformation, boolean transferred) {
    public static CommonListenerCookie createInitial(GameProfile p_302024_, boolean p_320180_) {
        return new CommonListenerCookie(p_302024_, 0, ClientInformation.createDefault(), p_320180_);
    }
}
