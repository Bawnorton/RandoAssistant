package com.bawnorton.randoassistant.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerConfig {
    private static ServerConfig INSTANCE;

    @Expose
    @SerializedName("donk_enabled")
    public Boolean donkEnabled;

    private ServerConfig() {
    }

    public static ServerConfig getInstance() {
        if (INSTANCE == null) INSTANCE = new ServerConfig();
        return INSTANCE;
    }

    public static void update(ServerConfig config) {
        INSTANCE = config;
    }
}
