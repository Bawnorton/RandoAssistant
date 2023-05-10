package com.bawnorton.randoassistant.event;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.config.ConfigManager;
import com.bawnorton.randoassistant.keybind.KeybindManager;
import com.bawnorton.randoassistant.tracking.Tracker;
import com.bawnorton.randoassistant.tracking.trackable.TrackableCrawler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.text.Text;

public class EventManager {

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if(client.player == null) {
                RandoAssistant.LOGGER.error("Client player is null, cannot load loot tables");
                return;
            }
            Tracker.getInstance().clear();
            TrackableCrawler.clearCache();
            RandoAssistant.getAllLootTables(client.player);
            RandoAssistant.getAllInteractions(client.player);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeybindManager.revealKeyBinding.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new ConfirmScreen((result) -> {
                    if (result) {
                        Tracker.getInstance().enableAll();
                    }
                    MinecraftClient.getInstance().setScreen(null);
                }, Text.of("Add all loot tables?"), Text.of("This may cause the game client and world to lag briefly.")));
            }
            while (KeybindManager.resetKeyBinding.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new ConfirmScreen((result) -> {
                    if (result) {
                        Tracker.getInstance().disableAll();
                    }
                    MinecraftClient.getInstance().setScreen(null);
                }, Text.of("Reset all loot tables?"), Text.of("This will clear all added loot tables from the graph.")));
            }
            while (KeybindManager.configScreen.wasPressed()) {
                if (client.player != null) {
                    MinecraftClient.getInstance().setScreen(ConfigManager.getConfigScreen());
                }
            }
            while(KeybindManager.randomizeColours.wasPressed()) {
                if(client.player != null) {
                    Config.getInstance().randomizeColours = !Config.getInstance().randomizeColours;
                    ConfigManager.saveConfig();
                    if(Config.getInstance().randomizeColours) {
                        client.player.sendMessage(Text.of("Colours have been randomized"), false);
                    } else {
                        client.player.sendMessage(Text.of("Colours have been unrandomized"), false);
                    }
                    RandoAssistantClient.seed++;
                    client.worldRenderer.reload();
                }
            }
        });
    }
}
