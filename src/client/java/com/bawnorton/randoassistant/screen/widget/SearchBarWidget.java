package com.bawnorton.randoassistant.screen.widget;

import com.bawnorton.randoassistant.screen.widget.drawable.NodeWidget;
import com.bawnorton.randoassistant.search.SearchManager;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Optional;

public class SearchBarWidget extends WTextField {
    private static SearchBarWidget instance;
    private static SearchManager<NodeWidget> searchManager;

    public SearchBarWidget() {
        super(Text.of("Search..."));
        instance = this;
        this.setEditable(true);
        this.setChangedListener(this::inputChanged);
        setMaxLength(100);

        searchManager = new SearchManager<>(GraphDisplayWidget.getInstance().getNodes());
    }

    public static SearchBarWidget getInstance() {
        return instance;
    }

    public void inputChanged(String text) {
        Optional<NodeWidget> node = searchManager.getBestMatch(text);
        if (node.isPresent()) {
            node.get().select();
            GraphDisplayWidget.getInstance().centerOnNode(node.get());
        } else {
            NodeWidget.deselect();
        }
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        width = MinecraftClient.getInstance().getWindow().getScaledWidth() - 220;

        matrices.push();
        matrices.translate(0, 0, 500);
        DrawableHelper.fill(matrices, x, y, x + this.getWidth(), y + this.getHeight(), 0x7F000000);
        super.paint(matrices, x, y, mouseX, mouseY);
        matrices.pop();
    }

    public SearchManager<NodeWidget> getManager() {
        return searchManager;
    }
}
