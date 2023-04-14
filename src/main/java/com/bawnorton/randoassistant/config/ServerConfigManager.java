package com.bawnorton.randoassistant.config;

import com.bawnorton.randoassistant.RandoAssistant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve(RandoAssistant.MOD_ID + "_server.json");

    public static void loadConfig() {
        ServerConfig config = load();

        if (config.donkEnabled == null) config.donkEnabled = true;

        ServerConfig.update(config);
        save();
        RandoAssistant.LOGGER.info("Loaded config");
    }

    private static ServerConfig load() {
        ServerConfig config = ServerConfig.getInstance();
        try {
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath.getParent());
                Files.createFile(configPath);
                return config;
            }
            try {
                config = GSON.fromJson(Files.newBufferedReader(configPath), ServerConfig.class);
            } catch (JsonSyntaxException e) {
                RandoAssistant.LOGGER.error("Failed to parse config file, using default config");
                config = ServerConfig.getInstance();
            }
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to load config", e);
        }
        return config == null ? ServerConfig.getInstance() : config;
    }

    private static void save() {
        try {
            Files.write(configPath, GSON.toJson(ServerConfig.getInstance()).getBytes());
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to save config", e);
        }
    }

    public static void saveConfig() {
        save();
        RandoAssistant.LOGGER.info("Saved config");
    }
}
