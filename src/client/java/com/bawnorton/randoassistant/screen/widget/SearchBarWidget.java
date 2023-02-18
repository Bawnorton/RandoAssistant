package com.bawnorton.randoassistant.screen.widget;

import com.bawnorton.randoassistant.screen.widget.drawable.NodeWidget;
import com.bawnorton.randoassistant.search.SearchManager;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Optional;

public class SearchBarWidget extends WTextField {
    private static SearchManager<NodeWidget> searchManager;
    private final GraphDisplayWidget graphDisplay;

    public SearchBarWidget(GraphDisplayWidget graphDisplay) {
        super(Text.of("Search..."));
        this.setEditable(true);
        this.setChangedListener(this::inputChanged);
        setMaxLength(100);

        this.graphDisplay = graphDisplay;
        searchManager = new SearchManager<>(graphDisplay.getNodes());
    }

    public void inputChanged(String text) {
        Optional<NodeWidget> node = searchManager.getBestMatch(text);
        if (node.isPresent()) {
            node.get().select();
            graphDisplay.centerOnNode(node.get());
        } else {
            NodeWidget.deselect();
        }
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
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
