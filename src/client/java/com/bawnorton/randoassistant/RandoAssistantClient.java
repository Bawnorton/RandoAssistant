package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.config.ConfigManager;
import com.bawnorton.randoassistant.event.client.EventManager;
import com.bawnorton.randoassistant.networking.client.Networking;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.math.random.Random;

public class RandoAssistantClient implements ClientModInitializer {
    public static long seed = Random.create().nextLong();
    public static boolean isInstalledOnServer = false;
    public static boolean datapackDetected = false;

    @Override
    public void onInitializeClient() {
        Networking.init();
        ConfigManager.loadConfig();
        EventManager.init();
    }

    public static boolean hideStar() {
        return Config.getInstance().autoToggle && !datapackDetected;
    }
}