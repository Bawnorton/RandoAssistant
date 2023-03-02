package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.graph.GraphDrawer;
import com.bawnorton.randoassistant.graph.LootTableGraph;
import com.bawnorton.randoassistant.screen.widget.*;
import com.bawnorton.randoassistant.screen.widget.drawable.NodeWidget;
import grapher.graph.drawing.Drawing;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class LootTableScreen extends LightweightGuiDescription {
    private static LootTableScreen instance;

    private final WPlainPanel panel;
    private final GraphDrawer drawer;

    public LootTableScreen() {
        instance = this;
        LootTableGraph graph = RandoAssistant.lootTableMap.getGraph();
        drawer = graph.getDrawer();
        MinecraftClient.getInstance().getWindow().setScaleFactor(RandoAssistantClient.SCALE.get());

        panel = new WPlainPanel() {
            @Override
            public InputResult onMouseScroll(int x, int y, double amount) {
                width = MinecraftClient.getInstance().getWindow().getScaledWidth();
                height = MinecraftClient.getInstance().getWindow().getScaledHeight();
                return super.onMouseScroll(x, y, amount);
            }

            @Override
            public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
                if(Config.getInstance().debug) {
                    MinecraftClient.getInstance().currentScreen.renderOrderedTooltip(matrices, MinecraftClient.getInstance().textRenderer.wrapLines(Text.of("Mouse: " + mouseX + ", " + mouseY), 200), mouseX, mouseY);
                }
                super.paint(matrices, x, y, mouseX, mouseY);
            }
        };
        setRootPanel(panel);
        setFullscreen(true);
        panel.setInsets(Insets.NONE);
        panel.setBackgroundPainter(((matrices, left, top, panel1) -> {}));
        CenteredLabelWidget drawingLabel = new CenteredLabelWidget("Drawing graph...");
        panel.add(drawingLabel, drawingLabel.x(), drawingLabel.y());

        updateGraph();
    }

    public static LootTableScreen getInstance() {
        return instance;
    }

    public void redrawWithSelectedNode() {
        NodeWidget selected = NodeWidget.getSelectedNode();
        if(selected == null) return;

        drawer.updateDrawing(selected.getVertex());
        clearPanel();
        CenteredLabelWidget drawingLabel = new CenteredLabelWidget("Drawing graph...");
        panel.add(drawingLabel, drawingLabel.x(), drawingLabel.y());
        updateGraph();
    }

    public void redraw() {
        drawer.updateDrawing();
        clearPanel();
        CenteredLabelWidget drawingLabel = new CenteredLabelWidget("Drawing graph...");
        panel.add(drawingLabel, drawingLabel.x(), drawingLabel.y());
        updateGraph();
    }

    private void drawGraph(Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing) {
        GraphDisplayWidget graphDisplayWidget = new GraphDisplayWidget(drawing);
        SearchBarWidget searchBar = new SearchBarWidget();
        HideOtherNodesWidget hideOtherNodesWidget = new HideOtherNodesWidget();
        HideChildrenWidget hideChildrenWidget = new HideChildrenWidget();
        SearchTypeWidget searchTypeWidget = new SearchTypeWidget();
        ShowOneLineWidget showOneLineWidget = new ShowOneLineWidget();
        showOneLineWidget.setMaxValue(graphDisplayWidget.getLineCount() - 1);
        showOneLineWidget.setValue(-1, true);

        Window window = MinecraftClient.getInstance().getWindow();

        if(!RandoAssistantClient.hideOtherNodes) {
            graphDisplayWidget.centerOnNode(NodeWidget.getSelectedNode());
        } else {
            graphDisplayWidget.resetOffset();
        }

        clearPanel();
        panel.add(graphDisplayWidget, 0, 0);
        panel.add(searchBar, 30, 40, window.getScaledWidth() - 220, 20);
        panel.add(searchTypeWidget, window.getScaledWidth() - 160, 40, 120, 20);
        panel.add(hideChildrenWidget, window.getScaledWidth() - 180, window.getScaledHeight() - 40, 140, 20);
        panel.add(hideOtherNodesWidget, window.getScaledWidth() - 180, window.getScaledHeight() - 70, 140, 20);
        panel.add(showOneLineWidget, window.getScaledWidth() - 180, window.getScaledHeight() - 100, 140, 20);

        if(searchBar.getHost() == null) {
            searchBar.setHost(this);
        }
        if(showOneLineWidget.getHost() == null) {
            showOneLineWidget.setHost(this);
        }
    }

    public void updateGraph() {
        Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing = drawer.getDrawing();
        if (drawing == null) {
            drawer.afterDrawing(() -> drawGraph(drawer.getDrawing()), () -> {
                clearPanel();
                CenteredLabelWidget failedLabel = new CenteredLabelWidget("Failed to draw graph");
                CenteredLabelWidget reasonLabel = new CenteredLabelWidget("Reason: " + drawer.getErrorMessage(), 20);
                panel.add(failedLabel, failedLabel.x(), failedLabel.y());
                panel.add(reasonLabel, reasonLabel.x(), reasonLabel.y());
            });
        } else {
            drawGraph(drawing);
        }
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
