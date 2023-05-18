package com.bawnorton.randoassistant.networking.client;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.networking.NetworkingConstants;
import com.bawnorton.randoassistant.networking.SerializeableCrafting;
import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.tracking.Tracker;
import com.bawnorton.randoassistant.tracking.trackable.TrackableCrawler;
import com.bawnorton.randoassistant.util.LootAdvancement;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class Networking {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.LOOT_TABLE_PACKET, (client, handler, buf, responseSender) -> {
            SerializeableLootTable lootTable = new SerializeableLootTable(buf.getWrittenBytes());
            client.execute(() -> Tracker.getInstance().track(lootTable));
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.INTERACTION_PACKET, (client, handler, buf, responseSender) -> {
            SerializeableInteraction interaction = new SerializeableInteraction(buf.getWrittenBytes());
            client.execute(() -> Tracker.getInstance().track(interaction));
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.CRAFTING_PACKET, (client, handler, buf, responseSender) -> {
            SerializeableCrafting crafting = new SerializeableCrafting(buf.getWrittenBytes());
            client.execute(() -> Tracker.getInstance().track(crafting));
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.CLEAR_CACHE_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> Tracker.getInstance().clearCache());
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.DEBUG_PACKET, (client, handler, buf, responseSender) -> {
            ItemStack stack = buf.readItemStack();
            Identifier id = buf.readRegistryValue(Registries.CUSTOM_STAT);
            client.execute(() -> {
                RandoAssistant.LOGGER.info("Loot table id: " + id);
                Tracker.getInstance().debug(stack.getItem());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.HANDSHAKE_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> RandoAssistantClient.isInstalledOnServer = true);
        });
    }

    public static void requestHandshakePacket() {
        ClientPlayNetworking.send(NetworkingConstants.HANDSHAKE_PACKET, PacketByteBufs.create());
    }

    public static void requestStatsPacket() {
        ClientPlayNetworking.send(NetworkingConstants.STATS_PACKET, PacketByteBufs.create());
    }

    public static void requestAdvancementUnlock(LootAdvancement advancement) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(advancement.ordinal());
        ClientPlayNetworking.send(NetworkingConstants.ADVANCEMENT_UNLOCK_PACKET, buf);
    }
}
