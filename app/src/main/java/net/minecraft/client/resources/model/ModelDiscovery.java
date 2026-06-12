package net.minecraft.client.resources.model;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class ModelDiscovery {
    static final Logger LOGGER = LogUtils.getLogger();
    private final Map<ResourceLocation, UnbakedModel> inputModels;
    final UnbakedModel missingModel;
    private final List<ResolvableModel> topModels = new ArrayList<>();
    private final Map<ResourceLocation, UnbakedModel> referencedModels = new HashMap<>();

    public ModelDiscovery(Map<ResourceLocation, UnbakedModel> p_360750_, UnbakedModel p_365355_) {
        this.inputModels = p_360750_;
        this.missingModel = p_365355_;
        this.referencedModels.put(MissingBlockModel.LOCATION, p_365355_);
    }

    public void registerSpecialModels() {
        this.referencedModels.put(ItemModelGenerator.GENERATED_ITEM_MODEL_ID, new ItemModelGenerator());
    }

    public void addRoot(ResolvableModel p_388596_) {
        this.topModels.add(p_388596_);
    }

    public void discoverDependencies() {
        this.topModels.forEach(p_386259_ -> p_386259_.resolveDependencies(new ModelDiscovery.ResolverImpl()));
    }

    public Map<ResourceLocation, UnbakedModel> getReferencedModels() {
        return this.referencedModels;
    }

    public Set<ResourceLocation> getUnreferencedModels() {
        return Sets.difference(this.inputModels.keySet(), this.referencedModels.keySet());
    }

    UnbakedModel getBlockModel(ResourceLocation p_360328_) {
        return this.referencedModels.computeIfAbsent(p_360328_, this::loadBlockModel);
    }

    private UnbakedModel loadBlockModel(ResourceLocation p_361274_) {
        UnbakedModel unbakedmodel = this.inputModels.get(p_361274_);
        if (unbakedmodel == null) {
            LOGGER.warn("Missing block model: '{}'", p_361274_);
            return this.missingModel;
        } else {
            return unbakedmodel;
        }
    }

    class ResolverImpl implements ResolvableModel.Resolver {
        private final List<ResourceLocation> stack = new ArrayList<>();
        private final Set<ResourceLocation> resolvedModels = new HashSet<>();

        @Override
        public UnbakedModel resolve(ResourceLocation p_361784_) {
            if (this.stack.contains(p_361784_)) {
                ModelDiscovery.LOGGER.warn("Detected model loading loop: {}->{}", this.stacktraceToString(), p_361784_);
                return ModelDiscovery.this.missingModel;
            } else {
                UnbakedModel unbakedmodel = ModelDiscovery.this.getBlockModel(p_361784_);
                if (this.resolvedModels.add(p_361784_)) {
                    this.stack.add(p_361784_);
                    unbakedmodel.resolveDependencies(this);
                    this.stack.remove(p_361784_);
                }

                return unbakedmodel;
            }
        }

        private String stacktraceToString() {
            return this.stack.stream().map(ResourceLocation::toString).collect(Collectors.joining("->"));
        }
    }
}
