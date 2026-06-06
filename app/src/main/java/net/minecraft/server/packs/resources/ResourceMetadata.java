package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.GsonHelper;

public interface ResourceMetadata {
    ResourceMetadata EMPTY = new ResourceMetadata() {
        @Override
        public <T> Optional<T> getSection(MetadataSectionType<T> p_389466_) {
            return Optional.empty();
        }
    };
    IoSupplier<ResourceMetadata> EMPTY_SUPPLIER = () -> EMPTY;

    static ResourceMetadata fromJsonStream(InputStream p_215581_) throws IOException {
        ResourceMetadata resourcemetadata;
        try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(p_215581_, StandardCharsets.UTF_8))) {
            final JsonObject jsonobject = GsonHelper.parse(bufferedreader);
            resourcemetadata = new ResourceMetadata() {
                @Override
                public <T> Optional<T> getSection(MetadataSectionType<T> p_389450_) {
                    String s = p_389450_.name();
                    if (jsonobject.has(s)) {
                        T t = p_389450_.codec().parse(JsonOps.INSTANCE, jsonobject.get(s)).getOrThrow(JsonParseException::new);
                        return Optional.of(t);
                    } else {
                        return Optional.empty();
                    }
                }
            };
        }

        return resourcemetadata;
    }

    <T> Optional<T> getSection(MetadataSectionType<T> p_389584_);

    default ResourceMetadata copySections(Collection<MetadataSectionType<?>> p_295308_) {
        ResourceMetadata.Builder resourcemetadata$builder = new ResourceMetadata.Builder();

        for (MetadataSectionType<?> metadatasectiontype : p_295308_) {
            this.copySection(resourcemetadata$builder, metadatasectiontype);
        }

        return resourcemetadata$builder.build();
    }

    private <T> void copySection(ResourceMetadata.Builder p_295962_, MetadataSectionType<T> p_389635_) {
        this.getSection(p_389635_).ifPresent(p_389381_ -> p_295962_.put(p_389635_, (T)p_389381_));
    }

    public static class Builder {
        private final ImmutableMap.Builder<MetadataSectionType<?>, Object> map = ImmutableMap.builder();

        public <T> ResourceMetadata.Builder put(MetadataSectionType<T> p_389721_, T p_294133_) {
            this.map.put(p_389721_, p_294133_);
            return this;
        }

        public ResourceMetadata build() {
            final ImmutableMap<MetadataSectionType<?>, Object> immutablemap = this.map.build();
            return immutablemap.isEmpty() ? ResourceMetadata.EMPTY : new ResourceMetadata() {
                @Override
                public <T> Optional<T> getSection(MetadataSectionType<T> p_389492_) {
                    return Optional.ofNullable((T)immutablemap.get(p_389492_));
                }
            };
        }
    }
}
