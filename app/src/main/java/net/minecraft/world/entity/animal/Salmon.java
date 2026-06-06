package net.minecraft.world.entity.animal;

import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class Salmon extends AbstractSchoolingFish implements VariantHolder<Salmon.Variant> {
    private static final String TAG_TYPE = "type";
    private static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(Salmon.class, EntityDataSerializers.INT);

    public Salmon(EntityType<? extends Salmon> p_29790_, Level p_29791_) {
        super(p_29790_, p_29791_);
        this.refreshDimensions();
    }

    @Override
    public int getMaxSchoolSize() {
        return 5;
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.SALMON_BUCKET);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SALMON_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SALMON_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_29795_) {
        return SoundEvents.SALMON_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.SALMON_FLOP;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_360913_) {
        super.defineSynchedData(p_360913_);
        p_360913_.define(DATA_TYPE, Salmon.Variant.MEDIUM.id());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_368738_) {
        super.onSyncedDataUpdated(p_368738_);
        if (DATA_TYPE.equals(p_368738_)) {
            this.refreshDimensions();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_365152_) {
        super.addAdditionalSaveData(p_365152_);
        p_365152_.putString("type", this.getVariant().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_360554_) {
        super.readAdditionalSaveData(p_360554_);
        this.setVariant(Salmon.Variant.byName(p_360554_.getString("type")));
    }

    @Override
    public void saveToBucketTag(ItemStack p_368592_) {
        Bucketable.saveDefaultDataToBucketTag(this, p_368592_);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, p_368592_, p_368755_ -> p_368755_.putString("type", this.getVariant().getSerializedName()));
    }

    @Override
    public void loadFromBucketTag(CompoundTag p_368552_) {
        Bucketable.loadDefaultDataFromBucketTag(this, p_368552_);
        this.setVariant(Salmon.Variant.byName(p_368552_.getString("type")));
    }

    public void setVariant(Salmon.Variant p_364663_) {
        this.entityData.set(DATA_TYPE, p_364663_.id);
    }

    public Salmon.Variant getVariant() {
        return Salmon.Variant.BY_ID.apply(this.entityData.get(DATA_TYPE));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor p_360331_, DifficultyInstance p_360341_, EntitySpawnReason p_362539_, @Nullable SpawnGroupData p_361173_
    ) {
        SimpleWeightedRandomList.Builder<Salmon.Variant> builder = SimpleWeightedRandomList.builder();
        builder.add(Salmon.Variant.SMALL, 30);
        builder.add(Salmon.Variant.MEDIUM, 50);
        builder.add(Salmon.Variant.LARGE, 15);
        builder.build().getRandomValue(this.random).ifPresent(this::setVariant);
        return super.finalizeSpawn(p_360331_, p_360341_, p_362539_, p_361173_);
    }

    public float getSalmonScale() {
        return this.getVariant().boundingBoxScale;
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose p_368711_) {
        return super.getDefaultDimensions(p_368711_).scale(this.getSalmonScale());
    }

    public static enum Variant implements StringRepresentable {
        SMALL("small", 0, 0.5F),
        MEDIUM("medium", 1, 1.0F),
        LARGE("large", 2, 1.5F);

        public static final StringRepresentable.EnumCodec<Salmon.Variant> CODEC = StringRepresentable.fromEnum(Salmon.Variant::values);
        static final IntFunction<Salmon.Variant> BY_ID = ByIdMap.continuous(Salmon.Variant::id, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        private final String name;
        final int id;
        final float boundingBoxScale;

        private Variant(String p_360859_, int p_382959_, float p_368635_) {
            this.name = p_360859_;
            this.id = p_382959_;
            this.boundingBoxScale = p_368635_;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        int id() {
            return this.id;
        }

        static Salmon.Variant byName(String p_361165_) {
            return CODEC.byName(p_361165_, MEDIUM);
        }
    }
}
