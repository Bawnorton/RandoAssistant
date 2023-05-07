package com.bawnorton.randoassistant.networking;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class Networking {
    public static final Object SERVER_LOCK = new Object();
    public static MinecraftServer server;

    private static boolean initialized = false;

    public static void sendStatsPacket(ServerPlayerEntity player) {
        player.getStatHandler().sendStats(player);
    }

    public static void sendLootTablePacket(ServerPlayerEntity player, SerializeableLootTable lootTable) {
        waitForServer(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBytes(lootTable.toBytes());
            ServerPlayNetworking.send(player, NetworkingConstants.LOOT_TABLE_PACKET, buf);
        });
    }

    public static void sendInteractionPacket(ServerPlayerEntity player, SerializeableInteraction interaction) {
        waitForServer(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBytes(interaction.toBytes());
            ServerPlayNetworking.send(player, NetworkingConstants.INTERACTION_PACKET, buf);
        });
    }

    public static void sendFinishedPacket(ServerPlayerEntity serverPlayer) {
        waitForServer(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            ServerPlayNetworking.send(serverPlayer, NetworkingConstants.FINISHED_PACKET, buf);
        });
    }

    private static void waitForServer(Runnable runnable) {
        if(!initialized) {
            new Thread(() -> {
                synchronized (SERVER_LOCK) {
                    while (server == null) {
                        try {
                            SERVER_LOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                initialized = true;
                runnable.run();
            }).start();
        } else {
            runnable.run();
        }
    }
}
