package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.event.EventManager;
import com.bawnorton.randoassistant.file.FileManager;
import com.bawnorton.randoassistant.config.ConfigManager;
import com.bawnorton.randoassistant.keybind.KeybindManager;
import com.bawnorton.randoassistant.util.Wrapper;
import net.fabricmc.api.ClientModInitializer;

public class RandoAssistantClient implements ClientModInitializer {
    public static Wrapper<Double> SCALE = Wrapper.ofNothing();
    public static Wrapper<Double> ACTUAL_SCALE = Wrapper.ofNothing();

    public static boolean hideOtherNodes = false;
    public static boolean hideChildren = false;
    public static int showLine = -1;

    @Override
    public void onInitializeClient() {
        ConfigManager.loadConfig();
        FileManager.init();
        KeybindManager.init();
        EventManager.init();
    }
}