package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.CompiledShader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ShaderManager extends SimplePreparableReloadListener<ShaderManager.Configs> implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final String SHADER_PATH = "shaders";
    public static final String SHADER_INCLUDE_PATH = "shaders/include/";
    private static final FileToIdConverter PROGRAM_ID_CONVERTER = FileToIdConverter.json("shaders");
    private static final FileToIdConverter POST_CHAIN_ID_CONVERTER = FileToIdConverter.json("post_effect");
    public static final int MAX_LOG_LENGTH = 32768;
    final TextureManager textureManager;
    private final Consumer<Exception> recoveryHandler;
    private ShaderManager.CompilationCache compilationCache = new ShaderManager.CompilationCache(ShaderManager.Configs.EMPTY);

    public ShaderManager(TextureManager p_366478_, Consumer<Exception> p_368560_) {
        this.textureManager = p_366478_;
        this.recoveryHandler = p_368560_;
    }

    protected ShaderManager.Configs prepare(ResourceManager p_366761_, ProfilerFiller p_366562_) {
        Builder<ResourceLocation, ShaderProgramConfig> builder = ImmutableMap.builder();
        Builder<ShaderManager.ShaderSourceKey, String> builder1 = ImmutableMap.builder();
        Map<ResourceLocation, Resource> map = p_366761_.listResources("shaders", p_366504_ -> isProgram(p_366504_) || isShader(p_366504_));

        for (Entry<ResourceLocation, Resource> entry : map.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            CompiledShader.Type compiledshader$type = CompiledShader.Type.byLocation(resourcelocation);
            if (compiledshader$type != null) {
                loadShader(resourcelocation, entry.getValue(), compiledshader$type, map, builder1);
            } else if (isProgram(resourcelocation)) {
                loadProgram(resourcelocation, entry.getValue(), builder);
            }
        }

        Builder<ResourceLocation, PostChainConfig> builder2 = ImmutableMap.builder();

        for (Entry<ResourceLocation, Resource> entry1 : POST_CHAIN_ID_CONVERTER.listMatchingResources(p_366761_).entrySet()) {
            loadPostChain(entry1.getKey(), entry1.getValue(), builder2);
        }

        return new ShaderManager.Configs(builder.build(), builder1.build(), builder2.build());
    }

    private static void loadShader(
        ResourceLocation p_366513_,
        Resource p_366763_,
        CompiledShader.Type p_366461_,
        Map<ResourceLocation, Resource> p_366725_,
        Builder<ShaderManager.ShaderSourceKey, String> p_366733_
    ) {
        ResourceLocation resourcelocation = p_366461_.idConverter().fileToId(p_366513_);
        GlslPreprocessor glslpreprocessor = createPreprocessor(p_366725_, p_366513_);

        try (Reader reader = p_366763_.openAsReader()) {
            String s = IOUtils.toString(reader);
            p_366733_.put(new ShaderManager.ShaderSourceKey(resourcelocation, p_366461_), String.join("", glslpreprocessor.process(s)));
        } catch (IOException ioexception) {
            LOGGER.error("Failed to load shader source at {}", p_366513_, ioexception);
        }
    }

    private static GlslPreprocessor createPreprocessor(final Map<ResourceLocation, Resource> p_366891_, ResourceLocation p_366860_) {
        final ResourceLocation resourcelocation = p_366860_.withPath(FileUtil::getFullResourcePath);
        return new GlslPreprocessor() {
            private final Set<ResourceLocation> importedLocations = new ObjectArraySet<>();

            @Override
            public String applyImport(boolean p_366551_, String p_366739_) {
                ResourceLocation resourcelocation1;
                try {
                    if (p_366551_) {
                        resourcelocation1 = resourcelocation.withPath(p_366623_ -> FileUtil.normalizeResourcePath(p_366623_ + p_366739_));
                    } else {
                        resourcelocation1 = ResourceLocation.parse(p_366739_).withPrefix("shaders/include/");
                    }
                } catch (ResourceLocationException resourcelocationexception) {
                    ShaderManager.LOGGER.error("Malformed GLSL import {}: {}", p_366739_, resourcelocationexception.getMessage());
                    return "#error " + resourcelocationexception.getMessage();
                }

                if (!this.importedLocations.add(resourcelocation1)) {
                    return null;
                } else {
                    try {
                        String s;
                        try (Reader reader = p_366891_.get(resourcelocation1).openAsReader()) {
                            s = IOUtils.toString(reader);
                        }

                        return s;
                    } catch (IOException ioexception) {
                        ShaderManager.LOGGER.error("Could not open GLSL import {}: {}", resourcelocation1, ioexception.getMessage());
                        return "#error " + ioexception.getMessage();
                    }
                }
            }
        };
    }

    private static void loadProgram(ResourceLocation p_366422_, Resource p_366602_, Builder<ResourceLocation, ShaderProgramConfig> p_366850_) {
        ResourceLocation resourcelocation = PROGRAM_ID_CONVERTER.fileToId(p_366422_);

        try (Reader reader = p_366602_.openAsReader()) {
            JsonElement jsonelement = JsonParser.parseReader(reader);
            ShaderProgramConfig shaderprogramconfig = ShaderProgramConfig.CODEC.parse(JsonOps.INSTANCE, jsonelement).getOrThrow(JsonSyntaxException::new);
            p_366850_.put(resourcelocation, shaderprogramconfig);
        } catch (JsonParseException | IOException ioexception) {
            LOGGER.error("Failed to parse shader config at {}", p_366422_, ioexception);
        }
    }

    private static void loadPostChain(ResourceLocation p_366838_, Resource p_366558_, Builder<ResourceLocation, PostChainConfig> p_366892_) {
        ResourceLocation resourcelocation = POST_CHAIN_ID_CONVERTER.fileToId(p_366838_);

        try (Reader reader = p_366558_.openAsReader()) {
            JsonElement jsonelement = JsonParser.parseReader(reader);
            p_366892_.put(resourcelocation, PostChainConfig.CODEC.parse(JsonOps.INSTANCE, jsonelement).getOrThrow(JsonSyntaxException::new));
        } catch (JsonParseException | IOException ioexception) {
            LOGGER.error("Failed to parse post chain at {}", p_366838_, ioexception);
        }
    }

    private static boolean isProgram(ResourceLocation p_366404_) {
        return p_366404_.getPath().endsWith(".json");
    }

    private static boolean isShader(ResourceLocation p_366565_) {
        return CompiledShader.Type.byLocation(p_366565_) != null || p_366565_.getPath().endsWith(".glsl");
    }

    protected void apply(ShaderManager.Configs p_366597_, ResourceManager p_366533_, ProfilerFiller p_366866_) {
        ShaderManager.CompilationCache shadermanager$compilationcache = new ShaderManager.CompilationCache(p_366597_);
        Map<ShaderProgram, ShaderManager.CompilationException> map = new HashMap<>();
        Set<ShaderProgram> set = new HashSet<>(CoreShaders.getProgramsToPreload());

        for (PostChainConfig postchainconfig : p_366597_.postChains.values()) {
            for (PostChainConfig.Pass postchainconfig$pass : postchainconfig.passes()) {
                set.add(postchainconfig$pass.program());
            }
        }

        for (ShaderProgram shaderprogram : set) {
            try {
                shadermanager$compilationcache.programs.put(shaderprogram, Optional.of(shadermanager$compilationcache.compileProgram(shaderprogram)));
            } catch (ShaderManager.CompilationException shadermanager$compilationexception) {
                map.put(shaderprogram, shadermanager$compilationexception);
            }
        }

        if (!map.isEmpty()) {
            shadermanager$compilationcache.close();
            throw new RuntimeException(
                "Failed to load required shader programs:\n"
                    + map.entrySet()
                        .stream()
                        .map(p_366523_ -> " - " + p_366523_.getKey() + ": " + p_366523_.getValue().getMessage())
                        .collect(Collectors.joining("\n"))
            );
        } else {
            this.compilationCache.close();
            this.compilationCache = shadermanager$compilationcache;
        }
    }

    @Override
    public String getName() {
        return "Shader Loader";
    }

    private void tryTriggerRecovery(Exception p_383219_) {
        if (!this.compilationCache.triggeredRecovery) {
            this.recoveryHandler.accept(p_383219_);
            this.compilationCache.triggeredRecovery = true;
        }
    }

    public void preloadForStartup(ResourceProvider p_366778_, ShaderProgram... p_366598_) throws IOException, ShaderManager.CompilationException {
        for (ShaderProgram shaderprogram : p_366598_) {
            Resource resource = p_366778_.getResourceOrThrow(PROGRAM_ID_CONVERTER.idToFile(shaderprogram.configId()));

            try (Reader reader = resource.openAsReader()) {
                JsonElement jsonelement = JsonParser.parseReader(reader);
                ShaderProgramConfig shaderprogramconfig = ShaderProgramConfig.CODEC.parse(JsonOps.INSTANCE, jsonelement).getOrThrow(JsonSyntaxException::new);
                ShaderDefines shaderdefines = shaderprogramconfig.defines().withOverrides(shaderprogram.defines());
                CompiledShader compiledshader = this.preloadShader(p_366778_, shaderprogramconfig.vertex(), CompiledShader.Type.VERTEX, shaderdefines);
                CompiledShader compiledshader1 = this.preloadShader(p_366778_, shaderprogramconfig.fragment(), CompiledShader.Type.FRAGMENT, shaderdefines);
                CompiledShaderProgram compiledshaderprogram = linkProgram(shaderprogram, shaderprogramconfig, compiledshader, compiledshader1);
                this.compilationCache.programs.put(shaderprogram, Optional.of(compiledshaderprogram));
            }
        }
    }

    private CompiledShader preloadShader(ResourceProvider p_366664_, ResourceLocation p_366827_, CompiledShader.Type p_366782_, ShaderDefines p_366425_) throws IOException, ShaderManager.CompilationException {
        ResourceLocation resourcelocation = p_366782_.idConverter().idToFile(p_366827_);

        CompiledShader compiledshader1;
        try (Reader reader = p_366664_.getResourceOrThrow(resourcelocation).openAsReader()) {
            String s = IOUtils.toString(reader);
            String s1 = GlslPreprocessor.injectDefines(s, p_366425_);
            CompiledShader compiledshader = CompiledShader.compile(p_366827_, p_366782_, s1);
            this.compilationCache.shaders.put(new ShaderManager.ShaderCompilationKey(p_366827_, p_366782_, p_366425_), compiledshader);
            compiledshader1 = compiledshader;
        }

        return compiledshader1;
    }

    @Nullable
    public CompiledShaderProgram getProgram(ShaderProgram p_366726_) {
        try {
            return this.compilationCache.getOrCompileProgram(p_366726_);
        } catch (ShaderManager.CompilationException shadermanager$compilationexception) {
            LOGGER.error("Failed to load shader program: {}", p_366726_, shadermanager$compilationexception);
            this.compilationCache.programs.put(p_366726_, Optional.empty());
            this.tryTriggerRecovery(shadermanager$compilationexception);
            return null;
        }
    }

    public CompiledShaderProgram getProgramForLoading(ShaderProgram p_371850_) throws ShaderManager.CompilationException {
        CompiledShaderProgram compiledshaderprogram = this.compilationCache.getOrCompileProgram(p_371850_);
        if (compiledshaderprogram == null) {
            throw new ShaderManager.CompilationException("Shader '" + p_371850_ + "' could not be found");
        } else {
            return compiledshaderprogram;
        }
    }

    static CompiledShaderProgram linkProgram(ShaderProgram p_366640_, ShaderProgramConfig p_366864_, CompiledShader p_366716_, CompiledShader p_366871_) throws ShaderManager.CompilationException {
        CompiledShaderProgram compiledshaderprogram = CompiledShaderProgram.link(p_366716_, p_366871_, p_366640_.vertexFormat());
        compiledshaderprogram.setupUniforms(p_366864_.uniforms(), p_366864_.samplers());
        return compiledshaderprogram;
    }

    @Nullable
    public PostChain getPostChain(ResourceLocation p_366709_, Set<ResourceLocation> p_366652_) {
        try {
            return this.compilationCache.getOrLoadPostChain(p_366709_, p_366652_);
        } catch (ShaderManager.CompilationException shadermanager$compilationexception) {
            LOGGER.error("Failed to load post chain: {}", p_366709_, shadermanager$compilationexception);
            this.compilationCache.postChains.put(p_366709_, Optional.empty());
            this.tryTriggerRecovery(shadermanager$compilationexception);
            return null;
        }
    }

    @Override
    public void close() {
        this.compilationCache.close();
    }

    @OnlyIn(Dist.CLIENT)
    class CompilationCache implements AutoCloseable {
        private final ShaderManager.Configs configs;
        final Map<ShaderProgram, Optional<CompiledShaderProgram>> programs = new HashMap<>();
        final Map<ShaderManager.ShaderCompilationKey, CompiledShader> shaders = new HashMap<>();
        final Map<ResourceLocation, Optional<PostChain>> postChains = new HashMap<>();
        boolean triggeredRecovery;

        CompilationCache(ShaderManager.Configs p_368510_) {
            this.configs = p_368510_;
        }

        @Nullable
        public CompiledShaderProgram getOrCompileProgram(ShaderProgram p_368706_) throws ShaderManager.CompilationException {
            Optional<CompiledShaderProgram> optional = this.programs.get(p_368706_);
            if (optional != null) {
                return optional.orElse(null);
            } else {
                CompiledShaderProgram compiledshaderprogram = this.compileProgram(p_368706_);
                this.programs.put(p_368706_, Optional.of(compiledshaderprogram));
                return compiledshaderprogram;
            }
        }

        CompiledShaderProgram compileProgram(ShaderProgram p_368538_) throws ShaderManager.CompilationException {
            ShaderProgramConfig shaderprogramconfig = this.configs.programs.get(p_368538_.configId());
            if (shaderprogramconfig == null) {
                throw new ShaderManager.CompilationException("Could not find program with id: " + p_368538_.configId());
            } else {
                ShaderDefines shaderdefines = shaderprogramconfig.defines().withOverrides(p_368538_.defines());
                CompiledShader compiledshader = this.getOrCompileShader(shaderprogramconfig.vertex(), CompiledShader.Type.VERTEX, shaderdefines);
                CompiledShader compiledshader1 = this.getOrCompileShader(shaderprogramconfig.fragment(), CompiledShader.Type.FRAGMENT, shaderdefines);
                return ShaderManager.linkProgram(p_368538_, shaderprogramconfig, compiledshader, compiledshader1);
            }
        }

        private CompiledShader getOrCompileShader(ResourceLocation p_368708_, CompiledShader.Type p_368521_, ShaderDefines p_368640_) throws ShaderManager.CompilationException {
            ShaderManager.ShaderCompilationKey shadermanager$shadercompilationkey = new ShaderManager.ShaderCompilationKey(p_368708_, p_368521_, p_368640_);
            CompiledShader compiledshader = this.shaders.get(shadermanager$shadercompilationkey);
            if (compiledshader == null) {
                compiledshader = this.compileShader(shadermanager$shadercompilationkey);
                this.shaders.put(shadermanager$shadercompilationkey, compiledshader);
            }

            return compiledshader;
        }

        private CompiledShader compileShader(ShaderManager.ShaderCompilationKey p_368763_) throws ShaderManager.CompilationException {
            String s = this.configs.shaderSources.get(new ShaderManager.ShaderSourceKey(p_368763_.id, p_368763_.type));
            if (s == null) {
                throw new ShaderManager.CompilationException("Could not find shader: " + p_368763_);
            } else {
                String s1 = GlslPreprocessor.injectDefines(s, p_368763_.defines);
                return CompiledShader.compile(p_368763_.id, p_368763_.type, s1);
            }
        }

        @Nullable
        public PostChain getOrLoadPostChain(ResourceLocation p_368720_, Set<ResourceLocation> p_368722_) throws ShaderManager.CompilationException {
            Optional<PostChain> optional = this.postChains.get(p_368720_);
            if (optional != null) {
                return optional.orElse(null);
            } else {
                PostChain postchain = this.loadPostChain(p_368720_, p_368722_);
                this.postChains.put(p_368720_, Optional.of(postchain));
                return postchain;
            }
        }

        private PostChain loadPostChain(ResourceLocation p_368578_, Set<ResourceLocation> p_368581_) throws ShaderManager.CompilationException {
            PostChainConfig postchainconfig = this.configs.postChains.get(p_368578_);
            if (postchainconfig == null) {
                throw new ShaderManager.CompilationException("Could not find post chain with id: " + p_368578_);
            } else {
                return PostChain.load(postchainconfig, ShaderManager.this.textureManager, ShaderManager.this, p_368581_);
            }
        }

        @Override
        public void close() {
            RenderSystem.assertOnRenderThread();
            this.programs.values().forEach(p_368606_ -> p_368606_.ifPresent(CompiledShaderProgram::close));
            this.shaders.values().forEach(CompiledShader::close);
            this.programs.clear();
            this.shaders.clear();
            this.postChains.clear();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CompilationException extends Exception {
        public CompilationException(String p_366771_) {
            super(p_366771_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Configs(
        Map<ResourceLocation, ShaderProgramConfig> programs,
        Map<ShaderManager.ShaderSourceKey, String> shaderSources,
        Map<ResourceLocation, PostChainConfig> postChains
    ) {
        public static final ShaderManager.Configs EMPTY = new ShaderManager.Configs(Map.of(), Map.of(), Map.of());
    }

    @OnlyIn(Dist.CLIENT)
    static record ShaderCompilationKey(ResourceLocation id, CompiledShader.Type type, ShaderDefines defines) {
        @Override
        public String toString() {
            String s = this.id + " (" + this.type + ")";
            return !this.defines.isEmpty() ? s + " with " + this.defines : s;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record ShaderSourceKey(ResourceLocation id, CompiledShader.Type type) {
        @Override
        public String toString() {
            return this.id + " (" + this.type + ")";
        }
    }
}
