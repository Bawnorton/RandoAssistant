package com.bawnorton.randoassistant.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class SearchBarWidget extends WTextField {
    public SearchBarWidget(GraphDisplayWidget graphDisplayWidget) {
        super(Text.of("Search..."));
        this.setEditable(true);
        this.setChangedListener(graphDisplayWidget::inputChanged);
        setMaxLength(100);
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        matrices.push();
        matrices.translate(0, 0, 500);
        DrawableHelper.fill(matrices, x, y, x + this.getWidth(), y + this.getHeight(), 0x7F000000);
        super.paint(matrices, x, y, mouseX, mouseY);
        matrices.pop();
    }
}
