package com.bawnorton.randoassistant.networking.client;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.networking.NetworkingConstants;
import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.tracking.Tracker;
import com.bawnorton.randoassistant.tracking.trackable.TrackableCrawler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.CLEAR_CACHE_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                TrackableCrawler.clearCache();
                Tracker.getInstance().clearCache();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.DEBUG_PACKET, (client, handler, buf, responseSender) -> {
            ItemStack stack = buf.readItemStack();
            Identifier id = buf.readRegistryValue(Registries.CUSTOM_STAT);
            client.execute(() -> {
                RandoAssistant.LOGGER.info("Loot table id: " + id);
                Tracker.getInstance().debug(stack.getItem());
            });
        });
    }
}
