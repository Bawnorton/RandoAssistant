package com.bawnorton.randoassistant.event;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.config.ConfigManager;
import com.bawnorton.randoassistant.file.FileManager;
import com.bawnorton.randoassistant.graph.InteractionMap;
import com.bawnorton.randoassistant.graph.LootTableMap;
import com.bawnorton.randoassistant.keybind.KeybindManager;
import com.bawnorton.randoassistant.util.Status;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.text.Text;

import java.util.Objects;

public class EventManager {

    public static void init() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ConfigManager.saveConfig();
            try {
                FileManager.saveLootTableMap();
                FileManager.saveInteractionMap();

                RandoAssistant.LOGGER.info("Saved loot table map with " + RandoAssistantClient.lootTableMap.getGraph().getVertices().size() + " entries");
                RandoAssistantClient.saveStatus = Status.SUCCESS;
            } catch (Exception e) {
                RandoAssistant.LOGGER.error("Failed to save randoassistant data", e);
                RandoAssistantClient.saveStatus = Status.FAILURE;

                RandoAssistant.LOGGER.info("Attempting to dump data to most recent save");

                try {
                    FileManager.dumpLootTableMap();
                    FileManager.dumpInteractionMap();

                    RandoAssistant.LOGGER.info("Dumped loot table map with " + RandoAssistantClient.lootTableMap.getGraph().getVertices().size() + " entries");
                    RandoAssistantClient.dumpStatus = Status.SUCCESS;
                } catch (Exception e2) {
                    RandoAssistant.LOGGER.error("Failed to dump randoassistant data", e2);
                    RandoAssistantClient.dumpStatus = Status.FAILURE;
                    FileManager.createFailureZip();
                }
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            try {
                FileManager.loadLootTableMap();
                FileManager.loadInteractionMap();
                RandoAssistantClient.lootTableMap.getGraph().getExecutor().draw();

                ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
                if (networkHandler != null) {
                    networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
                }
                RandoAssistant.LOGGER.info("Loaded loot table map with " + RandoAssistantClient.lootTableMap.getGraph().getVertices().size() + " entries");


                if(RandoAssistantClient.lootTableMap.getGraph().getVertices().size() != 0 && Config.getInstance().toasts) {
                    MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                            SystemToast.Type.UNSECURE_SERVER_WARNING,
                            Text.of("§b[RandoAssistant]"),
                            Text.of("Successfully loaded " + RandoAssistantClient.lootTableMap.getGraph().getVertices().size() + " loot tables")
                    ));
                }
            } catch (Exception e) {
                RandoAssistant.LOGGER.error("Failed to load randoassistant data", e);
                MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                        SystemToast.Type.UNSECURE_SERVER_WARNING,
                        Text.of("§cFailed to load RandoAssistant data"),
                        Text.of("Attempting to load from most recent save")
                ));

                try {
                    String seed = FileManager.loadLootTableMapRecovery();
                    FileManager.loadInteractionMapRecovery();
                    RandoAssistantClient.lootTableMap.getGraph().getExecutor().draw();

                    ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
                    if (networkHandler != null) {
                        networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
                    }

                    RandoAssistant.LOGGER.info("Recovered loot table map with " + RandoAssistantClient.lootTableMap.getGraph().getVertices().size() + " entries");

                    MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                            SystemToast.Type.UNSECURE_SERVER_WARNING,
                            Text.of("§b[RandoAssistant]"),
                            Text.of("Successfully recovered " + RandoAssistantClient.lootTableMap.getGraph().getVertices().size() + " loot tables (" + seed + ")")
                    ));
                } catch (Exception e2) {
                    RandoAssistant.LOGGER.error("Failed to load randoassistant data from most recent save", e2);
                    MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                            SystemToast.Type.UNSECURE_SERVER_WARNING,
                            Text.of("§cFailed to recover RandoAssistant data from most recent save"),
                            Text.of("§cPlease send the report generated in the .minecraft folder to the mod author")
                    ));
                    FileManager.createFailureZip();
                }
            }
        });



        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeybindManager.revealKeyBinding.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new ConfirmScreen((result) -> {
                    if (result) {
                        RandoAssistantClient.lootTableMap.getGraph().getExecutor().disableDrawTask();
                        RandoAssistant.addAllLootTables(Objects.requireNonNull(client.player));
                    }
                    MinecraftClient.getInstance().setScreen(null);
                }, Text.of("Add all loot tables?"), Text.of("This may cause the game client and world to lag briefly.")));
            }
            while (KeybindManager.resetKeyBinding.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new ConfirmScreen((result) -> {
                    if (result) {
                        RandoAssistantClient.lootTableMap = new LootTableMap();
                        RandoAssistantClient.interactionMap = new InteractionMap();
                    }
                    MinecraftClient.getInstance().setScreen(null);
                }, Text.of("Reset all loot tables?"), Text.of("This will clear all loot tables from the graph.")));
            }
            while (KeybindManager.configScreen.wasPressed()) {
                if (client.player != null) {
                    MinecraftClient.getInstance().setScreen(ConfigManager.getConfigScreen());
                }
            }
        });
    }
}
