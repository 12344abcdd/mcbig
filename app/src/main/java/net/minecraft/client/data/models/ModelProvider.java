package net.minecraft.client.data.models;

import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.data.models.blockstates.BlockStateGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelProvider implements DataProvider {
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider itemInfoPathProvider;
    private final PackOutput.PathProvider modelPathProvider;

    public ModelProvider(PackOutput p_388260_) {
        this.blockStatePathProvider = p_388260_.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.itemInfoPathProvider = p_388260_.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.modelPathProvider = p_388260_.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput p_387857_) {
        ModelProvider.ItemInfoCollector modelprovider$iteminfocollector = new ModelProvider.ItemInfoCollector();
        ModelProvider.BlockStateGeneratorCollector modelprovider$blockstategeneratorcollector = new ModelProvider.BlockStateGeneratorCollector();
        ModelProvider.SimpleModelCollector modelprovider$simplemodelcollector = new ModelProvider.SimpleModelCollector();
        new BlockModelGenerators(modelprovider$blockstategeneratorcollector, modelprovider$iteminfocollector, modelprovider$simplemodelcollector).run();
        new ItemModelGenerators(modelprovider$iteminfocollector, modelprovider$simplemodelcollector).run();
        modelprovider$blockstategeneratorcollector.validate();
        modelprovider$iteminfocollector.finalizeAndValidate();
        return CompletableFuture.allOf(
            modelprovider$blockstategeneratorcollector.save(p_387857_, this.blockStatePathProvider),
            modelprovider$simplemodelcollector.save(p_387857_, this.modelPathProvider),
            modelprovider$iteminfocollector.save(p_387857_, this.itemInfoPathProvider)
        );
    }

    static <T> CompletableFuture<?> saveAll(CachedOutput p_387084_, Function<T, Path> p_386455_, Map<T, ? extends Supplier<JsonElement>> p_386585_) {
        return DataProvider.saveAll(p_387084_, Supplier::get, p_386455_, p_386585_);
    }

    @Override
    public final String getName() {
        return "Model Definitions";
    }

    @OnlyIn(Dist.CLIENT)
    static class BlockStateGeneratorCollector implements Consumer<BlockStateGenerator> {
        private final Map<Block, BlockStateGenerator> generators = new HashMap<>();

        public void accept(BlockStateGenerator p_388748_) {
            Block block = p_388748_.getBlock();
            BlockStateGenerator blockstategenerator = this.generators.put(block, p_388748_);
            if (blockstategenerator != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + block);
            }
        }

        public void validate() {
            Stream<Holder.Reference<Block>> stream = BuiltInRegistries.BLOCK.listElements().filter(p_388333_ -> true);
            List<ResourceLocation> list = stream.filter(p_386843_ -> !this.generators.containsKey(p_386843_.value()))
                .map(p_386823_ -> p_386823_.key().location())
                .toList();
            if (!list.isEmpty()) {
                throw new IllegalStateException("Missing blockstate definitions for: " + list);
            }
        }

        public CompletableFuture<?> save(CachedOutput p_388014_, PackOutput.PathProvider p_388192_) {
            return ModelProvider.saveAll(p_388014_, p_387598_ -> p_388192_.json(p_387598_.builtInRegistryHolder().key().location()), this.generators);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ItemInfoCollector implements ItemModelOutput {
        private final Map<Item, ClientItem> itemInfos = new HashMap<>();
        private final Map<Item, Item> copies = new HashMap<>();

        @Override
        public void accept(Item p_387063_, ItemModel.Unbaked p_388578_) {
            this.register(p_387063_, new ClientItem(p_388578_, ClientItem.Properties.DEFAULT));
        }

        private void register(Item p_388205_, ClientItem p_388233_) {
            ClientItem clientitem = this.itemInfos.put(p_388205_, p_388233_);
            if (clientitem != null) {
                throw new IllegalStateException("Duplicate item model definition for " + p_388205_);
            }
        }

        @Override
        public void copy(Item p_386920_, Item p_386789_) {
            this.copies.put(p_386789_, p_386920_);
        }

        public void finalizeAndValidate() {
            BuiltInRegistries.ITEM.forEach(p_388426_ -> {
                if (!this.copies.containsKey(p_388426_)) {
                    if (p_388426_ instanceof BlockItem blockitem && !this.itemInfos.containsKey(blockitem)) {
                        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(blockitem.getBlock());
                        this.accept(blockitem, ItemModelUtils.plainModel(resourcelocation));
                    }
                }
            });
            this.copies.forEach((p_386494_, p_386575_) -> {
                ClientItem clientitem = this.itemInfos.get(p_386575_);
                if (clientitem == null) {
                    throw new IllegalStateException("Missing donor: " + p_386575_ + " -> " + p_386494_);
                } else {
                    this.register(p_386494_, clientitem);
                }
            });
            List<ResourceLocation> list = BuiltInRegistries.ITEM
                .listElements()
                .filter(p_388636_ -> !this.itemInfos.containsKey(p_388636_.value()))
                .map(p_388278_ -> p_388278_.key().location())
                .toList();
            if (!list.isEmpty()) {
                throw new IllegalStateException("Missing item model definitions for: " + list);
            }
        }

        public CompletableFuture<?> save(CachedOutput p_387552_, PackOutput.PathProvider p_388501_) {
            return DataProvider.saveAll(
                p_387552_, ClientItem.CODEC, p_388594_ -> p_388501_.json(p_388594_.builtInRegistryHolder().key().location()), this.itemInfos
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class SimpleModelCollector implements BiConsumer<ResourceLocation, ModelInstance> {
        private final Map<ResourceLocation, ModelInstance> models = new HashMap<>();

        public void accept(ResourceLocation p_388633_, ModelInstance p_388119_) {
            Supplier<JsonElement> supplier = this.models.put(p_388633_, p_388119_);
            if (supplier != null) {
                throw new IllegalStateException("Duplicate model definition for " + p_388633_);
            }
        }

        public CompletableFuture<?> save(CachedOutput p_386795_, PackOutput.PathProvider p_388673_) {
            return ModelProvider.saveAll(p_386795_, p_388673_::json, this.models);
        }
    }
}
