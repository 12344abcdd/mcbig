package net.minecraft.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;

public class ItemStackRenderState {
    ItemDisplayContext displayContext = ItemDisplayContext.NONE;
    boolean isLeftHand;
    private int activeLayerCount;
    private ItemStackRenderState.LayerRenderState[] layers = new ItemStackRenderState.LayerRenderState[]{new ItemStackRenderState.LayerRenderState()};

    public void ensureCapacity(int p_387339_) {
        int i = this.layers.length;
        int j = this.activeLayerCount + p_387339_;
        if (j > i) {
            this.layers = Arrays.copyOf(this.layers, j);

            for (int k = i; k < j; k++) {
                this.layers[k] = new ItemStackRenderState.LayerRenderState();
            }
        }
    }

    public ItemStackRenderState.LayerRenderState newLayer() {
        this.ensureCapacity(1);
        return this.layers[this.activeLayerCount++];
    }

    public void clear() {
        this.displayContext = ItemDisplayContext.NONE;
        this.isLeftHand = false;

        for (int i = 0; i < this.activeLayerCount; i++) {
            this.layers[i].clear();
        }

        this.activeLayerCount = 0;
    }

    private ItemStackRenderState.LayerRenderState firstLayer() {
        return this.layers[0];
    }

    public boolean isEmpty() {
        return this.activeLayerCount == 0;
    }

    public boolean isGui3d() {
        return this.firstLayer().isGui3d();
    }

    public boolean usesBlockLight() {
        return this.firstLayer().usesBlockLight();
    }

    @Nullable
    public TextureAtlasSprite pickParticleIcon(RandomSource p_387539_) {
        if (this.activeLayerCount == 0) {
            return null;
        } else {
            BakedModel bakedmodel = this.layers[p_387539_.nextInt(this.activeLayerCount)].model;
            return bakedmodel == null ? null : bakedmodel.getParticleIcon();
        }
    }

    public ItemTransform transform() {
        return this.firstLayer().transform();
    }

    public void render(PoseStack p_388193_, MultiBufferSource p_388719_, int p_386913_, int p_387272_) {
        for (int i = 0; i < this.activeLayerCount; i++) {
            this.layers[i].render(p_388193_, p_388719_, p_386913_, p_387272_);
        }
    }

    public static enum FoilType {
        NONE,
        STANDARD,
        SPECIAL;
    }

    public class LayerRenderState {
        @Nullable
        BakedModel model;
        @Nullable
        private RenderType renderType;
        private ItemStackRenderState.FoilType foilType = ItemStackRenderState.FoilType.NONE;
        private int[] tintLayers = new int[0];
        @Nullable
        private SpecialModelRenderer<Object> specialRenderer;
        @Nullable
        private Object argumentForSpecialRendering;

        public void clear() {
            this.model = null;
            this.renderType = null;
            this.foilType = ItemStackRenderState.FoilType.NONE;
            this.specialRenderer = null;
            this.argumentForSpecialRendering = null;
            Arrays.fill(this.tintLayers, -1);
        }

        public void setupBlockModel(BakedModel p_386829_, RenderType p_387529_) {
            this.model = p_386829_;
            this.renderType = p_387529_;
        }

        public <T> void setupSpecialModel(SpecialModelRenderer<T> p_386884_, @Nullable T p_388093_, BakedModel p_387112_) {
            this.model = p_387112_;
            this.specialRenderer = eraseSpecialRenderer(p_386884_);
            this.argumentForSpecialRendering = p_388093_;
        }

        private static SpecialModelRenderer<Object> eraseSpecialRenderer(SpecialModelRenderer<?> p_388852_) {
            return (SpecialModelRenderer<Object>)p_388852_;
        }

        public void setFoilType(ItemStackRenderState.FoilType p_386511_) {
            this.foilType = p_386511_;
        }

        public int[] prepareTintLayers(int p_387793_) {
            if (p_387793_ > this.tintLayers.length) {
                this.tintLayers = new int[p_387793_];
                Arrays.fill(this.tintLayers, -1);
            }

            return this.tintLayers;
        }

        ItemTransform transform() {
            return this.model != null ? this.model.getTransforms().getTransform(ItemStackRenderState.this.displayContext) : ItemTransform.NO_TRANSFORM;
        }

        void render(PoseStack p_387607_, MultiBufferSource p_386763_, int p_387589_, int p_388775_) {
            p_387607_.pushPose();
            this.transform().apply(ItemStackRenderState.this.isLeftHand, p_387607_);
            p_387607_.translate(-0.5F, -0.5F, -0.5F);
            if (this.specialRenderer != null) {
                this.specialRenderer
                    .render(
                        this.argumentForSpecialRendering,
                        ItemStackRenderState.this.displayContext,
                        p_387607_,
                        p_386763_,
                        p_387589_,
                        p_388775_,
                        this.foilType != ItemStackRenderState.FoilType.NONE
                    );
            } else if (this.model != null) {
                ItemRenderer.renderItem(
                    ItemStackRenderState.this.displayContext,
                    p_387607_,
                    p_386763_,
                    p_387589_,
                    p_388775_,
                    this.tintLayers,
                    this.model,
                    this.renderType,
                    this.foilType
                );
            }

            p_387607_.popPose();
        }

        boolean isGui3d() {
            return this.model != null && this.model.isGui3d();
        }

        boolean usesBlockLight() {
            return this.model != null && this.model.usesBlockLight();
        }
    }
}
