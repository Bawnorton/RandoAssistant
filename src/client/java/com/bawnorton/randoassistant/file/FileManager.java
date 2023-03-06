package com.bawnorton.randoassistant.file;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.mixin.ServerWorldAccessor;
import com.bawnorton.randoassistant.networking.Networking;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileManager {
    public static final File ASSISTANT_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("RandoAssistant").toFile();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        try {
            if (!Files.exists(ASSISTANT_DIRECTORY.toPath())) {
                RandoAssistant.LOGGER.info("Creating RandoAssistant directory");
                Files.createDirectory(ASSISTANT_DIRECTORY.toPath());
            }
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to create RandoAssistant directory", e);
        }
    }

    public static Path getLootTablePath() throws IOException {
        String name = getFileName();
        Path path = ASSISTANT_DIRECTORY.toPath().resolve(name + ".json");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        return path;
    }

    public static Path getInteractionPath() throws IOException {
        String name = getFileName();
        Path path = ASSISTANT_DIRECTORY.toPath().resolve(name + "_interaction.json");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        return path;
    }

    private static String getFileName() {
        String[] name = new String[]{((ServerWorldAccessor) Objects.requireNonNull(Networking.server.getWorld(World.OVERWORLD))).getWorldProperties().getLevelName()};
        Networking.server.getSaveProperties().getDataConfiguration().dataPacks().getEnabled().forEach((file) -> {
            if (file.contains("random_loot")) {
                Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
                Matcher matcher = pattern.matcher(file);
                if (matcher.find()) name[0] = matcher.group();
            }
        });
        return name[0];
    }
}
