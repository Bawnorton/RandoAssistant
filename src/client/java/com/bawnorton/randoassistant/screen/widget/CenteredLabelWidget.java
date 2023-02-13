package com.bawnorton.randoassistant.screen.widget;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class CenteredLabelWidget extends WLabel {
    private final int yOffset;

    public CenteredLabelWidget(String text, int yOffset) {
        super(Text.of(text));
        this.width = MinecraftClient.getInstance().textRenderer.getWidth(text);
        this.height = 20;
        this.yOffset = yOffset;
    }

    public CenteredLabelWidget(String text) {
        this(text, 0);
    }

    public int x() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - this.width / 2;
    }

    public int y() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 - this.height / 2 + yOffset;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer renderer = mc.textRenderer;
        int yOffset = switch (verticalAlignment) {
            case CENTER -> height / 2 - renderer.fontHeight / 2;
            case BOTTOM -> height - renderer.fontHeight;
            case TOP -> 0;
        };

        ScreenDrawing.drawString(matrices, text.asOrderedText(), horizontalAlignment, x, y + yOffset, this.getWidth(), -1);

        Style hoveredTextStyle = getTextStyleAt(mouseX, mouseY);
        ScreenDrawing.drawTextHover(matrices, hoveredTextStyle, x + mouseX, y + mouseY);
    }
}
