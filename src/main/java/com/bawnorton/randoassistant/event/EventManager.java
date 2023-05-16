package com.bawnorton.randoassistant.event;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.networking.Networking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class EventManager {

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            if(player == null) {
                RandoAssistant.LOGGER.error("Player is null, cannot load loot tables");
                return;
            }
            Networking.sendClearCachePacket(player);
            RandoAssistant.getAllLootTables(player);
            RandoAssistant.getAllInteractions(player);
            RandoAssistant.getAllRecipes(player);
        });
    }
}
