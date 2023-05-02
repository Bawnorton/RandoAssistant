package com.bawnorton.randoassistant.screen.widget;

import com.bawnorton.randoassistant.config.Config;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.DoubleSummaryStatistics;

public class NextMatchWidget extends WButton {
    private static NextMatchWidget instance;
    private int count;

    public NextMatchWidget() {
        super(Text.of("Next"));
        instance = this;
        count = 0;
    }

    public static NextMatchWidget getInstance() {
        return instance;
    }

    public int getCount() {
        return count;
    }

    @Override
    public InputResult onClick(int x, int y, int button) {
        count++;
        SearchBarWidget.getInstance().inputChanged(SearchBarWidget.getInstance().getText());
        return InputResult.PROCESSED;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        this.x = MinecraftClient.getInstance().getWindow().getScaledWidth() - 280;

        matrices.push();
        matrices.translate(0, 0, 100);
        super.paint(matrices, x, y, mouseX, mouseY);

        Tooltip tooltip = Tooltip.of(Text.of("Go to next match"));

        if (isWithinBounds(mouseX, mouseY)) {
            MinecraftClient.getInstance().currentScreen.renderOrderedTooltip(matrices, tooltip.getLines(MinecraftClient.getInstance()), mouseX + x, mouseY + y - height / 2);
        }
        if (Config.getInstance().debug) {
            DrawableHelper.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x80ff0000);
        }
        matrices.pop();
    }
}
