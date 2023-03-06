package com.bawnorton.randoassistant.networking.client;

import com.bawnorton.randoassistant.networking.NetworkingConstants;
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
    }
}
