package com.bawnorton.randoassistant.command;

import com.bawnorton.randoassistant.networking.Networking;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class CommandHandler {
    public static void init() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            registerEnableAllCommand(dispatcher);
            registerDisableAllCommand(dispatcher);
        }));
    }

    private static void registerEnableAllCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("enableall").executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayer();
            Networking.sendEnableAllPacket(player);
            return 1;
        }));
    }

    private static void registerDisableAllCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("disableall").executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayer();
            Networking.sendDisableAllPacket(player);
            return 1;
        }));
    }
}
