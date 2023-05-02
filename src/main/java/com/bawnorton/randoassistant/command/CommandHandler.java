package com.bawnorton.randoassistant.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandHandler {
    public static void init() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
        }));
    }
}
