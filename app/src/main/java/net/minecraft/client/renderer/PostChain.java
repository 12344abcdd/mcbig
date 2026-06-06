package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class PostChain {
    public static final ResourceLocation MAIN_TARGET_ID = ResourceLocation.withDefaultNamespace("main");
    private final List<PostPass> passes;
    private final Map<ResourceLocation, PostChainConfig.InternalTarget> internalTargets;
    private final Set<ResourceLocation> externalTargets;

    private PostChain(List<PostPass> p_364536_, Map<ResourceLocation, PostChainConfig.InternalTarget> p_361479_, Set<ResourceLocation> p_361607_) {
        this.passes = p_364536_;
        this.internalTargets = p_361479_;
        this.externalTargets = p_361607_;
    }

    public static PostChain load(PostChainConfig p_366777_, TextureManager p_110034_, ShaderManager p_366532_, Set<ResourceLocation> p_362436_) throws ShaderManager.CompilationException {
        Stream<ResourceLocation> stream = p_366777_.passes()
            .stream()
            .flatMap(p_359203_ -> p_359203_.inputs().stream())
            .flatMap(p_359200_ -> p_359200_.referencedTargets().stream());
        Set<ResourceLocation> set = stream.filter(p_359202_ -> !p_366777_.internalTargets().containsKey(p_359202_)).collect(Collectors.toSet());
        Set<ResourceLocation> set1 = Sets.difference(set, p_362436_);
        if (!set1.isEmpty()) {
            throw new ShaderManager.CompilationException("Referenced external targets are not available in this context: " + set1);
        } else {
            Builder<PostPass> builder = ImmutableList.builder();

            for (PostChainConfig.Pass postchainconfig$pass : p_366777_.passes()) {
                builder.add(createPass(p_110034_, p_366532_, postchainconfig$pass));
            }

            return new PostChain(builder.build(), p_366777_.internalTargets(), set);
        }
    }

    // $VF: Inserted dummy exception handlers to handle obfuscated exceptions
    private static PostPass createPass(TextureManager p_364641_, ShaderManager p_366893_, PostChainConfig.Pass p_361826_) throws ShaderManager.CompilationException {
        CompiledShaderProgram compiledshaderprogram = p_366893_.getProgramForLoading(p_361826_.program());

        for (PostChainConfig.Uniform postchainconfig$uniform : p_361826_.uniforms()) {
            String s = postchainconfig$uniform.name();
            if (compiledshaderprogram.getUniform(s) == null) {
                throw new ShaderManager.CompilationException("Uniform '" + s + "' does not exist for " + p_361826_.programId());
            }
        }

        String s2 = p_361826_.programId().toString();
        PostPass postpass = new PostPass(s2, compiledshaderprogram, p_361826_.outputTarget(), p_361826_.uniforms());

        for (PostChainConfig.Input postchainconfig$input : p_361826_.inputs()) {
            Objects.requireNonNull(postchainconfig$input);
            switch (postchainconfig$input) {
                case PostChainConfig.TextureInput(String s3, ResourceLocation resourcelocation, int i, int j, boolean flag):
                    AbstractTexture abstracttexture = p_364641_.getTexture(resourcelocation.withPath(p_359199_ -> "textures/effect/" + p_359199_ + ".png"));
                    abstracttexture.setFilter(flag, false);
                    postpass.addInput(new PostPass.TextureInput(s3, abstracttexture, i, j));
                    continue;
                case PostChainConfig.TargetInput(String s1, ResourceLocation resourcelocation1, boolean flag1, boolean flag2):
                    postpass.addInput(new PostPass.TargetInput(s1, resourcelocation1, flag1, flag2));
                    continue;
                default:
                    throw new MatchException(null, null);
            }
        }

        return postpass;
    }

    // $VF: Inserted dummy exception handlers to handle obfuscated exceptions
    public void addToFrame(FrameGraphBuilder p_362523_, int p_361423_, int p_362735_, PostChain.TargetBundle p_361871_) {
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, (float)p_361423_, 0.0F, (float)p_362735_, 0.1F, 1000.0F);
        Map<ResourceLocation, ResourceHandle<RenderTarget>> map = new HashMap<>(this.internalTargets.size() + this.externalTargets.size());

        for (ResourceLocation resourcelocation : this.externalTargets) {
            map.put(resourcelocation, p_361871_.getOrThrow(resourcelocation));
        }

        for (Entry<ResourceLocation, PostChainConfig.InternalTarget> entry : this.internalTargets.entrySet()) {
            ResourceLocation resourcelocation1 = entry.getKey();
            RenderTargetDescriptor rendertargetdescriptor = switch (entry.getValue()) {
                case PostChainConfig.FixedSizedTarget(int i, int j) -> {
                    yield new RenderTargetDescriptor(i, j, true);
                }
                case PostChainConfig.FullScreenTarget postchainconfig$fullscreentarget -> new RenderTargetDescriptor(p_361423_, p_362735_, true);
                default -> throw new MatchException(null, null);
            };
            map.put(resourcelocation1, p_362523_.createInternal(resourcelocation1.toString(), rendertargetdescriptor));
        }

        for (PostPass postpass : this.passes) {
            postpass.addToFrame(p_362523_, map, matrix4f);
        }

        for (ResourceLocation resourcelocation2 : this.externalTargets) {
            p_361871_.replace(resourcelocation2, map.get(resourcelocation2));
        }
    }

    @Deprecated
    public void process(RenderTarget p_361528_, GraphicsResourceAllocator p_361187_) {
        FrameGraphBuilder framegraphbuilder = new FrameGraphBuilder();
        PostChain.TargetBundle postchain$targetbundle = PostChain.TargetBundle.of(MAIN_TARGET_ID, framegraphbuilder.importExternal("main", p_361528_));
        this.addToFrame(framegraphbuilder, p_361528_.width, p_361528_.height, postchain$targetbundle);
        framegraphbuilder.execute(p_361187_);
    }

    public void setUniform(String p_332204_, float p_331999_) {
        for (PostPass postpass : this.passes) {
            postpass.getShader().safeGetUniform(p_332204_).set(p_331999_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface TargetBundle {
        static PostChain.TargetBundle of(final ResourceLocation p_362084_, final ResourceHandle<RenderTarget> p_365173_) {
            return new PostChain.TargetBundle() {
                private ResourceHandle<RenderTarget> handle = p_365173_;

                @Override
                public void replace(ResourceLocation p_363133_, ResourceHandle<RenderTarget> p_360603_) {
                    if (p_363133_.equals(p_362084_)) {
                        this.handle = p_360603_;
                    } else {
                        throw new IllegalArgumentException("No target with id " + p_363133_);
                    }
                }

                @Nullable
                @Override
                public ResourceHandle<RenderTarget> get(ResourceLocation p_360954_) {
                    return p_360954_.equals(p_362084_) ? this.handle : null;
                }
            };
        }

        void replace(ResourceLocation p_362165_, ResourceHandle<RenderTarget> p_362344_);

        @Nullable
        ResourceHandle<RenderTarget> get(ResourceLocation p_364001_);

        default ResourceHandle<RenderTarget> getOrThrow(ResourceLocation p_360758_) {
            ResourceHandle<RenderTarget> resourcehandle = this.get(p_360758_);
            if (resourcehandle == null) {
                throw new IllegalArgumentException("Missing target with id " + p_360758_);
            } else {
                return resourcehandle;
            }
        }
    }
}
