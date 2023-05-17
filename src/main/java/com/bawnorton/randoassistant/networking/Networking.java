package com.bawnorton.randoassistant.networking;

import com.bawnorton.randoassistant.RandoAssistant;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class Networking {
    public static final Object SERVER_LOCK = new Object();
    public static MinecraftServer server;
    private static boolean initialized = false;

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.HANDSHAKE_PACKET, (server, player, handler, buf, responseSender) -> sendHandshakePacket(player));
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.STATS_PACKET, (server, player, handler, buf, responseSender) -> waitForServer(() -> player.getStatHandler().sendStats(player)));
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.ADVANCEMENT_UNLOCK_PACKET, (server, player, handler, buf, responseSender) -> waitForServer(() -> {
            int i = buf.readInt();
            ServerAdvancementLoader loader = server.getAdvancementLoader();
            Advancement advancement = switch (i) {
                case 0 -> loader.get(new Identifier(RandoAssistant.MOD_ID, "all_loottables"));
                case 1 -> loader.get(new Identifier(RandoAssistant.MOD_ID, "all_block_loottables"));
                case 2 -> loader.get(new Identifier(RandoAssistant.MOD_ID, "all_entity_loottables"));
                case 3 -> loader.get(new Identifier(RandoAssistant.MOD_ID, "all_other_loottables"));
                default -> null;
            };
            if(advancement == null) return;
            AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
            if(progress.isDone()) return;
            for(String criterion : progress.getUnobtainedCriteria()) {
                player.getAdvancementTracker().grantCriterion(advancement, criterion);
            }
        }));
    }

    public static void sendSerializeablePacket(ServerPlayerEntity player, Serializeable serializeable) {
        waitForServer(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBytes(serializeable.toBytes());
            ServerPlayNetworking.send(player, serializeable.getTypePacket(), buf);
        });
    }

    public static void sendClearCachePacket(ServerPlayerEntity serverPlayer) {
        waitForServer(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            ServerPlayNetworking.send(serverPlayer, NetworkingConstants.CLEAR_CACHE_PACKET, buf);
        });
    }

    public static void sendDebugPacket(ServerPlayerEntity player, Item item) {
        waitForServer(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            Block block = Block.getBlockFromItem(item);
            buf.writeItemStack(item.getDefaultStack());
            buf.writeRegistryValue(Registries.CUSTOM_STAT, block.getLootTableId());
            ServerPlayNetworking.send(player, NetworkingConstants.DEBUG_PACKET, buf);
        });
    }

    public static void sendHandshakePacket(ServerPlayerEntity player) {
        waitForServer(() -> {
            ServerPlayNetworking.send(player, NetworkingConstants.HANDSHAKE_PACKET, PacketByteBufs.create());
            player.getStatHandler().sendStats(player);
        });
    }

    private static void waitForServer(Runnable runnable) {
        if(server == null || !initialized) {
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
