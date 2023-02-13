package com.bawnorton.randoassistant.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class CenteredButtonWidget extends WButton {
    private final int yOffset;

    public CenteredButtonWidget(String text, int yOffset) {
        super(Text.of(text));
        this.width = MinecraftClient.getInstance().getWindow().getScaledWidth() / 4;
        this.height = 20;
        this.yOffset = yOffset;
    }

    public int x() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - this.width / 2;
    }

    public int y() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 - this.height / 2 + yOffset;
    }
}
