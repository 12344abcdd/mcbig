package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiPartGenerator implements BlockStateGenerator {
    private final Block block;
    private final List<MultiPartGenerator.Entry> parts = Lists.newArrayList();

    private MultiPartGenerator(Block p_387028_) {
        this.block = p_387028_;
    }

    @Override
    public Block getBlock() {
        return this.block;
    }

    public static MultiPartGenerator multiPart(Block p_388674_) {
        return new MultiPartGenerator(p_388674_);
    }

    public MultiPartGenerator with(List<Variant> p_388187_) {
        this.parts.add(new MultiPartGenerator.Entry(p_388187_));
        return this;
    }

    public MultiPartGenerator with(Variant p_386925_) {
        return this.with(ImmutableList.of(p_386925_));
    }

    public MultiPartGenerator with(Condition p_386636_, List<Variant> p_387831_) {
        this.parts.add(new MultiPartGenerator.ConditionalEntry(p_386636_, p_387831_));
        return this;
    }

    public MultiPartGenerator with(Condition p_386500_, Variant... p_387262_) {
        return this.with(p_386500_, ImmutableList.copyOf(p_387262_));
    }

    public MultiPartGenerator with(Condition p_387558_, Variant p_387640_) {
        return this.with(p_387558_, ImmutableList.of(p_387640_));
    }

    public JsonElement get() {
        StateDefinition<Block, BlockState> statedefinition = this.block.getStateDefinition();
        this.parts.forEach(p_387270_ -> p_387270_.validate(statedefinition));
        JsonArray jsonarray = new JsonArray();
        this.parts.stream().map(MultiPartGenerator.Entry::get).forEach(jsonarray::add);
        JsonObject jsonobject = new JsonObject();
        jsonobject.add("multipart", jsonarray);
        return jsonobject;
    }

    @OnlyIn(Dist.CLIENT)
    static class ConditionalEntry extends MultiPartGenerator.Entry {
        private final Condition condition;

        ConditionalEntry(Condition p_388387_, List<Variant> p_386881_) {
            super(p_386881_);
            this.condition = p_388387_;
        }

        @Override
        public void validate(StateDefinition<?, ?> p_387844_) {
            this.condition.validate(p_387844_);
        }

        @Override
        public void decorate(JsonObject p_387633_) {
            p_387633_.add("when", this.condition.get());
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Entry implements Supplier<JsonElement> {
        private final List<Variant> variants;

        Entry(List<Variant> p_388550_) {
            this.variants = p_388550_;
        }

        public void validate(StateDefinition<?, ?> p_388493_) {
        }

        public void decorate(JsonObject p_387170_) {
        }

        public JsonElement get() {
            JsonObject jsonobject = new JsonObject();
            this.decorate(jsonobject);
            jsonobject.add("apply", Variant.convertList(this.variants));
            return jsonobject;
        }
    }
}
