package com.bawnorton.randoassistant.config;

import com.bawnorton.randoassistant.RandoAssistant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve(RandoAssistant.MOD_ID + ".json");

    public static void loadConfig() {
        Config config = load();

        if (config.debug == null) config.debug = false;
        if (config.searchType == null) config.searchType = Config.SearchType.CONTAINS;
        if (config.childDepth == null) config.childDepth = 100;
        if (config.parentDepth == null) config.parentDepth = 100;

        Config.update(config);
        save();
        RandoAssistant.LOGGER.info("Loaded config");
    }

    private static Config load() {
        Config config = Config.getInstance();
        try {
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath.getParent());
                Files.createFile(configPath);
                return config;
            }
            try {
                config = GSON.fromJson(Files.newBufferedReader(configPath), Config.class);
            } catch (JsonSyntaxException e) {
                RandoAssistant.LOGGER.error("Failed to parse config file, using default config");
                config = Config.getInstance();
            }
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to load config", e);
        }
        return config == null ? Config.getInstance() : config;
    }

    private static void save() {
        try {
            Files.write(configPath, GSON.toJson(Config.getInstance()).getBytes());
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to save config", e);
        }
    }

    public static void saveConfig() {
        save();
        RandoAssistant.LOGGER.info("Saved config");
    }
}
