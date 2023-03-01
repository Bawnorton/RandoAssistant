package com.bawnorton.randoassistant.screen.widget;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.screen.LootTableScreen;
import com.bawnorton.randoassistant.screen.widget.drawable.NodeWidget;
import io.github.cottonmc.cotton.gui.widget.WLabeledSlider;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ShowOneLineWidget extends WLabeledSlider {

    public ShowOneLineWidget() {
        super(-1, 9);
        value = RandoAssistantClient.showLine;
        if(value == -1) {
            setLabel(Text.of("Showing All Lines"));
        } else {
            setLabel(Text.of("Showing Line " + (value + 1)));
        }
    }

    @Override
    protected void onValueChanged(int value) {
        super.onValueChanged(value);
        RandoAssistantClient.showLine = value;
        NodeWidget.refreshSelectedNode();
        if(value == -1) {
            setLabel(Text.of("Showing All Lines"));
        } else {
            setLabel(Text.of("Showing Line " + (value + 1)));
        }
    }

    @Override
    public InputResult onMouseUp(int x, int y, int button) {
        if(RandoAssistantClient.hideOtherNodes) {
            LootTableScreen.instance.redrawWithSelectedNode();
        }
        return super.onMouseUp(x, y, button);
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        this.x = MinecraftClient.getInstance().getWindow().getScaledWidth() - 180;
        this.y = MinecraftClient.getInstance().getWindow().getScaledHeight() - 100;

        matrices.push();
        matrices.translate(0, 0, 100);
        super.paint(matrices, x, y, mouseX, mouseY);
        matrices.pop();

        if(Config.getInstance().debug) {
            DrawableHelper.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x80ff0000);
        }
    }
}
