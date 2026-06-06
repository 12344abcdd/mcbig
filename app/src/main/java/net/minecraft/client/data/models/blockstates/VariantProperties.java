package net.minecraft.client.data.models.blockstates;

import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VariantProperties {
    public static final VariantProperty<VariantProperties.Rotation> X_ROT = new VariantProperty<>("x", p_387011_ -> new JsonPrimitive(p_387011_.value));
    public static final VariantProperty<VariantProperties.Rotation> Y_ROT = new VariantProperty<>("y", p_387473_ -> new JsonPrimitive(p_387473_.value));
    public static final VariantProperty<ResourceLocation> MODEL = new VariantProperty<>("model", p_387591_ -> new JsonPrimitive(p_387591_.toString()));
    public static final VariantProperty<Boolean> UV_LOCK = new VariantProperty<>("uvlock", JsonPrimitive::new);
    public static final VariantProperty<Integer> WEIGHT = new VariantProperty<>("weight", JsonPrimitive::new);

    @OnlyIn(Dist.CLIENT)
    public static enum Rotation {
        R0(0),
        R90(90),
        R180(180),
        R270(270);

        final int value;

        private Rotation(int p_387625_) {
            this.value = p_387625_;
        }
    }
}
