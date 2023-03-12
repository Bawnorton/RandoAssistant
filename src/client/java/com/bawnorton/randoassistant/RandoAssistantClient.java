package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.config.ConfigManager;
import com.bawnorton.randoassistant.event.EventManager;
import com.bawnorton.randoassistant.file.FileManager;
import com.bawnorton.randoassistant.graph.InteractionMap;
import com.bawnorton.randoassistant.graph.LootTableMap;
import com.bawnorton.randoassistant.keybind.KeybindManager;
import com.bawnorton.randoassistant.networking.client.Networking;
import com.bawnorton.randoassistant.util.Easing;
import com.bawnorton.randoassistant.util.Status;
import com.bawnorton.randoassistant.util.Wrapper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RandoAssistantClient implements ClientModInitializer {
    private static final Identifier STAR = new Identifier(RandoAssistant.MOD_ID, "textures/gui/item_stars.png");

    public static Wrapper<Double> SCALE = Wrapper.ofNothing();
    public static Wrapper<Double> ACTUAL_SCALE = Wrapper.ofNothing();

    public static boolean hideOtherNodes = false;
    public static boolean hideChildren = false;
    public static int showLine = -1;

    public static LootTableMap lootTableMap;
    public static InteractionMap interactionMap;

    public static Status saveStatus = Status.NONE;
    public static Status dumpStatus = Status.NONE;

    @Override
    public void onInitializeClient() {
        Networking.init();

        ConfigManager.loadConfig();
        FileManager.init();
        KeybindManager.init();
        EventManager.init();
    }

    public static void renderStar(MatrixStack matrices, int x, int y) {
        float timeOffset = Math.abs(((System.currentTimeMillis() % 2000) / 1000.0f) - 1.0f);

        matrices.push();
        matrices.translate(0, -Easing.ease(0, 1, timeOffset), 410);
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderTexture(0, STAR);
        DrawableHelper.drawTexture(matrices, x, y, 0, 0, 8, 8, 16, 16);
        matrices.pop();
    }
}