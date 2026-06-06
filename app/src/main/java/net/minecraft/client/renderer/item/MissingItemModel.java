package net.minecraft.client.renderer.item;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MissingItemModel implements ItemModel {
    private final BakedModel model;

    public MissingItemModel(BakedModel p_387829_) {
        this.model = p_387829_;
    }

    @Override
    public void update(
        ItemStackRenderState p_386627_,
        ItemStack p_388292_,
        ItemModelResolver p_388302_,
        ItemDisplayContext p_388518_,
        @Nullable ClientLevel p_387367_,
        @Nullable LivingEntity p_388182_,
        int p_388913_
    ) {
        p_386627_.newLayer().setupBlockModel(this.model, Sheets.cutoutBlockSheet());
    }
}
