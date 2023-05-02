package com.bawnorton.randoassistant;

import com.bawnorton.randoassistant.config.ConfigManager;
import com.bawnorton.randoassistant.event.EventManager;
import com.bawnorton.randoassistant.keybind.KeybindManager;
import com.bawnorton.randoassistant.networking.client.Networking;
import com.bawnorton.randoassistant.util.Easing;
import com.bawnorton.randoassistant.util.Status;
import com.bawnorton.randoassistant.util.tuples.Wrapper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class RandoAssistantClient implements ClientModInitializer {
    private static final Identifier STAR = new Identifier(RandoAssistant.MOD_ID, "textures/gui/item_stars.png");

    public static Wrapper<Double> SCALE = Wrapper.ofNothing();
    public static Wrapper<Double> ACTUAL_SCALE = Wrapper.ofNothing();

    public static Status saveStatus = Status.NONE;
    public static Status dumpStatus = Status.NONE;
    public static long seed = Random.create().nextLong();

    @Override
    public void onInitializeClient() {
        Networking.init();

        ConfigManager.loadConfig();
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