package com.bawnorton.randoassistant.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindManager {
    public static KeyBinding revealKeyBinding;
    public static KeyBinding resetKeyBinding;
    public static KeyBinding configScreen;
    public static KeyBinding randomizeColours;

    public static void init() {
        revealKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.randoassistant.reveal",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.randoassistant"
        ));
        resetKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.randoassistant.reset",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.randoassistant"
        ));
        configScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.randoassistant.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.randoassistant"
        ));
        randomizeColours = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.randoassistant.randomizer",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.randoassistant"
        ));
    }
}
