package com.bawnorton.randoassistant.command;

import com.bawnorton.randoassistant.networking.Networking;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class CommandHandler {
    public static void init() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
                registerDebugCommend(dispatcher);
            }
        }));
    }

    private static void registerDebugCommend(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("debug")
                .executes((context) -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    assert player != null;
                    Item item = player.getMainHandStack().getItem();
                    Networking.sendDebugPacket(player, item);
                    return 0;
        }));
    }
}
