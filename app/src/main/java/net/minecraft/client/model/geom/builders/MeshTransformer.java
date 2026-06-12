package net.minecraft.client.model.geom.builders;

import net.minecraft.client.model.geom.PartPose;

@FunctionalInterface
public interface MeshTransformer {
    static MeshTransformer scaling(float p_365200_) {
        float f = 24.016F * (1.0F - p_365200_);
        return p_362687_ -> p_362687_.transformed(p_362796_ -> p_362796_.scaled(p_365200_).translated(0.0F, f, 0.0F));
    }

    MeshDefinition apply(MeshDefinition p_364993_);
}
