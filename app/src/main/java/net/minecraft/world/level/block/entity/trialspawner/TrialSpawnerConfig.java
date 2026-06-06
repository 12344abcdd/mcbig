package net.minecraft.world.level.block.entity.trialspawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public record TrialSpawnerConfig(
    int spawnRange,
    float totalMobs,
    float simultaneousMobs,
    float totalMobsAddedPerPlayer,
    float simultaneousMobsAddedPerPlayer,
    int ticksBetweenSpawn,
    SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition,
    SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject,
    ResourceKey<LootTable> itemsToDropWhenOminous
) {
    public static final TrialSpawnerConfig DEFAULT = builder().build();
    public static final Codec<TrialSpawnerConfig> DIRECT_CODEC = RecordCodecBuilder.create(
        p_338041_ -> p_338041_.group(
                    Codec.intRange(1, 128).optionalFieldOf("spawn_range", DEFAULT.spawnRange).forGetter(TrialSpawnerConfig::spawnRange),
                    Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("total_mobs", DEFAULT.totalMobs).forGetter(TrialSpawnerConfig::totalMobs),
                    Codec.floatRange(0.0F, Float.MAX_VALUE)
                        .optionalFieldOf("simultaneous_mobs", DEFAULT.simultaneousMobs)
                        .forGetter(TrialSpawnerConfig::simultaneousMobs),
                    Codec.floatRange(0.0F, Float.MAX_VALUE)
                        .optionalFieldOf("total_mobs_added_per_player", DEFAULT.totalMobsAddedPerPlayer)
                        .forGetter(TrialSpawnerConfig::totalMobsAddedPerPlayer),
                    Codec.floatRange(0.0F, Float.MAX_VALUE)
                        .optionalFieldOf("simultaneous_mobs_added_per_player", DEFAULT.simultaneousMobsAddedPerPlayer)
                        .forGetter(TrialSpawnerConfig::simultaneousMobsAddedPerPlayer),
                    Codec.intRange(0, Integer.MAX_VALUE)
                        .optionalFieldOf("ticks_between_spawn", DEFAULT.ticksBetweenSpawn)
                        .forGetter(TrialSpawnerConfig::ticksBetweenSpawn),
                    SpawnData.LIST_CODEC
                        .optionalFieldOf("spawn_potentials", SimpleWeightedRandomList.empty())
                        .forGetter(TrialSpawnerConfig::spawnPotentialsDefinition),
                    SimpleWeightedRandomList.wrappedCodecAllowingEmpty(ResourceKey.codec(Registries.LOOT_TABLE))
                        .optionalFieldOf("loot_tables_to_eject", DEFAULT.lootTablesToEject)
                        .forGetter(TrialSpawnerConfig::lootTablesToEject),
                    ResourceKey.codec(Registries.LOOT_TABLE)
                        .optionalFieldOf("items_to_drop_when_ominous", DEFAULT.itemsToDropWhenOminous)
                        .forGetter(TrialSpawnerConfig::itemsToDropWhenOminous)
                )
                .apply(p_338041_, TrialSpawnerConfig::new)
    );
    public static final Codec<Holder<TrialSpawnerConfig>> CODEC = RegistryFileCodec.create(Registries.TRIAL_SPAWNER_CONFIG, DIRECT_CODEC);

    public int calculateTargetTotalMobs(int p_312026_) {
        return (int)Math.floor((double)(this.totalMobs + this.totalMobsAddedPerPlayer * (float)p_312026_));
    }

    public int calculateTargetSimultaneousMobs(int p_312885_) {
        return (int)Math.floor((double)(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * (float)p_312885_));
    }

    public long ticksBetweenItemSpawners() {
        return 160L;
    }

    public static TrialSpawnerConfig.Builder builder() {
        return new TrialSpawnerConfig.Builder();
    }

    public TrialSpawnerConfig withSpawning(EntityType<?> p_388049_) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(p_388049_).toString());
        SpawnData spawndata = new SpawnData(compoundtag, Optional.empty(), Optional.empty());
        return new TrialSpawnerConfig(
            this.spawnRange,
            this.totalMobs,
            this.simultaneousMobs,
            this.totalMobsAddedPerPlayer,
            this.simultaneousMobsAddedPerPlayer,
            this.ticksBetweenSpawn,
            SimpleWeightedRandomList.single(spawndata),
            this.lootTablesToEject,
            this.itemsToDropWhenOminous
        );
    }

    public static class Builder {
        private int spawnRange = 4;
        private float totalMobs = 6.0F;
        private float simultaneousMobs = 2.0F;
        private float totalMobsAddedPerPlayer = 2.0F;
        private float simultaneousMobsAddedPerPlayer = 1.0F;
        private int ticksBetweenSpawn = 40;
        private SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition = SimpleWeightedRandomList.empty();
        private SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject = SimpleWeightedRandomList.<ResourceKey<LootTable>>builder()
            .add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_CONSUMABLES)
            .add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_KEY)
            .build();
        private ResourceKey<LootTable> itemsToDropWhenOminous = BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS;

        public TrialSpawnerConfig.Builder spawnRange(int p_368590_) {
            this.spawnRange = p_368590_;
            return this;
        }

        public TrialSpawnerConfig.Builder totalMobs(float p_368764_) {
            this.totalMobs = p_368764_;
            return this;
        }

        public TrialSpawnerConfig.Builder simultaneousMobs(float p_368497_) {
            this.simultaneousMobs = p_368497_;
            return this;
        }

        public TrialSpawnerConfig.Builder totalMobsAddedPerPlayer(float p_368619_) {
            this.totalMobsAddedPerPlayer = p_368619_;
            return this;
        }

        public TrialSpawnerConfig.Builder simultaneousMobsAddedPerPlayer(float p_368672_) {
            this.simultaneousMobsAddedPerPlayer = p_368672_;
            return this;
        }

        public TrialSpawnerConfig.Builder ticksBetweenSpawn(int p_368690_) {
            this.ticksBetweenSpawn = p_368690_;
            return this;
        }

        public TrialSpawnerConfig.Builder spawnPotentialsDefinition(SimpleWeightedRandomList<SpawnData> p_368657_) {
            this.spawnPotentialsDefinition = p_368657_;
            return this;
        }

        public TrialSpawnerConfig.Builder lootTablesToEject(SimpleWeightedRandomList<ResourceKey<LootTable>> p_368643_) {
            this.lootTablesToEject = p_368643_;
            return this;
        }

        public TrialSpawnerConfig.Builder itemsToDropWhenOminous(ResourceKey<LootTable> p_368515_) {
            this.itemsToDropWhenOminous = p_368515_;
            return this;
        }

        public TrialSpawnerConfig build() {
            return new TrialSpawnerConfig(
                this.spawnRange,
                this.totalMobs,
                this.simultaneousMobs,
                this.totalMobsAddedPerPlayer,
                this.simultaneousMobsAddedPerPlayer,
                this.ticksBetweenSpawn,
                this.spawnPotentialsDefinition,
                this.lootTablesToEject,
                this.itemsToDropWhenOminous
            );
        }
    }
}
