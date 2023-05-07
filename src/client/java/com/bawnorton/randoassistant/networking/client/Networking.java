package com.bawnorton.randoassistant.networking.client;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.networking.NetworkingConstants;
import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.tracking.Tracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.FINISHED_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                Set<Identifier> trackedLootTables = new HashSet<>();
                Tracker.getInstance().getGraph().forEach(vertex -> {
                    if(!trackedLootTables.add(vertex.getIdentifier())) {
                        RandoAssistant.LOGGER.warn("Duplicate loot table: " + vertex.getIdentifier().toString());
                    }
                });
            });
        });
    }
}
