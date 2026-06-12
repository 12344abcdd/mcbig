package net.minecraft.client.resources.server;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface PackReloadConfig {
    void scheduleReload(PackReloadConfig.Callbacks p_314413_);

    public interface Callbacks {
        void onSuccess();

        void onFailure(boolean p_314498_);

        List<PackReloadConfig.IdAndPath> packsToLoad();
    }

    public static record IdAndPath(UUID id, Path path) {
    }
}
