package com.bawnorton.randoassistant.event;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.tracking.Tracker;
import com.bawnorton.randoassistant.tracking.trackable.TrackableCrawler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

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
    }
}
