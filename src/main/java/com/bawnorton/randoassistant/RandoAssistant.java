package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.command.CommandHandler;
import com.bawnorton.randoassistant.event.EventManager;
import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.stat.StatsManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandoAssistant implements ModInitializer {
    public static final String MOD_ID = "randoassistant";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("RandoAssistant Initialised");
        CommandHandler.init();
        StatsManager.init();
        EventManager.init();
        Networking.init();
    }
}