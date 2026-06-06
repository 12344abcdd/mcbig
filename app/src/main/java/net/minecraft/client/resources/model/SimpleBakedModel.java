package net.minecraft.client.resources.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleBakedModel implements BakedModel {
    public static final String PARTICLE_TEXTURE_REFERENCE = "particle";
    private final List<BakedQuad> unculledFaces;
    private final Map<Direction, List<BakedQuad>> culledFaces;
    private final boolean hasAmbientOcclusion;
    private final boolean isGui3d;
    private final boolean usesBlockLight;
    private final TextureAtlasSprite particleIcon;
    private final ItemTransforms transforms;

    public SimpleBakedModel(
        List<BakedQuad> p_119489_,
        Map<Direction, List<BakedQuad>> p_119490_,
        boolean p_119491_,
        boolean p_119492_,
        boolean p_119493_,
        TextureAtlasSprite p_119494_,
        ItemTransforms p_119495_
    ) {
        this.unculledFaces = p_119489_;
        this.culledFaces = p_119490_;
        this.hasAmbientOcclusion = p_119491_;
        this.isGui3d = p_119493_;
        this.usesBlockLight = p_119492_;
        this.particleIcon = p_119494_;
        this.transforms = p_119495_;
    }

    public static BakedModel bakeElements(
        List<BlockElement> p_387963_,
        TextureSlots p_388507_,
        SpriteGetter p_387357_,
        ModelState p_388846_,
        boolean p_386975_,
        boolean p_388143_,
        boolean p_386706_,
        ItemTransforms p_388032_
    ) {
        TextureAtlasSprite textureatlassprite = findSprite(p_387357_, p_388507_, "particle");
        SimpleBakedModel.Builder simplebakedmodel$builder = new SimpleBakedModel.Builder(p_386975_, p_388143_, p_386706_, p_388032_)
            .particle(textureatlassprite);

        for (BlockElement blockelement : p_387963_) {
            for (Direction direction : blockelement.faces.keySet()) {
                BlockElementFace blockelementface = blockelement.faces.get(direction);
                TextureAtlasSprite textureatlassprite1 = findSprite(p_387357_, p_388507_, blockelementface.texture());
                if (blockelementface.cullForDirection() == null) {
                    simplebakedmodel$builder.addUnculledFace(bakeFace(blockelement, blockelementface, textureatlassprite1, direction, p_388846_));
                } else {
                    simplebakedmodel$builder.addCulledFace(
                        Direction.rotate(p_388846_.getRotation().getMatrix(), blockelementface.cullForDirection()),
                        bakeFace(blockelement, blockelementface, textureatlassprite1, direction, p_388846_)
                    );
                }
            }
        }

        return simplebakedmodel$builder.build();
    }

    private static BakedQuad bakeFace(
        BlockElement p_388164_, BlockElementFace p_388456_, TextureAtlasSprite p_388731_, Direction p_386700_, ModelState p_386475_
    ) {
        return FaceBakery.bakeQuad(
            p_388164_.from, p_388164_.to, p_388456_, p_388731_, p_386700_, p_386475_, p_388164_.rotation, p_388164_.shade, p_388164_.lightEmission
        );
    }

    private static TextureAtlasSprite findSprite(SpriteGetter p_386599_, TextureSlots p_388750_, String p_386831_) {
        Material material = p_388750_.getMaterial(p_386831_);
        return material != null ? p_386599_.get(material) : p_386599_.reportMissingReference(p_386831_);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState p_235054_, @Nullable Direction p_235055_, RandomSource p_235056_) {
        return p_235055_ == null ? this.unculledFaces : this.culledFaces.get(p_235055_);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.hasAmbientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return this.isGui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return this.usesBlockLight;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.particleIcon;
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.transforms;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final ImmutableList.Builder<BakedQuad> unculledFaces = ImmutableList.builder();
        private final EnumMap<Direction, ImmutableList.Builder<BakedQuad>> culledFaces = Maps.newEnumMap(Direction.class);
        private final boolean hasAmbientOcclusion;
        @Nullable
        private TextureAtlasSprite particleIcon;
        private final boolean usesBlockLight;
        private final boolean isGui3d;
        private final ItemTransforms transforms;

        public Builder(boolean p_119519_, boolean p_371851_, boolean p_371825_, ItemTransforms p_371619_) {
            this.hasAmbientOcclusion = p_119519_;
            this.usesBlockLight = p_371851_;
            this.isGui3d = p_371825_;
            this.transforms = p_371619_;

            for (Direction direction : Direction.values()) {
                this.culledFaces.put(direction, ImmutableList.builder());
            }
        }

        public SimpleBakedModel.Builder addCulledFace(Direction p_119531_, BakedQuad p_119532_) {
            this.culledFaces.get(p_119531_).add(p_119532_);
            return this;
        }

        public SimpleBakedModel.Builder addUnculledFace(BakedQuad p_119527_) {
            this.unculledFaces.add(p_119527_);
            return this;
        }

        public SimpleBakedModel.Builder particle(TextureAtlasSprite p_119529_) {
            this.particleIcon = p_119529_;
            return this;
        }

        public SimpleBakedModel.Builder item() {
            return this;
        }

        public BakedModel build() {
            if (this.particleIcon == null) {
                throw new RuntimeException("Missing particle!");
            } else {
                Map<Direction, List<BakedQuad>> map = Maps.transformValues(this.culledFaces, ImmutableList.Builder::build);
                return new SimpleBakedModel(
                    this.unculledFaces.build(),
                    new EnumMap<>(map),
                    this.hasAmbientOcclusion,
                    this.usesBlockLight,
                    this.isGui3d,
                    this.particleIcon,
                    this.transforms
                );
            }
        }
    }
}
