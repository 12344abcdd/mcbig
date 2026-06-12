package net.minecraft.client.resources.model;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.world.item.ItemDisplayContext;

public interface UnbakedModel extends ResolvableModel {
    boolean DEFAULT_AMBIENT_OCCLUSION = true;
    UnbakedModel.GuiLight DEFAULT_GUI_LIGHT = UnbakedModel.GuiLight.SIDE;

    BakedModel bake(TextureSlots p_386641_, ModelBaker p_250133_, ModelState p_119536_, boolean p_387129_, boolean p_388638_, ItemTransforms p_386911_);

    @Nullable
    default Boolean getAmbientOcclusion() {
        return null;
    }

    @Nullable
    default UnbakedModel.GuiLight getGuiLight() {
        return null;
    }

    @Nullable
    default ItemTransforms getTransforms() {
        return null;
    }

    default TextureSlots.Data getTextureSlots() {
        return TextureSlots.Data.EMPTY;
    }

    @Nullable
    default UnbakedModel getParent() {
        return null;
    }

    static BakedModel bakeWithTopModelValues(UnbakedModel p_388418_, ModelBaker p_388525_, ModelState p_386740_) {
        TextureSlots textureslots = getTopTextureSlots(p_388418_, p_388525_.rootName());
        boolean flag = getTopAmbientOcclusion(p_388418_);
        boolean flag1 = getTopGuiLight(p_388418_).lightLikeBlock();
        ItemTransforms itemtransforms = getTopTransforms(p_388418_);
        return p_388418_.bake(textureslots, p_388525_, p_386740_, flag, flag1, itemtransforms);
    }

    static TextureSlots getTopTextureSlots(UnbakedModel p_387784_, ModelDebugName p_388419_) {
        TextureSlots.Resolver textureslots$resolver = new TextureSlots.Resolver();

        while (p_387784_ != null) {
            textureslots$resolver.addLast(p_387784_.getTextureSlots());
            p_387784_ = p_387784_.getParent();
        }

        return textureslots$resolver.resolve(p_388419_);
    }

    static boolean getTopAmbientOcclusion(UnbakedModel p_387023_) {
        while (p_387023_ != null) {
            Boolean obool = p_387023_.getAmbientOcclusion();
            if (obool != null) {
                return obool;
            }

            p_387023_ = p_387023_.getParent();
        }

        return true;
    }

    static UnbakedModel.GuiLight getTopGuiLight(UnbakedModel p_387074_) {
        while (p_387074_ != null) {
            UnbakedModel.GuiLight unbakedmodel$guilight = p_387074_.getGuiLight();
            if (unbakedmodel$guilight != null) {
                return unbakedmodel$guilight;
            }

            p_387074_ = p_387074_.getParent();
        }

        return DEFAULT_GUI_LIGHT;
    }

    static ItemTransform getTopTransform(UnbakedModel p_386817_, ItemDisplayContext p_388109_) {
        while (p_386817_ != null) {
            ItemTransforms itemtransforms = p_386817_.getTransforms();
            if (itemtransforms != null) {
                ItemTransform itemtransform = itemtransforms.getTransform(p_388109_);
                if (itemtransform != ItemTransform.NO_TRANSFORM) {
                    return itemtransform;
                }
            }

            p_386817_ = p_386817_.getParent();
        }

        return ItemTransform.NO_TRANSFORM;
    }

    static ItemTransforms getTopTransforms(UnbakedModel p_388720_) {
        ItemTransform itemtransform = getTopTransform(p_388720_, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        ItemTransform itemtransform1 = getTopTransform(p_388720_, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        ItemTransform itemtransform2 = getTopTransform(p_388720_, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        ItemTransform itemtransform3 = getTopTransform(p_388720_, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        ItemTransform itemtransform4 = getTopTransform(p_388720_, ItemDisplayContext.HEAD);
        ItemTransform itemtransform5 = getTopTransform(p_388720_, ItemDisplayContext.GUI);
        ItemTransform itemtransform6 = getTopTransform(p_388720_, ItemDisplayContext.GROUND);
        ItemTransform itemtransform7 = getTopTransform(p_388720_, ItemDisplayContext.FIXED);
        return new ItemTransforms(itemtransform, itemtransform1, itemtransform2, itemtransform3, itemtransform4, itemtransform5, itemtransform6, itemtransform7);
    }

    public static enum GuiLight {
        FRONT("front"),
        SIDE("side");

        private final String name;

        private GuiLight(String p_387953_) {
            this.name = p_387953_;
        }

        public static UnbakedModel.GuiLight getByName(String p_388510_) {
            for (UnbakedModel.GuiLight unbakedmodel$guilight : values()) {
                if (unbakedmodel$guilight.name.equals(p_388510_)) {
                    return unbakedmodel$guilight;
                }
            }

            throw new IllegalArgumentException("Invalid gui light: " + p_388510_);
        }

        public boolean lightLikeBlock() {
            return this == SIDE;
        }
    }
}
