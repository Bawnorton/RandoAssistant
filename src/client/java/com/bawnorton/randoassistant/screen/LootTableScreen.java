package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.screen.widget.*;
import com.bawnorton.randoassistant.graph.GraphDrawer;
import com.bawnorton.randoassistant.graph.LootTableGraph;
import grapher.graph.drawing.Drawing;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.MinecraftClient;

public class LootTableScreen extends LightweightGuiDescription {
    private final WPlainPanel panel;

    public LootTableScreen() {
        LootTableGraph graph = RandoAssistant.lootTableMap.getGraph();
        GraphDrawer drawer = graph.getDrawer();
        Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing = drawer.getDrawing();

        panel = new WPlainPanel();
        setRootPanel(panel);
        setFullscreen(true);
        panel.setInsets(Insets.NONE);
        panel.setBackgroundPainter(((matrices, left, top, panel1) -> {
        }));
        CenteredLabelWidget drawingLabel = new CenteredLabelWidget("Drawing graph...");
        panel.add(drawingLabel, drawingLabel.x(), drawingLabel.y());

        if (drawing == null) {
            drawer.afterDrawing(() -> {
                if (drawer.didFailToDraw()) {
                    clearPanel();
                    CenteredLabelWidget failedLabel = new CenteredLabelWidget("Failed to draw graph");
                    CenteredLabelWidget reasonLabel = new CenteredLabelWidget("Reason:" + drawer.getErrorMessage(), 20);
                    panel.add(failedLabel, failedLabel.x(), failedLabel.y());
                    panel.add(reasonLabel, reasonLabel.x(), reasonLabel.y());
                    if (!drawer.getErrorMessage().contains("Null")) {
                        CenteredButtonWidget retryButton = new CenteredButtonWidget("Retry", 40);
                        retryButton.setOnClick(() -> MinecraftClient.getInstance().setScreen(new CottonClientScreen(new LootTableScreen())));
                        panel.add(retryButton, retryButton.x(), retryButton.y());
                    }
                    return;
                }
                drawGraph(drawer.getDrawing());
            });
        } else {
            drawGraph(drawing);
        }
    }

    private void drawGraph(Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing) {
        GraphDisplayWidget graphDisplayWidget = new GraphDisplayWidget(drawing);
        SearchBarWidget searchBar = new SearchBarWidget(graphDisplayWidget);
        SearchTypeWidget searchTypeWidget = new SearchTypeWidget(searchBar);

        clearPanel();
        panel.add(graphDisplayWidget, 0, 0);
        panel.add(searchBar, 40, 40, MinecraftClient.getInstance().getWindow().getScaledWidth() - 220, 20);
        panel.add(searchTypeWidget, MinecraftClient.getInstance().getWindow().getScaledWidth() - 160, 40, 120, 20);
    }

    private void clearPanel() {
        for (WWidget widget : panel.streamChildren().toList()) {
            panel.remove(widget);
        }
    }

    @Override
    public void addPainters() {
    }
}
