package net.minecraft.client.renderer.item;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemModelResolver {
    private final Function<ResourceLocation, ItemModel> modelGetter;
    private final Function<ResourceLocation, ClientItem.Properties> clientProperties;

    public ItemModelResolver(ModelManager p_387246_) {
        this.modelGetter = p_387246_::getItemModel;
        this.clientProperties = p_387246_::getItemProperties;
    }

    public void updateForLiving(ItemStackRenderState p_387100_, ItemStack p_387635_, ItemDisplayContext p_388107_, boolean p_387193_, LivingEntity p_388201_) {
        this.updateForTopItem(p_387100_, p_387635_, p_388107_, p_387193_, p_388201_.level(), p_388201_, p_388201_.getId() + p_388107_.ordinal());
    }

    public void updateForNonLiving(ItemStackRenderState p_386914_, ItemStack p_388286_, ItemDisplayContext p_387479_, Entity p_386766_) {
        this.updateForTopItem(p_386914_, p_388286_, p_387479_, false, p_386766_.level(), null, p_386766_.getId());
    }

    public void updateForTopItem(
        ItemStackRenderState p_387014_,
        ItemStack p_388693_,
        ItemDisplayContext p_388835_,
        boolean p_387327_,
        @Nullable Level p_388064_,
        @Nullable LivingEntity p_388047_,
        int p_388137_
    ) {
        p_387014_.clear();
        if (!p_388693_.isEmpty()) {
            p_387014_.displayContext = p_388835_;
            p_387014_.isLeftHand = p_387327_;
            this.appendItemLayers(p_387014_, p_388693_, p_388835_, p_388064_, p_388047_, p_388137_);
        }
    }

    private static void fixupSkullProfile(ItemStack p_387791_) {
        if (p_387791_.getItem() instanceof BlockItem blockitem && blockitem.getBlock() instanceof AbstractSkullBlock) {
            ResolvableProfile resolvableprofile = p_387791_.get(DataComponents.PROFILE);
            if (resolvableprofile != null && !resolvableprofile.isResolved()) {
                p_387791_.remove(DataComponents.PROFILE);
                resolvableprofile.resolve().thenAcceptAsync(p_386927_ -> p_387791_.set(DataComponents.PROFILE, p_386927_), Minecraft.getInstance());
            }
        }
    }

    public void appendItemLayers(
        ItemStackRenderState p_387962_,
        ItemStack p_388009_,
        ItemDisplayContext p_387722_,
        @Nullable Level p_388449_,
        @Nullable LivingEntity p_388907_,
        int p_388206_
    ) {
        fixupSkullProfile(p_388009_);
        ResourceLocation resourcelocation = p_388009_.get(DataComponents.ITEM_MODEL);
        if (resourcelocation != null) {
            this.modelGetter
                .apply(resourcelocation)
                .update(p_387962_, p_388009_, this, p_387722_, p_388449_ instanceof ClientLevel clientlevel ? clientlevel : null, p_388907_, p_388206_);
        }
    }

    public boolean shouldPlaySwapAnimation(ItemStack p_390529_) {
        ResourceLocation resourcelocation = p_390529_.get(DataComponents.ITEM_MODEL);
        return resourcelocation == null ? true : this.clientProperties.apply(resourcelocation).handAnimationOnSwap();
    }
}
