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
    @SerializedName("silktouch_unbroken_block_icon")
    public Boolean silktouchUnbrokenBlockIcon;

    @Expose
    @SerializedName("auto_toggle")
    public Boolean autoToggle;

    @Expose
    @SerializedName("enable_override")
    public Boolean enableOverride;

    @Expose
    @SerializedName("randomize_colours")
    public Boolean randomizeColours;

    @Expose
    @SerializedName("search_depth")
    public Integer searchDepth;

    @Expose
    @SerializedName("highlight_radius")
    public Integer highlightRadius;

    @Expose
    @SerializedName("enable_crafting")
    public Boolean enableCrafting;

    @Expose
    @SerializedName("enable_interactions")
    public Boolean enableInteractions;

    @Expose
    @SerializedName("invert_search")
    public Boolean invertSearch;

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
