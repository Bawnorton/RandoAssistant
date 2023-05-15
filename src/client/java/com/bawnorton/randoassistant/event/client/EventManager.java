package com.bawnorton.randoassistant.event.client;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.networking.client.Networking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class EventManager {

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            RandoAssistantClient.isInstalledOnServer = false;
            Networking.requestHandshakePacket();
        });
    }
}
