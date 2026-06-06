package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.world.entity.player.Input;

public record InputPredicate(
    Optional<Boolean> forward,
    Optional<Boolean> backward,
    Optional<Boolean> left,
    Optional<Boolean> right,
    Optional<Boolean> jump,
    Optional<Boolean> sneak,
    Optional<Boolean> sprint
) {
    public static final Codec<InputPredicate> CODEC = RecordCodecBuilder.create(
        p_371185_ -> p_371185_.group(
                    Codec.BOOL.optionalFieldOf("forward").forGetter(InputPredicate::forward),
                    Codec.BOOL.optionalFieldOf("backward").forGetter(InputPredicate::backward),
                    Codec.BOOL.optionalFieldOf("left").forGetter(InputPredicate::left),
                    Codec.BOOL.optionalFieldOf("right").forGetter(InputPredicate::right),
                    Codec.BOOL.optionalFieldOf("jump").forGetter(InputPredicate::jump),
                    Codec.BOOL.optionalFieldOf("sneak").forGetter(InputPredicate::sneak),
                    Codec.BOOL.optionalFieldOf("sprint").forGetter(InputPredicate::sprint)
                )
                .apply(p_371185_, InputPredicate::new)
    );

    public boolean matches(Input p_371459_) {
        return this.matches(this.forward, p_371459_.forward())
            && this.matches(this.backward, p_371459_.backward())
            && this.matches(this.left, p_371459_.left())
            && this.matches(this.right, p_371459_.right())
            && this.matches(this.jump, p_371459_.jump())
            && this.matches(this.sneak, p_371459_.shift())
            && this.matches(this.sprint, p_371459_.sprint());
    }

    private boolean matches(Optional<Boolean> p_371721_, boolean p_371618_) {
        return p_371721_.<Boolean>map(p_371317_ -> p_371317_ == p_371618_).orElse(true);
    }
}
