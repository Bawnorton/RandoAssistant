package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.config.ConfigManager;
import com.bawnorton.randoassistant.event.EventManager;
import com.bawnorton.randoassistant.keybind.KeybindManager;
import com.bawnorton.randoassistant.networking.client.Networking;
import com.bawnorton.randoassistant.util.Easing;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RandoAssistantClient implements ClientModInitializer {
    public static long seed = Random.create().nextLong();

    @Override
    public void onInitializeClient() {
        Networking.init();

        ConfigManager.loadConfig();
        KeybindManager.init();
        EventManager.init();
    }
}