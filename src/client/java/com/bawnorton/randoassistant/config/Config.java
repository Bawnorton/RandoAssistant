package com.bawnorton.randoassistant.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Config {
    private static Config INSTANCE;

    @Expose
    @SerializedName("debug")
    public Boolean debug;

    @Expose
    @SerializedName("unbroken_block_icon")
    public Boolean unbrokenBlockIcon;

    @Expose
    @SerializedName("toasts")
    public Boolean toasts;

    @Expose
    @SerializedName("child_depth")
    public Integer childDepth;

    @Expose
    @SerializedName("parent_depth")
    public Integer parentDepth;

    @Expose
    @SerializedName("randomize_colours")
    public Boolean randomizeColours;

    private Config() {
    }

    public static Config getInstance() {
        if (INSTANCE == null) INSTANCE = new Config();
        return INSTANCE;
    }

    public static void update(Config config) {
        INSTANCE = config;
    }
}
