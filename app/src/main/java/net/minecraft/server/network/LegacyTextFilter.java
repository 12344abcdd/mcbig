package net.minecraft.server.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.util.GsonHelper;

public class LegacyTextFilter extends ServerTextFilter {
    private static final String ENDPOINT = "v1/chat";
    final URL joinEndpoint;
    final LegacyTextFilter.JoinOrLeaveEncoder joinEncoder;
    final URL leaveEndpoint;
    final LegacyTextFilter.JoinOrLeaveEncoder leaveEncoder;
    private final String authKey;

    private LegacyTextFilter(
        URL p_363560_,
        ServerTextFilter.MessageEncoder p_365315_,
        URL p_364476_,
        LegacyTextFilter.JoinOrLeaveEncoder p_360868_,
        URL p_365117_,
        LegacyTextFilter.JoinOrLeaveEncoder p_363479_,
        String p_364988_,
        ServerTextFilter.IgnoreStrategy p_364465_,
        ExecutorService p_365348_
    ) {
        super(p_363560_, p_365315_, p_364465_, p_365348_);
        this.joinEndpoint = p_364476_;
        this.joinEncoder = p_360868_;
        this.leaveEndpoint = p_365117_;
        this.leaveEncoder = p_363479_;
        this.authKey = p_364988_;
    }

    @Nullable
    public static ServerTextFilter createTextFilterFromConfig(String p_363094_) {
        try {
            JsonObject jsonobject = GsonHelper.parse(p_363094_);
            URI uri = new URI(GsonHelper.getAsString(jsonobject, "apiServer"));
            String s = GsonHelper.getAsString(jsonobject, "apiKey");
            if (s.isEmpty()) {
                throw new IllegalArgumentException("Missing API key");
            } else {
                int i = GsonHelper.getAsInt(jsonobject, "ruleId", 1);
                String s1 = GsonHelper.getAsString(jsonobject, "serverId", "");
                String s2 = GsonHelper.getAsString(jsonobject, "roomId", "Java:Chat");
                int j = GsonHelper.getAsInt(jsonobject, "hashesToDrop", -1);
                int k = GsonHelper.getAsInt(jsonobject, "maxConcurrentRequests", 7);
                JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "endpoints", null);
                String s3 = getEndpointFromConfig(jsonobject1, "chat", "v1/chat");
                boolean flag = s3.equals("v1/chat");
                URL url = uri.resolve("/" + s3).toURL();
                URL url1 = getEndpoint(uri, jsonobject1, "join", "v1/join");
                URL url2 = getEndpoint(uri, jsonobject1, "leave", "v1/leave");
                LegacyTextFilter.JoinOrLeaveEncoder legacytextfilter$joinorleaveencoder = p_363954_ -> {
                    JsonObject jsonobject2 = new JsonObject();
                    jsonobject2.addProperty("server", s1);
                    jsonobject2.addProperty("room", s2);
                    jsonobject2.addProperty("user_id", p_363954_.getId().toString());
                    jsonobject2.addProperty("user_display_name", p_363954_.getName());
                    return jsonobject2;
                };
                ServerTextFilter.MessageEncoder servertextfilter$messageencoder;
                if (flag) {
                    servertextfilter$messageencoder = (p_363081_, p_365373_) -> {
                        JsonObject jsonobject2 = new JsonObject();
                        jsonobject2.addProperty("rule", i);
                        jsonobject2.addProperty("server", s1);
                        jsonobject2.addProperty("room", s2);
                        jsonobject2.addProperty("player", p_363081_.getId().toString());
                        jsonobject2.addProperty("player_display_name", p_363081_.getName());
                        jsonobject2.addProperty("text", p_365373_);
                        jsonobject2.addProperty("language", "*");
                        return jsonobject2;
                    };
                } else {
                    String s4 = String.valueOf(i);
                    servertextfilter$messageencoder = (p_365239_, p_360684_) -> {
                        JsonObject jsonobject2 = new JsonObject();
                        jsonobject2.addProperty("rule_id", s4);
                        jsonobject2.addProperty("category", s1);
                        jsonobject2.addProperty("subcategory", s2);
                        jsonobject2.addProperty("user_id", p_365239_.getId().toString());
                        jsonobject2.addProperty("user_display_name", p_365239_.getName());
                        jsonobject2.addProperty("text", p_360684_);
                        jsonobject2.addProperty("language", "*");
                        return jsonobject2;
                    };
                }

                ServerTextFilter.IgnoreStrategy servertextfilter$ignorestrategy = ServerTextFilter.IgnoreStrategy.select(j);
                ExecutorService executorservice = createWorkerPool(k);
                String s5 = Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.US_ASCII));
                return new LegacyTextFilter(
                    url,
                    servertextfilter$messageencoder,
                    url1,
                    legacytextfilter$joinorleaveencoder,
                    url2,
                    legacytextfilter$joinorleaveencoder,
                    s5,
                    servertextfilter$ignorestrategy,
                    executorservice
                );
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse chat filter config {}", p_363094_, exception);
            return null;
        }
    }

    @Override
    public TextFilter createContext(GameProfile p_364054_) {
        return new ServerTextFilter.PlayerContext(p_364054_) {
            @Override
            public void join() {
                LegacyTextFilter.this.processJoinOrLeave(
                    this.profile, LegacyTextFilter.this.joinEndpoint, LegacyTextFilter.this.joinEncoder, this.streamExecutor
                );
            }

            @Override
            public void leave() {
                LegacyTextFilter.this.processJoinOrLeave(
                    this.profile, LegacyTextFilter.this.leaveEndpoint, LegacyTextFilter.this.leaveEncoder, this.streamExecutor
                );
            }
        };
    }

    void processJoinOrLeave(GameProfile p_364281_, URL p_364927_, LegacyTextFilter.JoinOrLeaveEncoder p_362293_, Executor p_363061_) {
        p_363061_.execute(() -> {
            JsonObject jsonobject = p_362293_.encode(p_364281_);

            try {
                this.processRequest(jsonobject, p_364927_);
            } catch (Exception exception) {
                LOGGER.warn("Failed to send join/leave packet to {} for player {}", p_364927_, p_364281_, exception);
            }
        });
    }

    private void processRequest(JsonObject p_364402_, URL p_360408_) throws IOException {
        HttpURLConnection httpurlconnection = this.makeRequest(p_364402_, p_360408_);

        try (InputStream inputstream = httpurlconnection.getInputStream()) {
            this.drainStream(inputstream);
        }
    }

    @Override
    protected void setAuthorizationProperty(HttpURLConnection p_365203_) {
        p_365203_.setRequestProperty("Authorization", "Basic " + this.authKey);
    }

    @Override
    protected FilteredText filterText(String p_361804_, ServerTextFilter.IgnoreStrategy p_362791_, JsonObject p_364399_) {
        boolean flag = GsonHelper.getAsBoolean(p_364399_, "response", false);
        if (flag) {
            return FilteredText.passThrough(p_361804_);
        } else {
            String s = GsonHelper.getAsString(p_364399_, "hashed", null);
            if (s == null) {
                return FilteredText.fullyFiltered(p_361804_);
            } else {
                JsonArray jsonarray = GsonHelper.getAsJsonArray(p_364399_, "hashes");
                FilterMask filtermask = this.parseMask(p_361804_, jsonarray, p_362791_);
                return new FilteredText(p_361804_, filtermask);
            }
        }
    }

    @FunctionalInterface
    interface JoinOrLeaveEncoder {
        JsonObject encode(GameProfile p_364802_);
    }
}
