package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.command.CommandHandler;
import com.bawnorton.randoassistant.event.EventManager;
import com.bawnorton.randoassistant.item.Wob;
import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.stat.StatsManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandoAssistant implements ModInitializer {
    public static final String MOD_ID = "randoassistant";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item WOB = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "wob"), new Wob(new FabricItemSettings()));

    @Override
    public void onInitialize() {
        LOGGER.info("RandoAssistant Initialised");
        CommandHandler.init();
        StatsManager.init();
        EventManager.init();
        Networking.init();
    }
}