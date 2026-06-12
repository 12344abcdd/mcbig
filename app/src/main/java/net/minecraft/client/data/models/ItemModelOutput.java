package net.minecraft.client.data.models;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.world.item.Item;

public interface ItemModelOutput {
    void accept(Item p_387543_, ItemModel.Unbaked p_386880_);

    void copy(Item p_387316_, Item p_387995_);
}
