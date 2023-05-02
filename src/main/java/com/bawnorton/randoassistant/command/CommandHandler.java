package com.bawnorton.randoassistant.command;

import com.bawnorton.randoassistant.networking.Networking;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CommandHandler {
    public static void init() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            registerUpdateDrawingCommand(dispatcher);
        }));
    }

    private static void registerUpdateDrawingCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("updatedrawing").executes(context -> {
            Networking.sendUpdateDrawingPacket(context.getSource().getPlayer());
            return 0;
        }));
    }
}
