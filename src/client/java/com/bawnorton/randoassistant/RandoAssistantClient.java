package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.event.EventManager;
import com.bawnorton.randoassistant.file.FileManager;
import com.bawnorton.randoassistant.file.config.ConfigManager;
import com.bawnorton.randoassistant.keybind.KeybindManager;
import net.fabricmc.api.ClientModInitializer;

public class RandoAssistantClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.loadConfig();
        FileManager.init();
        KeybindManager.init();
        EventManager.init();
    }
}