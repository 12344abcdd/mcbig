package net.minecraft.client.resources.server;

import java.util.UUID;

public interface PackLoadFeedback {
    void reportUpdate(UUID p_315007_, PackLoadFeedback.Update p_314979_);

    void reportFinalResult(UUID p_314623_, PackLoadFeedback.FinalResult p_314920_);

    public static enum FinalResult {
        DECLINED,
        APPLIED,
        DISCARDED,
        DOWNLOAD_FAILED,
        ACTIVATION_FAILED;
    }

    public static enum Update {
        ACCEPTED,
        DOWNLOADED;
    }
}
