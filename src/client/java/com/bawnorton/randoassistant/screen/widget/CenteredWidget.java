package com.bawnorton.randoassistant.screen.widget;

import net.minecraft.client.MinecraftClient;

public interface CenteredWidget {
    default int x() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - getWidth() / 2;
    }

    default int y() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 - getHeight() + getYOffset();
    }

    int getWidth();
    int getHeight();
    int getYOffset();
}
