package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.mixin.ServerWorldAccessor;
import com.bawnorton.randoassistant.util.LootTableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class RandoAssistantClient implements ClientModInitializer {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final File ASSISTANT_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("RandoAssistant").toFile();

	@Override
	public void onInitializeClient() {
		try {
			if(!Files.exists(ASSISTANT_DIRECTORY.toPath())) {
				RandoAssistant.LOGGER.info("Creating RandoAssistant directory");
				Files.createDirectory(ASSISTANT_DIRECTORY.toPath());
			}
		} catch (IOException e) {
			RandoAssistant.LOGGER.error("Failed to create RandoAssistant directory", e);
		}

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			try {
				Files.write(getLootTablePath(), GSON.toJson(RandoAssistant.getCurrentLootTables().getSerializedLootTableMap()).getBytes());
			} catch (Exception e) {
				RandoAssistant.LOGGER.error("Failed to save loot tables to json", e);
			}
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			try	{
				RandoAssistant.setCurrentLootTables(LootTableMap.fromSerialized(GSON.fromJson(Files.newBufferedReader(getLootTablePath()), Map.class)));
			} catch (Exception e) {
				RandoAssistant.LOGGER.error("Failed to load loot tables from json", e);
			}
		});

		KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.randoassistant.test",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_K,
				"category.randoassistant"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while(keyBinding.wasPressed()) {
				LootTableMap lootTableMap = RandoAssistant.getCurrentLootTables();
				for(Map.Entry<String, List<String>> lootEntry: lootTableMap.getSerializedLootTableMap().entrySet()) {
					RandoAssistant.LOGGER.info("Loot table: " + lootEntry.getKey());
					for(String item: lootEntry.getValue()) {
						RandoAssistant.LOGGER.info("Item: " + item);
					}
				}
			}
		});
	}

	private Path getLootTablePath() throws IOException {
		String name = ((ServerWorldAccessor) RandoAssistant.currentServer.getWorld(World.OVERWORLD)).getWorldProperties().getLevelName();
		Path path = ASSISTANT_DIRECTORY.toPath().resolve(name + ".json");
		if(!Files.exists(path)) {
			Files.createFile(path);
		}
		return path;
	}
}