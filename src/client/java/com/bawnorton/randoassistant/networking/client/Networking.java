package com.bawnorton.randoassistant.networking.client;

import com.bawnorton.randoassistant.networking.NetworkingConstants;
import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.tracking.Tracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class Networking {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.LOOT_TABLE_PACKET, (client, handler, buf, responseSender) -> {
            SerializeableLootTable lootTable = SerializeableLootTable.fromBytes(buf.getWrittenBytes());
            client.execute(() -> Tracker.getInstance().track(lootTable));
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.INTERACTION_PACKET, (client, handler, buf, responseSender) -> {
            SerializeableInteraction interaction = SerializeableInteraction.fromBytes(buf.getWrittenBytes());
            client.execute(() -> Tracker.getInstance().track(interaction));
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.UPDATE_DRAWING_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
//                TrackingGraph.getInstance();
            });
        });
    }
}
