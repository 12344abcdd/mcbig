package com.mojang.realmsclient.dto;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record RealmsSettings(boolean hardcore) {
}
