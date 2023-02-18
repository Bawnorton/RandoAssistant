package com.bawnorton.randoassistant.file.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Config {
    private static Config INSTANCE;

    @Expose @SerializedName("debug")
    public Boolean debug = false;

    @Expose @SerializedName("show_interaction_lines")
    public Boolean showInteractionLines;

    @Expose @SerializedName("search_type")
    public SearchType searchType = SearchType.EXACT;

    @Expose @SerializedName("child_depth")
    public Integer childDepth = 100;

    @Expose @SerializedName("parent_depth")
    public Integer parentDepth = 100;

    private Config() {
    }

    public static Config getInstance() {
        if (INSTANCE == null) INSTANCE = new Config();
        return INSTANCE;
    }

    public static void update(Config config) {
        INSTANCE = config;
    }

    public enum SearchType {
        @Expose @SerializedName("fuzzy")
        FUZZY,
        @Expose @SerializedName("exact")
        EXACT,
        @Expose @SerializedName("contains")
        CONTAINS;

        public SearchType next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }
}
