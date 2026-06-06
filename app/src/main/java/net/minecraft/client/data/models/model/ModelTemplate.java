package net.minecraft.client.data.models.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelTemplate {
    private final Optional<ResourceLocation> model;
    private final Set<TextureSlot> requiredSlots;
    private final Optional<String> suffix;

    public ModelTemplate(Optional<ResourceLocation> p_388104_, Optional<String> p_386919_, TextureSlot... p_388141_) {
        this.model = p_388104_;
        this.suffix = p_386919_;
        this.requiredSlots = ImmutableSet.copyOf(p_388141_);
    }

    public ResourceLocation getDefaultModelLocation(Block p_386647_) {
        return ModelLocationUtils.getModelLocation(p_386647_, this.suffix.orElse(""));
    }

    public ResourceLocation create(Block p_387250_, TextureMapping p_386985_, BiConsumer<ResourceLocation, ModelInstance> p_388339_) {
        return this.create(ModelLocationUtils.getModelLocation(p_387250_, this.suffix.orElse("")), p_386985_, p_388339_);
    }

    public ResourceLocation createWithSuffix(Block p_388627_, String p_388661_, TextureMapping p_386633_, BiConsumer<ResourceLocation, ModelInstance> p_388050_) {
        return this.create(ModelLocationUtils.getModelLocation(p_388627_, p_388661_ + this.suffix.orElse("")), p_386633_, p_388050_);
    }

    public ResourceLocation createWithOverride(
        Block p_388829_, String p_388013_, TextureMapping p_386709_, BiConsumer<ResourceLocation, ModelInstance> p_386791_
    ) {
        return this.create(ModelLocationUtils.getModelLocation(p_388829_, p_388013_), p_386709_, p_386791_);
    }

    public ResourceLocation create(Item p_386721_, TextureMapping p_387847_, BiConsumer<ResourceLocation, ModelInstance> p_386603_) {
        return this.create(ModelLocationUtils.getModelLocation(p_386721_, this.suffix.orElse("")), p_387847_, p_386603_);
    }

    public ResourceLocation create(ResourceLocation p_388380_, TextureMapping p_387099_, BiConsumer<ResourceLocation, ModelInstance> p_387748_) {
        Map<TextureSlot, ResourceLocation> map = this.createMap(p_387099_);
        p_387748_.accept(p_388380_, () -> {
            JsonObject jsonobject = new JsonObject();
            this.model.ifPresent(p_388657_ -> jsonobject.addProperty("parent", p_388657_.toString()));
            if (!map.isEmpty()) {
                JsonObject jsonobject1 = new JsonObject();
                map.forEach((p_387287_, p_386479_) -> jsonobject1.addProperty(p_387287_.getId(), p_386479_.toString()));
                jsonobject.add("textures", jsonobject1);
            }

            return jsonobject;
        });
        return p_388380_;
    }

    private Map<TextureSlot, ResourceLocation> createMap(TextureMapping p_387972_) {
        return Streams.concat(this.requiredSlots.stream(), p_387972_.getForced()).collect(ImmutableMap.toImmutableMap(Function.identity(), p_387972_::get));
    }
}
