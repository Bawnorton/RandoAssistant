package com.bawnorton.randoassistant.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class CenteredButtonWidget extends WButton implements CenteredWidget {
    private final int yOffset;

    public CenteredButtonWidget(String text, int yOffset) {
        super(Text.of(text));
        this.width = MinecraftClient.getInstance().getWindow().getScaledWidth() / 4;
        this.height = 20;
        this.yOffset = yOffset;
    }

    @Override
    public int getYOffset() {
        return yOffset;
    }
}
