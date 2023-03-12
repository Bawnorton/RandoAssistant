package com.bawnorton.randoassistant.file;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.graph.InteractionMap;
import com.bawnorton.randoassistant.graph.LootTableMap;
import com.bawnorton.randoassistant.mixin.ServerWorldAccessor;
import com.bawnorton.randoassistant.networking.Networking;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public static void saveLootTableMap() {
        try {
            JsonElement json = RandoAssistantClient.lootTableMap.serialize();
            if(json == null) return;
            String toString = GSON.toJson(json);
            Files.writeString(getLootTablePath(), toString);
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to save loot table map", e);
        }
    }

    public static void saveInteractionMap() {
        try {
            JsonElement json = RandoAssistantClient.interactionMap.serialize();
            if(json == null) return;
            String toString = GSON.toJson(json);
            Files.writeString(getInteractionPath(), toString);
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to save interaction map", e);
        }
    }

    public static void loadLootTableMap() {
        try {
            JsonReader reader = new JsonReader(Files.newBufferedReader(getLootTablePath()));
            JsonElement json = GSON.fromJson(reader, JsonElement.class);
            RandoAssistantClient.lootTableMap = LootTableMap.deserialize(json);
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to load loot table map", e);
        }
    }

    public static void loadInteractionMap() {
        try {
            JsonReader reader = new JsonReader(Files.newBufferedReader(getInteractionPath()));
            JsonElement json = GSON.fromJson(reader, JsonElement.class);
            RandoAssistantClient.interactionMap = InteractionMap.deserialize(json);
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to load interaction map", e);
        }
    }

    public static void dumpLootTableMap() {
        try {
            JsonElement json = RandoAssistantClient.lootTableMap.serialize();
            if (json == null) return;
            String toString = GSON.toJson(json);
            Files.writeString(getRecoveryLootTablePath(), toString);
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to dump loot table map", e);
        }
    }

    public static void dumpInteractionMap() {
        try {
            JsonElement json = RandoAssistantClient.interactionMap.serialize();
            if (json == null) return;
            String toString = GSON.toJson(json);
            Files.writeString(getRecoveryInteractionPath(), toString);
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to dump interaction map", e);
        }
    }

    public static String loadLootTableMapRecovery() {
        try {
            Path path = getRecoveryLootTablePath();
            JsonReader reader = new JsonReader(Files.newBufferedReader(path));
            JsonElement json = GSON.fromJson(reader, JsonElement.class);
            RandoAssistantClient.lootTableMap = LootTableMap.deserialize(json);
            return path.getFileName().toString().replace(".json", "");
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to load loot table map recovery", e);
        }
        return null;
    }

    public static void loadInteractionMapRecovery() {
        try {
            JsonReader reader = new JsonReader(Files.newBufferedReader(getRecoveryInteractionPath()));
            JsonElement json = GSON.fromJson(reader, JsonElement.class);
            RandoAssistantClient.interactionMap = InteractionMap.deserialize(json);
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to load interaction map recovery", e);
        }
    }

    public static void createFailureZip() {
        String currentDateTime = Date.from(Instant.now()).toString().replace(" ", "_").replace(":", "-");
        File failureDir = ASSISTANT_DIRECTORY.toPath().resolve("rando_assistant_report_" + currentDateTime).toFile();
        try {
            Files.createDirectory(failureDir.toPath());
            // copy mc log to failure dir
            File mcLog = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("latest.log").toFile();
            Files.copy(mcLog.toPath(), failureDir.toPath().resolve("latest.log"));
            String seed = estimateSeed();
            // create report file
            String text = "Seed: " + seed + "\n" +
                    "Loot Table Map: " + RandoAssistantClient.lootTableMap + "\n" +
                    "Interaction Map: " + RandoAssistantClient.interactionMap;
            Files.writeString(failureDir.toPath().resolve("rando_assistant_report.txt"), text);
            if (seed != null) {
                File[] files = ASSISTANT_DIRECTORY.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().endsWith(".json") && file.getName().contains(seed)) {
                            Files.copy(file.toPath(), failureDir.toPath().resolve(file.getName()));
                        }
                    }
                }
            }
            // create zip
            File zip = FabricLoader.getInstance().getGameDir().resolve("rando_assistant_report_" + currentDateTime + ".zip").toFile();
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zip));
            for (File file : failureDir.listFiles()) {
                if (file.getName().endsWith(".zip")) continue;
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                Files.copy(file.toPath(), zipOutputStream);
                zipOutputStream.closeEntry();
            }
            zipOutputStream.close();
            // delete failure dir
            FileUtils.deleteDirectory(failureDir);
            RandoAssistant.LOGGER.info("Created failure zip at " + zip.getAbsolutePath());
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to create failure zip", e);
            // create backup failure file
            try {
                File mcLog = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("latest.log").toFile();
                Files.copy(mcLog.toPath(), failureDir.toPath().resolve("rando_assistant_backup_report_" + currentDateTime + ".log"));
            } catch (IOException ex) {
                RandoAssistant.LOGGER.error("Failed to create backup failure file", ex);
            }
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

    public static Path getRecoveryLootTablePath() throws IOException {
        String name = estimateSeed();
        if(name == null) throw new RuntimeException("Could not estimate seed");
        Path path = ASSISTANT_DIRECTORY.toPath().resolve(name + ".json");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        return path;
    }

    public static Path getRecoveryInteractionPath() throws IOException {
        String name = estimateSeed();
        if(name == null) throw new RuntimeException("Could not estimate seed");
        Path path = ASSISTANT_DIRECTORY.toPath().resolve(name + "_interaction.json");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        return path;
    }

    public static String estimateSeed() {
        Path saveDirectory = FabricLoader.getInstance().getGameDir().resolve("saves");
        File[] files = saveDirectory.toFile().listFiles();
        if(files == null) return null;

        long lastModified = 0;
        File mostRecent = null;
        for (File file : files) {
            if(file.getName().equals(".DS_Store")) continue;
            if (file.lastModified() > lastModified) {
                mostRecent = file;
                lastModified = file.lastModified();
            }
        }
        if(mostRecent == null) return null;

        File datapackDir = mostRecent.toPath().resolve("datapacks").toFile();
        if(!datapackDir.exists()) return null;

        File[] datapacks = datapackDir.listFiles();
        if(datapacks == null) return null;

        for (File datapack : datapacks) {
            String name = datapack.getName();
            if (!name.startsWith("random_loot")) continue;

            Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
            Matcher matcher = pattern.matcher(name);
            if (!matcher.find()) continue;

            return matcher.group();
        }
        return null;
    }

    private static String getFileName() {
        try {
            String[] name = new String[]{((ServerWorldAccessor) Objects.requireNonNull(Networking.server.getWorld(World.OVERWORLD))).getWorldProperties().getLevelName()};
            Networking.server.getSaveProperties().getDataConfiguration().dataPacks().getEnabled().forEach((file) -> {
                if (file.contains("random_loot")) {
                    Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
                    Matcher matcher = pattern.matcher(file);
                    if (matcher.find()) name[0] = matcher.group();
                }
            });
            return name[0];
        } catch (Exception e) {
            return estimateSeed();
        }
    }
}
