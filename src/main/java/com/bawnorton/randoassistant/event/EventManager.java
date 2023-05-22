package com.bawnorton.randoassistant.event;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.networking.Networking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class EventManager {

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {

        });
    }
}
