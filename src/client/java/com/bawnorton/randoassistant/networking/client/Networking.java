package com.bawnorton.randoassistant.networking.client;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.networking.NetworkingConstants;
import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.screen.LootTableScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;

public class Networking {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.BROKE_BLOCK_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
                if(networkHandler == null) return;
                networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.LOOT_TABLE_PACKET, (client, handler, buf, responseSender) -> {
            SerializeableLootTable lootTable = SerializeableLootTable.fromBytes(buf.getWrittenBytes());
            client.execute(() -> RandoAssistantClient.lootTableMap.addLootTable(lootTable));
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.INTERACTION_PACKET, (client, handler, buf, responseSender) -> {
            SerializeableInteraction interaction = SerializeableInteraction.fromBytes(buf.getWrittenBytes());
            client.execute(() -> RandoAssistantClient.interactionMap.addInteraction(interaction));
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.UPDATE_DRAWING_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                RandoAssistantClient.lootTableMap.getGraph().getExecutor().enableDrawTask();
                RandoAssistantClient.lootTableMap.getGraph().getExecutor().markDrawTaskDirty();
                RandoAssistantClient.lootTableMap.getGraph().getExecutor().draw(() -> LootTableScreen.getInstance().redraw());
            });
        });
    }
}
