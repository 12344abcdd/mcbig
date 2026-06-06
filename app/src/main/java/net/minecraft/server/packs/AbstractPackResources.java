package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.DataResult.Error;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public abstract class AbstractPackResources implements PackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;

    protected AbstractPackResources(PackLocationInfo p_326071_) {
        this.location = p_326071_;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionType<T> p_389404_) throws IOException {
        IoSupplier<InputStream> iosupplier = this.getRootResource(new String[]{"pack.mcmeta"});
        if (iosupplier == null) {
            return null;
        } else {
            Object object;
            try (InputStream inputstream = iosupplier.get()) {
                object = getMetadataFromStream(p_389404_, inputstream);
            }

            return (T)object;
        }
    }

    @Nullable
    public static <T> T getMetadataFromStream(MetadataSectionType<T> p_389726_, InputStream p_10216_) {
        JsonObject jsonobject;
        try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(p_10216_, StandardCharsets.UTF_8))) {
            jsonobject = GsonHelper.parse(bufferedreader);
        } catch (Exception exception) {
            LOGGER.error("Couldn't load {} metadata", p_389726_.name(), exception);
            return null;
        }

        return !jsonobject.has(p_389726_.name())
            ? null
            : p_389726_.codec()
                .parse(JsonOps.INSTANCE, jsonobject.get(p_389726_.name()))
                .ifError(p_389537_ -> LOGGER.error("Couldn't load {} metadata: {}", p_389726_.name(), p_389537_))
                .result()
                .orElse(null);
    }

    @Override
    public PackLocationInfo location() {
        return this.location;
    }
}
