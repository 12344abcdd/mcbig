package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import org.slf4j.Logger;

public record SerializableChunkData(
    Registry<Biome> biomeRegistry,
    ChunkPos chunkPos,
    int minSectionY,
    long lastUpdateTime,
    long inhabitedTime,
    ChunkStatus chunkStatus,
    @Nullable BlendingData.Packed blendingData,
    @Nullable BelowZeroRetrogen belowZeroRetrogen,
    UpgradeData upgradeData,
    @Nullable long[] carvingMask,
    Map<Heightmap.Types, long[]> heightmaps,
    ChunkAccess.PackedTicks packedTicks,
    ShortList[] postProcessingSections,
    boolean lightCorrect,
    List<SerializableChunkData.SectionData> sectionData,
    List<CompoundTag> entities,
    List<CompoundTag> blockEntities,
    CompoundTag structureData
) {
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(
        Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState()
    );
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_UPGRADE_DATA = "UpgradeData";
    private static final String BLOCK_TICKS_TAG = "block_ticks";
    private static final String FLUID_TICKS_TAG = "fluid_ticks";
    public static final String X_POS_TAG = "xPos";
    public static final String Z_POS_TAG = "zPos";
    public static final String HEIGHTMAPS_TAG = "Heightmaps";
    public static final String IS_LIGHT_ON_TAG = "isLightOn";
    public static final String SECTIONS_TAG = "sections";
    public static final String BLOCK_LIGHT_TAG = "BlockLight";
    public static final String SKY_LIGHT_TAG = "SkyLight";

    @Nullable
    public static SerializableChunkData parse(LevelHeightAccessor p_361938_, RegistryAccess p_365010_, CompoundTag p_362040_) {
        if (!p_362040_.contains("Status", 8)) {
            return null;
        } else {
            ChunkPos chunkpos = new ChunkPos(new me.alphamode.mcbig.math.BigInteger(p_362040_.getString("xPos")), new me.alphamode.mcbig.math.BigInteger(p_362040_.getString("zPos")));
            long i = p_362040_.getLong("LastUpdate");
            long j = p_362040_.getLong("InhabitedTime");
            ChunkStatus chunkstatus = ChunkStatus.byName(p_362040_.getString("Status"));
            UpgradeData upgradedata = p_362040_.contains("UpgradeData", 10)
                ? new UpgradeData(p_362040_.getCompound("UpgradeData"), p_361938_)
                : UpgradeData.EMPTY;
            boolean flag = p_362040_.getBoolean("isLightOn");
            BlendingData.Packed blendingdata$packed;
            if (p_362040_.contains("blending_data", 10)) {
                blendingdata$packed = BlendingData.Packed.CODEC
                    .parse(NbtOps.INSTANCE, p_362040_.getCompound("blending_data"))
                    .resultOrPartial(LOGGER::error)
                    .orElse(null);
            } else {
                blendingdata$packed = null;
            }

            BelowZeroRetrogen belowzeroretrogen;
            if (p_362040_.contains("below_zero_retrogen", 10)) {
                belowzeroretrogen = BelowZeroRetrogen.CODEC
                    .parse(NbtOps.INSTANCE, p_362040_.getCompound("below_zero_retrogen"))
                    .resultOrPartial(LOGGER::error)
                    .orElse(null);
            } else {
                belowzeroretrogen = null;
            }

            long[] along;
            if (p_362040_.contains("carving_mask", 12)) {
                along = p_362040_.getLongArray("carving_mask");
            } else {
                along = null;
            }

            CompoundTag compoundtag = p_362040_.getCompound("Heightmaps");
            Map<Heightmap.Types, long[]> map = new EnumMap<>(Heightmap.Types.class);

            for (Heightmap.Types heightmap$types : chunkstatus.heightmapsAfter()) {
                String s = heightmap$types.getSerializationKey();
                if (compoundtag.contains(s, 12)) {
                    map.put(heightmap$types, compoundtag.getLongArray(s));
                }
            }

            List<SavedTick<Block>> list1 = SavedTick.loadTickList(
                p_362040_.getList("block_ticks", 10), p_363212_ -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(p_363212_)), chunkpos
            );
            List<SavedTick<Fluid>> list2 = SavedTick.loadTickList(
                p_362040_.getList("fluid_ticks", 10), p_364483_ -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(p_364483_)), chunkpos
            );
            ChunkAccess.PackedTicks chunkaccess$packedticks = new ChunkAccess.PackedTicks(list1, list2);
            ListTag listtag = p_362040_.getList("PostProcessing", 9);
            ShortList[] ashortlist = new ShortList[listtag.size()];

            for (int k = 0; k < listtag.size(); k++) {
                ListTag listtag1 = listtag.getList(k);
                ShortList shortlist = new ShortArrayList(listtag1.size());

                for (int l = 0; l < listtag1.size(); l++) {
                    shortlist.add(listtag1.getShort(l));
                }

                ashortlist[k] = shortlist;
            }

            List<CompoundTag> list3 = Lists.transform(p_362040_.getList("entities", 10), p_365486_ -> (CompoundTag)p_365486_);
            List<CompoundTag> list4 = Lists.transform(p_362040_.getList("block_entities", 10), p_362941_ -> (CompoundTag)p_362941_);
            CompoundTag compoundtag2 = p_362040_.getCompound("structures");
            ListTag listtag2 = p_362040_.getList("sections", 10);
            List<SerializableChunkData.SectionData> list = new ArrayList<>(listtag2.size());
            Registry<Biome> registry = p_365010_.lookupOrThrow(Registries.BIOME);
            Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(registry);

            for (int i1 = 0; i1 < listtag2.size(); i1++) {
                CompoundTag compoundtag1 = listtag2.getCompound(i1);
                int j1 = compoundtag1.getByte("Y");
                LevelChunkSection levelchunksection;
                if (j1 >= p_361938_.getMinSectionY() && j1 <= p_361938_.getMaxSectionY()) {
                    PalettedContainer<BlockState> palettedcontainer;
                    if (compoundtag1.contains("block_states", 10)) {
                        palettedcontainer = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, compoundtag1.getCompound("block_states"))
                            .promotePartial(p_361842_ -> logErrors(chunkpos, j1, p_361842_))
                            .getOrThrow(SerializableChunkData.ChunkReadException::new);
                    } else {
                        palettedcontainer = new PalettedContainer<>(
                            Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES
                        );
                    }

                    PalettedContainerRO<Holder<Biome>> palettedcontainerro;
                    if (compoundtag1.contains("biomes", 10)) {
                        palettedcontainerro = codec.parse(NbtOps.INSTANCE, compoundtag1.getCompound("biomes"))
                            .promotePartial(p_361282_ -> logErrors(chunkpos, j1, p_361282_))
                            .getOrThrow(SerializableChunkData.ChunkReadException::new);
                    } else {
                        palettedcontainerro = new PalettedContainer<>(
                            registry.asHolderIdMap(), registry.getOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES
                        );
                    }

                    levelchunksection = new LevelChunkSection(palettedcontainer, palettedcontainerro);
                } else {
                    levelchunksection = null;
                }

                DataLayer datalayer = compoundtag1.contains("BlockLight", 7) ? new DataLayer(compoundtag1.getByteArray("BlockLight")) : null;
                DataLayer datalayer1 = compoundtag1.contains("SkyLight", 7) ? new DataLayer(compoundtag1.getByteArray("SkyLight")) : null;
                list.add(new SerializableChunkData.SectionData(j1, levelchunksection, datalayer, datalayer1));
            }

            return new SerializableChunkData(
                registry,
                chunkpos,
                p_361938_.getMinSectionY(),
                i,
                j,
                chunkstatus,
                blendingdata$packed,
                belowzeroretrogen,
                upgradedata,
                along,
                map,
                chunkaccess$packedticks,
                ashortlist,
                flag,
                list,
                list3,
                list4,
                compoundtag2
            );
        }
    }

    public ProtoChunk read(ServerLevel p_360452_, PoiManager p_364451_, RegionStorageInfo p_364971_, ChunkPos p_360628_) {
        if (!Objects.equals(p_360628_, this.chunkPos)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", p_360628_, p_360628_, this.chunkPos);
            p_360452_.getServer().reportMisplacedChunk(this.chunkPos, p_360628_, p_364971_);
        }

        int i = p_360452_.getSectionsCount();
        LevelChunkSection[] alevelchunksection = new LevelChunkSection[i];
        boolean flag = p_360452_.dimensionType().hasSkyLight();
        ChunkSource chunksource = p_360452_.getChunkSource();
        LevelLightEngine levellightengine = chunksource.getLightEngine();
        Registry<Biome> registry = p_360452_.registryAccess().lookupOrThrow(Registries.BIOME);
        boolean flag1 = false;

        for (SerializableChunkData.SectionData serializablechunkdata$sectiondata : this.sectionData) {
            SectionPos sectionpos = SectionPos.of(p_360628_, serializablechunkdata$sectiondata.y);
            if (serializablechunkdata$sectiondata.chunkSection != null) {
                alevelchunksection[p_360452_.getSectionIndexFromSectionY(serializablechunkdata$sectiondata.y)] = serializablechunkdata$sectiondata.chunkSection;
                p_364451_.checkConsistencyWithBlocks(sectionpos, serializablechunkdata$sectiondata.chunkSection);
            }

            boolean flag2 = serializablechunkdata$sectiondata.blockLight != null;
            boolean flag3 = flag && serializablechunkdata$sectiondata.skyLight != null;
            if (flag2 || flag3) {
                if (!flag1) {
                    levellightengine.retainData(p_360628_, true);
                    flag1 = true;
                }

                if (flag2) {
                    levellightengine.queueSectionData(LightLayer.BLOCK, sectionpos, serializablechunkdata$sectiondata.blockLight);
                }

                if (flag3) {
                    levellightengine.queueSectionData(LightLayer.SKY, sectionpos, serializablechunkdata$sectiondata.skyLight);
                }
            }
        }

        ChunkType chunktype = this.chunkStatus.getChunkType();
        ChunkAccess chunkaccess;
        if (chunktype == ChunkType.LEVELCHUNK) {
            LevelChunkTicks<Block> levelchunkticks = new LevelChunkTicks<>(this.packedTicks.blocks());
            LevelChunkTicks<Fluid> levelchunkticks1 = new LevelChunkTicks<>(this.packedTicks.fluids());
            chunkaccess = new LevelChunk(
                p_360452_.getLevel(),
                p_360628_,
                this.upgradeData,
                levelchunkticks,
                levelchunkticks1,
                this.inhabitedTime,
                alevelchunksection,
                postLoadChunk(p_360452_, this.entities, this.blockEntities),
                BlendingData.unpack(this.blendingData)
            );
        } else {
            ProtoChunkTicks<Block> protochunkticks = ProtoChunkTicks.load(this.packedTicks.blocks());
            ProtoChunkTicks<Fluid> protochunkticks1 = ProtoChunkTicks.load(this.packedTicks.fluids());
            ProtoChunk protochunk1 = new ProtoChunk(
                p_360628_, this.upgradeData, alevelchunksection, protochunkticks, protochunkticks1, p_360452_, registry, BlendingData.unpack(this.blendingData)
            );
            chunkaccess = protochunk1;
            protochunk1.setInhabitedTime(this.inhabitedTime);
            if (this.belowZeroRetrogen != null) {
                protochunk1.setBelowZeroRetrogen(this.belowZeroRetrogen);
            }

            protochunk1.setPersistedStatus(this.chunkStatus);
            if (this.chunkStatus.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
                protochunk1.setLightEngine(levellightengine);
            }
        }

        chunkaccess.setLightCorrect(this.lightCorrect);
        EnumSet<Heightmap.Types> enumset = EnumSet.noneOf(Heightmap.Types.class);

        for (Heightmap.Types heightmap$types : chunkaccess.getPersistedStatus().heightmapsAfter()) {
            long[] along = this.heightmaps.get(heightmap$types);
            if (along != null) {
                chunkaccess.setHeightmap(heightmap$types, along);
            } else {
                enumset.add(heightmap$types);
            }
        }

        Heightmap.primeHeightmaps(chunkaccess, enumset);
        chunkaccess.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(p_360452_), this.structureData, p_360452_.getSeed()));
        chunkaccess.setAllReferences(unpackStructureReferences(p_360452_.registryAccess(), p_360628_, this.structureData));

        for (int j = 0; j < this.postProcessingSections.length; j++) {
            chunkaccess.addPackedPostProcess(this.postProcessingSections[j], j);
        }

        if (chunktype == ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)chunkaccess, false);
        } else {
            ProtoChunk protochunk = (ProtoChunk)chunkaccess;

            for (CompoundTag compoundtag : this.entities) {
                protochunk.addEntity(compoundtag);
            }

            for (CompoundTag compoundtag1 : this.blockEntities) {
                protochunk.setBlockEntityNbt(compoundtag1);
            }

            if (this.carvingMask != null) {
                protochunk.setCarvingMask(new CarvingMask(this.carvingMask, chunkaccess.getMinY()));
            }

            return protochunk;
        }
    }

    private static void logErrors(ChunkPos p_361949_, int p_364275_, String p_360703_) {
        LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", p_361949_.x, p_364275_, p_361949_.z, p_360703_);
    }

    private static Codec<PalettedContainerRO<Holder<Biome>>> makeBiomeCodec(Registry<Biome> p_364562_) {
        return PalettedContainer.codecRO(
            p_364562_.asHolderIdMap(), p_364562_.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, p_364562_.getOrThrow(Biomes.PLAINS)
        );
    }

    public static SerializableChunkData copyOf(ServerLevel p_365319_, ChunkAccess p_362284_) {
        if (!p_362284_.canBeSerialized()) {
            throw new IllegalArgumentException("Chunk can't be serialized: " + p_362284_);
        } else {
            ChunkPos chunkpos = p_362284_.getPos();
            List<SerializableChunkData.SectionData> list = new ArrayList<>();
            LevelChunkSection[] alevelchunksection = p_362284_.getSections();
            LevelLightEngine levellightengine = p_365319_.getChunkSource().getLightEngine();

            for (int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); i++) {
                int j = p_362284_.getSectionIndexFromSectionY(i);
                boolean flag = j >= 0 && j < alevelchunksection.length;
                DataLayer datalayer = levellightengine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkpos, i));
                DataLayer datalayer1 = levellightengine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkpos, i));
                DataLayer datalayer2 = datalayer != null && !datalayer.isEmpty() ? datalayer.copy() : null;
                DataLayer datalayer3 = datalayer1 != null && !datalayer1.isEmpty() ? datalayer1.copy() : null;
                if (flag || datalayer2 != null || datalayer3 != null) {
                    LevelChunkSection levelchunksection = flag ? alevelchunksection[j].copy() : null;
                    list.add(new SerializableChunkData.SectionData(i, levelchunksection, datalayer2, datalayer3));
                }
            }

            List<CompoundTag> list1 = new ArrayList<>(p_362284_.getBlockEntitiesPos().size());

            for (BlockPos blockpos : p_362284_.getBlockEntitiesPos()) {
                CompoundTag compoundtag = p_362284_.getBlockEntityNbtForSaving(blockpos, p_365319_.registryAccess());
                if (compoundtag != null) {
                    list1.add(compoundtag);
                }
            }

            List<CompoundTag> list2 = new ArrayList<>();
            long[] along = null;
            if (p_362284_.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
                ProtoChunk protochunk = (ProtoChunk)p_362284_;
                list2.addAll(protochunk.getEntities());
                CarvingMask carvingmask = protochunk.getCarvingMask();
                if (carvingmask != null) {
                    along = carvingmask.toArray();
                }
            }

            Map<Heightmap.Types, long[]> map = new EnumMap<>(Heightmap.Types.class);

            for (Entry<Heightmap.Types, Heightmap> entry : p_362284_.getHeightmaps()) {
                if (p_362284_.getPersistedStatus().heightmapsAfter().contains(entry.getKey())) {
                    long[] along1 = entry.getValue().getRawData();
                    map.put(entry.getKey(), (long[])along1.clone());
                }
            }

            ChunkAccess.PackedTicks chunkaccess$packedticks = p_362284_.getTicksForSerialization(p_365319_.getGameTime());
            ShortList[] ashortlist = Arrays.stream(p_362284_.getPostProcessing())
                .map(p_363794_ -> p_363794_ != null ? new ShortArrayList(p_363794_) : null)
                .toArray(ShortList[]::new);
            CompoundTag compoundtag1 = packStructureData(
                StructurePieceSerializationContext.fromLevel(p_365319_), chunkpos, p_362284_.getAllStarts(), p_362284_.getAllReferences()
            );
            return new SerializableChunkData(
                p_365319_.registryAccess().lookupOrThrow(Registries.BIOME),
                chunkpos,
                p_362284_.getMinSectionY(),
                p_365319_.getGameTime(),
                p_362284_.getInhabitedTime(),
                p_362284_.getPersistedStatus(),
                Optionull.map(p_362284_.getBlendingData(), BlendingData::pack),
                p_362284_.getBelowZeroRetrogen(),
                p_362284_.getUpgradeData().copy(),
                along,
                map,
                chunkaccess$packedticks,
                ashortlist,
                p_362284_.isLightCorrect(),
                list,
                list2,
                list1,
                compoundtag1
            );
        }
    }

    public CompoundTag write() {
        CompoundTag compoundtag = NbtUtils.addCurrentDataVersion(new CompoundTag());
        compoundtag.putString("xPos", this.chunkPos.x.toString());
        compoundtag.putInt("yPos", this.minSectionY);
        compoundtag.putString("zPos", this.chunkPos.z.toString());
        compoundtag.putLong("LastUpdate", this.lastUpdateTime);
        compoundtag.putLong("InhabitedTime", this.inhabitedTime);
        compoundtag.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(this.chunkStatus).toString());
        if (this.blendingData != null) {
            BlendingData.Packed.CODEC
                .encodeStart(NbtOps.INSTANCE, this.blendingData)
                .resultOrPartial(LOGGER::error)
                .ifPresent(p_360480_ -> compoundtag.put("blending_data", p_360480_));
        }

        if (this.belowZeroRetrogen != null) {
            BelowZeroRetrogen.CODEC
                .encodeStart(NbtOps.INSTANCE, this.belowZeroRetrogen)
                .resultOrPartial(LOGGER::error)
                .ifPresent(p_363007_ -> compoundtag.put("below_zero_retrogen", p_363007_));
        }

        if (!this.upgradeData.isEmpty()) {
            compoundtag.put("UpgradeData", this.upgradeData.write());
        }

        ListTag listtag = new ListTag();
        Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(this.biomeRegistry);

        for (SerializableChunkData.SectionData serializablechunkdata$sectiondata : this.sectionData) {
            CompoundTag compoundtag1 = new CompoundTag();
            LevelChunkSection levelchunksection = serializablechunkdata$sectiondata.chunkSection;
            if (levelchunksection != null) {
                compoundtag1.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, levelchunksection.getStates()).getOrThrow());
                compoundtag1.put("biomes", codec.encodeStart(NbtOps.INSTANCE, levelchunksection.getBiomes()).getOrThrow());
            }

            if (serializablechunkdata$sectiondata.blockLight != null) {
                compoundtag1.putByteArray("BlockLight", serializablechunkdata$sectiondata.blockLight.getData());
            }

            if (serializablechunkdata$sectiondata.skyLight != null) {
                compoundtag1.putByteArray("SkyLight", serializablechunkdata$sectiondata.skyLight.getData());
            }

            if (!compoundtag1.isEmpty()) {
                compoundtag1.putByte("Y", (byte)serializablechunkdata$sectiondata.y);
                listtag.add(compoundtag1);
            }
        }

        compoundtag.put("sections", listtag);
        if (this.lightCorrect) {
            compoundtag.putBoolean("isLightOn", true);
        }

        ListTag listtag1 = new ListTag();
        listtag1.addAll(this.blockEntities);
        compoundtag.put("block_entities", listtag1);
        if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
            ListTag listtag2 = new ListTag();
            listtag2.addAll(this.entities);
            compoundtag.put("entities", listtag2);
            if (this.carvingMask != null) {
                compoundtag.putLongArray("carving_mask", this.carvingMask);
            }
        }

        saveTicks(compoundtag, this.packedTicks);
        compoundtag.put("PostProcessing", packOffsets(this.postProcessingSections));
        CompoundTag compoundtag2 = new CompoundTag();
        this.heightmaps.forEach((p_362472_, p_363515_) -> compoundtag2.put(p_362472_.getSerializationKey(), new LongArrayTag(p_363515_)));
        compoundtag.put("Heightmaps", compoundtag2);
        compoundtag.put("structures", this.structureData);
        return compoundtag;
    }

    private static void saveTicks(CompoundTag p_362920_, ChunkAccess.PackedTicks p_361228_) {
        ListTag listtag = new ListTag();

        for (SavedTick<Block> savedtick : p_361228_.blocks()) {
            listtag.add(savedtick.save(p_362770_ -> BuiltInRegistries.BLOCK.getKey(p_362770_).toString()));
        }

        p_362920_.put("block_ticks", listtag);
        ListTag listtag1 = new ListTag();

        for (SavedTick<Fluid> savedtick1 : p_361228_.fluids()) {
            listtag1.add(savedtick1.save(p_362023_ -> BuiltInRegistries.FLUID.getKey(p_362023_).toString()));
        }

        p_362920_.put("fluid_ticks", listtag1);
    }

    public static ChunkType getChunkTypeFromTag(@Nullable CompoundTag p_364385_) {
        return p_364385_ != null ? ChunkStatus.byName(p_364385_.getString("Status")).getChunkType() : ChunkType.PROTOCHUNK;
    }

    @Nullable
    private static LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel p_363005_, List<CompoundTag> p_362405_, List<CompoundTag> p_364156_) {
        return p_362405_.isEmpty() && p_364156_.isEmpty() ? null : p_361360_ -> {
            if (!p_362405_.isEmpty()) {
                p_363005_.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(p_362405_, p_363005_, EntitySpawnReason.LOAD));
            }

            for (CompoundTag compoundtag : p_364156_) {
                boolean flag = compoundtag.getBoolean("keepPacked");
                if (flag) {
                    p_361360_.setBlockEntityNbt(compoundtag);
                } else {
                    BlockPos blockpos = BlockEntity.getPosFromTag(compoundtag);
                    BlockEntity blockentity = BlockEntity.loadStatic(blockpos, p_361360_.getBlockState(blockpos), compoundtag, p_363005_.registryAccess());
                    if (blockentity != null) {
                        p_361360_.setBlockEntity(blockentity);
                    }
                }
            }
        };
    }

    private static CompoundTag packStructureData(
        StructurePieceSerializationContext p_361063_, ChunkPos p_365377_, Map<Structure, StructureStart> p_365385_, Map<Structure, it.unimi.dsi.fastutil.objects.ObjectSet<ChunkPos>> p_362410_
    ) {
        CompoundTag compoundtag = new CompoundTag();
        CompoundTag compoundtag1 = new CompoundTag();
        Registry<Structure> registry = p_361063_.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        for (Entry<Structure, StructureStart> entry : p_365385_.entrySet()) {
            ResourceLocation resourcelocation = registry.getKey(entry.getKey());
            compoundtag1.put(resourcelocation.toString(), entry.getValue().createTag(p_361063_, p_365377_));
        }

        compoundtag.put("starts", compoundtag1);
        CompoundTag compoundtag2 = new CompoundTag();

        for (Entry<Structure, it.unimi.dsi.fastutil.objects.ObjectSet<ChunkPos>> entry1 : p_362410_.entrySet()) {
            if (!entry1.getValue().isEmpty()) {
                ResourceLocation resourcelocation1 = registry.getKey(entry1.getKey());
                var listTag = new net.minecraft.nbt.ListTag();
                entry1.getValue().forEach(pos -> {
                    var tag = new CompoundTag();
                    tag.putString("x", pos.x.toString());
                    tag.putString("z", pos.z.toString());
                    listTag.add(tag);
                });
                compoundtag2.put(resourcelocation1.toString(), listTag);
            }
        }

        compoundtag.put("References", compoundtag2);
        return compoundtag;
    }

    private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext p_360982_, CompoundTag p_363682_, long p_362695_) {
        Map<Structure, StructureStart> map = Maps.newHashMap();
        Registry<Structure> registry = p_360982_.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        CompoundTag compoundtag = p_363682_.getCompound("starts");

        for (String s : compoundtag.getAllKeys()) {
            ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
            Structure structure = registry.getValue(resourcelocation);
            if (structure == null) {
                LOGGER.error("Unknown structure start: {}", resourcelocation);
            } else {
                StructureStart structurestart = StructureStart.loadStaticStart(p_360982_, compoundtag.getCompound(s), p_362695_);
                if (structurestart != null) {
                    map.put(structure, structurestart);
                }
            }
        }

        return map;
    }

    private static Map<Structure, it.unimi.dsi.fastutil.objects.ObjectSet<ChunkPos>> unpackStructureReferences(RegistryAccess p_360856_, ChunkPos p_362116_, CompoundTag p_360575_) {
        Map<Structure, it.unimi.dsi.fastutil.objects.ObjectSet<ChunkPos>> map = Maps.newHashMap();
        Registry<Structure> registry = p_360856_.lookupOrThrow(Registries.STRUCTURE);
        CompoundTag compoundtag = p_360575_.getCompound("References");

        for (String s : compoundtag.getAllKeys()) {
            ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
            Structure structure = registry.getValue(resourcelocation);
            if (structure == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", resourcelocation, p_362116_);
            } else {
                net.minecraft.nbt.ListTag along = compoundtag.getList(s, Tag.TAG_COMPOUND);
                if (!along.isEmpty()) {
                    map.put(structure, new it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<>(along.stream().map(tag -> {
                        var tagCompound = (CompoundTag)tag;
                        return new ChunkPos(tagCompound.getInt("x"), tagCompound.getInt("z"));
                    }).filter(p_360765_ -> {
                        ChunkPos chunkpos = p_360765_;
                        if (chunkpos.getChessboardDistance(p_362116_) > 8) {
                            LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", resourcelocation, chunkpos, p_362116_);
                            return false;
                        } else {
                            return true;
                        }
                    }).toArray(ChunkPos[]::new)));
                }
            }
        }

        return map;
    }

    private static ListTag packOffsets(ShortList[] p_364852_) {
        ListTag listtag = new ListTag();

        for (ShortList shortlist : p_364852_) {
            ListTag listtag1 = new ListTag();
            if (shortlist != null) {
                for (int i = 0; i < shortlist.size(); i++) {
                    listtag1.add(ShortTag.valueOf(shortlist.getShort(i)));
                }
            }

            listtag.add(listtag1);
        }

        return listtag;
    }

    public static class ChunkReadException extends NbtException {
        public ChunkReadException(String p_361971_) {
            super(p_361971_);
        }
    }

    public static record SectionData(int y, @Nullable LevelChunkSection chunkSection, @Nullable DataLayer blockLight, @Nullable DataLayer skyLight) {
    }
}
