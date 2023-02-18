package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.config.ConfigManager;
import com.bawnorton.randoassistant.mixin.ServerWorldAccessor;
import com.bawnorton.randoassistant.graph.LootTableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandoAssistantClient implements ClientModInitializer {
    public static final File ASSISTANT_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("RandoAssistant").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onInitializeClient() {
        ConfigManager.loadConfig();
        initDir();
        initEvents();
    }

    private static void initDir() {
        try {
            if (!Files.exists(ASSISTANT_DIRECTORY.toPath())) {
                RandoAssistant.LOGGER.info("Creating RandoAssistant directory");
                Files.createDirectory(ASSISTANT_DIRECTORY.toPath());
            }
        } catch (IOException e) {
            RandoAssistant.LOGGER.error("Failed to create RandoAssistant directory", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void initEvents() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ConfigManager.saveConfig();
            try {
                Files.write(getLootTablePath(), GSON.toJson(RandoAssistant.lootTableMap.getSerializedLootTableMap()).getBytes());
            } catch (Exception e) {
                RandoAssistant.LOGGER.error("Failed to save loot tables to json", e);
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            try {
                RandoAssistant.lootTableMap = LootTableMap.fromSerialized(GSON.fromJson(Files.newBufferedReader(getLootTablePath()), Map.class));
            } catch (Exception e) {
                RandoAssistant.LOGGER.error("Failed to load loot tables from json", e);
            }
        });

        KeyBinding revealKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.randoassistant.reveal",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.randoassistant"
        ));
        KeyBinding resetKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.randoassistant.reset",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.randoassistant"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (revealKeyBinding.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new ConfirmScreen((result) -> {
                    if (result) {
                        RandoAssistant.addAllLootTables(Objects.requireNonNull(client.player));
                    }
                    MinecraftClient.getInstance().setScreen(null);
                }, Text.of("Add all loot tables?"), Text.of("This may cause the game client and world to lag briefly.")));
            }
            while (resetKeyBinding.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new ConfirmScreen((result) -> {
                    if (result) {
                        RandoAssistant.lootTableMap = new LootTableMap();
                    }
                    MinecraftClient.getInstance().setScreen(null);
                }, Text.of("Reset all loot tables?"), Text.of("This will clear all loot tables from the graph.")));
            }
        });
    }

    private Path getLootTablePath() throws IOException {
        String[] name = new String[]{((ServerWorldAccessor) Objects.requireNonNull(RandoAssistant.currentServer.getWorld(World.OVERWORLD))).getWorldProperties().getLevelName()};
        RandoAssistant.currentServer.getSaveProperties().getDataConfiguration().dataPacks().getEnabled().forEach((file) -> {
            if (file.contains("random_loot")) {
                Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
                Matcher matcher = pattern.matcher(file);
                if (matcher.find()) name[0] = matcher.group();
            }
        });
        Path path = ASSISTANT_DIRECTORY.toPath().resolve(name[0] + ".json");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        return path;
    }
}