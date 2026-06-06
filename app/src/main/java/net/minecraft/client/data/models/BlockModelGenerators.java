package net.minecraft.client.data.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.color.item.GrassColorSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.blockstates.BlockStateGenerator;
import net.minecraft.client.data.models.blockstates.Condition;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.special.BannerSpecialRenderer;
import net.minecraft.client.renderer.special.BedSpecialRenderer;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.renderer.special.ConduitSpecialRenderer;
import net.minecraft.client.renderer.special.DecoratedPotSpecialRenderer;
import net.minecraft.client.renderer.special.ShulkerBoxSpecialRenderer;
import net.minecraft.client.renderer.special.SkullSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.MossyCarpetBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockModelGenerators {
    final Consumer<BlockStateGenerator> blockStateOutput;
    final ItemModelOutput itemModelOutput;
    final BiConsumer<ResourceLocation, ModelInstance> modelOutput;
    final List<Block> nonOrientableTrapdoor = ImmutableList.of(Blocks.OAK_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.IRON_TRAPDOOR);
    final Map<Block, BlockModelGenerators.BlockStateGeneratorSupplier> fullBlockModelCustomGenerators = ImmutableMap.<Block, BlockModelGenerators.BlockStateGeneratorSupplier>builder()
        .put(Blocks.STONE, BlockModelGenerators::createMirroredCubeGenerator)
        .put(Blocks.DEEPSLATE, BlockModelGenerators::createMirroredColumnGenerator)
        .put(Blocks.MUD_BRICKS, BlockModelGenerators::createNorthWestMirroredCubeGenerator)
        .build();
    final Map<Block, TexturedModel> texturedModels = ImmutableMap.<Block, TexturedModel>builder()
        .put(Blocks.SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL.get(Blocks.SANDSTONE))
        .put(Blocks.RED_SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL.get(Blocks.RED_SANDSTONE))
        .put(Blocks.SMOOTH_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top")))
        .put(Blocks.SMOOTH_RED_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top")))
        .put(
            Blocks.CUT_SANDSTONE,
            TexturedModel.COLUMN
                .get(Blocks.SANDSTONE)
                .updateTextures(p_387400_ -> p_387400_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_SANDSTONE)))
        )
        .put(
            Blocks.CUT_RED_SANDSTONE,
            TexturedModel.COLUMN
                .get(Blocks.RED_SANDSTONE)
                .updateTextures(p_387050_ -> p_387050_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_RED_SANDSTONE)))
        )
        .put(Blocks.QUARTZ_BLOCK, TexturedModel.COLUMN.get(Blocks.QUARTZ_BLOCK))
        .put(Blocks.SMOOTH_QUARTZ, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.QUARTZ_BLOCK, "_bottom")))
        .put(Blocks.BLACKSTONE, TexturedModel.COLUMN_WITH_WALL.get(Blocks.BLACKSTONE))
        .put(Blocks.DEEPSLATE, TexturedModel.COLUMN_WITH_WALL.get(Blocks.DEEPSLATE))
        .put(
            Blocks.CHISELED_QUARTZ_BLOCK,
            TexturedModel.COLUMN
                .get(Blocks.CHISELED_QUARTZ_BLOCK)
                .updateTextures(p_388696_ -> p_388696_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_QUARTZ_BLOCK)))
        )
        .put(Blocks.CHISELED_SANDSTONE, TexturedModel.COLUMN.get(Blocks.CHISELED_SANDSTONE).updateTextures(p_386968_ -> {
            p_386968_.put(TextureSlot.END, TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top"));
            p_386968_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_SANDSTONE));
        }))
        .put(Blocks.CHISELED_RED_SANDSTONE, TexturedModel.COLUMN.get(Blocks.CHISELED_RED_SANDSTONE).updateTextures(p_388607_ -> {
            p_388607_.put(TextureSlot.END, TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top"));
            p_388607_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_RED_SANDSTONE));
        }))
        .put(Blocks.CHISELED_TUFF_BRICKS, TexturedModel.COLUMN_WITH_WALL.get(Blocks.CHISELED_TUFF_BRICKS))
        .put(Blocks.CHISELED_TUFF, TexturedModel.COLUMN_WITH_WALL.get(Blocks.CHISELED_TUFF))
        .build();
    static final Map<BlockFamily.Variant, BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>> SHAPE_CONSUMERS = ImmutableMap.<BlockFamily.Variant, BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>>builder()
        .put(BlockFamily.Variant.BUTTON, BlockModelGenerators.BlockFamilyProvider::button)
        .put(BlockFamily.Variant.DOOR, BlockModelGenerators.BlockFamilyProvider::door)
        .put(BlockFamily.Variant.CHISELED, BlockModelGenerators.BlockFamilyProvider::fullBlockVariant)
        .put(BlockFamily.Variant.CRACKED, BlockModelGenerators.BlockFamilyProvider::fullBlockVariant)
        .put(BlockFamily.Variant.CUSTOM_FENCE, BlockModelGenerators.BlockFamilyProvider::customFence)
        .put(BlockFamily.Variant.FENCE, BlockModelGenerators.BlockFamilyProvider::fence)
        .put(BlockFamily.Variant.CUSTOM_FENCE_GATE, BlockModelGenerators.BlockFamilyProvider::customFenceGate)
        .put(BlockFamily.Variant.FENCE_GATE, BlockModelGenerators.BlockFamilyProvider::fenceGate)
        .put(BlockFamily.Variant.SIGN, BlockModelGenerators.BlockFamilyProvider::sign)
        .put(BlockFamily.Variant.SLAB, BlockModelGenerators.BlockFamilyProvider::slab)
        .put(BlockFamily.Variant.STAIRS, BlockModelGenerators.BlockFamilyProvider::stairs)
        .put(BlockFamily.Variant.PRESSURE_PLATE, BlockModelGenerators.BlockFamilyProvider::pressurePlate)
        .put(BlockFamily.Variant.TRAPDOOR, BlockModelGenerators.BlockFamilyProvider::trapdoor)
        .put(BlockFamily.Variant.WALL, BlockModelGenerators.BlockFamilyProvider::wall)
        .build();
    public static final List<Pair<Direction, Function<ResourceLocation, Variant>>> MULTIFACE_GENERATOR = List.of(
        Pair.of(Direction.NORTH, p_387778_ -> Variant.variant().with(VariantProperties.MODEL, p_387778_)),
        Pair.of(
            Direction.EAST,
            p_388821_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_388821_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            Direction.SOUTH,
            p_388197_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_388197_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            Direction.WEST,
            p_387054_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_387054_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            Direction.UP,
            p_387302_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_387302_)
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            Direction.DOWN,
            p_388466_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_388466_)
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
        )
    );
    private static final Map<BlockModelGenerators.BookSlotModelCacheKey, ResourceLocation> CHISELED_BOOKSHELF_SLOT_MODEL_CACHE = new HashMap<>();

    private static BlockStateGenerator createMirroredCubeGenerator(
        Block p_386814_, ResourceLocation p_388382_, TextureMapping p_388177_, BiConsumer<ResourceLocation, ModelInstance> p_387825_
    ) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_MIRRORED_ALL.create(p_386814_, p_388177_, p_387825_);
        return createRotatedVariant(p_386814_, p_388382_, resourcelocation);
    }

    private static BlockStateGenerator createNorthWestMirroredCubeGenerator(
        Block p_387597_, ResourceLocation p_387148_, TextureMapping p_387083_, BiConsumer<ResourceLocation, ModelInstance> p_388766_
    ) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_NORTH_WEST_MIRRORED_ALL.create(p_387597_, p_387083_, p_388766_);
        return createSimpleBlock(p_387597_, resourcelocation);
    }

    private static BlockStateGenerator createMirroredColumnGenerator(
        Block p_388746_, ResourceLocation p_388722_, TextureMapping p_388473_, BiConsumer<ResourceLocation, ModelInstance> p_388658_
    ) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN_MIRRORED.create(p_388746_, p_388473_, p_388658_);
        return createRotatedVariant(p_388746_, p_388722_, resourcelocation).with(createRotatedPillar());
    }

    public BlockModelGenerators(Consumer<BlockStateGenerator> p_387996_, ItemModelOutput p_387053_, BiConsumer<ResourceLocation, ModelInstance> p_387066_) {
        this.blockStateOutput = p_387996_;
        this.itemModelOutput = p_387053_;
        this.modelOutput = p_387066_;
    }

    private void registerSimpleItemModel(Item p_388475_, ResourceLocation p_388320_) {
        this.itemModelOutput.accept(p_388475_, ItemModelUtils.plainModel(p_388320_));
    }

    void registerSimpleItemModel(Block p_387646_, ResourceLocation p_386845_) {
        this.itemModelOutput.accept(p_387646_.asItem(), ItemModelUtils.plainModel(p_386845_));
    }

    private void registerSimpleTintedItemModel(Block p_387395_, ResourceLocation p_386834_, ItemTintSource p_388066_) {
        this.itemModelOutput.accept(p_387395_.asItem(), ItemModelUtils.tintedModel(p_386834_, p_388066_));
    }

    private ResourceLocation createFlatItemModel(Item p_387763_) {
        return ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(p_387763_), TextureMapping.layer0(p_387763_), this.modelOutput);
    }

    ResourceLocation createFlatItemModelWithBlockTexture(Item p_388334_, Block p_388118_) {
        return ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(p_388334_), TextureMapping.layer0(p_388118_), this.modelOutput);
    }

    private ResourceLocation createFlatItemModelWithBlockTexture(Item p_387141_, Block p_386598_, String p_388025_) {
        return ModelTemplates.FLAT_ITEM
            .create(
                ModelLocationUtils.getModelLocation(p_387141_), TextureMapping.layer0(TextureMapping.getBlockTexture(p_386598_, p_388025_)), this.modelOutput
            );
    }

    ResourceLocation createFlatItemModelWithBlockTextureAndOverlay(Item p_388212_, Block p_388074_, String p_387507_) {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(p_388074_);
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(p_388074_, p_387507_);
        return ModelTemplates.TWO_LAYERED_ITEM
            .create(ModelLocationUtils.getModelLocation(p_388212_), TextureMapping.layered(resourcelocation, resourcelocation1), this.modelOutput);
    }

    void registerSimpleFlatItemModel(Item p_388639_) {
        this.registerSimpleItemModel(p_388639_, this.createFlatItemModel(p_388639_));
    }

    private void registerSimpleFlatItemModel(Block p_387807_) {
        Item item = p_387807_.asItem();
        if (item != Items.AIR) {
            this.registerSimpleItemModel(item, this.createFlatItemModelWithBlockTexture(item, p_387807_));
        }
    }

    private void registerSimpleFlatItemModel(Block p_388029_, String p_388386_) {
        Item item = p_388029_.asItem();
        if (item != Items.AIR) {
            this.registerSimpleItemModel(item, this.createFlatItemModelWithBlockTexture(item, p_388029_, p_388386_));
        }
    }

    private void registerTwoLayerFlatItemModel(Block p_388447_, String p_386496_) {
        Item item = p_388447_.asItem();
        if (item != Items.AIR) {
            ResourceLocation resourcelocation = this.createFlatItemModelWithBlockTextureAndOverlay(item, p_388447_, p_386496_);
            this.registerSimpleItemModel(item, resourcelocation);
        }
    }

    private static PropertyDispatch createHorizontalFacingDispatch() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
            .select(Direction.NORTH, Variant.variant());
    }

    private static PropertyDispatch createHorizontalFacingDispatchAlt() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.SOUTH, Variant.variant())
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
    }

    private static PropertyDispatch createTorchHorizontalDispatch() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.EAST, Variant.variant())
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
    }

    private static PropertyDispatch createFacingDispatch() {
        return PropertyDispatch.property(BlockStateProperties.FACING)
            .select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(Direction.UP, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
            .select(Direction.NORTH, Variant.variant())
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
    }

    private static MultiVariantGenerator createRotatedVariant(Block p_387757_, ResourceLocation p_387476_) {
        return MultiVariantGenerator.multiVariant(p_387757_, createRotatedVariants(p_387476_));
    }

    private static Variant[] createRotatedVariants(ResourceLocation p_387601_) {
        return new Variant[]{
            Variant.variant().with(VariantProperties.MODEL, p_387601_),
            Variant.variant().with(VariantProperties.MODEL, p_387601_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90),
            Variant.variant().with(VariantProperties.MODEL, p_387601_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
            Variant.variant().with(VariantProperties.MODEL, p_387601_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
        };
    }

    private static MultiVariantGenerator createRotatedVariant(Block p_388497_, ResourceLocation p_388869_, ResourceLocation p_386491_) {
        return MultiVariantGenerator.multiVariant(
            p_388497_,
            Variant.variant().with(VariantProperties.MODEL, p_388869_),
            Variant.variant().with(VariantProperties.MODEL, p_386491_),
            Variant.variant().with(VariantProperties.MODEL, p_388869_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
            Variant.variant().with(VariantProperties.MODEL, p_386491_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
        );
    }

    private static PropertyDispatch createBooleanModelDispatch(BooleanProperty p_387151_, ResourceLocation p_387897_, ResourceLocation p_387223_) {
        return PropertyDispatch.property(p_387151_)
            .select(true, Variant.variant().with(VariantProperties.MODEL, p_387897_))
            .select(false, Variant.variant().with(VariantProperties.MODEL, p_387223_));
    }

    private void createRotatedMirroredVariantBlock(Block p_387109_) {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(p_387109_, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.CUBE_MIRRORED.create(p_387109_, this.modelOutput);
        this.blockStateOutput.accept(createRotatedVariant(p_387109_, resourcelocation, resourcelocation1));
    }

    private void createRotatedVariantBlock(Block p_388765_) {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(p_388765_, this.modelOutput);
        this.blockStateOutput.accept(createRotatedVariant(p_388765_, resourcelocation));
    }

    private void createBrushableBlock(Block p_388145_) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_388145_)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.DUSTED)
                            .generate(
                                p_386492_ -> {
                                    String s = "_" + p_386492_;
                                    ResourceLocation resourcelocation = TextureMapping.getBlockTexture(p_388145_, s);
                                    return Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            ModelTemplates.CUBE_ALL
                                                .createWithSuffix(p_388145_, s, new TextureMapping().put(TextureSlot.ALL, resourcelocation), this.modelOutput)
                                        );
                                }
                            )
                    )
            );
        this.registerSimpleItemModel(p_388145_, ModelLocationUtils.getModelLocation(p_388145_, "_0"));
    }

    static BlockStateGenerator createButton(Block p_388781_, ResourceLocation p_387569_, ResourceLocation p_387458_) {
        return MultiVariantGenerator.multiVariant(p_388781_)
            .with(
                PropertyDispatch.property(BlockStateProperties.POWERED)
                    .select(false, Variant.variant().with(VariantProperties.MODEL, p_387569_))
                    .select(true, Variant.variant().with(VariantProperties.MODEL, p_387458_))
            )
            .with(
                PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                    .select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                    .select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                    .select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                    .select(
                        AttachFace.WALL,
                        Direction.EAST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.WEST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.SOUTH,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.NORTH,
                        Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.CEILING,
                        Direction.EAST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        AttachFace.CEILING,
                        Direction.WEST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                    .select(
                        AttachFace.CEILING,
                        Direction.NORTH,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
            );
    }

    private static PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> configureDoorHalf(
        PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> p_387980_,
        DoubleBlockHalf p_388251_,
        ResourceLocation p_387751_,
        ResourceLocation p_387341_,
        ResourceLocation p_387988_,
        ResourceLocation p_388293_
    ) {
        return p_387980_.select(Direction.EAST, p_388251_, DoorHingeSide.LEFT, false, Variant.variant().with(VariantProperties.MODEL, p_387751_))
            .select(
                Direction.SOUTH,
                p_388251_,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_387751_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.WEST,
                p_388251_,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_387751_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.NORTH,
                p_388251_,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_387751_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.EAST, p_388251_, DoorHingeSide.RIGHT, false, Variant.variant().with(VariantProperties.MODEL, p_387988_))
            .select(
                Direction.SOUTH,
                p_388251_,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_387988_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.WEST,
                p_388251_,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_387988_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.NORTH,
                p_388251_,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_387988_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(
                Direction.EAST,
                p_388251_,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_387341_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.SOUTH,
                p_388251_,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_387341_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.WEST,
                p_388251_,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_387341_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.NORTH, p_388251_, DoorHingeSide.LEFT, true, Variant.variant().with(VariantProperties.MODEL, p_387341_))
            .select(
                Direction.EAST,
                p_388251_,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_388293_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.SOUTH, p_388251_, DoorHingeSide.RIGHT, true, Variant.variant().with(VariantProperties.MODEL, p_388293_))
            .select(
                Direction.WEST,
                p_388251_,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_388293_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.NORTH,
                p_388251_,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_388293_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            );
    }

    private static BlockStateGenerator createDoor(
        Block p_387449_,
        ResourceLocation p_388769_,
        ResourceLocation p_387578_,
        ResourceLocation p_388733_,
        ResourceLocation p_387800_,
        ResourceLocation p_387804_,
        ResourceLocation p_388812_,
        ResourceLocation p_388522_,
        ResourceLocation p_388789_
    ) {
        return MultiVariantGenerator.multiVariant(p_387449_)
            .with(
                configureDoorHalf(
                    configureDoorHalf(
                        PropertyDispatch.properties(
                            BlockStateProperties.HORIZONTAL_FACING,
                            BlockStateProperties.DOUBLE_BLOCK_HALF,
                            BlockStateProperties.DOOR_HINGE,
                            BlockStateProperties.OPEN
                        ),
                        DoubleBlockHalf.LOWER,
                        p_388769_,
                        p_387578_,
                        p_388733_,
                        p_387800_
                    ),
                    DoubleBlockHalf.UPPER,
                    p_387804_,
                    p_388812_,
                    p_388522_,
                    p_388789_
                )
            );
    }

    static BlockStateGenerator createCustomFence(
        Block p_388887_,
        ResourceLocation p_388670_,
        ResourceLocation p_386933_,
        ResourceLocation p_387004_,
        ResourceLocation p_387907_,
        ResourceLocation p_387294_
    ) {
        return MultiPartGenerator.multiPart(p_388887_)
            .with(Variant.variant().with(VariantProperties.MODEL, p_388670_))
            .with(
                Condition.condition().term(BlockStateProperties.NORTH, true),
                Variant.variant().with(VariantProperties.MODEL, p_386933_).with(VariantProperties.UV_LOCK, false)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST, true),
                Variant.variant().with(VariantProperties.MODEL, p_387004_).with(VariantProperties.UV_LOCK, false)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH, true),
                Variant.variant().with(VariantProperties.MODEL, p_387907_).with(VariantProperties.UV_LOCK, false)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST, true),
                Variant.variant().with(VariantProperties.MODEL, p_387294_).with(VariantProperties.UV_LOCK, false)
            );
    }

    static BlockStateGenerator createFence(Block p_387776_, ResourceLocation p_386938_, ResourceLocation p_388229_) {
        return MultiPartGenerator.multiPart(p_387776_)
            .with(Variant.variant().with(VariantProperties.MODEL, p_386938_))
            .with(
                Condition.condition().term(BlockStateProperties.NORTH, true),
                Variant.variant().with(VariantProperties.MODEL, p_388229_).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_388229_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_388229_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_388229_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            );
    }

    static BlockStateGenerator createWall(Block p_388539_, ResourceLocation p_387890_, ResourceLocation p_388342_, ResourceLocation p_388678_) {
        return MultiPartGenerator.multiPart(p_388539_)
            .with(Condition.condition().term(BlockStateProperties.UP, true), Variant.variant().with(VariantProperties.MODEL, p_387890_))
            .with(
                Condition.condition().term(BlockStateProperties.NORTH_WALL, WallSide.LOW),
                Variant.variant().with(VariantProperties.MODEL, p_388342_).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_388342_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_388342_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_388342_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.NORTH_WALL, WallSide.TALL),
                Variant.variant().with(VariantProperties.MODEL, p_388678_).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_388678_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_388678_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_388678_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            );
    }

    static BlockStateGenerator createFenceGate(
        Block p_388571_, ResourceLocation p_386483_, ResourceLocation p_387726_, ResourceLocation p_388338_, ResourceLocation p_386858_, boolean p_386665_
    ) {
        return MultiVariantGenerator.multiVariant(p_388571_, Variant.variant().with(VariantProperties.UV_LOCK, p_386665_))
            .with(createHorizontalFacingDispatchAlt())
            .with(
                PropertyDispatch.properties(BlockStateProperties.IN_WALL, BlockStateProperties.OPEN)
                    .select(false, false, Variant.variant().with(VariantProperties.MODEL, p_387726_))
                    .select(true, false, Variant.variant().with(VariantProperties.MODEL, p_386858_))
                    .select(false, true, Variant.variant().with(VariantProperties.MODEL, p_386483_))
                    .select(true, true, Variant.variant().with(VariantProperties.MODEL, p_388338_))
            );
    }

    static BlockStateGenerator createStairs(Block p_386997_, ResourceLocation p_388729_, ResourceLocation p_387459_, ResourceLocation p_387696_) {
        return MultiVariantGenerator.multiVariant(p_386997_)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE)
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, Variant.variant().with(VariantProperties.MODEL, p_387459_))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387459_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387459_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387459_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, Variant.variant().with(VariantProperties.MODEL, p_387696_))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, Variant.variant().with(VariantProperties.MODEL, p_387696_))
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, Variant.variant().with(VariantProperties.MODEL, p_388729_))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, Variant.variant().with(VariantProperties.MODEL, p_388729_))
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387459_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387459_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387459_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387459_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387696_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_388729_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
            );
    }

    private static BlockStateGenerator createOrientableTrapdoor(
        Block p_388423_, ResourceLocation p_388655_, ResourceLocation p_387142_, ResourceLocation p_386755_
    ) {
        return MultiVariantGenerator.multiVariant(p_388423_)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
                    .select(Direction.NORTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, p_387142_))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, p_387142_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, p_387142_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, p_387142_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, p_388655_))
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, p_388655_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, p_388655_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, p_388655_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.BOTTOM, true, Variant.variant().with(VariantProperties.MODEL, p_386755_))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, p_386755_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, p_386755_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, p_386755_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_386755_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_386755_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_386755_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_386755_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
    }

    private static BlockStateGenerator createTrapdoor(Block p_387676_, ResourceLocation p_387306_, ResourceLocation p_386539_, ResourceLocation p_387214_) {
        return MultiVariantGenerator.multiVariant(p_387676_)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
                    .select(Direction.NORTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, p_386539_))
                    .select(Direction.SOUTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, p_386539_))
                    .select(Direction.EAST, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, p_386539_))
                    .select(Direction.WEST, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, p_386539_))
                    .select(Direction.NORTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, p_387306_))
                    .select(Direction.SOUTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, p_387306_))
                    .select(Direction.EAST, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, p_387306_))
                    .select(Direction.WEST, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, p_387306_))
                    .select(Direction.NORTH, Half.BOTTOM, true, Variant.variant().with(VariantProperties.MODEL, p_387214_))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, p_387214_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, p_387214_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, p_387214_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.TOP, true, Variant.variant().with(VariantProperties.MODEL, p_387214_))
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, p_387214_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, p_387214_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, p_387214_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    static MultiVariantGenerator createSimpleBlock(Block p_387997_, ResourceLocation p_388814_) {
        return MultiVariantGenerator.multiVariant(p_387997_, Variant.variant().with(VariantProperties.MODEL, p_388814_));
    }

    private static PropertyDispatch createRotatedPillar() {
        return PropertyDispatch.property(BlockStateProperties.AXIS)
            .select(Direction.Axis.Y, Variant.variant())
            .select(Direction.Axis.Z, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(
                Direction.Axis.X,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            );
    }

    static BlockStateGenerator createPillarBlockUVLocked(Block p_387378_, TextureMapping p_388778_, BiConsumer<ResourceLocation, ModelInstance> p_387388_) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN_UV_LOCKED_X.create(p_387378_, p_388778_, p_387388_);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_COLUMN_UV_LOCKED_Y.create(p_387378_, p_388778_, p_387388_);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_COLUMN_UV_LOCKED_Z.create(p_387378_, p_388778_, p_387388_);
        ResourceLocation resourcelocation3 = ModelTemplates.CUBE_COLUMN.create(p_387378_, p_388778_, p_387388_);
        return MultiVariantGenerator.multiVariant(p_387378_, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
            .with(
                PropertyDispatch.property(BlockStateProperties.AXIS)
                    .select(Direction.Axis.X, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    .select(Direction.Axis.Z, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
            );
    }

    static BlockStateGenerator createAxisAlignedPillarBlock(Block p_388031_, ResourceLocation p_388445_) {
        return MultiVariantGenerator.multiVariant(p_388031_, Variant.variant().with(VariantProperties.MODEL, p_388445_)).with(createRotatedPillar());
    }

    private void createAxisAlignedPillarBlockCustomModel(Block p_387506_, ResourceLocation p_388910_) {
        this.blockStateOutput.accept(createAxisAlignedPillarBlock(p_387506_, p_388910_));
    }

    public void createAxisAlignedPillarBlock(Block p_388127_, TexturedModel.Provider p_386733_) {
        ResourceLocation resourcelocation = p_386733_.create(p_388127_, this.modelOutput);
        this.blockStateOutput.accept(createAxisAlignedPillarBlock(p_388127_, resourcelocation));
    }

    private void createHorizontallyRotatedBlock(Block p_386622_, TexturedModel.Provider p_387864_) {
        ResourceLocation resourcelocation = p_387864_.create(p_386622_, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_386622_, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(createHorizontalFacingDispatch())
            );
    }

    static BlockStateGenerator createRotatedPillarWithHorizontalVariant(Block p_388818_, ResourceLocation p_388699_, ResourceLocation p_387012_) {
        return MultiVariantGenerator.multiVariant(p_388818_)
            .with(
                PropertyDispatch.property(BlockStateProperties.AXIS)
                    .select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, p_388699_))
                    .select(
                        Direction.Axis.Z,
                        Variant.variant().with(VariantProperties.MODEL, p_387012_).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.Axis.X,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_387012_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
    }

    private void createRotatedPillarWithHorizontalVariant(Block p_388224_, TexturedModel.Provider p_387073_, TexturedModel.Provider p_388588_) {
        ResourceLocation resourcelocation = p_387073_.create(p_388224_, this.modelOutput);
        ResourceLocation resourcelocation1 = p_388588_.create(p_388224_, this.modelOutput);
        this.blockStateOutput.accept(createRotatedPillarWithHorizontalVariant(p_388224_, resourcelocation, resourcelocation1));
    }

    private void createCreakingHeart(Block p_386651_) {
        Function<TexturedModel.Provider, ResourceLocation> function = p_386772_ -> p_386772_.updateTexture(
                    p_387855_ -> p_387855_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(p_386651_, "_active"))
                )
                .updateTexture(p_387715_ -> p_387715_.put(TextureSlot.END, TextureMapping.getBlockTexture(p_386651_, "_top_active")))
                .createWithSuffix(p_386651_, "_active", this.modelOutput);
        ResourceLocation resourcelocation = TexturedModel.COLUMN_ALT.create(p_386651_, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.COLUMN_HORIZONTAL_ALT.create(p_386651_, this.modelOutput);
        ResourceLocation resourcelocation2 = function.apply(TexturedModel.COLUMN_ALT);
        ResourceLocation resourcelocation3 = function.apply(TexturedModel.COLUMN_HORIZONTAL_ALT);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_386651_)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.AXIS, CreakingHeartBlock.ACTIVE)
                            .select(Direction.Axis.Y, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(
                                Direction.Axis.Z,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.Axis.X,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(Direction.Axis.Y, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                            .select(
                                Direction.Axis.Z,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.Axis.X,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                    )
            );
    }

    private ResourceLocation createSuffixedVariant(
        Block p_386543_, String p_388926_, ModelTemplate p_387126_, Function<ResourceLocation, TextureMapping> p_387576_
    ) {
        return p_387126_.createWithSuffix(p_386543_, p_388926_, p_387576_.apply(TextureMapping.getBlockTexture(p_386543_, p_388926_)), this.modelOutput);
    }

    static BlockStateGenerator createPressurePlate(Block p_387085_, ResourceLocation p_388820_, ResourceLocation p_388216_) {
        return MultiVariantGenerator.multiVariant(p_387085_).with(createBooleanModelDispatch(BlockStateProperties.POWERED, p_388216_, p_388820_));
    }

    static BlockStateGenerator createSlab(Block p_388700_, ResourceLocation p_388944_, ResourceLocation p_387729_, ResourceLocation p_386692_) {
        return MultiVariantGenerator.multiVariant(p_388700_)
            .with(
                PropertyDispatch.property(BlockStateProperties.SLAB_TYPE)
                    .select(SlabType.BOTTOM, Variant.variant().with(VariantProperties.MODEL, p_388944_))
                    .select(SlabType.TOP, Variant.variant().with(VariantProperties.MODEL, p_387729_))
                    .select(SlabType.DOUBLE, Variant.variant().with(VariantProperties.MODEL, p_386692_))
            );
    }

    public void createTrivialCube(Block p_386512_) {
        this.createTrivialBlock(p_386512_, TexturedModel.CUBE);
    }

    public void createTrivialBlock(Block p_387678_, TexturedModel.Provider p_386545_) {
        this.blockStateOutput.accept(createSimpleBlock(p_387678_, p_386545_.create(p_387678_, this.modelOutput)));
    }

    public void createTintedLeaves(Block p_387323_, TexturedModel.Provider p_388806_, int p_388716_) {
        ResourceLocation resourcelocation = p_388806_.create(p_387323_, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_387323_, resourcelocation));
        this.registerSimpleTintedItemModel(p_387323_, resourcelocation, ItemModelUtils.constantTint(p_388716_));
    }

    private void createVine() {
        this.createMultifaceBlockStates(Blocks.VINE);
        ResourceLocation resourcelocation = this.createFlatItemModelWithBlockTexture(Items.VINE, Blocks.VINE);
        this.registerSimpleTintedItemModel(Blocks.VINE, resourcelocation, ItemModelUtils.constantTint(-12012264));
    }

    private void createItemWithGrassTint(Block p_388714_) {
        ResourceLocation resourcelocation = this.createFlatItemModelWithBlockTexture(p_388714_.asItem(), p_388714_);
        this.registerSimpleTintedItemModel(p_388714_, resourcelocation, new GrassColorSource());
    }

    private BlockModelGenerators.BlockFamilyProvider family(Block p_388779_) {
        TexturedModel texturedmodel = this.texturedModels.getOrDefault(p_388779_, TexturedModel.CUBE.get(p_388779_));
        return new BlockModelGenerators.BlockFamilyProvider(texturedmodel.getMapping()).fullBlock(p_388779_, texturedmodel.getTemplate());
    }

    public void createHangingSign(Block p_388881_, Block p_388188_, Block p_387794_) {
        ResourceLocation resourcelocation = this.createParticleOnlyBlockModel(p_388188_, p_388881_);
        this.blockStateOutput.accept(createSimpleBlock(p_388188_, resourcelocation));
        this.blockStateOutput.accept(createSimpleBlock(p_387794_, resourcelocation));
        this.registerSimpleFlatItemModel(p_388188_.asItem());
    }

    void createDoor(Block p_386982_) {
        TextureMapping texturemapping = TextureMapping.door(p_386982_);
        ResourceLocation resourcelocation = ModelTemplates.DOOR_BOTTOM_LEFT.create(p_386982_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.create(p_386982_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.DOOR_BOTTOM_RIGHT.create(p_386982_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.create(p_386982_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.DOOR_TOP_LEFT.create(p_386982_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.DOOR_TOP_LEFT_OPEN.create(p_386982_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation6 = ModelTemplates.DOOR_TOP_RIGHT.create(p_386982_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation7 = ModelTemplates.DOOR_TOP_RIGHT_OPEN.create(p_386982_, texturemapping, this.modelOutput);
        this.registerSimpleFlatItemModel(p_386982_.asItem());
        this.blockStateOutput
            .accept(
                createDoor(
                    p_386982_,
                    resourcelocation,
                    resourcelocation1,
                    resourcelocation2,
                    resourcelocation3,
                    resourcelocation4,
                    resourcelocation5,
                    resourcelocation6,
                    resourcelocation7
                )
            );
    }

    private void copyDoorModel(Block p_386799_, Block p_388294_) {
        ResourceLocation resourcelocation = ModelTemplates.DOOR_BOTTOM_LEFT.getDefaultModelLocation(p_386799_);
        ResourceLocation resourcelocation1 = ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.getDefaultModelLocation(p_386799_);
        ResourceLocation resourcelocation2 = ModelTemplates.DOOR_BOTTOM_RIGHT.getDefaultModelLocation(p_386799_);
        ResourceLocation resourcelocation3 = ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.getDefaultModelLocation(p_386799_);
        ResourceLocation resourcelocation4 = ModelTemplates.DOOR_TOP_LEFT.getDefaultModelLocation(p_386799_);
        ResourceLocation resourcelocation5 = ModelTemplates.DOOR_TOP_LEFT_OPEN.getDefaultModelLocation(p_386799_);
        ResourceLocation resourcelocation6 = ModelTemplates.DOOR_TOP_RIGHT.getDefaultModelLocation(p_386799_);
        ResourceLocation resourcelocation7 = ModelTemplates.DOOR_TOP_RIGHT_OPEN.getDefaultModelLocation(p_386799_);
        this.itemModelOutput.copy(p_386799_.asItem(), p_388294_.asItem());
        this.blockStateOutput
            .accept(
                createDoor(
                    p_388294_,
                    resourcelocation,
                    resourcelocation1,
                    resourcelocation2,
                    resourcelocation3,
                    resourcelocation4,
                    resourcelocation5,
                    resourcelocation6,
                    resourcelocation7
                )
            );
    }

    void createOrientableTrapdoor(Block p_388937_) {
        TextureMapping texturemapping = TextureMapping.defaultTexture(p_388937_);
        ResourceLocation resourcelocation = ModelTemplates.ORIENTABLE_TRAPDOOR_TOP.create(p_388937_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM.create(p_388937_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN.create(p_388937_, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createOrientableTrapdoor(p_388937_, resourcelocation, resourcelocation1, resourcelocation2));
        this.registerSimpleItemModel(p_388937_, resourcelocation1);
    }

    void createTrapdoor(Block p_387551_) {
        TextureMapping texturemapping = TextureMapping.defaultTexture(p_387551_);
        ResourceLocation resourcelocation = ModelTemplates.TRAPDOOR_TOP.create(p_387551_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.TRAPDOOR_BOTTOM.create(p_387551_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.TRAPDOOR_OPEN.create(p_387551_, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createTrapdoor(p_387551_, resourcelocation, resourcelocation1, resourcelocation2));
        this.registerSimpleItemModel(p_387551_, resourcelocation1);
    }

    private void copyTrapdoorModel(Block p_388461_, Block p_388742_) {
        ResourceLocation resourcelocation = ModelTemplates.TRAPDOOR_TOP.getDefaultModelLocation(p_388461_);
        ResourceLocation resourcelocation1 = ModelTemplates.TRAPDOOR_BOTTOM.getDefaultModelLocation(p_388461_);
        ResourceLocation resourcelocation2 = ModelTemplates.TRAPDOOR_OPEN.getDefaultModelLocation(p_388461_);
        this.itemModelOutput.copy(p_388461_.asItem(), p_388742_.asItem());
        this.blockStateOutput.accept(createTrapdoor(p_388742_, resourcelocation, resourcelocation1, resourcelocation2));
    }

    private void createBigDripLeafBlock() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF, "_partial_tilt");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF, "_full_tilt");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.BIG_DRIPLEAF)
                    .with(createHorizontalFacingDispatch())
                    .with(
                        PropertyDispatch.property(BlockStateProperties.TILT)
                            .select(Tilt.NONE, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(Tilt.UNSTABLE, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(Tilt.PARTIAL, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(Tilt.FULL, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                    )
            );
    }

    private BlockModelGenerators.WoodProvider woodProvider(Block p_387211_) {
        return new BlockModelGenerators.WoodProvider(TextureMapping.logColumn(p_387211_));
    }

    private void createNonTemplateModelBlock(Block p_387703_) {
        this.createNonTemplateModelBlock(p_387703_, p_387703_);
    }

    private void createNonTemplateModelBlock(Block p_388490_, Block p_387734_) {
        this.blockStateOutput.accept(createSimpleBlock(p_388490_, ModelLocationUtils.getModelLocation(p_387734_)));
    }

    private void createCrossBlockWithDefaultItem(Block p_386508_, BlockModelGenerators.PlantType p_387047_) {
        this.registerSimpleItemModel(p_386508_.asItem(), p_387047_.createItemModel(this, p_386508_));
        this.createCrossBlock(p_386508_, p_387047_);
    }

    private void createCrossBlockWithDefaultItem(Block p_386851_, BlockModelGenerators.PlantType p_387264_, TextureMapping p_388800_) {
        this.registerSimpleFlatItemModel(p_386851_);
        this.createCrossBlock(p_386851_, p_387264_, p_388800_);
    }

    private void createCrossBlock(Block p_388178_, BlockModelGenerators.PlantType p_387157_) {
        TextureMapping texturemapping = p_387157_.getTextureMapping(p_388178_);
        this.createCrossBlock(p_388178_, p_387157_, texturemapping);
    }

    private void createCrossBlock(Block p_388360_, BlockModelGenerators.PlantType p_386631_, TextureMapping p_388352_) {
        ResourceLocation resourcelocation = p_386631_.getCross().create(p_388360_, p_388352_, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_388360_, resourcelocation));
    }

    private void createCrossBlock(Block p_387742_, BlockModelGenerators.PlantType p_388653_, Property<Integer> p_386701_, int... p_388717_) {
        if (p_386701_.getPossibleValues().size() != p_388717_.length) {
            throw new IllegalArgumentException("missing values for property: " + p_386701_);
        } else {
            PropertyDispatch propertydispatch = PropertyDispatch.property(p_386701_).generate(p_388685_ -> {
                String s = "_stage" + p_388717_[p_388685_];
                TextureMapping texturemapping = TextureMapping.cross(TextureMapping.getBlockTexture(p_387742_, s));
                ResourceLocation resourcelocation = p_388653_.getCross().createWithSuffix(p_387742_, s, texturemapping, this.modelOutput);
                return Variant.variant().with(VariantProperties.MODEL, resourcelocation);
            });
            this.registerSimpleFlatItemModel(p_387742_.asItem());
            this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(p_387742_).with(propertydispatch));
        }
    }

    private void createPlantWithDefaultItem(Block p_386547_, Block p_386918_, BlockModelGenerators.PlantType p_388872_) {
        this.registerSimpleItemModel(p_386547_.asItem(), p_388872_.createItemModel(this, p_386547_));
        this.createPlant(p_386547_, p_386918_, p_388872_);
    }

    private void createPlant(Block p_387680_, Block p_386994_, BlockModelGenerators.PlantType p_386928_) {
        this.createCrossBlock(p_387680_, p_386928_);
        TextureMapping texturemapping = p_386928_.getPlantTextureMapping(p_387680_);
        ResourceLocation resourcelocation = p_386928_.getCrossPot().create(p_386994_, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_386994_, resourcelocation));
    }

    private void createCoralFans(Block p_387950_, Block p_386922_) {
        TexturedModel texturedmodel = TexturedModel.CORAL_FAN.get(p_387950_);
        ResourceLocation resourcelocation = texturedmodel.create(p_387950_, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_387950_, resourcelocation));
        ResourceLocation resourcelocation1 = ModelTemplates.CORAL_WALL_FAN.create(p_386922_, texturedmodel.getMapping(), this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_386922_, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    .with(createHorizontalFacingDispatch())
            );
        this.registerSimpleFlatItemModel(p_387950_);
    }

    private void createStems(Block p_388332_, Block p_388176_) {
        this.registerSimpleFlatItemModel(p_388332_.asItem());
        TextureMapping texturemapping = TextureMapping.stem(p_388332_);
        TextureMapping texturemapping1 = TextureMapping.attachedStem(p_388332_, p_388176_);
        ResourceLocation resourcelocation = ModelTemplates.ATTACHED_STEM.create(p_388176_, texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_388176_, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(
                        PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
                            .select(Direction.WEST, Variant.variant())
                            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_388332_)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_7)
                            .generate(
                                p_387818_ -> Variant.variant()
                                        .with(VariantProperties.MODEL, ModelTemplates.STEMS[p_387818_].create(p_388332_, texturemapping, this.modelOutput))
                            )
                    )
            );
    }

    private void createPitcherPlant() {
        Block block = Blocks.PITCHER_PLANT;
        this.registerSimpleFlatItemModel(block.asItem());
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block, "_top");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(block, "_bottom");
        this.createDoubleBlock(block, resourcelocation, resourcelocation1);
    }

    private void createPitcherCrop() {
        Block block = Blocks.PITCHER_CROP;
        this.registerSimpleFlatItemModel(block.asItem());
        PropertyDispatch propertydispatch = PropertyDispatch.properties(PitcherCropBlock.AGE, BlockStateProperties.DOUBLE_BLOCK_HALF)
            .generate((p_388433_, p_386761_) -> {
                return switch (p_386761_) {
                    case UPPER -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_top_stage_" + p_388433_));
                    case LOWER -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_bottom_stage_" + p_388433_));
                };
            });
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(propertydispatch));
    }

    private void createCoral(
        Block p_388324_, Block p_388892_, Block p_387808_, Block p_387530_, Block p_387248_, Block p_388266_, Block p_386798_, Block p_388491_
    ) {
        this.createCrossBlockWithDefaultItem(p_388324_, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createCrossBlockWithDefaultItem(p_388892_, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTrivialCube(p_387808_);
        this.createTrivialCube(p_387530_);
        this.createCoralFans(p_387248_, p_386798_);
        this.createCoralFans(p_388266_, p_388491_);
    }

    private void createDoublePlant(Block p_388543_, BlockModelGenerators.PlantType p_388551_) {
        ResourceLocation resourcelocation = this.createSuffixedVariant(p_388543_, "_top", p_388551_.getCross(), TextureMapping::cross);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(p_388543_, "_bottom", p_388551_.getCross(), TextureMapping::cross);
        this.createDoubleBlock(p_388543_, resourcelocation, resourcelocation1);
    }

    private void createDoublePlantWithDefaultItem(Block p_386502_, BlockModelGenerators.PlantType p_386561_) {
        this.registerSimpleFlatItemModel(p_386502_, "_top");
        this.createDoublePlant(p_386502_, p_386561_);
    }

    private void createTintedDoublePlant(Block p_388276_) {
        ResourceLocation resourcelocation = this.createFlatItemModelWithBlockTexture(p_388276_.asItem(), p_388276_, "_top");
        this.registerSimpleTintedItemModel(p_388276_, resourcelocation, new GrassColorSource());
        this.createDoublePlant(p_388276_, BlockModelGenerators.PlantType.TINTED);
    }

    private void createSunflower() {
        this.registerSimpleFlatItemModel(Blocks.SUNFLOWER, "_front");
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SUNFLOWER, "_top");
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(
            Blocks.SUNFLOWER, "_bottom", BlockModelGenerators.PlantType.NOT_TINTED.getCross(), TextureMapping::cross
        );
        this.createDoubleBlock(Blocks.SUNFLOWER, resourcelocation, resourcelocation1);
    }

    private void createTallSeagrass() {
        ResourceLocation resourcelocation = this.createSuffixedVariant(Blocks.TALL_SEAGRASS, "_top", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(
            Blocks.TALL_SEAGRASS, "_bottom", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture
        );
        this.createDoubleBlock(Blocks.TALL_SEAGRASS, resourcelocation, resourcelocation1);
    }

    private void createSmallDripleaf() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SMALL_DRIPLEAF, "_top");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.SMALL_DRIPLEAF, "_bottom");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SMALL_DRIPLEAF)
                    .with(createHorizontalFacingDispatch())
                    .with(
                        PropertyDispatch.property(BlockStateProperties.DOUBLE_BLOCK_HALF)
                            .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    )
            );
    }

    private void createDoubleBlock(Block p_387817_, ResourceLocation p_388723_, ResourceLocation p_387877_) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_387817_)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.DOUBLE_BLOCK_HALF)
                            .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, p_387877_))
                            .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, p_388723_))
                    )
            );
    }

    private void createPassiveRail(Block p_386594_) {
        TextureMapping texturemapping = TextureMapping.rail(p_386594_);
        TextureMapping texturemapping1 = TextureMapping.rail(TextureMapping.getBlockTexture(p_386594_, "_corner"));
        ResourceLocation resourcelocation = ModelTemplates.RAIL_FLAT.create(p_386594_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.RAIL_CURVED.create(p_386594_, texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.RAIL_RAISED_NE.create(p_386594_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.RAIL_RAISED_SW.create(p_386594_, texturemapping, this.modelOutput);
        this.registerSimpleFlatItemModel(p_386594_);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_386594_)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.RAIL_SHAPE)
                            .select(RailShape.NORTH_SOUTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(
                                RailShape.EAST_WEST,
                                Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.ASCENDING_EAST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.ASCENDING_WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(RailShape.ASCENDING_NORTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                            .select(RailShape.ASCENDING_SOUTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                            .select(RailShape.SOUTH_EAST, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(
                                RailShape.SOUTH_WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.NORTH_WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                RailShape.NORTH_EAST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createActiveRail(Block p_387058_) {
        ResourceLocation resourcelocation = this.createSuffixedVariant(p_387058_, "", ModelTemplates.RAIL_FLAT, TextureMapping::rail);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(p_387058_, "", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail);
        ResourceLocation resourcelocation2 = this.createSuffixedVariant(p_387058_, "", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail);
        ResourceLocation resourcelocation3 = this.createSuffixedVariant(p_387058_, "_on", ModelTemplates.RAIL_FLAT, TextureMapping::rail);
        ResourceLocation resourcelocation4 = this.createSuffixedVariant(p_387058_, "_on", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail);
        ResourceLocation resourcelocation5 = this.createSuffixedVariant(p_387058_, "_on", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail);
        PropertyDispatch propertydispatch = PropertyDispatch.properties(BlockStateProperties.POWERED, BlockStateProperties.RAIL_SHAPE_STRAIGHT)
            .generate(
                (p_387790_, p_388067_) -> {
                    switch (p_388067_) {
                        case NORTH_SOUTH:
                            return Variant.variant().with(VariantProperties.MODEL, p_387790_ ? resourcelocation3 : resourcelocation);
                        case EAST_WEST:
                            return Variant.variant()
                                .with(VariantProperties.MODEL, p_387790_ ? resourcelocation3 : resourcelocation)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                        case ASCENDING_EAST:
                            return Variant.variant()
                                .with(VariantProperties.MODEL, p_387790_ ? resourcelocation4 : resourcelocation1)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                        case ASCENDING_WEST:
                            return Variant.variant()
                                .with(VariantProperties.MODEL, p_387790_ ? resourcelocation5 : resourcelocation2)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                        case ASCENDING_NORTH:
                            return Variant.variant().with(VariantProperties.MODEL, p_387790_ ? resourcelocation4 : resourcelocation1);
                        case ASCENDING_SOUTH:
                            return Variant.variant().with(VariantProperties.MODEL, p_387790_ ? resourcelocation5 : resourcelocation2);
                        default:
                            throw new UnsupportedOperationException("Fix you generator!");
                    }
                }
            );
        this.registerSimpleFlatItemModel(p_387058_);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(p_387058_).with(propertydispatch));
    }

    private void createAirLikeBlock(Block p_387420_, Item p_387056_) {
        ResourceLocation resourcelocation = ModelTemplates.PARTICLE_ONLY.create(p_387420_, TextureMapping.particleFromItem(p_387056_), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_387420_, resourcelocation));
    }

    private void createAirLikeBlock(Block p_386565_, ResourceLocation p_388581_) {
        ResourceLocation resourcelocation = ModelTemplates.PARTICLE_ONLY.create(p_386565_, TextureMapping.particle(p_388581_), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_386565_, resourcelocation));
    }

    private ResourceLocation createParticleOnlyBlockModel(Block p_387451_, Block p_388513_) {
        return ModelTemplates.PARTICLE_ONLY.create(p_387451_, TextureMapping.particle(p_388513_), this.modelOutput);
    }

    public void createParticleOnlyBlock(Block p_388804_, Block p_387571_) {
        this.blockStateOutput.accept(createSimpleBlock(p_388804_, this.createParticleOnlyBlockModel(p_388804_, p_387571_)));
    }

    private void createParticleOnlyBlock(Block p_386675_) {
        this.createParticleOnlyBlock(p_386675_, p_386675_);
    }

    private void createFullAndCarpetBlocks(Block p_387946_, Block p_386778_) {
        this.createTrivialCube(p_387946_);
        ResourceLocation resourcelocation = TexturedModel.CARPET.get(p_387946_).create(p_386778_, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_386778_, resourcelocation));
    }

    private void createFlowerBed(Block p_388462_) {
        this.registerSimpleFlatItemModel(p_388462_.asItem());
        ResourceLocation resourcelocation = TexturedModel.FLOWERBED_1.create(p_388462_, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.FLOWERBED_2.create(p_388462_, this.modelOutput);
        ResourceLocation resourcelocation2 = TexturedModel.FLOWERBED_3.create(p_388462_, this.modelOutput);
        ResourceLocation resourcelocation3 = TexturedModel.FLOWERBED_4.create(p_388462_, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(p_388462_)
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4)
                            .term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4)
                            .term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private void createColoredBlockWithRandomRotations(TexturedModel.Provider p_386728_, Block... p_388210_) {
        for (Block block : p_388210_) {
            ResourceLocation resourcelocation = p_386728_.create(block, this.modelOutput);
            this.blockStateOutput.accept(createRotatedVariant(block, resourcelocation));
        }
    }

    private void createColoredBlockWithStateRotations(TexturedModel.Provider p_387213_, Block... p_387927_) {
        for (Block block : p_387927_) {
            ResourceLocation resourcelocation = p_387213_.create(block, this.modelOutput);
            this.blockStateOutput
                .accept(
                    MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                        .with(createHorizontalFacingDispatchAlt())
                );
        }
    }

    private void createGlassBlocks(Block p_388488_, Block p_387279_) {
        this.createTrivialCube(p_388488_);
        TextureMapping texturemapping = TextureMapping.pane(p_388488_, p_387279_);
        ResourceLocation resourcelocation = ModelTemplates.STAINED_GLASS_PANE_POST.create(p_387279_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.STAINED_GLASS_PANE_SIDE.create(p_387279_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT.create(p_387279_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.STAINED_GLASS_PANE_NOSIDE.create(p_387279_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT.create(p_387279_, texturemapping, this.modelOutput);
        Item item = p_387279_.asItem();
        this.registerSimpleItemModel(item, this.createFlatItemModelWithBlockTexture(item, p_388488_));
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(p_387279_)
                    .with(Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.SOUTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.NORTH, false), Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                    .with(Condition.condition().term(BlockStateProperties.EAST, false), Variant.variant().with(VariantProperties.MODEL, resourcelocation4))
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private void createCommandBlock(Block p_388271_) {
        TextureMapping texturemapping = TextureMapping.commandBlock(p_388271_);
        ResourceLocation resourcelocation = ModelTemplates.COMMAND_BLOCK.create(p_388271_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(
            p_388271_, "_conditional", ModelTemplates.COMMAND_BLOCK, p_387325_ -> texturemapping.copyAndUpdate(TextureSlot.SIDE, p_387325_)
        );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_388271_)
                    .with(createBooleanModelDispatch(BlockStateProperties.CONDITIONAL, resourcelocation1, resourcelocation))
                    .with(createFacingDispatch())
            );
    }

    private void createAnvil(Block p_388819_) {
        ResourceLocation resourcelocation = TexturedModel.ANVIL.create(p_388819_, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_388819_, resourcelocation).with(createHorizontalFacingDispatchAlt()));
    }

    private List<Variant> createBambooModels(int p_387368_) {
        String s = "_age" + p_387368_;
        return IntStream.range(1, 5)
            .mapToObj(p_386941_ -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, p_386941_ + s)))
            .collect(Collectors.toList());
    }

    private void createBamboo() {
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.BAMBOO)
                    .with(Condition.condition().term(BlockStateProperties.AGE_1, 0), this.createBambooModels(0))
                    .with(Condition.condition().term(BlockStateProperties.AGE_1, 1), this.createBambooModels(1))
                    .with(
                        Condition.condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.SMALL),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_small_leaves"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.LARGE),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_large_leaves"))
                    )
            );
    }

    private PropertyDispatch createColumnWithFacing() {
        return PropertyDispatch.property(BlockStateProperties.FACING)
            .select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
            .select(Direction.UP, Variant.variant())
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(
                Direction.SOUTH,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.WEST,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(
                Direction.EAST,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            );
    }

    private void createBarrel() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.BARREL, "_top_open");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.BARREL)
                    .with(this.createColumnWithFacing())
                    .with(
                        PropertyDispatch.property(BlockStateProperties.OPEN)
                            .select(
                                false, Variant.variant().with(VariantProperties.MODEL, TexturedModel.CUBE_TOP_BOTTOM.create(Blocks.BARREL, this.modelOutput))
                            )
                            .select(
                                true,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        TexturedModel.CUBE_TOP_BOTTOM
                                            .get(Blocks.BARREL)
                                            .updateTextures(p_386917_ -> p_386917_.put(TextureSlot.TOP, resourcelocation))
                                            .createWithSuffix(Blocks.BARREL, "_open", this.modelOutput)
                                    )
                            )
                    )
            );
    }

    private static <T extends Comparable<T>> PropertyDispatch createEmptyOrFullDispatch(
        Property<T> p_388904_, T p_388480_, ResourceLocation p_388721_, ResourceLocation p_388448_
    ) {
        Variant variant = Variant.variant().with(VariantProperties.MODEL, p_388721_);
        Variant variant1 = Variant.variant().with(VariantProperties.MODEL, p_388448_);
        return PropertyDispatch.property(p_388904_).generate(p_388058_ -> {
            boolean flag = p_388058_.compareTo(p_388480_) >= 0;
            return flag ? variant : variant1;
        });
    }

    private void createBeeNest(Block p_386805_, Function<Block, TextureMapping> p_387180_) {
        TextureMapping texturemapping = p_387180_.apply(p_386805_).copyForced(TextureSlot.SIDE, TextureSlot.PARTICLE);
        TextureMapping texturemapping1 = texturemapping.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(p_386805_, "_front_honey"));
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(p_386805_, "_empty", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(p_386805_, "_honey", texturemapping1, this.modelOutput);
        this.itemModelOutput
            .accept(
                p_386805_.asItem(),
                ItemModelUtils.selectBlockItemProperty(
                    BeehiveBlock.HONEY_LEVEL, ItemModelUtils.plainModel(resourcelocation), Map.of(5, ItemModelUtils.plainModel(resourcelocation1))
                )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_386805_)
                    .with(createHorizontalFacingDispatch())
                    .with(createEmptyOrFullDispatch(BeehiveBlock.HONEY_LEVEL, 5, resourcelocation1, resourcelocation))
            );
    }

    private void createCropBlock(Block p_387553_, Property<Integer> p_386757_, int... p_388514_) {
        if (p_386757_.getPossibleValues().size() != p_388514_.length) {
            throw new IllegalArgumentException();
        } else {
            Int2ObjectMap<ResourceLocation> int2objectmap = new Int2ObjectOpenHashMap<>();
            PropertyDispatch propertydispatch = PropertyDispatch.property(p_386757_)
                .generate(
                    p_388091_ -> {
                        int i = p_388514_[p_388091_];
                        ResourceLocation resourcelocation = int2objectmap.computeIfAbsent(
                            i, p_387534_ -> this.createSuffixedVariant(p_387553_, "_stage" + i, ModelTemplates.CROP, TextureMapping::crop)
                        );
                        return Variant.variant().with(VariantProperties.MODEL, resourcelocation);
                    }
                );
            this.registerSimpleFlatItemModel(p_387553_.asItem());
            this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(p_387553_).with(propertydispatch));
        }
    }

    private void createBell() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.BELL, "_floor");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_ceiling");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_wall");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_between_walls");
        this.registerSimpleFlatItemModel(Items.BELL);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.BELL)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.BELL_ATTACHMENT)
                            .select(Direction.NORTH, BellAttachType.FLOOR, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(
                                Direction.SOUTH,
                                BellAttachType.FLOOR,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.EAST,
                                BellAttachType.FLOOR,
                                Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.WEST,
                                BellAttachType.FLOOR,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(Direction.NORTH, BellAttachType.CEILING, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(
                                Direction.SOUTH,
                                BellAttachType.CEILING,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.EAST,
                                BellAttachType.CEILING,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.WEST,
                                BellAttachType.CEILING,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                Direction.NORTH,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                Direction.SOUTH,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(Direction.EAST, BellAttachType.SINGLE_WALL, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                            .select(
                                Direction.WEST,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.SOUTH,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.NORTH,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(Direction.EAST, BellAttachType.DOUBLE_WALL, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                            .select(
                                Direction.WEST,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                    )
            );
    }

    private void createGrindstone() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        Blocks.GRINDSTONE, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.GRINDSTONE))
                    )
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                            .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                            .select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                            .select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                            .select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(AttachFace.WALL, Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                            .select(
                                AttachFace.WALL,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                            .select(
                                AttachFace.CEILING,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.NORTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createFurnace(Block p_388515_, TexturedModel.Provider p_387926_) {
        ResourceLocation resourcelocation = p_387926_.create(p_388515_, this.modelOutput);
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(p_388515_, "_front_on");
        ResourceLocation resourcelocation2 = p_387926_.get(p_388515_)
            .updateTextures(p_388889_ -> p_388889_.put(TextureSlot.FRONT, resourcelocation1))
            .createWithSuffix(p_388515_, "_on", this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_388515_)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation2, resourcelocation))
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createCampfires(Block... p_387949_) {
        ResourceLocation resourcelocation = ModelLocationUtils.decorateBlockModelLocation("campfire_off");

        for (Block block : p_387949_) {
            ResourceLocation resourcelocation1 = ModelTemplates.CAMPFIRE.create(block, TextureMapping.campfire(block), this.modelOutput);
            this.registerSimpleFlatItemModel(block.asItem());
            this.blockStateOutput
                .accept(
                    MultiVariantGenerator.multiVariant(block)
                        .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation1, resourcelocation))
                        .with(createHorizontalFacingDispatchAlt())
                );
        }
    }

    private void createAzalea(Block p_386649_) {
        ResourceLocation resourcelocation = ModelTemplates.AZALEA.create(p_386649_, TextureMapping.cubeTop(p_386649_), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_386649_, resourcelocation));
    }

    private void createPottedAzalea(Block p_386806_) {
        ResourceLocation resourcelocation;
        if (p_386806_ == Blocks.POTTED_FLOWERING_AZALEA) {
            resourcelocation = ModelTemplates.POTTED_FLOWERING_AZALEA.create(p_386806_, TextureMapping.pottedAzalea(p_386806_), this.modelOutput);
        } else {
            resourcelocation = ModelTemplates.POTTED_AZALEA.create(p_386806_, TextureMapping.pottedAzalea(p_386806_), this.modelOutput);
        }

        this.blockStateOutput.accept(createSimpleBlock(p_386806_, resourcelocation));
    }

    private void createBookshelf() {
        TextureMapping texturemapping = TextureMapping.column(
            TextureMapping.getBlockTexture(Blocks.BOOKSHELF), TextureMapping.getBlockTexture(Blocks.OAK_PLANKS)
        );
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(Blocks.BOOKSHELF, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(Blocks.BOOKSHELF, resourcelocation));
    }

    private void createRedstoneWire() {
        this.registerSimpleFlatItemModel(Items.REDSTONE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.REDSTONE_WIRE)
                    .with(
                        Condition.or(
                            Condition.condition()
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.NONE),
                            Condition.condition()
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                        ),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_dot"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt1"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side1"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private void createComparator() {
        this.registerSimpleFlatItemModel(Items.COMPARATOR);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.COMPARATOR)
                    .with(createHorizontalFacingDispatchAlt())
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.MODE_COMPARATOR, BlockStateProperties.POWERED)
                            .select(
                                ComparatorMode.COMPARE,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR))
                            )
                            .select(
                                ComparatorMode.COMPARE,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on"))
                            )
                            .select(
                                ComparatorMode.SUBTRACT,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_subtract"))
                            )
                            .select(
                                ComparatorMode.SUBTRACT,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on_subtract"))
                            )
                    )
            );
    }

    private void createSmoothStoneSlab() {
        TextureMapping texturemapping = TextureMapping.cube(Blocks.SMOOTH_STONE);
        TextureMapping texturemapping1 = TextureMapping.column(
            TextureMapping.getBlockTexture(Blocks.SMOOTH_STONE_SLAB, "_side"), texturemapping.get(TextureSlot.TOP)
        );
        ResourceLocation resourcelocation = ModelTemplates.SLAB_BOTTOM.create(Blocks.SMOOTH_STONE_SLAB, texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.SLAB_TOP.create(Blocks.SMOOTH_STONE_SLAB, texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_COLUMN
            .createWithOverride(Blocks.SMOOTH_STONE_SLAB, "_double", texturemapping1, this.modelOutput);
        this.blockStateOutput.accept(createSlab(Blocks.SMOOTH_STONE_SLAB, resourcelocation, resourcelocation1, resourcelocation2));
        this.blockStateOutput
            .accept(createSimpleBlock(Blocks.SMOOTH_STONE, ModelTemplates.CUBE_ALL.create(Blocks.SMOOTH_STONE, texturemapping, this.modelOutput)));
    }

    private void createBrewingStand() {
        this.registerSimpleFlatItemModel(Items.BREWING_STAND);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.BREWING_STAND)
                    .with(Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND)))
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_0, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_1, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_2, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle2"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_0, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_1, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_2, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty2"))
                    )
            );
    }

    private void createMushroomBlock(Block p_388752_) {
        ResourceLocation resourcelocation = ModelTemplates.SINGLE_FACE.create(p_388752_, TextureMapping.defaultTexture(p_388752_), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelLocationUtils.decorateBlockModelLocation("mushroom_block_inside");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(p_388752_)
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(Condition.condition().term(BlockStateProperties.NORTH, false), Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, false)
                    )
            );
        this.registerSimpleItemModel(p_388752_, TexturedModel.CUBE.createWithSuffix(p_388752_, "_inventory", this.modelOutput));
    }

    private void createCakeBlock() {
        this.registerSimpleFlatItemModel(Items.CAKE);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CAKE)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.BITES)
                            .select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE)))
                            .select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice1")))
                            .select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice2")))
                            .select(3, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice3")))
                            .select(4, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice4")))
                            .select(5, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice5")))
                            .select(6, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice6")))
                    )
            );
    }

    private void createCartographyTable() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.DARK_OAK_PLANKS))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_top"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side1"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side2"));
        this.blockStateOutput
            .accept(createSimpleBlock(Blocks.CARTOGRAPHY_TABLE, ModelTemplates.CUBE.create(Blocks.CARTOGRAPHY_TABLE, texturemapping, this.modelOutput)));
    }

    private void createSmithingTable() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_bottom"))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_top"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"));
        this.blockStateOutput
            .accept(createSimpleBlock(Blocks.SMITHING_TABLE, ModelTemplates.CUBE.create(Blocks.SMITHING_TABLE, texturemapping, this.modelOutput)));
    }

    private void createCraftingTableLike(Block p_388054_, Block p_387222_, BiFunction<Block, Block, TextureMapping> p_386655_) {
        TextureMapping texturemapping = p_386655_.apply(p_388054_, p_387222_);
        this.blockStateOutput.accept(createSimpleBlock(p_388054_, ModelTemplates.CUBE.create(p_388054_, texturemapping, this.modelOutput)));
    }

    public void createGenericCube(Block p_387472_) {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(p_387472_, "_particle"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(p_387472_, "_down"))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(p_387472_, "_up"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(p_387472_, "_north"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(p_387472_, "_south"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(p_387472_, "_east"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(p_387472_, "_west"));
        this.blockStateOutput.accept(createSimpleBlock(p_387472_, ModelTemplates.CUBE.create(p_387472_, texturemapping, this.modelOutput)));
    }

    private void createPumpkins() {
        TextureMapping texturemapping = TextureMapping.column(Blocks.PUMPKIN);
        this.blockStateOutput.accept(createSimpleBlock(Blocks.PUMPKIN, ModelLocationUtils.getModelLocation(Blocks.PUMPKIN)));
        this.createPumpkinVariant(Blocks.CARVED_PUMPKIN, texturemapping);
        this.createPumpkinVariant(Blocks.JACK_O_LANTERN, texturemapping);
    }

    private void createPumpkinVariant(Block p_387285_, TextureMapping p_386503_) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ORIENTABLE
            .create(p_387285_, p_386503_.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(p_387285_)), this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_387285_, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createCauldrons() {
        this.registerSimpleFlatItemModel(Items.CAULDRON);
        this.createNonTemplateModelBlock(Blocks.CAULDRON);
        this.blockStateOutput
            .accept(
                createSimpleBlock(
                    Blocks.LAVA_CAULDRON,
                    ModelTemplates.CAULDRON_FULL
                        .create(Blocks.LAVA_CAULDRON, TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.LAVA, "_still")), this.modelOutput)
                )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.WATER_CAULDRON)
                    .with(
                        PropertyDispatch.property(LayeredCauldronBlock.LEVEL)
                            .select(
                                1,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL1
                                            .createWithSuffix(
                                                Blocks.WATER_CAULDRON,
                                                "_level1",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                2,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL2
                                            .createWithSuffix(
                                                Blocks.WATER_CAULDRON,
                                                "_level2",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                3,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_FULL
                                            .createWithSuffix(
                                                Blocks.WATER_CAULDRON,
                                                "_full",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")),
                                                this.modelOutput
                                            )
                                    )
                            )
                    )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.POWDER_SNOW_CAULDRON)
                    .with(
                        PropertyDispatch.property(LayeredCauldronBlock.LEVEL)
                            .select(
                                1,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL1
                                            .createWithSuffix(
                                                Blocks.POWDER_SNOW_CAULDRON,
                                                "_level1",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                2,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL2
                                            .createWithSuffix(
                                                Blocks.POWDER_SNOW_CAULDRON,
                                                "_level2",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                3,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_FULL
                                            .createWithSuffix(
                                                Blocks.POWDER_SNOW_CAULDRON,
                                                "_full",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)),
                                                this.modelOutput
                                            )
                                    )
                            )
                    )
            );
    }

    private void createChorusFlower() {
        TextureMapping texturemapping = TextureMapping.defaultTexture(Blocks.CHORUS_FLOWER);
        ResourceLocation resourcelocation = ModelTemplates.CHORUS_FLOWER.create(Blocks.CHORUS_FLOWER, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(
            Blocks.CHORUS_FLOWER, "_dead", ModelTemplates.CHORUS_FLOWER, p_388494_ -> texturemapping.copyAndUpdate(TextureSlot.TEXTURE, p_388494_)
        );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CHORUS_FLOWER)
                    .with(createEmptyOrFullDispatch(BlockStateProperties.AGE_5, 5, resourcelocation1, resourcelocation))
            );
    }

    private void createCrafterBlock() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.CRAFTER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_triggered");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_crafting");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_crafting_triggered");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CRAFTER)
                    .with(PropertyDispatch.property(BlockStateProperties.ORIENTATION).generate(p_387710_ -> this.applyRotation(p_387710_, Variant.variant())))
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.TRIGGERED, CrafterBlock.CRAFTING)
                            .select(false, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(true, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                            .select(true, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(false, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                    )
            );
    }

    private void createDispenserBlock(Block p_387922_) {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_side"))
            .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(p_387922_, "_front"));
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
            .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(p_387922_, "_front_vertical"));
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ORIENTABLE.create(p_387922_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_ORIENTABLE_VERTICAL.create(p_387922_, texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_387922_)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.FACING)
                            .select(
                                Direction.DOWN,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(Direction.UP, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(
                                Direction.EAST,
                                Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createEndPortalFrame() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME, "_filled");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.END_PORTAL_FRAME)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.EYE)
                            .select(false, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(true, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createChorusPlant() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_side");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside1");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside2");
        ResourceLocation resourcelocation4 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside3");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.CHORUS_PLANT)
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1).with(VariantProperties.WEIGHT, 2),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation4)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
            );
    }

    private void createComposter() {
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.COMPOSTER)
                    .with(Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER)))
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 1),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 2),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents2"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 3),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents3"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 4),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents4"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 5),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents5"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 6),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents6"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 7),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents7"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 8),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents_ready"))
                    )
            );
    }

    private void createCopperBulb(Block p_386673_) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ALL.create(p_386673_, TextureMapping.cube(p_386673_), this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(p_386673_, "_powered", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        ResourceLocation resourcelocation2 = this.createSuffixedVariant(p_386673_, "_lit", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        ResourceLocation resourcelocation3 = this.createSuffixedVariant(p_386673_, "_lit_powered", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        this.blockStateOutput.accept(this.createCopperBulb(p_386673_, resourcelocation, resourcelocation2, resourcelocation1, resourcelocation3));
    }

    private BlockStateGenerator createCopperBulb(
        Block p_386987_, ResourceLocation p_387409_, ResourceLocation p_388072_, ResourceLocation p_386745_, ResourceLocation p_388444_
    ) {
        return MultiVariantGenerator.multiVariant(p_386987_)
            .with(
                PropertyDispatch.properties(BlockStateProperties.LIT, BlockStateProperties.POWERED)
                    .generate(
                        (p_388879_, p_387027_) -> p_388879_
                                ? Variant.variant().with(VariantProperties.MODEL, p_387027_ ? p_388444_ : p_388072_)
                                : Variant.variant().with(VariantProperties.MODEL, p_387027_ ? p_386745_ : p_387409_)
                    )
            );
    }

    private void copyCopperBulbModel(Block p_388614_, Block p_387741_) {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(p_388614_);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(p_388614_, "_powered");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(p_388614_, "_lit");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(p_388614_, "_lit_powered");
        this.itemModelOutput.copy(p_388614_.asItem(), p_387741_.asItem());
        this.blockStateOutput.accept(this.createCopperBulb(p_387741_, resourcelocation, resourcelocation2, resourcelocation1, resourcelocation3));
    }

    private void createAmethystCluster(Block p_388409_) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        p_388409_,
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelTemplates.CROSS.create(p_388409_, TextureMapping.cross(p_388409_), this.modelOutput))
                    )
                    .with(this.createColumnWithFacing())
            );
    }

    private void createAmethystClusters() {
        this.createAmethystCluster(Blocks.SMALL_AMETHYST_BUD);
        this.createAmethystCluster(Blocks.MEDIUM_AMETHYST_BUD);
        this.createAmethystCluster(Blocks.LARGE_AMETHYST_BUD);
        this.createAmethystCluster(Blocks.AMETHYST_CLUSTER);
    }

    private void createPointedDripstone() {
        PropertyDispatch.C2<Direction, DripstoneThickness> c2 = PropertyDispatch.properties(
            BlockStateProperties.VERTICAL_DIRECTION, BlockStateProperties.DRIPSTONE_THICKNESS
        );

        for (DripstoneThickness dripstonethickness : DripstoneThickness.values()) {
            c2.select(Direction.UP, dripstonethickness, this.createPointedDripstoneVariant(Direction.UP, dripstonethickness));
        }

        for (DripstoneThickness dripstonethickness1 : DripstoneThickness.values()) {
            c2.select(Direction.DOWN, dripstonethickness1, this.createPointedDripstoneVariant(Direction.DOWN, dripstonethickness1));
        }

        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.POINTED_DRIPSTONE).with(c2));
    }

    private Variant createPointedDripstoneVariant(Direction p_387068_, DripstoneThickness p_388190_) {
        String s = "_" + p_387068_.getSerializedName() + "_" + p_388190_.getSerializedName();
        TextureMapping texturemapping = TextureMapping.cross(TextureMapping.getBlockTexture(Blocks.POINTED_DRIPSTONE, s));
        return Variant.variant()
            .with(VariantProperties.MODEL, ModelTemplates.POINTED_DRIPSTONE.createWithSuffix(Blocks.POINTED_DRIPSTONE, s, texturemapping, this.modelOutput));
    }

    private void createNyliumBlock(Block p_387188_) {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.NETHERRACK))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(p_387188_))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(p_387188_, "_side"));
        this.blockStateOutput.accept(createSimpleBlock(p_387188_, ModelTemplates.CUBE_BOTTOM_TOP.create(p_387188_, texturemapping, this.modelOutput)));
    }

    private void createDaylightDetector() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_side");
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_top"))
            .put(TextureSlot.SIDE, resourcelocation);
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_inverted_top"))
            .put(TextureSlot.SIDE, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.DAYLIGHT_DETECTOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.INVERTED)
                            .select(
                                false,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.DAYLIGHT_DETECTOR.create(Blocks.DAYLIGHT_DETECTOR, texturemapping, this.modelOutput)
                                    )
                            )
                            .select(
                                true,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.DAYLIGHT_DETECTOR
                                            .create(
                                                ModelLocationUtils.getModelLocation(Blocks.DAYLIGHT_DETECTOR, "_inverted"), texturemapping1, this.modelOutput
                                            )
                                    )
                            )
                    )
            );
    }

    private void createRotatableColumn(Block p_388161_) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_388161_, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(p_388161_)))
                    .with(this.createColumnWithFacing())
            );
    }

    private void createLightningRod() {
        Block block = Blocks.LIGHTNING_ROD;
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block, "_on");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(block);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block)))
                    .with(this.createColumnWithFacing())
                    .with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourcelocation, resourcelocation1))
            );
    }

    private void createFarmland() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND));
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"));
        ResourceLocation resourcelocation = ModelTemplates.FARMLAND.create(Blocks.FARMLAND, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FARMLAND
            .create(TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"), texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.FARMLAND)
                    .with(createEmptyOrFullDispatch(BlockStateProperties.MOISTURE, 7, resourcelocation1, resourcelocation))
            );
    }

    private List<ResourceLocation> createFloorFireModels(Block p_387402_) {
        ResourceLocation resourcelocation = ModelTemplates.FIRE_FLOOR
            .create(ModelLocationUtils.getModelLocation(p_387402_, "_floor0"), TextureMapping.fire0(p_387402_), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FIRE_FLOOR
            .create(ModelLocationUtils.getModelLocation(p_387402_, "_floor1"), TextureMapping.fire1(p_387402_), this.modelOutput);
        return ImmutableList.of(resourcelocation, resourcelocation1);
    }

    private List<ResourceLocation> createSideFireModels(Block p_387079_) {
        ResourceLocation resourcelocation = ModelTemplates.FIRE_SIDE
            .create(ModelLocationUtils.getModelLocation(p_387079_, "_side0"), TextureMapping.fire0(p_387079_), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FIRE_SIDE
            .create(ModelLocationUtils.getModelLocation(p_387079_, "_side1"), TextureMapping.fire1(p_387079_), this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.FIRE_SIDE_ALT
            .create(ModelLocationUtils.getModelLocation(p_387079_, "_side_alt0"), TextureMapping.fire0(p_387079_), this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.FIRE_SIDE_ALT
            .create(ModelLocationUtils.getModelLocation(p_387079_, "_side_alt1"), TextureMapping.fire1(p_387079_), this.modelOutput);
        return ImmutableList.of(resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3);
    }

    private List<ResourceLocation> createTopFireModels(Block p_387163_) {
        ResourceLocation resourcelocation = ModelTemplates.FIRE_UP
            .create(ModelLocationUtils.getModelLocation(p_387163_, "_up0"), TextureMapping.fire0(p_387163_), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FIRE_UP
            .create(ModelLocationUtils.getModelLocation(p_387163_, "_up1"), TextureMapping.fire1(p_387163_), this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.FIRE_UP_ALT
            .create(ModelLocationUtils.getModelLocation(p_387163_, "_up_alt0"), TextureMapping.fire0(p_387163_), this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.FIRE_UP_ALT
            .create(ModelLocationUtils.getModelLocation(p_387163_, "_up_alt1"), TextureMapping.fire1(p_387163_), this.modelOutput);
        return ImmutableList.of(resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3);
    }

    private static List<Variant> wrapModels(List<ResourceLocation> p_388626_, UnaryOperator<Variant> p_388669_) {
        return p_388626_.stream().map(p_387721_ -> Variant.variant().with(VariantProperties.MODEL, p_387721_)).map(p_388669_).collect(Collectors.toList());
    }

    private void createFire() {
        Condition condition = Condition.condition()
            .term(BlockStateProperties.NORTH, false)
            .term(BlockStateProperties.EAST, false)
            .term(BlockStateProperties.SOUTH, false)
            .term(BlockStateProperties.WEST, false)
            .term(BlockStateProperties.UP, false);
        List<ResourceLocation> list = this.createFloorFireModels(Blocks.FIRE);
        List<ResourceLocation> list1 = this.createSideFireModels(Blocks.FIRE);
        List<ResourceLocation> list2 = this.createTopFireModels(Blocks.FIRE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.FIRE)
                    .with(condition, wrapModels(list, p_387290_ -> p_387290_))
                    .with(Condition.or(Condition.condition().term(BlockStateProperties.NORTH, true), condition), wrapModels(list1, p_386602_ -> p_386602_))
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.EAST, true), condition),
                        wrapModels(list1, p_388679_ -> p_388679_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                    )
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.SOUTH, true), condition),
                        wrapModels(list1, p_387956_ -> p_387956_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    )
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.WEST, true), condition),
                        wrapModels(list1, p_386620_ -> p_386620_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                    )
                    .with(Condition.condition().term(BlockStateProperties.UP, true), wrapModels(list2, p_387194_ -> p_387194_))
            );
    }

    private void createSoulFire() {
        List<ResourceLocation> list = this.createFloorFireModels(Blocks.SOUL_FIRE);
        List<ResourceLocation> list1 = this.createSideFireModels(Blocks.SOUL_FIRE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.SOUL_FIRE)
                    .with(wrapModels(list, p_387713_ -> p_387713_))
                    .with(wrapModels(list1, p_387314_ -> p_387314_))
                    .with(wrapModels(list1, p_388922_ -> p_388922_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)))
                    .with(wrapModels(list1, p_388318_ -> p_388318_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)))
                    .with(wrapModels(list1, p_386556_ -> p_386556_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)))
            );
    }

    private void createLantern(Block p_386669_) {
        ResourceLocation resourcelocation = TexturedModel.LANTERN.create(p_386669_, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.HANGING_LANTERN.create(p_386669_, this.modelOutput);
        this.registerSimpleFlatItemModel(p_386669_.asItem());
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_386669_)
                    .with(createBooleanModelDispatch(BlockStateProperties.HANGING, resourcelocation1, resourcelocation))
            );
    }

    private void createMuddyMangroveRoots() {
        TextureMapping texturemapping = TextureMapping.column(
            TextureMapping.getBlockTexture(Blocks.MUDDY_MANGROVE_ROOTS, "_side"), TextureMapping.getBlockTexture(Blocks.MUDDY_MANGROVE_ROOTS, "_top")
        );
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(Blocks.MUDDY_MANGROVE_ROOTS, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createAxisAlignedPillarBlock(Blocks.MUDDY_MANGROVE_ROOTS, resourcelocation));
    }

    private void createMangrovePropagule() {
        this.registerSimpleFlatItemModel(Items.MANGROVE_PROPAGULE);
        Block block = Blocks.MANGROVE_PROPAGULE;
        PropertyDispatch.C2<Boolean, Integer> c2 = PropertyDispatch.properties(MangrovePropaguleBlock.HANGING, MangrovePropaguleBlock.AGE);
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block);

        for (int i = 0; i <= 4; i++) {
            ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(block, "_hanging_" + i);
            c2.select(true, i, Variant.variant().with(VariantProperties.MODEL, resourcelocation1));
            c2.select(false, i, Variant.variant().with(VariantProperties.MODEL, resourcelocation));
        }

        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.MANGROVE_PROPAGULE).with(c2));
    }

    private void createFrostedIce() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.FROSTED_ICE)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_3)
                            .select(
                                0,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_0", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                1,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_1", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                2,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_2", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                3,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_3", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                    )
            );
    }

    private void createGrassBlocks() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.DIRT);
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, resourcelocation)
            .copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_snow"));
        Variant variant = Variant.variant()
            .with(VariantProperties.MODEL, ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.GRASS_BLOCK, "_snow", texturemapping, this.modelOutput));
        this.createGrassLikeBlock(Blocks.GRASS_BLOCK, ModelLocationUtils.getModelLocation(Blocks.GRASS_BLOCK), variant);
        this.registerSimpleTintedItemModel(Blocks.GRASS_BLOCK, ModelLocationUtils.getModelLocation(Blocks.GRASS_BLOCK), new GrassColorSource());
        ResourceLocation resourcelocation1 = TexturedModel.CUBE_TOP_BOTTOM
            .get(Blocks.MYCELIUM)
            .updateTextures(p_388599_ -> p_388599_.put(TextureSlot.BOTTOM, resourcelocation))
            .create(Blocks.MYCELIUM, this.modelOutput);
        this.createGrassLikeBlock(Blocks.MYCELIUM, resourcelocation1, variant);
        ResourceLocation resourcelocation2 = TexturedModel.CUBE_TOP_BOTTOM
            .get(Blocks.PODZOL)
            .updateTextures(p_388519_ -> p_388519_.put(TextureSlot.BOTTOM, resourcelocation))
            .create(Blocks.PODZOL, this.modelOutput);
        this.createGrassLikeBlock(Blocks.PODZOL, resourcelocation2, variant);
    }

    private void createGrassLikeBlock(Block p_386747_, ResourceLocation p_387698_, Variant p_387993_) {
        List<Variant> list = Arrays.asList(createRotatedVariants(p_387698_));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_386747_)
                    .with(PropertyDispatch.property(BlockStateProperties.SNOWY).select(true, p_387993_).select(false, list))
            );
    }

    private void createCocoa() {
        this.registerSimpleFlatItemModel(Items.COCOA_BEANS);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.COCOA)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_2)
                            .select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage0")))
                            .select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage1")))
                            .select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage2")))
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createDirtPath() {
        this.blockStateOutput.accept(createRotatedVariant(Blocks.DIRT_PATH, ModelLocationUtils.getModelLocation(Blocks.DIRT_PATH)));
    }

    private void createWeightedPressurePlate(Block p_388755_, Block p_387629_) {
        TextureMapping texturemapping = TextureMapping.defaultTexture(p_387629_);
        ResourceLocation resourcelocation = ModelTemplates.PRESSURE_PLATE_UP.create(p_388755_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.PRESSURE_PLATE_DOWN.create(p_388755_, texturemapping, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_388755_)
                    .with(createEmptyOrFullDispatch(BlockStateProperties.POWER, 1, resourcelocation1, resourcelocation))
            );
    }

    private void createHopper() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.HOPPER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.HOPPER, "_side");
        this.registerSimpleFlatItemModel(Items.HOPPER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.HOPPER)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.FACING_HOPPER)
                            .select(Direction.DOWN, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void copyModel(Block p_388439_, Block p_387216_) {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(p_388439_);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(p_387216_, Variant.variant().with(VariantProperties.MODEL, resourcelocation)));
        this.itemModelOutput.copy(p_388439_.asItem(), p_387216_.asItem());
    }

    private void createIronBars() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post_ends");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap_alt");
        ResourceLocation resourcelocation4 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side");
        ResourceLocation resourcelocation5 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side_alt");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.IRON_BARS)
                    .with(Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, true)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, true)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, true)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation4))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.SOUTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation5))
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation5).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
        this.registerSimpleFlatItemModel(Blocks.IRON_BARS);
    }

    private void createNonTemplateHorizontalBlock(Block p_388554_) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_388554_, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(p_388554_)))
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createLever() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.LEVER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.LEVER, "_on");
        this.registerSimpleFlatItemModel(Blocks.LEVER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.LEVER)
                    .with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourcelocation, resourcelocation1))
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                            .select(
                                AttachFace.CEILING,
                                Direction.NORTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                            .select(
                                AttachFace.CEILING,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                            .select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                            .select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                            .select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(AttachFace.WALL, Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                            .select(
                                AttachFace.WALL,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createLilyPad() {
        ResourceLocation resourcelocation = this.createFlatItemModelWithBlockTexture(Items.LILY_PAD, Blocks.LILY_PAD);
        this.registerSimpleTintedItemModel(Blocks.LILY_PAD, resourcelocation, ItemModelUtils.constantTint(-9321636));
        this.blockStateOutput.accept(createRotatedVariant(Blocks.LILY_PAD, ModelLocationUtils.getModelLocation(Blocks.LILY_PAD)));
    }

    private void createFrogspawnBlock() {
        this.registerSimpleFlatItemModel(Blocks.FROGSPAWN);
        this.blockStateOutput.accept(createSimpleBlock(Blocks.FROGSPAWN, ModelLocationUtils.getModelLocation(Blocks.FROGSPAWN)));
    }

    private void createNetherPortalBlock() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.NETHER_PORTAL)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.HORIZONTAL_AXIS)
                            .select(
                                Direction.Axis.X,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ns"))
                            )
                            .select(
                                Direction.Axis.Z,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ew"))
                            )
                    )
            );
    }

    private void createNetherrack() {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(Blocks.NETHERRACK, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                    Blocks.NETHERRACK,
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                )
            );
    }

    private void createObserver() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.OBSERVER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.OBSERVER, "_on");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.OBSERVER)
                    .with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourcelocation1, resourcelocation))
                    .with(createFacingDispatch())
            );
    }

    private void createPistons() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.PISTON, "_bottom"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky");
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(Blocks.PISTON, "_top");
        TextureMapping texturemapping1 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, resourcelocation);
        TextureMapping texturemapping2 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, resourcelocation1);
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.PISTON, "_base");
        this.createPistonVariant(Blocks.PISTON, resourcelocation2, texturemapping2);
        this.createPistonVariant(Blocks.STICKY_PISTON, resourcelocation2, texturemapping1);
        ResourceLocation resourcelocation3 = ModelTemplates.CUBE_BOTTOM_TOP
            .createWithSuffix(Blocks.PISTON, "_inventory", texturemapping.copyAndUpdate(TextureSlot.TOP, resourcelocation1), this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.CUBE_BOTTOM_TOP
            .createWithSuffix(Blocks.STICKY_PISTON, "_inventory", texturemapping.copyAndUpdate(TextureSlot.TOP, resourcelocation), this.modelOutput);
        this.registerSimpleItemModel(Blocks.PISTON, resourcelocation3);
        this.registerSimpleItemModel(Blocks.STICKY_PISTON, resourcelocation4);
    }

    private void createPistonVariant(Block p_387297_, ResourceLocation p_387538_, TextureMapping p_388405_) {
        ResourceLocation resourcelocation = ModelTemplates.PISTON.create(p_387297_, p_388405_, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_387297_)
                    .with(createBooleanModelDispatch(BlockStateProperties.EXTENDED, p_387538_, resourcelocation))
                    .with(createFacingDispatch())
            );
    }

    private void createPistonHeads() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.UNSTICKY, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        TextureMapping texturemapping1 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky"));
        TextureMapping texturemapping2 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.PISTON_HEAD)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.SHORT, BlockStateProperties.PISTON_TYPE)
                            .select(
                                false,
                                PistonType.DEFAULT,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head", texturemapping2, this.modelOutput)
                                    )
                            )
                            .select(
                                false,
                                PistonType.STICKY,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head_sticky", texturemapping1, this.modelOutput)
                                    )
                            )
                            .select(
                                true,
                                PistonType.DEFAULT,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD_SHORT.createWithSuffix(Blocks.PISTON, "_head_short", texturemapping2, this.modelOutput)
                                    )
                            )
                            .select(
                                true,
                                PistonType.STICKY,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD_SHORT
                                            .createWithSuffix(Blocks.PISTON, "_head_short_sticky", texturemapping1, this.modelOutput)
                                    )
                            )
                    )
                    .with(createFacingDispatch())
            );
    }

    private void createTrialSpawner() {
        Block block = Blocks.TRIAL_SPAWNER;
        TextureMapping texturemapping = TextureMapping.trialSpawner(block, "_side_inactive", "_top_inactive");
        TextureMapping texturemapping1 = TextureMapping.trialSpawner(block, "_side_active", "_top_active");
        TextureMapping texturemapping2 = TextureMapping.trialSpawner(block, "_side_active", "_top_ejecting_reward");
        TextureMapping texturemapping3 = TextureMapping.trialSpawner(block, "_side_inactive_ominous", "_top_inactive_ominous");
        TextureMapping texturemapping4 = TextureMapping.trialSpawner(block, "_side_active_ominous", "_top_active_ominous");
        TextureMapping texturemapping5 = TextureMapping.trialSpawner(block, "_side_active_ominous", "_top_ejecting_reward_ominous");
        ResourceLocation resourcelocation = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.create(block, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_active", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES
            .createWithSuffix(block, "_ejecting_reward", texturemapping2, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES
            .createWithSuffix(block, "_inactive_ominous", texturemapping3, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES
            .createWithSuffix(block, "_active_ominous", texturemapping4, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES
            .createWithSuffix(block, "_ejecting_reward_ominous", texturemapping5, this.modelOutput);
        this.registerSimpleItemModel(block, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(block)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.TRIAL_SPAWNER_STATE, BlockStateProperties.OMINOUS)
                            .generate(
                                (p_387309_, p_388867_) -> {
                                    return switch (p_387309_) {
                                        case INACTIVE, COOLDOWN -> Variant.variant()
                                        .with(VariantProperties.MODEL, p_388867_ ? resourcelocation3 : resourcelocation);
                                        case WAITING_FOR_PLAYERS, ACTIVE, WAITING_FOR_REWARD_EJECTION -> Variant.variant()
                                        .with(VariantProperties.MODEL, p_388867_ ? resourcelocation4 : resourcelocation1);
                                        case EJECTING_REWARD -> Variant.variant()
                                        .with(VariantProperties.MODEL, p_388867_ ? resourcelocation5 : resourcelocation2);
                                    };
                                }
                            )
                    )
            );
    }

    private void createVault() {
        Block block = Blocks.VAULT;
        TextureMapping texturemapping = TextureMapping.vault(block, "_front_off", "_side_off", "_top", "_bottom");
        TextureMapping texturemapping1 = TextureMapping.vault(block, "_front_on", "_side_on", "_top", "_bottom");
        TextureMapping texturemapping2 = TextureMapping.vault(block, "_front_ejecting", "_side_on", "_top", "_bottom");
        TextureMapping texturemapping3 = TextureMapping.vault(block, "_front_ejecting", "_side_on", "_top_ejecting", "_bottom");
        ResourceLocation resourcelocation = ModelTemplates.VAULT.create(block, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.VAULT.createWithSuffix(block, "_active", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.VAULT.createWithSuffix(block, "_unlocking", texturemapping2, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.VAULT.createWithSuffix(block, "_ejecting_reward", texturemapping3, this.modelOutput);
        TextureMapping texturemapping4 = TextureMapping.vault(block, "_front_off_ominous", "_side_off_ominous", "_top_ominous", "_bottom_ominous");
        TextureMapping texturemapping5 = TextureMapping.vault(block, "_front_on_ominous", "_side_on_ominous", "_top_ominous", "_bottom_ominous");
        TextureMapping texturemapping6 = TextureMapping.vault(block, "_front_ejecting_ominous", "_side_on_ominous", "_top_ominous", "_bottom_ominous");
        TextureMapping texturemapping7 = TextureMapping.vault(block, "_front_ejecting_ominous", "_side_on_ominous", "_top_ejecting_ominous", "_bottom_ominous");
        ResourceLocation resourcelocation4 = ModelTemplates.VAULT.createWithSuffix(block, "_ominous", texturemapping4, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.VAULT.createWithSuffix(block, "_active_ominous", texturemapping5, this.modelOutput);
        ResourceLocation resourcelocation6 = ModelTemplates.VAULT.createWithSuffix(block, "_unlocking_ominous", texturemapping6, this.modelOutput);
        ResourceLocation resourcelocation7 = ModelTemplates.VAULT.createWithSuffix(block, "_ejecting_reward_ominous", texturemapping7, this.modelOutput);
        this.registerSimpleItemModel(block, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(block)
                    .with(createHorizontalFacingDispatch())
                    .with(PropertyDispatch.properties(VaultBlock.STATE, VaultBlock.OMINOUS).generate((p_387243_, p_388586_) -> {
                        return switch (p_387243_) {
                            case INACTIVE -> Variant.variant().with(VariantProperties.MODEL, p_388586_ ? resourcelocation4 : resourcelocation);
                            case ACTIVE -> Variant.variant().with(VariantProperties.MODEL, p_388586_ ? resourcelocation5 : resourcelocation1);
                            case UNLOCKING -> Variant.variant().with(VariantProperties.MODEL, p_388586_ ? resourcelocation6 : resourcelocation2);
                            case EJECTING -> Variant.variant().with(VariantProperties.MODEL, p_388586_ ? resourcelocation7 : resourcelocation3);
                        };
                    }))
            );
    }

    private void createSculkSensor() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SCULK_SENSOR, "_inactive");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.SCULK_SENSOR, "_active");
        this.registerSimpleItemModel(Blocks.SCULK_SENSOR, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SCULK_SENSOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.SCULK_SENSOR_PHASE)
                            .generate(
                                p_388718_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            p_388718_ != SculkSensorPhase.ACTIVE && p_388718_ != SculkSensorPhase.COOLDOWN
                                                ? resourcelocation
                                                : resourcelocation1
                                        )
                            )
                    )
            );
    }

    private void createCalibratedSculkSensor() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.CALIBRATED_SCULK_SENSOR, "_inactive");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.CALIBRATED_SCULK_SENSOR, "_active");
        this.registerSimpleItemModel(Blocks.CALIBRATED_SCULK_SENSOR, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CALIBRATED_SCULK_SENSOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.SCULK_SENSOR_PHASE)
                            .generate(
                                p_387414_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            p_387414_ != SculkSensorPhase.ACTIVE && p_387414_ != SculkSensorPhase.COOLDOWN
                                                ? resourcelocation
                                                : resourcelocation1
                                        )
                            )
                    )
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createSculkShrieker() {
        ResourceLocation resourcelocation = ModelTemplates.SCULK_SHRIEKER.create(Blocks.SCULK_SHRIEKER, TextureMapping.sculkShrieker(false), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.SCULK_SHRIEKER
            .createWithSuffix(Blocks.SCULK_SHRIEKER, "_can_summon", TextureMapping.sculkShrieker(true), this.modelOutput);
        this.registerSimpleItemModel(Blocks.SCULK_SHRIEKER, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SCULK_SHRIEKER)
                    .with(createBooleanModelDispatch(BlockStateProperties.CAN_SUMMON, resourcelocation1, resourcelocation))
            );
    }

    private void createScaffolding() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_stable");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_unstable");
        this.registerSimpleItemModel(Blocks.SCAFFOLDING, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SCAFFOLDING)
                    .with(createBooleanModelDispatch(BlockStateProperties.BOTTOM, resourcelocation1, resourcelocation))
            );
    }

    private void createCaveVines() {
        ResourceLocation resourcelocation = this.createSuffixedVariant(Blocks.CAVE_VINES, "", ModelTemplates.CROSS, TextureMapping::cross);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(Blocks.CAVE_VINES, "_lit", ModelTemplates.CROSS, TextureMapping::cross);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CAVE_VINES)
                    .with(createBooleanModelDispatch(BlockStateProperties.BERRIES, resourcelocation1, resourcelocation))
            );
        ResourceLocation resourcelocation2 = this.createSuffixedVariant(Blocks.CAVE_VINES_PLANT, "", ModelTemplates.CROSS, TextureMapping::cross);
        ResourceLocation resourcelocation3 = this.createSuffixedVariant(Blocks.CAVE_VINES_PLANT, "_lit", ModelTemplates.CROSS, TextureMapping::cross);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CAVE_VINES_PLANT)
                    .with(createBooleanModelDispatch(BlockStateProperties.BERRIES, resourcelocation3, resourcelocation2))
            );
    }

    private void createRedstoneLamp() {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(Blocks.REDSTONE_LAMP, this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(Blocks.REDSTONE_LAMP, "_on", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REDSTONE_LAMP)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation1, resourcelocation))
            );
    }

    private void createNormalTorch(Block p_387133_, Block p_388383_) {
        TextureMapping texturemapping = TextureMapping.torch(p_387133_);
        this.blockStateOutput.accept(createSimpleBlock(p_387133_, ModelTemplates.TORCH.create(p_387133_, texturemapping, this.modelOutput)));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        p_388383_,
                        Variant.variant().with(VariantProperties.MODEL, ModelTemplates.WALL_TORCH.create(p_388383_, texturemapping, this.modelOutput))
                    )
                    .with(createTorchHorizontalDispatch())
            );
        this.registerSimpleFlatItemModel(p_387133_);
    }

    private void createRedstoneTorch() {
        TextureMapping texturemapping = TextureMapping.torch(Blocks.REDSTONE_TORCH);
        TextureMapping texturemapping1 = TextureMapping.torch(TextureMapping.getBlockTexture(Blocks.REDSTONE_TORCH, "_off"));
        ResourceLocation resourcelocation = ModelTemplates.REDSTONE_TORCH.create(Blocks.REDSTONE_TORCH, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.TORCH_UNLIT.createWithSuffix(Blocks.REDSTONE_TORCH, "_off", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REDSTONE_TORCH)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation, resourcelocation1))
            );
        ResourceLocation resourcelocation2 = ModelTemplates.REDSTONE_WALL_TORCH.create(Blocks.REDSTONE_WALL_TORCH, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.WALL_TORCH_UNLIT
            .createWithSuffix(Blocks.REDSTONE_WALL_TORCH, "_off", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REDSTONE_WALL_TORCH)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation2, resourcelocation3))
                    .with(createTorchHorizontalDispatch())
            );
        this.registerSimpleFlatItemModel(Blocks.REDSTONE_TORCH);
    }

    private void createRepeater() {
        this.registerSimpleFlatItemModel(Items.REPEATER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REPEATER)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.DELAY, BlockStateProperties.LOCKED, BlockStateProperties.POWERED)
                            .generate((p_388361_, p_388200_, p_388664_) -> {
                                StringBuilder stringbuilder = new StringBuilder();
                                stringbuilder.append('_').append(p_388361_).append("tick");
                                if (p_388664_) {
                                    stringbuilder.append("_on");
                                }

                                if (p_388200_) {
                                    stringbuilder.append("_locked");
                                }

                                return Variant.variant()
                                    .with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.REPEATER, stringbuilder.toString()));
                            })
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createSeaPickle() {
        this.registerSimpleFlatItemModel(Items.SEA_PICKLE);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SEA_PICKLE)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.PICKLES, BlockStateProperties.WATERLOGGED)
                            .select(1, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("dead_sea_pickle"))))
                            .select(2, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("two_dead_sea_pickles"))))
                            .select(3, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("three_dead_sea_pickles"))))
                            .select(4, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("four_dead_sea_pickles"))))
                            .select(1, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("sea_pickle"))))
                            .select(2, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("two_sea_pickles"))))
                            .select(3, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("three_sea_pickles"))))
                            .select(4, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("four_sea_pickles"))))
                    )
            );
    }

    private void createSnowBlocks() {
        TextureMapping texturemapping = TextureMapping.cube(Blocks.SNOW);
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ALL.create(Blocks.SNOW_BLOCK, texturemapping, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SNOW)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.LAYERS)
                            .generate(
                                p_388767_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            p_388767_ < 8 ? ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height" + p_388767_ * 2) : resourcelocation
                                        )
                            )
                    )
            );
        this.registerSimpleItemModel(Blocks.SNOW, ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height2"));
        this.blockStateOutput.accept(createSimpleBlock(Blocks.SNOW_BLOCK, resourcelocation));
    }

    private void createStonecutter() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        Blocks.STONECUTTER, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.STONECUTTER))
                    )
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createStructureBlock() {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(Blocks.STRUCTURE_BLOCK, this.modelOutput);
        this.registerSimpleItemModel(Blocks.STRUCTURE_BLOCK, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.STRUCTURE_BLOCK)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.STRUCTUREBLOCK_MODE)
                            .generate(
                                p_388734_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            this.createSuffixedVariant(
                                                Blocks.STRUCTURE_BLOCK, "_" + p_388734_.getSerializedName(), ModelTemplates.CUBE_ALL, TextureMapping::cube
                                            )
                                        )
                            )
                    )
            );
    }

    private void createSweetBerryBush() {
        this.registerSimpleFlatItemModel(Items.SWEET_BERRIES);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SWEET_BERRY_BUSH)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_3)
                            .generate(
                                p_388136_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            this.createSuffixedVariant(
                                                Blocks.SWEET_BERRY_BUSH, "_stage" + p_388136_, ModelTemplates.CROSS, TextureMapping::cross
                                            )
                                        )
                            )
                    )
            );
    }

    private void createTripwire() {
        this.registerSimpleFlatItemModel(Items.STRING);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TRIPWIRE)
                    .with(
                        PropertyDispatch.properties(
                                BlockStateProperties.ATTACHED,
                                BlockStateProperties.EAST,
                                BlockStateProperties.NORTH,
                                BlockStateProperties.SOUTH,
                                BlockStateProperties.WEST
                            )
                            .select(
                                false,
                                false,
                                false,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                            )
                            .select(
                                false,
                                false,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                false,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                true,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                false,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                false,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                true,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                true,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                true,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                true,
                                true,
                                true,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nsew"))
                            )
                            .select(
                                true,
                                false,
                                false,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                            )
                            .select(
                                true,
                                false,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                            )
                            .select(
                                true,
                                false,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                true,
                                false,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                false,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                false,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                true,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                true,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                true,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                true,
                                true,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nsew"))
                            )
                    )
            );
    }

    private void createTripwireHook() {
        this.registerSimpleFlatItemModel(Blocks.TRIPWIRE_HOOK);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TRIPWIRE_HOOK)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACHED, BlockStateProperties.POWERED)
                            .generate(
                                (p_386642_, p_387018_) -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            TextureMapping.getBlockTexture(Blocks.TRIPWIRE_HOOK, (p_386642_ ? "_attached" : "") + (p_387018_ ? "_on" : ""))
                                        )
                            )
                    )
                    .with(createHorizontalFacingDispatch())
            );
    }

    private ResourceLocation createTurtleEggModel(int p_387392_, String p_387935_, TextureMapping p_388813_) {
        switch (p_387392_) {
            case 1:
                return ModelTemplates.TURTLE_EGG.create(ModelLocationUtils.decorateBlockModelLocation(p_387935_ + "turtle_egg"), p_388813_, this.modelOutput);
            case 2:
                return ModelTemplates.TWO_TURTLE_EGGS
                    .create(ModelLocationUtils.decorateBlockModelLocation("two_" + p_387935_ + "turtle_eggs"), p_388813_, this.modelOutput);
            case 3:
                return ModelTemplates.THREE_TURTLE_EGGS
                    .create(ModelLocationUtils.decorateBlockModelLocation("three_" + p_387935_ + "turtle_eggs"), p_388813_, this.modelOutput);
            case 4:
                return ModelTemplates.FOUR_TURTLE_EGGS
                    .create(ModelLocationUtils.decorateBlockModelLocation("four_" + p_387935_ + "turtle_eggs"), p_388813_, this.modelOutput);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private ResourceLocation createTurtleEggModel(Integer p_386499_, Integer p_387511_) {
        switch (p_387511_) {
            case 0:
                return this.createTurtleEggModel(p_386499_, "", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG)));
            case 1:
                return this.createTurtleEggModel(
                    p_386499_, "slightly_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_slightly_cracked"))
                );
            case 2:
                return this.createTurtleEggModel(
                    p_386499_, "very_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_very_cracked"))
                );
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void createTurtleEgg() {
        this.registerSimpleFlatItemModel(Items.TURTLE_EGG);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TURTLE_EGG)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.EGGS, BlockStateProperties.HATCH)
                            .generateList((p_387353_, p_388815_) -> Arrays.asList(createRotatedVariants(this.createTurtleEggModel(p_387353_, p_388815_))))
                    )
            );
    }

    private void createSnifferEgg() {
        this.registerSimpleFlatItemModel(Items.SNIFFER_EGG);
        Function<Integer, ResourceLocation> function = p_387045_ -> {
            String s = switch (p_387045_) {
                case 1 -> "_slightly_cracked";
                case 2 -> "_very_cracked";
                default -> "_not_cracked";
            };
            TextureMapping texturemapping = TextureMapping.snifferEgg(s);
            return ModelTemplates.SNIFFER_EGG.createWithSuffix(Blocks.SNIFFER_EGG, s, texturemapping, this.modelOutput);
        };
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SNIFFER_EGG)
                    .with(
                        PropertyDispatch.property(SnifferEggBlock.HATCH)
                            .generate(p_388753_ -> Variant.variant().with(VariantProperties.MODEL, function.apply(p_388753_)))
                    )
            );
    }

    private void createMultiface(Block p_386688_) {
        this.registerSimpleFlatItemModel(p_386688_);
        this.createMultifaceBlockStates(p_386688_);
    }

    private void createMultiface(Block p_387905_, Item p_386940_) {
        this.registerSimpleFlatItemModel(p_386940_);
        this.createMultifaceBlockStates(p_387905_);
    }

    private void createMultifaceBlockStates(Block p_387937_) {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(p_387937_);
        MultiPartGenerator multipartgenerator = MultiPartGenerator.multiPart(p_387937_);
        Condition.TerminalCondition condition$terminalcondition = Util.make(
            Condition.condition(), p_387548_ -> MULTIFACE_GENERATOR.stream().map(Pair::getFirst).map(MultifaceBlock::getFaceProperty).forEach(p_387201_ -> {
                    if (p_387937_.defaultBlockState().hasProperty(p_387201_)) {
                        p_387548_.term(p_387201_, false);
                    }
                })
        );

        for (Pair<Direction, Function<ResourceLocation, Variant>> pair : MULTIFACE_GENERATOR) {
            BooleanProperty booleanproperty = MultifaceBlock.getFaceProperty(pair.getFirst());
            Function<ResourceLocation, Variant> function = pair.getSecond();
            if (p_387937_.defaultBlockState().hasProperty(booleanproperty)) {
                multipartgenerator.with(Condition.condition().term(booleanproperty, true), function.apply(resourcelocation));
                multipartgenerator.with(condition$terminalcondition, function.apply(resourcelocation));
            }
        }

        this.blockStateOutput.accept(multipartgenerator);
    }

    private void createMossyCarpet(Block p_386524_) {
        ResourceLocation resourcelocation = TexturedModel.CARPET.create(p_386524_, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.MOSSY_CARPET_SIDE
            .get(p_386524_)
            .updateTextures(p_386949_ -> p_386949_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(p_386524_, "_side_tall")))
            .createWithSuffix(p_386524_, "_side_tall", this.modelOutput);
        ResourceLocation resourcelocation2 = TexturedModel.MOSSY_CARPET_SIDE
            .get(p_386524_)
            .updateTextures(p_388660_ -> p_388660_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(p_386524_, "_side_small")))
            .createWithSuffix(p_386524_, "_side_small", this.modelOutput);
        MultiPartGenerator multipartgenerator = MultiPartGenerator.multiPart(p_386524_);
        Condition.TerminalCondition condition$terminalcondition = Condition.condition().term(MossyCarpetBlock.BASE, false);
        multipartgenerator.with(Condition.condition().term(MossyCarpetBlock.BASE, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation));
        multipartgenerator.with(condition$terminalcondition, Variant.variant().with(VariantProperties.MODEL, resourcelocation));
        MULTIFACE_GENERATOR.stream().map(Pair::getFirst).forEach(p_386445_ -> {
            EnumProperty<WallSide> enumproperty1 = MossyCarpetBlock.getPropertyForFace(p_386445_);
            if (enumproperty1 != null && p_386524_.defaultBlockState().hasProperty(enumproperty1)) {
                condition$terminalcondition.term(enumproperty1, WallSide.NONE);
            }
        });

        for (Pair<Direction, Function<ResourceLocation, Variant>> pair : MULTIFACE_GENERATOR) {
            Direction direction = pair.getFirst();
            EnumProperty<WallSide> enumproperty = MossyCarpetBlock.getPropertyForFace(direction);
            if (enumproperty != null) {
                Function<ResourceLocation, Variant> function = pair.getSecond();
                multipartgenerator.with(Condition.condition().term(enumproperty, WallSide.TALL), function.apply(resourcelocation1));
                multipartgenerator.with(Condition.condition().term(enumproperty, WallSide.LOW), function.apply(resourcelocation2));
                multipartgenerator.with(condition$terminalcondition, function.apply(resourcelocation1));
            }
        }

        this.blockStateOutput.accept(multipartgenerator);
    }

    private void createHangingMoss(Block p_386702_) {
        PropertyDispatch propertydispatch = PropertyDispatch.property(HangingMossBlock.TIP)
            .generate(
                p_387841_ -> {
                    String s = p_387841_ ? "_tip" : "";
                    TextureMapping texturemapping = TextureMapping.cross(TextureMapping.getBlockTexture(p_386702_, s));
                    ResourceLocation resourcelocation = BlockModelGenerators.PlantType.NOT_TINTED
                        .getCross()
                        .createWithSuffix(p_386702_, s, texturemapping, this.modelOutput);
                    return Variant.variant().with(VariantProperties.MODEL, resourcelocation);
                }
            );
        this.registerSimpleFlatItemModel(p_386702_);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(p_386702_).with(propertydispatch));
    }

    private void createSculkCatalyst() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_bottom");
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, resourcelocation)
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_side"));
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.BOTTOM, resourcelocation)
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_top_bloom"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_side_bloom"));
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.SCULK_CATALYST, "", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_BOTTOM_TOP
            .createWithSuffix(Blocks.SCULK_CATALYST, "_bloom", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SCULK_CATALYST)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.BLOOM)
                            .generate(p_387544_ -> Variant.variant().with(VariantProperties.MODEL, p_387544_ ? resourcelocation2 : resourcelocation1))
                    )
            );
        this.registerSimpleItemModel(Blocks.SCULK_CATALYST, resourcelocation1);
    }

    private void createChiseledBookshelf() {
        Block block = Blocks.CHISELED_BOOKSHELF;
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block);
        MultiPartGenerator multipartgenerator = MultiPartGenerator.multiPart(block);
        List.of(
                Pair.of(Direction.NORTH, VariantProperties.Rotation.R0),
                Pair.of(Direction.EAST, VariantProperties.Rotation.R90),
                Pair.of(Direction.SOUTH, VariantProperties.Rotation.R180),
                Pair.of(Direction.WEST, VariantProperties.Rotation.R270)
            )
            .forEach(
                p_386828_ -> {
                    Direction direction = p_386828_.getFirst();
                    VariantProperties.Rotation variantproperties$rotation = p_386828_.getSecond();
                    Condition.TerminalCondition condition$terminalcondition = Condition.condition().term(BlockStateProperties.HORIZONTAL_FACING, direction);
                    multipartgenerator.with(
                        condition$terminalcondition,
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, variantproperties$rotation)
                            .with(VariantProperties.UV_LOCK, true)
                    );
                    this.addSlotStateAndRotationVariants(multipartgenerator, condition$terminalcondition, variantproperties$rotation);
                }
            );
        this.blockStateOutput.accept(multipartgenerator);
        this.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block, "_inventory"));
        CHISELED_BOOKSHELF_SLOT_MODEL_CACHE.clear();
    }

    private void addSlotStateAndRotationVariants(MultiPartGenerator p_387496_, Condition.TerminalCondition p_386597_, VariantProperties.Rotation p_387166_) {
        List.of(
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_LEFT),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_MID),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_RIGHT),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_LEFT),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_MID),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_RIGHT)
            )
            .forEach(p_387189_ -> {
                BooleanProperty booleanproperty = p_387189_.getFirst();
                ModelTemplate modeltemplate = p_387189_.getSecond();
                this.addBookSlotModel(p_387496_, p_386597_, p_387166_, booleanproperty, modeltemplate, true);
                this.addBookSlotModel(p_387496_, p_386597_, p_387166_, booleanproperty, modeltemplate, false);
            });
    }

    private void addBookSlotModel(
        MultiPartGenerator p_387881_,
        Condition.TerminalCondition p_388393_,
        VariantProperties.Rotation p_388523_,
        BooleanProperty p_388343_,
        ModelTemplate p_388654_,
        boolean p_388780_
    ) {
        String s = p_388780_ ? "_occupied" : "_empty";
        TextureMapping texturemapping = new TextureMapping().put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(Blocks.CHISELED_BOOKSHELF, s));
        BlockModelGenerators.BookSlotModelCacheKey blockmodelgenerators$bookslotmodelcachekey = new BlockModelGenerators.BookSlotModelCacheKey(p_388654_, s);
        ResourceLocation resourcelocation = CHISELED_BOOKSHELF_SLOT_MODEL_CACHE.computeIfAbsent(
            blockmodelgenerators$bookslotmodelcachekey, p_387964_ -> p_388654_.createWithSuffix(Blocks.CHISELED_BOOKSHELF, s, texturemapping, this.modelOutput)
        );
        p_387881_.with(
            Condition.and(p_388393_, Condition.condition().term(p_388343_, p_388780_)),
            Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, p_388523_)
        );
    }

    private void createMagmaBlock() {
        this.blockStateOutput
            .accept(
                createSimpleBlock(
                    Blocks.MAGMA_BLOCK,
                    ModelTemplates.CUBE_ALL
                        .create(Blocks.MAGMA_BLOCK, TextureMapping.cube(ModelLocationUtils.decorateBlockModelLocation("magma")), this.modelOutput)
                )
            );
    }

    private void createShulkerBox(Block p_387895_, @Nullable DyeColor p_388244_) {
        this.createParticleOnlyBlock(p_387895_);
        Item item = p_387895_.asItem();
        ResourceLocation resourcelocation = ModelTemplates.SHULKER_BOX_INVENTORY.create(item, TextureMapping.particle(p_387895_), this.modelOutput);
        ItemModel.Unbaked itemmodel$unbaked = p_388244_ != null
            ? ItemModelUtils.specialModel(resourcelocation, new ShulkerBoxSpecialRenderer.Unbaked(p_388244_))
            : ItemModelUtils.specialModel(resourcelocation, new ShulkerBoxSpecialRenderer.Unbaked());
        this.itemModelOutput.accept(item, itemmodel$unbaked);
    }

    private void createGrowingPlant(Block p_388940_, Block p_387685_, BlockModelGenerators.PlantType p_386807_) {
        this.createCrossBlock(p_388940_, p_386807_);
        this.createCrossBlock(p_387685_, p_386807_);
    }

    private void createInfestedStone() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.STONE);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.STONE, "_mirrored");
        this.blockStateOutput.accept(createRotatedVariant(Blocks.INFESTED_STONE, resourcelocation, resourcelocation1));
        this.registerSimpleItemModel(Blocks.INFESTED_STONE, resourcelocation);
    }

    private void createInfestedDeepslate() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.DEEPSLATE);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.DEEPSLATE, "_mirrored");
        this.blockStateOutput.accept(createRotatedVariant(Blocks.INFESTED_DEEPSLATE, resourcelocation, resourcelocation1).with(createRotatedPillar()));
        this.registerSimpleItemModel(Blocks.INFESTED_DEEPSLATE, resourcelocation);
    }

    private void createNetherRoots(Block p_386802_, Block p_386924_) {
        this.createCrossBlockWithDefaultItem(p_386802_, BlockModelGenerators.PlantType.NOT_TINTED);
        TextureMapping texturemapping = TextureMapping.plant(TextureMapping.getBlockTexture(p_386802_, "_pot"));
        ResourceLocation resourcelocation = BlockModelGenerators.PlantType.NOT_TINTED.getCrossPot().create(p_386924_, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_386924_, resourcelocation));
    }

    private void createRespawnAnchor() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_bottom");
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top_off");
        ResourceLocation resourcelocation2 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top");
        ResourceLocation[] aresourcelocation = new ResourceLocation[5];

        for (int i = 0; i < 5; i++) {
            TextureMapping texturemapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, resourcelocation)
                .put(TextureSlot.TOP, i == 0 ? resourcelocation1 : resourcelocation2)
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_side" + i));
            aresourcelocation[i] = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.RESPAWN_ANCHOR, "_" + i, texturemapping, this.modelOutput);
        }

        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.RESPAWN_ANCHOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.RESPAWN_ANCHOR_CHARGES)
                            .generate(p_386983_ -> Variant.variant().with(VariantProperties.MODEL, aresourcelocation[p_386983_]))
                    )
            );
        this.registerSimpleItemModel(Blocks.RESPAWN_ANCHOR, aresourcelocation[0]);
    }

    private Variant applyRotation(FrontAndTop p_387983_, Variant p_386835_) {
        switch (p_387983_) {
            case DOWN_NORTH:
                return p_386835_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
            case DOWN_SOUTH:
                return p_386835_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case DOWN_WEST:
                return p_386835_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case DOWN_EAST:
                return p_386835_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            case UP_NORTH:
                return p_386835_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case UP_SOUTH:
                return p_386835_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270);
            case UP_WEST:
                return p_386835_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            case UP_EAST:
                return p_386835_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case NORTH_UP:
                return p_386835_;
            case SOUTH_UP:
                return p_386835_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case WEST_UP:
                return p_386835_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case EAST_UP:
                return p_386835_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            default:
                throw new UnsupportedOperationException("Rotation " + p_387983_ + " can't be expressed with existing x and y values");
        }
    }

    private void createJigsaw() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_top");
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_bottom");
        ResourceLocation resourcelocation2 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_side");
        ResourceLocation resourcelocation3 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_lock");
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.DOWN, resourcelocation2)
            .put(TextureSlot.WEST, resourcelocation2)
            .put(TextureSlot.EAST, resourcelocation2)
            .put(TextureSlot.PARTICLE, resourcelocation)
            .put(TextureSlot.NORTH, resourcelocation)
            .put(TextureSlot.SOUTH, resourcelocation1)
            .put(TextureSlot.UP, resourcelocation3);
        ResourceLocation resourcelocation4 = ModelTemplates.CUBE_DIRECTIONAL.create(Blocks.JIGSAW, texturemapping, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.JIGSAW, Variant.variant().with(VariantProperties.MODEL, resourcelocation4))
                    .with(PropertyDispatch.property(BlockStateProperties.ORIENTATION).generate(p_387292_ -> this.applyRotation(p_387292_, Variant.variant())))
            );
    }

    private void createPetrifiedOakSlab() {
        Block block = Blocks.OAK_PLANKS;
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block);
        TexturedModel texturedmodel = TexturedModel.CUBE.get(block);
        Block block1 = Blocks.PETRIFIED_OAK_SLAB;
        ResourceLocation resourcelocation1 = ModelTemplates.SLAB_BOTTOM.create(block1, texturedmodel.getMapping(), this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.SLAB_TOP.create(block1, texturedmodel.getMapping(), this.modelOutput);
        this.blockStateOutput.accept(createSlab(block1, resourcelocation1, resourcelocation2, resourcelocation));
    }

    private void createHead(Block p_387503_, Block p_388317_, SkullBlock.Type p_387567_, ResourceLocation p_388939_) {
        ResourceLocation resourcelocation = ModelLocationUtils.decorateBlockModelLocation("skull");
        this.blockStateOutput.accept(createSimpleBlock(p_387503_, resourcelocation));
        this.blockStateOutput.accept(createSimpleBlock(p_388317_, resourcelocation));
        this.itemModelOutput.accept(p_387503_.asItem(), ItemModelUtils.specialModel(p_388939_, new SkullSpecialRenderer.Unbaked(p_387567_)));
    }

    private void createHeads() {
        ResourceLocation resourcelocation = ModelLocationUtils.decorateItemModelLocation("template_skull");
        this.createHead(Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, SkullBlock.Types.CREEPER, resourcelocation);
        this.createHead(Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, SkullBlock.Types.PLAYER, resourcelocation);
        this.createHead(Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, SkullBlock.Types.ZOMBIE, resourcelocation);
        this.createHead(Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, SkullBlock.Types.SKELETON, resourcelocation);
        this.createHead(Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, SkullBlock.Types.WITHER_SKELETON, resourcelocation);
        this.createHead(Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD, SkullBlock.Types.PIGLIN, resourcelocation);
        this.createHead(Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, SkullBlock.Types.DRAGON, ModelLocationUtils.getModelLocation(Items.DRAGON_HEAD));
    }

    private void createBanner(Block p_386638_, Block p_388464_, DyeColor p_388291_) {
        ResourceLocation resourcelocation = ModelLocationUtils.decorateBlockModelLocation("banner");
        ResourceLocation resourcelocation1 = ModelLocationUtils.decorateItemModelLocation("template_banner");
        this.blockStateOutput.accept(createSimpleBlock(p_386638_, resourcelocation));
        this.blockStateOutput.accept(createSimpleBlock(p_388464_, resourcelocation));
        Item item = p_386638_.asItem();
        this.itemModelOutput.accept(item, ItemModelUtils.specialModel(resourcelocation1, new BannerSpecialRenderer.Unbaked(p_388291_)));
    }

    private void createBanners() {
        this.createBanner(Blocks.WHITE_BANNER, Blocks.WHITE_WALL_BANNER, DyeColor.WHITE);
        this.createBanner(Blocks.ORANGE_BANNER, Blocks.ORANGE_WALL_BANNER, DyeColor.ORANGE);
        this.createBanner(Blocks.MAGENTA_BANNER, Blocks.MAGENTA_WALL_BANNER, DyeColor.MAGENTA);
        this.createBanner(Blocks.LIGHT_BLUE_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER, DyeColor.LIGHT_BLUE);
        this.createBanner(Blocks.YELLOW_BANNER, Blocks.YELLOW_WALL_BANNER, DyeColor.YELLOW);
        this.createBanner(Blocks.LIME_BANNER, Blocks.LIME_WALL_BANNER, DyeColor.LIME);
        this.createBanner(Blocks.PINK_BANNER, Blocks.PINK_WALL_BANNER, DyeColor.PINK);
        this.createBanner(Blocks.GRAY_BANNER, Blocks.GRAY_WALL_BANNER, DyeColor.GRAY);
        this.createBanner(Blocks.LIGHT_GRAY_BANNER, Blocks.LIGHT_GRAY_WALL_BANNER, DyeColor.LIGHT_GRAY);
        this.createBanner(Blocks.CYAN_BANNER, Blocks.CYAN_WALL_BANNER, DyeColor.CYAN);
        this.createBanner(Blocks.PURPLE_BANNER, Blocks.PURPLE_WALL_BANNER, DyeColor.PURPLE);
        this.createBanner(Blocks.BLUE_BANNER, Blocks.BLUE_WALL_BANNER, DyeColor.BLUE);
        this.createBanner(Blocks.BROWN_BANNER, Blocks.BROWN_WALL_BANNER, DyeColor.BROWN);
        this.createBanner(Blocks.GREEN_BANNER, Blocks.GREEN_WALL_BANNER, DyeColor.GREEN);
        this.createBanner(Blocks.RED_BANNER, Blocks.RED_WALL_BANNER, DyeColor.RED);
        this.createBanner(Blocks.BLACK_BANNER, Blocks.BLACK_WALL_BANNER, DyeColor.BLACK);
    }

    private void createChest(Block p_387020_, Block p_388374_, ResourceLocation p_387415_, boolean p_387592_) {
        this.createParticleOnlyBlock(p_387020_, p_388374_);
        Item item = p_387020_.asItem();
        ResourceLocation resourcelocation = ModelTemplates.CHEST_INVENTORY.create(item, TextureMapping.particle(p_388374_), this.modelOutput);
        ItemModel.Unbaked itemmodel$unbaked = ItemModelUtils.specialModel(resourcelocation, new ChestSpecialRenderer.Unbaked(p_387415_));
        if (p_387592_) {
            ItemModel.Unbaked itemmodel$unbaked1 = ItemModelUtils.specialModel(
                resourcelocation, new ChestSpecialRenderer.Unbaked(ChestSpecialRenderer.GIFT_CHEST_TEXTURE)
            );
            this.itemModelOutput.accept(item, ItemModelUtils.isXmas(itemmodel$unbaked1, itemmodel$unbaked));
        } else {
            this.itemModelOutput.accept(item, itemmodel$unbaked);
        }
    }

    private void createChests() {
        this.createChest(Blocks.CHEST, Blocks.OAK_PLANKS, ChestSpecialRenderer.NORMAL_CHEST_TEXTURE, true);
        this.createChest(Blocks.TRAPPED_CHEST, Blocks.OAK_PLANKS, ChestSpecialRenderer.TRAPPED_CHEST_TEXTURE, true);
        this.createChest(Blocks.ENDER_CHEST, Blocks.OBSIDIAN, ChestSpecialRenderer.ENDER_CHEST_TEXTURE, false);
    }

    private void createBed(Block p_387718_, Block p_386452_, DyeColor p_387181_) {
        ResourceLocation resourcelocation = ModelLocationUtils.decorateBlockModelLocation("bed");
        this.blockStateOutput.accept(createSimpleBlock(p_387718_, resourcelocation));
        Item item = p_387718_.asItem();
        ResourceLocation resourcelocation1 = ModelTemplates.BED_INVENTORY
            .create(ModelLocationUtils.getModelLocation(item), TextureMapping.particle(p_386452_), this.modelOutput);
        this.itemModelOutput.accept(item, ItemModelUtils.specialModel(resourcelocation1, new BedSpecialRenderer.Unbaked(p_387181_)));
    }

    private void createBeds() {
        this.createBed(Blocks.WHITE_BED, Blocks.WHITE_WOOL, DyeColor.WHITE);
        this.createBed(Blocks.ORANGE_BED, Blocks.ORANGE_WOOL, DyeColor.ORANGE);
        this.createBed(Blocks.MAGENTA_BED, Blocks.MAGENTA_WOOL, DyeColor.MAGENTA);
        this.createBed(Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL, DyeColor.LIGHT_BLUE);
        this.createBed(Blocks.YELLOW_BED, Blocks.YELLOW_WOOL, DyeColor.YELLOW);
        this.createBed(Blocks.LIME_BED, Blocks.LIME_WOOL, DyeColor.LIME);
        this.createBed(Blocks.PINK_BED, Blocks.PINK_WOOL, DyeColor.PINK);
        this.createBed(Blocks.GRAY_BED, Blocks.GRAY_WOOL, DyeColor.GRAY);
        this.createBed(Blocks.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL, DyeColor.LIGHT_GRAY);
        this.createBed(Blocks.CYAN_BED, Blocks.CYAN_WOOL, DyeColor.CYAN);
        this.createBed(Blocks.PURPLE_BED, Blocks.PURPLE_WOOL, DyeColor.PURPLE);
        this.createBed(Blocks.BLUE_BED, Blocks.BLUE_WOOL, DyeColor.BLUE);
        this.createBed(Blocks.BROWN_BED, Blocks.BROWN_WOOL, DyeColor.BROWN);
        this.createBed(Blocks.GREEN_BED, Blocks.GREEN_WOOL, DyeColor.GREEN);
        this.createBed(Blocks.RED_BED, Blocks.RED_WOOL, DyeColor.RED);
        this.createBed(Blocks.BLACK_BED, Blocks.BLACK_WOOL, DyeColor.BLACK);
    }

    private void generateSimpleSpecialItemModel(Block p_387001_, SpecialModelRenderer.Unbaked p_386518_) {
        Item item = p_387001_.asItem();
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(item);
        this.itemModelOutput.accept(item, ItemModelUtils.specialModel(resourcelocation, p_386518_));
    }

    public void run() {
        BlockFamilies.getAllFamilies()
            .filter(BlockFamily::shouldGenerateModel)
            .forEach(p_386718_ -> this.family(p_386718_.getBaseBlock()).generateFor(p_386718_));
        this.family(Blocks.CUT_COPPER)
            .generateFor(BlockFamilies.CUT_COPPER)
            .donateModelTo(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER)
            .donateModelTo(Blocks.CHISELED_COPPER, Blocks.WAXED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_CUT_COPPER);
        this.family(Blocks.EXPOSED_CUT_COPPER)
            .generateFor(BlockFamilies.EXPOSED_CUT_COPPER)
            .donateModelTo(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER)
            .donateModelTo(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_EXPOSED_CUT_COPPER);
        this.family(Blocks.WEATHERED_CUT_COPPER)
            .generateFor(BlockFamilies.WEATHERED_CUT_COPPER)
            .donateModelTo(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER)
            .donateModelTo(Blocks.WEATHERED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_WEATHERED_CUT_COPPER);
        this.family(Blocks.OXIDIZED_CUT_COPPER)
            .generateFor(BlockFamilies.OXIDIZED_CUT_COPPER)
            .donateModelTo(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER)
            .donateModelTo(Blocks.OXIDIZED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_OXIDIZED_CUT_COPPER);
        this.createCopperBulb(Blocks.COPPER_BULB);
        this.createCopperBulb(Blocks.EXPOSED_COPPER_BULB);
        this.createCopperBulb(Blocks.WEATHERED_COPPER_BULB);
        this.createCopperBulb(Blocks.OXIDIZED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.COPPER_BULB, Blocks.WAXED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.EXPOSED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.WEATHERED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.OXIDIZED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB);
        this.createNonTemplateModelBlock(Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.CAVE_AIR, Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.VOID_AIR, Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.BEACON);
        this.createNonTemplateModelBlock(Blocks.CACTUS);
        this.createNonTemplateModelBlock(Blocks.BUBBLE_COLUMN, Blocks.WATER);
        this.createNonTemplateModelBlock(Blocks.DRAGON_EGG);
        this.createNonTemplateModelBlock(Blocks.DRIED_KELP_BLOCK);
        this.createNonTemplateModelBlock(Blocks.ENCHANTING_TABLE);
        this.createNonTemplateModelBlock(Blocks.FLOWER_POT);
        this.registerSimpleFlatItemModel(Items.FLOWER_POT);
        this.createNonTemplateModelBlock(Blocks.HONEY_BLOCK);
        this.createNonTemplateModelBlock(Blocks.WATER);
        this.createNonTemplateModelBlock(Blocks.LAVA);
        this.createNonTemplateModelBlock(Blocks.SLIME_BLOCK);
        this.registerSimpleFlatItemModel(Items.CHAIN);
        this.createCandleAndCandleCake(Blocks.WHITE_CANDLE, Blocks.WHITE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.ORANGE_CANDLE, Blocks.ORANGE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.MAGENTA_CANDLE, Blocks.MAGENTA_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.LIGHT_BLUE_CANDLE, Blocks.LIGHT_BLUE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.YELLOW_CANDLE, Blocks.YELLOW_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.LIME_CANDLE, Blocks.LIME_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.PINK_CANDLE, Blocks.PINK_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.GRAY_CANDLE, Blocks.GRAY_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.LIGHT_GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.CYAN_CANDLE, Blocks.CYAN_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.PURPLE_CANDLE, Blocks.PURPLE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.BLUE_CANDLE, Blocks.BLUE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.BROWN_CANDLE, Blocks.BROWN_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.GREEN_CANDLE, Blocks.GREEN_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.RED_CANDLE, Blocks.RED_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.BLACK_CANDLE, Blocks.BLACK_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.CANDLE, Blocks.CANDLE_CAKE);
        this.createNonTemplateModelBlock(Blocks.POTTED_BAMBOO);
        this.createNonTemplateModelBlock(Blocks.POTTED_CACTUS);
        this.createNonTemplateModelBlock(Blocks.POWDER_SNOW);
        this.createNonTemplateModelBlock(Blocks.SPORE_BLOSSOM);
        this.createAzalea(Blocks.AZALEA);
        this.createAzalea(Blocks.FLOWERING_AZALEA);
        this.createPottedAzalea(Blocks.POTTED_AZALEA);
        this.createPottedAzalea(Blocks.POTTED_FLOWERING_AZALEA);
        this.createCaveVines();
        this.createFullAndCarpetBlocks(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET);
        this.createMossyCarpet(Blocks.PALE_MOSS_CARPET);
        this.createHangingMoss(Blocks.PALE_HANGING_MOSS);
        this.createTrivialCube(Blocks.PALE_MOSS_BLOCK);
        this.createFlowerBed(Blocks.PINK_PETALS);
        this.createAirLikeBlock(Blocks.BARRIER, Items.BARRIER);
        this.registerSimpleFlatItemModel(Items.BARRIER);
        this.createLightBlock();
        this.createAirLikeBlock(Blocks.STRUCTURE_VOID, Items.STRUCTURE_VOID);
        this.registerSimpleFlatItemModel(Items.STRUCTURE_VOID);
        this.createAirLikeBlock(Blocks.MOVING_PISTON, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        this.createTrivialCube(Blocks.COAL_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_COAL_ORE);
        this.createTrivialCube(Blocks.COAL_BLOCK);
        this.createTrivialCube(Blocks.DIAMOND_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_DIAMOND_ORE);
        this.createTrivialCube(Blocks.DIAMOND_BLOCK);
        this.createTrivialCube(Blocks.EMERALD_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_EMERALD_ORE);
        this.createTrivialCube(Blocks.EMERALD_BLOCK);
        this.createTrivialCube(Blocks.GOLD_ORE);
        this.createTrivialCube(Blocks.NETHER_GOLD_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_GOLD_ORE);
        this.createTrivialCube(Blocks.GOLD_BLOCK);
        this.createTrivialCube(Blocks.IRON_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_IRON_ORE);
        this.createTrivialCube(Blocks.IRON_BLOCK);
        this.createTrivialBlock(Blocks.ANCIENT_DEBRIS, TexturedModel.COLUMN);
        this.createTrivialCube(Blocks.NETHERITE_BLOCK);
        this.createTrivialCube(Blocks.LAPIS_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_LAPIS_ORE);
        this.createTrivialCube(Blocks.LAPIS_BLOCK);
        this.createTrivialCube(Blocks.RESIN_BLOCK);
        this.createTrivialCube(Blocks.NETHER_QUARTZ_ORE);
        this.createTrivialCube(Blocks.REDSTONE_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_REDSTONE_ORE);
        this.createTrivialCube(Blocks.REDSTONE_BLOCK);
        this.createTrivialCube(Blocks.GILDED_BLACKSTONE);
        this.createTrivialCube(Blocks.BLUE_ICE);
        this.createTrivialCube(Blocks.CLAY);
        this.createTrivialCube(Blocks.COARSE_DIRT);
        this.createTrivialCube(Blocks.CRYING_OBSIDIAN);
        this.createTrivialCube(Blocks.END_STONE);
        this.createTrivialCube(Blocks.GLOWSTONE);
        this.createTrivialCube(Blocks.GRAVEL);
        this.createTrivialCube(Blocks.HONEYCOMB_BLOCK);
        this.createTrivialCube(Blocks.ICE);
        this.createTrivialBlock(Blocks.JUKEBOX, TexturedModel.CUBE_TOP);
        this.createTrivialBlock(Blocks.LODESTONE, TexturedModel.COLUMN);
        this.createTrivialBlock(Blocks.MELON, TexturedModel.COLUMN);
        this.createNonTemplateModelBlock(Blocks.MANGROVE_ROOTS);
        this.createNonTemplateModelBlock(Blocks.POTTED_MANGROVE_PROPAGULE);
        this.createTrivialCube(Blocks.NETHER_WART_BLOCK);
        this.createTrivialCube(Blocks.NOTE_BLOCK);
        this.createTrivialCube(Blocks.PACKED_ICE);
        this.createTrivialCube(Blocks.OBSIDIAN);
        this.createTrivialCube(Blocks.QUARTZ_BRICKS);
        this.createTrivialCube(Blocks.SEA_LANTERN);
        this.createTrivialCube(Blocks.SHROOMLIGHT);
        this.createTrivialCube(Blocks.SOUL_SAND);
        this.createTrivialCube(Blocks.SOUL_SOIL);
        this.createTrivialBlock(Blocks.SPAWNER, TexturedModel.CUBE_INNER_FACES);
        this.createCreakingHeart(Blocks.CREAKING_HEART);
        this.createTrivialCube(Blocks.SPONGE);
        this.createTrivialBlock(Blocks.SEAGRASS, TexturedModel.SEAGRASS);
        this.registerSimpleFlatItemModel(Items.SEAGRASS);
        this.createTrivialBlock(Blocks.TNT, TexturedModel.CUBE_TOP_BOTTOM);
        this.createTrivialBlock(Blocks.TARGET, TexturedModel.COLUMN);
        this.createTrivialCube(Blocks.WARPED_WART_BLOCK);
        this.createTrivialCube(Blocks.WET_SPONGE);
        this.createTrivialCube(Blocks.AMETHYST_BLOCK);
        this.createTrivialCube(Blocks.BUDDING_AMETHYST);
        this.createTrivialCube(Blocks.CALCITE);
        this.createTrivialCube(Blocks.DRIPSTONE_BLOCK);
        this.createTrivialCube(Blocks.RAW_IRON_BLOCK);
        this.createTrivialCube(Blocks.RAW_COPPER_BLOCK);
        this.createTrivialCube(Blocks.RAW_GOLD_BLOCK);
        this.createRotatedMirroredVariantBlock(Blocks.SCULK);
        this.createNonTemplateModelBlock(Blocks.HEAVY_CORE);
        this.createPetrifiedOakSlab();
        this.createTrivialCube(Blocks.COPPER_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_COPPER_ORE);
        this.createTrivialCube(Blocks.COPPER_BLOCK);
        this.createTrivialCube(Blocks.EXPOSED_COPPER);
        this.createTrivialCube(Blocks.WEATHERED_COPPER);
        this.createTrivialCube(Blocks.OXIDIZED_COPPER);
        this.copyModel(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK);
        this.copyModel(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER);
        this.copyModel(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER);
        this.copyModel(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER);
        this.createDoor(Blocks.COPPER_DOOR);
        this.createDoor(Blocks.EXPOSED_COPPER_DOOR);
        this.createDoor(Blocks.WEATHERED_COPPER_DOOR);
        this.createDoor(Blocks.OXIDIZED_COPPER_DOOR);
        this.copyDoorModel(Blocks.COPPER_DOOR, Blocks.WAXED_COPPER_DOOR);
        this.copyDoorModel(Blocks.EXPOSED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR);
        this.copyDoorModel(Blocks.WEATHERED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR);
        this.copyDoorModel(Blocks.OXIDIZED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR);
        this.createTrapdoor(Blocks.COPPER_TRAPDOOR);
        this.createTrapdoor(Blocks.EXPOSED_COPPER_TRAPDOOR);
        this.createTrapdoor(Blocks.WEATHERED_COPPER_TRAPDOOR);
        this.createTrapdoor(Blocks.OXIDIZED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.COPPER_TRAPDOOR, Blocks.WAXED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR);
        this.createTrivialCube(Blocks.COPPER_GRATE);
        this.createTrivialCube(Blocks.EXPOSED_COPPER_GRATE);
        this.createTrivialCube(Blocks.WEATHERED_COPPER_GRATE);
        this.createTrivialCube(Blocks.OXIDIZED_COPPER_GRATE);
        this.copyModel(Blocks.COPPER_GRATE, Blocks.WAXED_COPPER_GRATE);
        this.copyModel(Blocks.EXPOSED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER_GRATE);
        this.copyModel(Blocks.WEATHERED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER_GRATE);
        this.copyModel(Blocks.OXIDIZED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER_GRATE);
        this.createWeightedPressurePlate(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.GOLD_BLOCK);
        this.createWeightedPressurePlate(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.IRON_BLOCK);
        this.createAmethystClusters();
        this.createBookshelf();
        this.createChiseledBookshelf();
        this.createBrewingStand();
        this.createCakeBlock();
        this.createCampfires(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
        this.createCartographyTable();
        this.createCauldrons();
        this.createChorusFlower();
        this.createChorusPlant();
        this.createComposter();
        this.createDaylightDetector();
        this.createEndPortalFrame();
        this.createRotatableColumn(Blocks.END_ROD);
        this.createLightningRod();
        this.createFarmland();
        this.createFire();
        this.createSoulFire();
        this.createFrostedIce();
        this.createGrassBlocks();
        this.createCocoa();
        this.createDirtPath();
        this.createGrindstone();
        this.createHopper();
        this.createIronBars();
        this.createLever();
        this.createLilyPad();
        this.createNetherPortalBlock();
        this.createNetherrack();
        this.createObserver();
        this.createPistons();
        this.createPistonHeads();
        this.createScaffolding();
        this.createRedstoneTorch();
        this.createRedstoneLamp();
        this.createRepeater();
        this.createSeaPickle();
        this.createSmithingTable();
        this.createSnowBlocks();
        this.createStonecutter();
        this.createStructureBlock();
        this.createSweetBerryBush();
        this.createTripwire();
        this.createTripwireHook();
        this.createTurtleEgg();
        this.createSnifferEgg();
        this.createVine();
        this.createMultiface(Blocks.GLOW_LICHEN);
        this.createMultiface(Blocks.SCULK_VEIN);
        this.createMultiface(Blocks.RESIN_CLUMP, Items.RESIN_CLUMP);
        this.createMagmaBlock();
        this.createJigsaw();
        this.createSculkSensor();
        this.createCalibratedSculkSensor();
        this.createSculkShrieker();
        this.createFrogspawnBlock();
        this.createMangrovePropagule();
        this.createMuddyMangroveRoots();
        this.createTrialSpawner();
        this.createVault();
        this.createNonTemplateHorizontalBlock(Blocks.LADDER);
        this.registerSimpleFlatItemModel(Blocks.LADDER);
        this.createNonTemplateHorizontalBlock(Blocks.LECTERN);
        this.createBigDripLeafBlock();
        this.createNonTemplateHorizontalBlock(Blocks.BIG_DRIPLEAF_STEM);
        this.createNormalTorch(Blocks.TORCH, Blocks.WALL_TORCH);
        this.createNormalTorch(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH);
        this.createCraftingTableLike(Blocks.CRAFTING_TABLE, Blocks.OAK_PLANKS, TextureMapping::craftingTable);
        this.createCraftingTableLike(Blocks.FLETCHING_TABLE, Blocks.BIRCH_PLANKS, TextureMapping::fletchingTable);
        this.createNyliumBlock(Blocks.CRIMSON_NYLIUM);
        this.createNyliumBlock(Blocks.WARPED_NYLIUM);
        this.createDispenserBlock(Blocks.DISPENSER);
        this.createDispenserBlock(Blocks.DROPPER);
        this.createCrafterBlock();
        this.createLantern(Blocks.LANTERN);
        this.createLantern(Blocks.SOUL_LANTERN);
        this.createAxisAlignedPillarBlockCustomModel(Blocks.CHAIN, ModelLocationUtils.getModelLocation(Blocks.CHAIN));
        this.createAxisAlignedPillarBlock(Blocks.BASALT, TexturedModel.COLUMN);
        this.createAxisAlignedPillarBlock(Blocks.POLISHED_BASALT, TexturedModel.COLUMN);
        this.createTrivialCube(Blocks.SMOOTH_BASALT);
        this.createAxisAlignedPillarBlock(Blocks.BONE_BLOCK, TexturedModel.COLUMN);
        this.createRotatedVariantBlock(Blocks.DIRT);
        this.createRotatedVariantBlock(Blocks.ROOTED_DIRT);
        this.createRotatedVariantBlock(Blocks.SAND);
        this.createBrushableBlock(Blocks.SUSPICIOUS_SAND);
        this.createBrushableBlock(Blocks.SUSPICIOUS_GRAVEL);
        this.createRotatedVariantBlock(Blocks.RED_SAND);
        this.createRotatedMirroredVariantBlock(Blocks.BEDROCK);
        this.createTrivialBlock(Blocks.REINFORCED_DEEPSLATE, TexturedModel.CUBE_TOP_BOTTOM);
        this.createRotatedPillarWithHorizontalVariant(Blocks.HAY_BLOCK, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createRotatedPillarWithHorizontalVariant(Blocks.PURPUR_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
        this.createRotatedPillarWithHorizontalVariant(Blocks.QUARTZ_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
        this.createRotatedPillarWithHorizontalVariant(Blocks.OCHRE_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createRotatedPillarWithHorizontalVariant(Blocks.VERDANT_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createRotatedPillarWithHorizontalVariant(Blocks.PEARLESCENT_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createHorizontallyRotatedBlock(Blocks.LOOM, TexturedModel.ORIENTABLE);
        this.createPumpkins();
        this.createBeeNest(Blocks.BEE_NEST, TextureMapping::orientableCube);
        this.createBeeNest(Blocks.BEEHIVE, TextureMapping::orientableCubeSameEnds);
        this.createCropBlock(Blocks.BEETROOTS, BlockStateProperties.AGE_3, 0, 1, 2, 3);
        this.createCropBlock(Blocks.CARROTS, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
        this.createCropBlock(Blocks.NETHER_WART, BlockStateProperties.AGE_3, 0, 1, 1, 2);
        this.createCropBlock(Blocks.POTATOES, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
        this.createCropBlock(Blocks.WHEAT, BlockStateProperties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
        this.createCrossBlock(Blocks.TORCHFLOWER_CROP, BlockModelGenerators.PlantType.NOT_TINTED, BlockStateProperties.AGE_1, 0, 1);
        this.createPitcherCrop();
        this.createPitcherPlant();
        this.createBanners();
        this.createBeds();
        this.createHeads();
        this.createChests();
        this.createShulkerBox(Blocks.SHULKER_BOX, null);
        this.createShulkerBox(Blocks.WHITE_SHULKER_BOX, DyeColor.WHITE);
        this.createShulkerBox(Blocks.ORANGE_SHULKER_BOX, DyeColor.ORANGE);
        this.createShulkerBox(Blocks.MAGENTA_SHULKER_BOX, DyeColor.MAGENTA);
        this.createShulkerBox(Blocks.LIGHT_BLUE_SHULKER_BOX, DyeColor.LIGHT_BLUE);
        this.createShulkerBox(Blocks.YELLOW_SHULKER_BOX, DyeColor.YELLOW);
        this.createShulkerBox(Blocks.LIME_SHULKER_BOX, DyeColor.LIME);
        this.createShulkerBox(Blocks.PINK_SHULKER_BOX, DyeColor.PINK);
        this.createShulkerBox(Blocks.GRAY_SHULKER_BOX, DyeColor.GRAY);
        this.createShulkerBox(Blocks.LIGHT_GRAY_SHULKER_BOX, DyeColor.LIGHT_GRAY);
        this.createShulkerBox(Blocks.CYAN_SHULKER_BOX, DyeColor.CYAN);
        this.createShulkerBox(Blocks.PURPLE_SHULKER_BOX, DyeColor.PURPLE);
        this.createShulkerBox(Blocks.BLUE_SHULKER_BOX, DyeColor.BLUE);
        this.createShulkerBox(Blocks.BROWN_SHULKER_BOX, DyeColor.BROWN);
        this.createShulkerBox(Blocks.GREEN_SHULKER_BOX, DyeColor.GREEN);
        this.createShulkerBox(Blocks.RED_SHULKER_BOX, DyeColor.RED);
        this.createShulkerBox(Blocks.BLACK_SHULKER_BOX, DyeColor.BLACK);
        this.createParticleOnlyBlock(Blocks.CONDUIT);
        this.generateSimpleSpecialItemModel(Blocks.CONDUIT, new ConduitSpecialRenderer.Unbaked());
        this.createParticleOnlyBlock(Blocks.DECORATED_POT, Blocks.TERRACOTTA);
        this.generateSimpleSpecialItemModel(Blocks.DECORATED_POT, new DecoratedPotSpecialRenderer.Unbaked());
        this.createParticleOnlyBlock(Blocks.END_PORTAL, Blocks.OBSIDIAN);
        this.createParticleOnlyBlock(Blocks.END_GATEWAY, Blocks.OBSIDIAN);
        this.createTrivialCube(Blocks.AZALEA_LEAVES);
        this.createTrivialCube(Blocks.FLOWERING_AZALEA_LEAVES);
        this.createTrivialCube(Blocks.WHITE_CONCRETE);
        this.createTrivialCube(Blocks.ORANGE_CONCRETE);
        this.createTrivialCube(Blocks.MAGENTA_CONCRETE);
        this.createTrivialCube(Blocks.LIGHT_BLUE_CONCRETE);
        this.createTrivialCube(Blocks.YELLOW_CONCRETE);
        this.createTrivialCube(Blocks.LIME_CONCRETE);
        this.createTrivialCube(Blocks.PINK_CONCRETE);
        this.createTrivialCube(Blocks.GRAY_CONCRETE);
        this.createTrivialCube(Blocks.LIGHT_GRAY_CONCRETE);
        this.createTrivialCube(Blocks.CYAN_CONCRETE);
        this.createTrivialCube(Blocks.PURPLE_CONCRETE);
        this.createTrivialCube(Blocks.BLUE_CONCRETE);
        this.createTrivialCube(Blocks.BROWN_CONCRETE);
        this.createTrivialCube(Blocks.GREEN_CONCRETE);
        this.createTrivialCube(Blocks.RED_CONCRETE);
        this.createTrivialCube(Blocks.BLACK_CONCRETE);
        this.createColoredBlockWithRandomRotations(
            TexturedModel.CUBE,
            Blocks.WHITE_CONCRETE_POWDER,
            Blocks.ORANGE_CONCRETE_POWDER,
            Blocks.MAGENTA_CONCRETE_POWDER,
            Blocks.LIGHT_BLUE_CONCRETE_POWDER,
            Blocks.YELLOW_CONCRETE_POWDER,
            Blocks.LIME_CONCRETE_POWDER,
            Blocks.PINK_CONCRETE_POWDER,
            Blocks.GRAY_CONCRETE_POWDER,
            Blocks.LIGHT_GRAY_CONCRETE_POWDER,
            Blocks.CYAN_CONCRETE_POWDER,
            Blocks.PURPLE_CONCRETE_POWDER,
            Blocks.BLUE_CONCRETE_POWDER,
            Blocks.BROWN_CONCRETE_POWDER,
            Blocks.GREEN_CONCRETE_POWDER,
            Blocks.RED_CONCRETE_POWDER,
            Blocks.BLACK_CONCRETE_POWDER
        );
        this.createTrivialCube(Blocks.TERRACOTTA);
        this.createTrivialCube(Blocks.WHITE_TERRACOTTA);
        this.createTrivialCube(Blocks.ORANGE_TERRACOTTA);
        this.createTrivialCube(Blocks.MAGENTA_TERRACOTTA);
        this.createTrivialCube(Blocks.LIGHT_BLUE_TERRACOTTA);
        this.createTrivialCube(Blocks.YELLOW_TERRACOTTA);
        this.createTrivialCube(Blocks.LIME_TERRACOTTA);
        this.createTrivialCube(Blocks.PINK_TERRACOTTA);
        this.createTrivialCube(Blocks.GRAY_TERRACOTTA);
        this.createTrivialCube(Blocks.LIGHT_GRAY_TERRACOTTA);
        this.createTrivialCube(Blocks.CYAN_TERRACOTTA);
        this.createTrivialCube(Blocks.PURPLE_TERRACOTTA);
        this.createTrivialCube(Blocks.BLUE_TERRACOTTA);
        this.createTrivialCube(Blocks.BROWN_TERRACOTTA);
        this.createTrivialCube(Blocks.GREEN_TERRACOTTA);
        this.createTrivialCube(Blocks.RED_TERRACOTTA);
        this.createTrivialCube(Blocks.BLACK_TERRACOTTA);
        this.createTrivialCube(Blocks.TINTED_GLASS);
        this.createGlassBlocks(Blocks.GLASS, Blocks.GLASS_PANE);
        this.createGlassBlocks(Blocks.WHITE_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.ORANGE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.MAGENTA_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.YELLOW_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIME_STAINED_GLASS, Blocks.LIME_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.PINK_STAINED_GLASS, Blocks.PINK_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.GRAY_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.CYAN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.PURPLE_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BLUE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BROWN_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.GREEN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.RED_STAINED_GLASS, Blocks.RED_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BLACK_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS_PANE);
        this.createColoredBlockWithStateRotations(
            TexturedModel.GLAZED_TERRACOTTA,
            Blocks.WHITE_GLAZED_TERRACOTTA,
            Blocks.ORANGE_GLAZED_TERRACOTTA,
            Blocks.MAGENTA_GLAZED_TERRACOTTA,
            Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA,
            Blocks.YELLOW_GLAZED_TERRACOTTA,
            Blocks.LIME_GLAZED_TERRACOTTA,
            Blocks.PINK_GLAZED_TERRACOTTA,
            Blocks.GRAY_GLAZED_TERRACOTTA,
            Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA,
            Blocks.CYAN_GLAZED_TERRACOTTA,
            Blocks.PURPLE_GLAZED_TERRACOTTA,
            Blocks.BLUE_GLAZED_TERRACOTTA,
            Blocks.BROWN_GLAZED_TERRACOTTA,
            Blocks.GREEN_GLAZED_TERRACOTTA,
            Blocks.RED_GLAZED_TERRACOTTA,
            Blocks.BLACK_GLAZED_TERRACOTTA
        );
        this.createFullAndCarpetBlocks(Blocks.WHITE_WOOL, Blocks.WHITE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.ORANGE_WOOL, Blocks.ORANGE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.MAGENTA_WOOL, Blocks.MAGENTA_CARPET);
        this.createFullAndCarpetBlocks(Blocks.LIGHT_BLUE_WOOL, Blocks.LIGHT_BLUE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.YELLOW_WOOL, Blocks.YELLOW_CARPET);
        this.createFullAndCarpetBlocks(Blocks.LIME_WOOL, Blocks.LIME_CARPET);
        this.createFullAndCarpetBlocks(Blocks.PINK_WOOL, Blocks.PINK_CARPET);
        this.createFullAndCarpetBlocks(Blocks.GRAY_WOOL, Blocks.GRAY_CARPET);
        this.createFullAndCarpetBlocks(Blocks.LIGHT_GRAY_WOOL, Blocks.LIGHT_GRAY_CARPET);
        this.createFullAndCarpetBlocks(Blocks.CYAN_WOOL, Blocks.CYAN_CARPET);
        this.createFullAndCarpetBlocks(Blocks.PURPLE_WOOL, Blocks.PURPLE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.BLUE_WOOL, Blocks.BLUE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.BROWN_WOOL, Blocks.BROWN_CARPET);
        this.createFullAndCarpetBlocks(Blocks.GREEN_WOOL, Blocks.GREEN_CARPET);
        this.createFullAndCarpetBlocks(Blocks.RED_WOOL, Blocks.RED_CARPET);
        this.createFullAndCarpetBlocks(Blocks.BLACK_WOOL, Blocks.BLACK_CARPET);
        this.createTrivialCube(Blocks.MUD);
        this.createTrivialCube(Blocks.PACKED_MUD);
        this.createPlant(Blocks.FERN, Blocks.POTTED_FERN, BlockModelGenerators.PlantType.TINTED);
        this.createItemWithGrassTint(Blocks.FERN);
        this.createPlantWithDefaultItem(Blocks.DANDELION, Blocks.POTTED_DANDELION, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.POPPY, Blocks.POTTED_POPPY, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.OPEN_EYEBLOSSOM, Blocks.POTTED_OPEN_EYEBLOSSOM, BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.CLOSED_EYEBLOSSOM, Blocks.POTTED_CLOSED_EYEBLOSSOM, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.BLUE_ORCHID, Blocks.POTTED_BLUE_ORCHID, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.ALLIUM, Blocks.POTTED_ALLIUM, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.AZURE_BLUET, Blocks.POTTED_AZURE_BLUET, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.RED_TULIP, Blocks.POTTED_RED_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.ORANGE_TULIP, Blocks.POTTED_ORANGE_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.WHITE_TULIP, Blocks.POTTED_WHITE_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.PINK_TULIP, Blocks.POTTED_PINK_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.OXEYE_DAISY, Blocks.POTTED_OXEYE_DAISY, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.CORNFLOWER, Blocks.POTTED_CORNFLOWER, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.LILY_OF_THE_VALLEY, Blocks.POTTED_LILY_OF_THE_VALLEY, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.WITHER_ROSE, Blocks.POTTED_WITHER_ROSE, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.RED_MUSHROOM, Blocks.POTTED_RED_MUSHROOM, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.BROWN_MUSHROOM, Blocks.POTTED_BROWN_MUSHROOM, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.DEAD_BUSH, Blocks.POTTED_DEAD_BUSH, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.TORCHFLOWER, Blocks.POTTED_TORCHFLOWER, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPointedDripstone();
        this.createMushroomBlock(Blocks.BROWN_MUSHROOM_BLOCK);
        this.createMushroomBlock(Blocks.RED_MUSHROOM_BLOCK);
        this.createMushroomBlock(Blocks.MUSHROOM_STEM);
        this.createCrossBlock(Blocks.SHORT_GRASS, BlockModelGenerators.PlantType.TINTED);
        this.createItemWithGrassTint(Blocks.SHORT_GRASS);
        this.createCrossBlock(Blocks.SUGAR_CANE, BlockModelGenerators.PlantType.TINTED);
        this.registerSimpleFlatItemModel(Items.SUGAR_CANE);
        this.createGrowingPlant(Blocks.KELP, Blocks.KELP_PLANT, BlockModelGenerators.PlantType.NOT_TINTED);
        this.registerSimpleFlatItemModel(Items.KELP);
        this.createCrossBlock(Blocks.HANGING_ROOTS, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createGrowingPlant(Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createGrowingPlant(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, BlockModelGenerators.PlantType.NOT_TINTED);
        this.registerSimpleFlatItemModel(Blocks.WEEPING_VINES, "_plant");
        this.registerSimpleFlatItemModel(Blocks.TWISTING_VINES, "_plant");
        this.createCrossBlockWithDefaultItem(
            Blocks.BAMBOO_SAPLING, BlockModelGenerators.PlantType.TINTED, TextureMapping.cross(TextureMapping.getBlockTexture(Blocks.BAMBOO, "_stage0"))
        );
        this.createBamboo();
        this.createCrossBlockWithDefaultItem(Blocks.COBWEB, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createDoublePlantWithDefaultItem(Blocks.LILAC, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createDoublePlantWithDefaultItem(Blocks.ROSE_BUSH, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createDoublePlantWithDefaultItem(Blocks.PEONY, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedDoublePlant(Blocks.TALL_GRASS);
        this.createTintedDoublePlant(Blocks.LARGE_FERN);
        this.createSunflower();
        this.createTallSeagrass();
        this.createSmallDripleaf();
        this.createCoral(
            Blocks.TUBE_CORAL,
            Blocks.DEAD_TUBE_CORAL,
            Blocks.TUBE_CORAL_BLOCK,
            Blocks.DEAD_TUBE_CORAL_BLOCK,
            Blocks.TUBE_CORAL_FAN,
            Blocks.DEAD_TUBE_CORAL_FAN,
            Blocks.TUBE_CORAL_WALL_FAN,
            Blocks.DEAD_TUBE_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.BRAIN_CORAL,
            Blocks.DEAD_BRAIN_CORAL,
            Blocks.BRAIN_CORAL_BLOCK,
            Blocks.DEAD_BRAIN_CORAL_BLOCK,
            Blocks.BRAIN_CORAL_FAN,
            Blocks.DEAD_BRAIN_CORAL_FAN,
            Blocks.BRAIN_CORAL_WALL_FAN,
            Blocks.DEAD_BRAIN_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.BUBBLE_CORAL,
            Blocks.DEAD_BUBBLE_CORAL,
            Blocks.BUBBLE_CORAL_BLOCK,
            Blocks.DEAD_BUBBLE_CORAL_BLOCK,
            Blocks.BUBBLE_CORAL_FAN,
            Blocks.DEAD_BUBBLE_CORAL_FAN,
            Blocks.BUBBLE_CORAL_WALL_FAN,
            Blocks.DEAD_BUBBLE_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.FIRE_CORAL,
            Blocks.DEAD_FIRE_CORAL,
            Blocks.FIRE_CORAL_BLOCK,
            Blocks.DEAD_FIRE_CORAL_BLOCK,
            Blocks.FIRE_CORAL_FAN,
            Blocks.DEAD_FIRE_CORAL_FAN,
            Blocks.FIRE_CORAL_WALL_FAN,
            Blocks.DEAD_FIRE_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.HORN_CORAL,
            Blocks.DEAD_HORN_CORAL,
            Blocks.HORN_CORAL_BLOCK,
            Blocks.DEAD_HORN_CORAL_BLOCK,
            Blocks.HORN_CORAL_FAN,
            Blocks.DEAD_HORN_CORAL_FAN,
            Blocks.HORN_CORAL_WALL_FAN,
            Blocks.DEAD_HORN_CORAL_WALL_FAN
        );
        this.createStems(Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM);
        this.createStems(Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
        this.woodProvider(Blocks.MANGROVE_LOG).logWithHorizontal(Blocks.MANGROVE_LOG).wood(Blocks.MANGROVE_WOOD);
        this.woodProvider(Blocks.STRIPPED_MANGROVE_LOG).logWithHorizontal(Blocks.STRIPPED_MANGROVE_LOG).wood(Blocks.STRIPPED_MANGROVE_WOOD);
        this.createHangingSign(Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN);
        this.createTintedLeaves(Blocks.MANGROVE_LEAVES, TexturedModel.LEAVES, -7158200);
        this.woodProvider(Blocks.ACACIA_LOG).logWithHorizontal(Blocks.ACACIA_LOG).wood(Blocks.ACACIA_WOOD);
        this.woodProvider(Blocks.STRIPPED_ACACIA_LOG).logWithHorizontal(Blocks.STRIPPED_ACACIA_LOG).wood(Blocks.STRIPPED_ACACIA_WOOD);
        this.createHangingSign(Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.ACACIA_SAPLING, Blocks.POTTED_ACACIA_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.ACACIA_LEAVES, TexturedModel.LEAVES, -12012264);
        this.woodProvider(Blocks.CHERRY_LOG).logUVLocked(Blocks.CHERRY_LOG).wood(Blocks.CHERRY_WOOD);
        this.woodProvider(Blocks.STRIPPED_CHERRY_LOG).logUVLocked(Blocks.STRIPPED_CHERRY_LOG).wood(Blocks.STRIPPED_CHERRY_WOOD);
        this.createHangingSign(Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.CHERRY_SAPLING, Blocks.POTTED_CHERRY_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTrivialBlock(Blocks.CHERRY_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.BIRCH_LOG).logWithHorizontal(Blocks.BIRCH_LOG).wood(Blocks.BIRCH_WOOD);
        this.woodProvider(Blocks.STRIPPED_BIRCH_LOG).logWithHorizontal(Blocks.STRIPPED_BIRCH_LOG).wood(Blocks.STRIPPED_BIRCH_WOOD);
        this.createHangingSign(Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.BIRCH_SAPLING, Blocks.POTTED_BIRCH_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.BIRCH_LEAVES, TexturedModel.LEAVES, -8345771);
        this.woodProvider(Blocks.OAK_LOG).logWithHorizontal(Blocks.OAK_LOG).wood(Blocks.OAK_WOOD);
        this.woodProvider(Blocks.STRIPPED_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_OAK_LOG).wood(Blocks.STRIPPED_OAK_WOOD);
        this.createHangingSign(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.OAK_SAPLING, Blocks.POTTED_OAK_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.OAK_LEAVES, TexturedModel.LEAVES, -12012264);
        this.woodProvider(Blocks.SPRUCE_LOG).logWithHorizontal(Blocks.SPRUCE_LOG).wood(Blocks.SPRUCE_WOOD);
        this.woodProvider(Blocks.STRIPPED_SPRUCE_LOG).logWithHorizontal(Blocks.STRIPPED_SPRUCE_LOG).wood(Blocks.STRIPPED_SPRUCE_WOOD);
        this.createHangingSign(Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.SPRUCE_SAPLING, Blocks.POTTED_SPRUCE_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.SPRUCE_LEAVES, TexturedModel.LEAVES, -10380959);
        this.woodProvider(Blocks.DARK_OAK_LOG).logWithHorizontal(Blocks.DARK_OAK_LOG).wood(Blocks.DARK_OAK_WOOD);
        this.woodProvider(Blocks.STRIPPED_DARK_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_DARK_OAK_LOG).wood(Blocks.STRIPPED_DARK_OAK_WOOD);
        this.createHangingSign(Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.DARK_OAK_SAPLING, Blocks.POTTED_DARK_OAK_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.DARK_OAK_LEAVES, TexturedModel.LEAVES, -12012264);
        this.woodProvider(Blocks.PALE_OAK_LOG).logWithHorizontal(Blocks.PALE_OAK_LOG).wood(Blocks.PALE_OAK_WOOD);
        this.woodProvider(Blocks.STRIPPED_PALE_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_PALE_OAK_LOG).wood(Blocks.STRIPPED_PALE_OAK_WOOD);
        this.createHangingSign(Blocks.STRIPPED_PALE_OAK_LOG, Blocks.PALE_OAK_HANGING_SIGN, Blocks.PALE_OAK_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.PALE_OAK_SAPLING, Blocks.POTTED_PALE_OAK_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTrivialBlock(Blocks.PALE_OAK_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.JUNGLE_LOG).logWithHorizontal(Blocks.JUNGLE_LOG).wood(Blocks.JUNGLE_WOOD);
        this.woodProvider(Blocks.STRIPPED_JUNGLE_LOG).logWithHorizontal(Blocks.STRIPPED_JUNGLE_LOG).wood(Blocks.STRIPPED_JUNGLE_WOOD);
        this.createHangingSign(Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.JUNGLE_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.JUNGLE_LEAVES, TexturedModel.LEAVES, -12012264);
        this.woodProvider(Blocks.CRIMSON_STEM).log(Blocks.CRIMSON_STEM).wood(Blocks.CRIMSON_HYPHAE);
        this.woodProvider(Blocks.STRIPPED_CRIMSON_STEM).log(Blocks.STRIPPED_CRIMSON_STEM).wood(Blocks.STRIPPED_CRIMSON_HYPHAE);
        this.createHangingSign(Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_FUNGUS, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createNetherRoots(Blocks.CRIMSON_ROOTS, Blocks.POTTED_CRIMSON_ROOTS);
        this.woodProvider(Blocks.WARPED_STEM).log(Blocks.WARPED_STEM).wood(Blocks.WARPED_HYPHAE);
        this.woodProvider(Blocks.STRIPPED_WARPED_STEM).log(Blocks.STRIPPED_WARPED_STEM).wood(Blocks.STRIPPED_WARPED_HYPHAE);
        this.createHangingSign(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.WARPED_FUNGUS, Blocks.POTTED_WARPED_FUNGUS, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createNetherRoots(Blocks.WARPED_ROOTS, Blocks.POTTED_WARPED_ROOTS);
        this.woodProvider(Blocks.BAMBOO_BLOCK).logUVLocked(Blocks.BAMBOO_BLOCK);
        this.woodProvider(Blocks.STRIPPED_BAMBOO_BLOCK).logUVLocked(Blocks.STRIPPED_BAMBOO_BLOCK);
        this.createHangingSign(Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN);
        this.createCrossBlock(Blocks.NETHER_SPROUTS, BlockModelGenerators.PlantType.NOT_TINTED);
        this.registerSimpleFlatItemModel(Items.NETHER_SPROUTS);
        this.createDoor(Blocks.IRON_DOOR);
        this.createTrapdoor(Blocks.IRON_TRAPDOOR);
        this.createSmoothStoneSlab();
        this.createPassiveRail(Blocks.RAIL);
        this.createActiveRail(Blocks.POWERED_RAIL);
        this.createActiveRail(Blocks.DETECTOR_RAIL);
        this.createActiveRail(Blocks.ACTIVATOR_RAIL);
        this.createComparator();
        this.createCommandBlock(Blocks.COMMAND_BLOCK);
        this.createCommandBlock(Blocks.REPEATING_COMMAND_BLOCK);
        this.createCommandBlock(Blocks.CHAIN_COMMAND_BLOCK);
        this.createAnvil(Blocks.ANVIL);
        this.createAnvil(Blocks.CHIPPED_ANVIL);
        this.createAnvil(Blocks.DAMAGED_ANVIL);
        this.createBarrel();
        this.createBell();
        this.createFurnace(Blocks.FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
        this.createFurnace(Blocks.BLAST_FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
        this.createFurnace(Blocks.SMOKER, TexturedModel.ORIENTABLE);
        this.createRedstoneWire();
        this.createRespawnAnchor();
        this.createSculkCatalyst();
        this.copyModel(Blocks.CHISELED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS);
        this.copyModel(Blocks.COBBLESTONE, Blocks.INFESTED_COBBLESTONE);
        this.copyModel(Blocks.CRACKED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS);
        this.copyModel(Blocks.MOSSY_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS);
        this.createInfestedStone();
        this.copyModel(Blocks.STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
        this.createInfestedDeepslate();
    }

    private void createLightBlock() {
        ItemModel.Unbaked itemmodel$unbaked = ItemModelUtils.plainModel(this.createFlatItemModel(Items.LIGHT));
        Map<Integer, ItemModel.Unbaked> map = new HashMap<>(16);
        PropertyDispatch.C1<Integer> c1 = PropertyDispatch.property(BlockStateProperties.LEVEL);

        for (int i = 0; i <= 15; i++) {
            String s = String.format(Locale.ROOT, "_%02d", i);
            ResourceLocation resourcelocation = TextureMapping.getItemTexture(Items.LIGHT, s);
            c1.select(
                i,
                Variant.variant()
                    .with(
                        VariantProperties.MODEL,
                        ModelTemplates.PARTICLE_ONLY.createWithSuffix(Blocks.LIGHT, s, TextureMapping.particle(resourcelocation), this.modelOutput)
                    )
            );
            ItemModel.Unbaked itemmodel$unbaked1 = ItemModelUtils.plainModel(
                ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(Items.LIGHT, s), TextureMapping.layer0(resourcelocation), this.modelOutput)
            );
            map.put(i, itemmodel$unbaked1);
        }

        this.itemModelOutput.accept(Items.LIGHT, ItemModelUtils.selectBlockItemProperty(LightBlock.LEVEL, itemmodel$unbaked, map));
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.LIGHT).with(c1));
    }

    private void createCandleAndCandleCake(Block p_388274_, Block p_387301_) {
        this.registerSimpleFlatItemModel(p_388274_.asItem());
        TextureMapping texturemapping = TextureMapping.cube(TextureMapping.getBlockTexture(p_388274_));
        TextureMapping texturemapping1 = TextureMapping.cube(TextureMapping.getBlockTexture(p_388274_, "_lit"));
        ResourceLocation resourcelocation = ModelTemplates.CANDLE.createWithSuffix(p_388274_, "_one_candle", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.TWO_CANDLES.createWithSuffix(p_388274_, "_two_candles", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.THREE_CANDLES.createWithSuffix(p_388274_, "_three_candles", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.FOUR_CANDLES.createWithSuffix(p_388274_, "_four_candles", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.CANDLE.createWithSuffix(p_388274_, "_one_candle_lit", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.TWO_CANDLES.createWithSuffix(p_388274_, "_two_candles_lit", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation6 = ModelTemplates.THREE_CANDLES.createWithSuffix(p_388274_, "_three_candles_lit", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation7 = ModelTemplates.FOUR_CANDLES.createWithSuffix(p_388274_, "_four_candles_lit", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_388274_)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.CANDLES, BlockStateProperties.LIT)
                            .select(1, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(2, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(3, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                            .select(4, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                            .select(1, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation4))
                            .select(2, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation5))
                            .select(3, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation6))
                            .select(4, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation7))
                    )
            );
        ResourceLocation resourcelocation8 = ModelTemplates.CANDLE_CAKE.create(p_387301_, TextureMapping.candleCake(p_388274_, false), this.modelOutput);
        ResourceLocation resourcelocation9 = ModelTemplates.CANDLE_CAKE
            .createWithSuffix(p_387301_, "_lit", TextureMapping.candleCake(p_388274_, true), this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_387301_).with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation9, resourcelocation8))
            );
    }

    @OnlyIn(Dist.CLIENT)
    class BlockFamilyProvider {
        private final TextureMapping mapping;
        private final Map<ModelTemplate, ResourceLocation> models = Maps.newHashMap();
        @Nullable
        private BlockFamily family;
        @Nullable
        private ResourceLocation fullBlock;
        private final Set<Block> skipGeneratingModelsFor = new HashSet<>();

        public BlockFamilyProvider(TextureMapping p_388151_) {
            this.mapping = p_388151_;
        }

        public BlockModelGenerators.BlockFamilyProvider fullBlock(Block p_388401_, ModelTemplate p_387245_) {
            this.fullBlock = p_387245_.create(p_388401_, this.mapping, BlockModelGenerators.this.modelOutput);
            if (BlockModelGenerators.this.fullBlockModelCustomGenerators.containsKey(p_388401_)) {
                BlockModelGenerators.this.blockStateOutput
                    .accept(
                        BlockModelGenerators.this.fullBlockModelCustomGenerators
                            .get(p_388401_)
                            .create(p_388401_, this.fullBlock, this.mapping, BlockModelGenerators.this.modelOutput)
                    );
            } else {
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(p_388401_, this.fullBlock));
            }

            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider donateModelTo(Block p_387771_, Block p_388618_) {
            ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(p_387771_);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(p_388618_, resourcelocation));
            BlockModelGenerators.this.itemModelOutput.copy(p_387771_.asItem(), p_388618_.asItem());
            this.skipGeneratingModelsFor.add(p_388618_);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider button(Block p_388929_) {
            ResourceLocation resourcelocation = ModelTemplates.BUTTON.create(p_388929_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.BUTTON_PRESSED.create(p_388929_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createButton(p_388929_, resourcelocation, resourcelocation1));
            ResourceLocation resourcelocation2 = ModelTemplates.BUTTON_INVENTORY.create(p_388929_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.registerSimpleItemModel(p_388929_, resourcelocation2);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider wall(Block p_387051_) {
            ResourceLocation resourcelocation = ModelTemplates.WALL_POST.create(p_387051_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.WALL_LOW_SIDE.create(p_387051_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.WALL_TALL_SIDE.create(p_387051_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createWall(p_387051_, resourcelocation, resourcelocation1, resourcelocation2));
            ResourceLocation resourcelocation3 = ModelTemplates.WALL_INVENTORY.create(p_387051_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.registerSimpleItemModel(p_387051_, resourcelocation3);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider customFence(Block p_387431_) {
            TextureMapping texturemapping = TextureMapping.customParticle(p_387431_);
            ResourceLocation resourcelocation = ModelTemplates.CUSTOM_FENCE_POST.create(p_387431_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.CUSTOM_FENCE_SIDE_NORTH
                .create(p_387431_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.CUSTOM_FENCE_SIDE_EAST.create(p_387431_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation3 = ModelTemplates.CUSTOM_FENCE_SIDE_SOUTH
                .create(p_387431_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation4 = ModelTemplates.CUSTOM_FENCE_SIDE_WEST.create(p_387431_, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(
                    BlockModelGenerators.createCustomFence(
                        p_387431_, resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3, resourcelocation4
                    )
                );
            ResourceLocation resourcelocation5 = ModelTemplates.CUSTOM_FENCE_INVENTORY.create(p_387431_, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.registerSimpleItemModel(p_387431_, resourcelocation5);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider fence(Block p_387512_) {
            ResourceLocation resourcelocation = ModelTemplates.FENCE_POST.create(p_387512_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.FENCE_SIDE.create(p_387512_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createFence(p_387512_, resourcelocation, resourcelocation1));
            ResourceLocation resourcelocation2 = ModelTemplates.FENCE_INVENTORY.create(p_387512_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.registerSimpleItemModel(p_387512_, resourcelocation2);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider customFenceGate(Block p_387810_) {
            TextureMapping texturemapping = TextureMapping.customParticle(p_387810_);
            ResourceLocation resourcelocation = ModelTemplates.CUSTOM_FENCE_GATE_OPEN.create(p_387810_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.CUSTOM_FENCE_GATE_CLOSED
                .create(p_387810_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.CUSTOM_FENCE_GATE_WALL_OPEN
                .create(p_387810_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation3 = ModelTemplates.CUSTOM_FENCE_GATE_WALL_CLOSED
                .create(p_387810_, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createFenceGate(p_387810_, resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3, false));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider fenceGate(Block p_386624_) {
            ResourceLocation resourcelocation = ModelTemplates.FENCE_GATE_OPEN.create(p_386624_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.FENCE_GATE_CLOSED.create(p_386624_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.FENCE_GATE_WALL_OPEN.create(p_386624_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation3 = ModelTemplates.FENCE_GATE_WALL_CLOSED.create(p_386624_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createFenceGate(p_386624_, resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3, true));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider pressurePlate(Block p_387753_) {
            ResourceLocation resourcelocation = ModelTemplates.PRESSURE_PLATE_UP.create(p_387753_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.PRESSURE_PLATE_DOWN.create(p_387753_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createPressurePlate(p_387753_, resourcelocation, resourcelocation1));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider sign(Block p_388436_) {
            if (this.family == null) {
                throw new IllegalStateException("Family not defined");
            } else {
                Block block = this.family.getVariants().get(BlockFamily.Variant.WALL_SIGN);
                ResourceLocation resourcelocation = ModelTemplates.PARTICLE_ONLY.create(p_388436_, this.mapping, BlockModelGenerators.this.modelOutput);
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(p_388436_, resourcelocation));
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, resourcelocation));
                BlockModelGenerators.this.registerSimpleFlatItemModel(p_388436_.asItem());
                return this;
            }
        }

        public BlockModelGenerators.BlockFamilyProvider slab(Block p_388247_) {
            if (this.fullBlock == null) {
                throw new IllegalStateException("Full block not generated yet");
            } else {
                ResourceLocation resourcelocation = this.getOrCreateModel(ModelTemplates.SLAB_BOTTOM, p_388247_);
                ResourceLocation resourcelocation1 = this.getOrCreateModel(ModelTemplates.SLAB_TOP, p_388247_);
                BlockModelGenerators.this.blockStateOutput
                    .accept(BlockModelGenerators.createSlab(p_388247_, resourcelocation, resourcelocation1, this.fullBlock));
                BlockModelGenerators.this.registerSimpleItemModel(p_388247_, resourcelocation);
                return this;
            }
        }

        public BlockModelGenerators.BlockFamilyProvider stairs(Block p_386852_) {
            ResourceLocation resourcelocation = this.getOrCreateModel(ModelTemplates.STAIRS_INNER, p_386852_);
            ResourceLocation resourcelocation1 = this.getOrCreateModel(ModelTemplates.STAIRS_STRAIGHT, p_386852_);
            ResourceLocation resourcelocation2 = this.getOrCreateModel(ModelTemplates.STAIRS_OUTER, p_386852_);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createStairs(p_386852_, resourcelocation, resourcelocation1, resourcelocation2));
            BlockModelGenerators.this.registerSimpleItemModel(p_386852_, resourcelocation1);
            return this;
        }

        private BlockModelGenerators.BlockFamilyProvider fullBlockVariant(Block p_386846_) {
            TexturedModel texturedmodel = BlockModelGenerators.this.texturedModels.getOrDefault(p_386846_, TexturedModel.CUBE.get(p_386846_));
            ResourceLocation resourcelocation = texturedmodel.create(p_386846_, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(p_386846_, resourcelocation));
            return this;
        }

        private BlockModelGenerators.BlockFamilyProvider door(Block p_388017_) {
            BlockModelGenerators.this.createDoor(p_388017_);
            return this;
        }

        private void trapdoor(Block p_388553_) {
            if (BlockModelGenerators.this.nonOrientableTrapdoor.contains(p_388553_)) {
                BlockModelGenerators.this.createTrapdoor(p_388553_);
            } else {
                BlockModelGenerators.this.createOrientableTrapdoor(p_388553_);
            }
        }

        private ResourceLocation getOrCreateModel(ModelTemplate p_387416_, Block p_388850_) {
            return this.models.computeIfAbsent(p_387416_, p_387666_ -> p_387666_.create(p_388850_, this.mapping, BlockModelGenerators.this.modelOutput));
        }

        public BlockModelGenerators.BlockFamilyProvider generateFor(BlockFamily p_387069_) {
            this.family = p_387069_;
            p_387069_.getVariants().forEach((p_388584_, p_388675_) -> {
                if (!this.skipGeneratingModelsFor.contains(p_388675_)) {
                    BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block> biconsumer = BlockModelGenerators.SHAPE_CONSUMERS.get(p_388584_);
                    if (biconsumer != null) {
                        biconsumer.accept(this, p_388675_);
                    }
                }
            });
            return this;
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    interface BlockStateGeneratorSupplier {
        BlockStateGenerator create(Block p_388739_, ResourceLocation p_388592_, TextureMapping p_386936_, BiConsumer<ResourceLocation, ModelInstance> p_387799_);
    }

    @OnlyIn(Dist.CLIENT)
    static record BookSlotModelCacheKey(ModelTemplate template, String modelSuffix) {
    }

    @OnlyIn(Dist.CLIENT)
    static enum PlantType {
        TINTED(ModelTemplates.TINTED_CROSS, ModelTemplates.TINTED_FLOWER_POT_CROSS, false),
        NOT_TINTED(ModelTemplates.CROSS, ModelTemplates.FLOWER_POT_CROSS, false),
        EMISSIVE_NOT_TINTED(ModelTemplates.CROSS_EMISSIVE, ModelTemplates.FLOWER_POT_CROSS_EMISSIVE, true);

        private final ModelTemplate blockTemplate;
        private final ModelTemplate flowerPotTemplate;
        private final boolean isEmissive;

        private PlantType(ModelTemplate p_388039_, ModelTemplate p_386525_, boolean p_387390_) {
            this.blockTemplate = p_388039_;
            this.flowerPotTemplate = p_386525_;
            this.isEmissive = p_387390_;
        }

        public ModelTemplate getCross() {
            return this.blockTemplate;
        }

        public ModelTemplate getCrossPot() {
            return this.flowerPotTemplate;
        }

        public ResourceLocation createItemModel(BlockModelGenerators p_387586_, Block p_387998_) {
            Item item = p_387998_.asItem();
            return this.isEmissive
                ? p_387586_.createFlatItemModelWithBlockTextureAndOverlay(item, p_387998_, "_emissive")
                : p_387586_.createFlatItemModelWithBlockTexture(item, p_387998_);
        }

        public TextureMapping getTextureMapping(Block p_387999_) {
            return this.isEmissive ? TextureMapping.crossEmissive(p_387999_) : TextureMapping.cross(p_387999_);
        }

        public TextureMapping getPlantTextureMapping(Block p_386562_) {
            return this.isEmissive ? TextureMapping.plantEmissive(p_386562_) : TextureMapping.plant(p_386562_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class WoodProvider {
        private final TextureMapping logMapping;

        public WoodProvider(TextureMapping p_386867_) {
            this.logMapping = p_386867_;
        }

        public BlockModelGenerators.WoodProvider wood(Block p_388351_) {
            TextureMapping texturemapping = this.logMapping.copyAndUpdate(TextureSlot.END, this.logMapping.get(TextureSlot.SIDE));
            ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(p_388351_, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(p_388351_, resourcelocation));
            return this;
        }

        public BlockModelGenerators.WoodProvider log(Block p_387195_) {
            ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(p_387195_, this.logMapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(p_387195_, resourcelocation));
            return this;
        }

        public BlockModelGenerators.WoodProvider logWithHorizontal(Block p_387121_) {
            ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(p_387121_, this.logMapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.CUBE_COLUMN_HORIZONTAL
                .create(p_387121_, this.logMapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(p_387121_, resourcelocation, resourcelocation1));
            return this;
        }

        public BlockModelGenerators.WoodProvider logUVLocked(Block p_387740_) {
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createPillarBlockUVLocked(p_387740_, this.logMapping, BlockModelGenerators.this.modelOutput));
            return this;
        }
    }
}
