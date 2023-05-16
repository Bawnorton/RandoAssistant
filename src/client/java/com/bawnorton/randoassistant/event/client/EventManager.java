package com.bawnorton.randoassistant.event.client;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.networking.client.Networking;
import com.bawnorton.randoassistant.tracking.Tracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class EventManager {

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            RandoAssistantClient.isInstalledOnServer = false;
            Config.getInstance().enableOverride = false;
            Tracker.getInstance().clear();
            Networking.requestHandshakePacket();
        });
    }
}
