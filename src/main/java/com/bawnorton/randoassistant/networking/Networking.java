package com.bawnorton.randoassistant.networking;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class Networking {
    public static final Object SERVER_LOCK = new Object();
    public static MinecraftServer server;

    private static boolean initialized = false;

    public static void sendBrokeBlockPacket(ServerPlayerEntity entity) {
        waitForServer(() -> ServerPlayNetworking.send(entity, NetworkingConstants.BROKE_BLOCK_PACKET, PacketByteBufs.create()));
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
