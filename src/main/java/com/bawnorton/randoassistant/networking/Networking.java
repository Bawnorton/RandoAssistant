package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.util.LootAdvancement;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class Networking {
    private static MinecraftServer server;

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.HANDSHAKE_PACKET, (server, player, handler, buf, responseSender) -> {
            Networking.server = server;
            sendHandshakePacket(player);
        });
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.STATS_PACKET, (server, player, handler, buf, responseSender) -> player.getStatHandler().sendStats(player));
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.ADVANCEMENT_UNLOCK_PACKET, (server, player, handler, buf, responseSender) -> LootAdvancement.fromOrdinal(buf.readInt()).grant(player));
    }

    public static void sendSerializeablePacket(ServerPlayerEntity player, Serializeable serializeable) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBytes(serializeable.toBytes());
        ServerPlayNetworking.send(player, serializeable.getTypePacket(), buf);
    }

    public static void sendClearCachePacket(ServerPlayerEntity serverPlayer) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(serverPlayer, NetworkingConstants.CLEAR_CACHE_PACKET, buf);
    }

    public static void sendDebugPacket(ServerPlayerEntity player, Item item) {
        PacketByteBuf buf = PacketByteBufs.create();
        Block block = Block.getBlockFromItem(item);
        buf.writeItemStack(item.getDefaultStack());
        buf.writeRegistryValue(Registries.CUSTOM_STAT, block.getLootTableId());
        ServerPlayNetworking.send(player, NetworkingConstants.DEBUG_PACKET, buf);
    }

    public static void sendHandshakePacket(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, NetworkingConstants.HANDSHAKE_PACKET, PacketByteBufs.create());
        player.getStatHandler().sendStats(player);
        Networking.sendClearCachePacket(player);
        RandoAssistant.getAllLootTables(player, server);
        RandoAssistant.getAllRecipes(player, server);
        RandoAssistant.getAllInteractions(player);
    }

    public static MinecraftServer getServer() {
        if(server == null) throw new NullPointerException("Server has not been initialized yet!");
        return server;
    }
}
