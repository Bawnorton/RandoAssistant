package com.bawnorton.randoassistant.screen.widget;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.screen.widget.drawable.NodeWidget;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class HideChildrenWidget extends WButton {

    public HideChildrenWidget() {
        this.setLabel(Text.of(RandoAssistantClient.hideChildren ? "Show Children" : "Hide Children"));
    }

    @Override
    public InputResult onClick(int x, int y, int button) {
        InputResult result = super.onClick(x, y, button);
        if (RandoAssistantClient.hideChildren) {
            this.setLabel(Text.of("Hide Children"));
        } else {
            this.setLabel(Text.of("Show Children"));
        }
        RandoAssistantClient.hideChildren = !RandoAssistantClient.hideChildren;
        NodeWidget.refreshSelectedNode();
        return result;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        this.x = MinecraftClient.getInstance().getWindow().getScaledWidth() - 180;
        this.y = MinecraftClient.getInstance().getWindow().getScaledHeight() - 40;

        matrices.push();
        matrices.translate(0, 0, 100);
        super.paint(matrices, x, y, mouseX, mouseY);
        matrices.pop();

        if (Config.getInstance().debug) {
            DrawableHelper.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x80ff0000);
        }
    }
}
