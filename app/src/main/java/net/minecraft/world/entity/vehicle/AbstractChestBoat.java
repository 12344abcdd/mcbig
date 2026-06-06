package net.minecraft.world.entity.vehicle;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTable;

public abstract class AbstractChestBoat extends AbstractBoat implements HasCustomInventoryScreen, ContainerEntity {
    private static final int CONTAINER_SIZE = 27;
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    public AbstractChestBoat(EntityType<? extends AbstractChestBoat> p_376778_, Level p_376182_, Supplier<Item> p_376195_) {
        super(p_376778_, p_376182_, p_376195_);
    }

    @Override
    protected float getSinglePassengerXOffset() {
        return 0.15F;
    }

    @Override
    protected int getMaxPassengers() {
        return 1;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_376826_) {
        super.addAdditionalSaveData(p_376826_);
        this.addChestVehicleSaveData(p_376826_, this.registryAccess());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_376621_) {
        super.readAdditionalSaveData(p_376621_);
        this.readChestVehicleSaveData(p_376621_, this.registryAccess());
    }

    @Override
    public void destroy(ServerLevel p_376333_, DamageSource p_376433_) {
        this.destroy(p_376333_, this.getDropItem());
        this.chestVehicleDestroyed(p_376433_, p_376333_, this);
    }

    @Override
    public void remove(Entity.RemovalReason p_376764_) {
        if (!this.level().isClientSide && p_376764_.shouldDestroy()) {
            Containers.dropContents(this.level(), this, this);
        }

        super.remove(p_376764_);
    }

    @Override
    public InteractionResult interact(Player p_376860_, InteractionHand p_376424_) {
        if (!p_376860_.isSecondaryUseActive()) {
            InteractionResult interactionresult = super.interact(p_376860_, p_376424_);
            if (interactionresult != InteractionResult.PASS) {
                return interactionresult;
            }
        }

        if (this.canAddPassenger(p_376860_) && !p_376860_.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        } else {
            InteractionResult interactionresult1 = this.interactWithContainerVehicle(p_376860_);
            if (interactionresult1.consumesAction() && p_376860_.level() instanceof ServerLevel serverlevel) {
                this.gameEvent(GameEvent.CONTAINER_OPEN, p_376860_);
                PiglinAi.angerNearbyPiglins(serverlevel, p_376860_, true);
            }

            return interactionresult1;
        }
    }

    @Override
    public void openCustomInventoryScreen(Player p_376437_) {
        p_376437_.openMenu(this);
        if (p_376437_.level() instanceof ServerLevel serverlevel) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, p_376437_);
            PiglinAi.angerNearbyPiglins(serverlevel, p_376437_, true);
        }
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public ItemStack getItem(int p_376545_) {
        return this.getChestVehicleItem(p_376545_);
    }

    @Override
    public ItemStack removeItem(int p_376479_, int p_376802_) {
        return this.removeChestVehicleItem(p_376479_, p_376802_);
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_376287_) {
        return this.removeChestVehicleItemNoUpdate(p_376287_);
    }

    @Override
    public void setItem(int p_376127_, ItemStack p_376526_) {
        this.setChestVehicleItem(p_376127_, p_376526_);
    }

    @Override
    public SlotAccess getSlot(int p_376264_) {
        return this.getChestVehicleSlot(p_376264_);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player p_376726_) {
        return this.isChestVehicleStillValid(p_376726_);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_376862_, Inventory p_376651_, Player p_376108_) {
        if (this.lootTable != null && p_376108_.isSpectator()) {
            return null;
        } else {
            this.unpackLootTable(p_376651_.player);
            return ChestMenu.threeRows(p_376862_, p_376651_, this);
        }
    }

    public void unpackLootTable(@Nullable Player p_376893_) {
        this.unpackChestVehicleLootTable(p_376893_);
    }

    @Nullable
    @Override
    public ResourceKey<LootTable> getContainerLootTable() {
        return this.lootTable;
    }

    @Override
    public void setContainerLootTable(@Nullable ResourceKey<LootTable> p_376327_) {
        this.lootTable = p_376327_;
    }

    @Override
    public long getContainerLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setContainerLootTableSeed(long p_376440_) {
        this.lootTableSeed = p_376440_;
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    @Override
    public void clearItemStacks() {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public void stopOpen(Player p_376820_) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.bigPosition(), GameEvent.Context.of(p_376820_));
    }
}
