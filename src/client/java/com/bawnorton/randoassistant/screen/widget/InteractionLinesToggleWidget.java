package com.bawnorton.randoassistant.screen.widget;

import com.bawnorton.randoassistant.file.config.Config;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class InteractionLinesToggleWidget extends WButton {
    private final GraphDisplayWidget graphDisplayWidget;
    private boolean isOn = Config.getInstance().showInteractionLines;

    public InteractionLinesToggleWidget(GraphDisplayWidget graphDisplayWidget) {
        super();
        this.setLabel(Text.of(isOn ? "Show Crafting Lines" : "Hide Crafting Lines"));
        this.graphDisplayWidget = graphDisplayWidget;
    }

    @Override
    public InputResult onClick(int x, int y, int button) {
        InputResult result = super.onClick(x, y, button);
        this.isOn = !this.isOn;
        graphDisplayWidget.displayCraftingLines = this.isOn;
        Config.getInstance().showInteractionLines = this.isOn;
        if (this.isOn) {
            this.setLabel(Text.of("Show Crafting Lines"));
        } else {
            this.setLabel(Text.of("Hide Crafting Lines"));
        }
        return result;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        matrices.push();
        matrices.translate(0, 0, 100);
        super.paint(matrices, x, y, mouseX, mouseY);
        matrices.pop();
    }
}
