package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.graph.LootTableGraph;
import com.bawnorton.randoassistant.screen.widget.*;
import com.bawnorton.randoassistant.screen.widget.drawable.NodeWidget;
import com.bawnorton.randoassistant.thread.GraphTaskExecutor;
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

    private final GraphTaskExecutor executor;
    private final WPlainPanel panel;

    public LootTableScreen() {
        instance = this;

        MinecraftClient.getInstance().getWindow().setScaleFactor(RandoAssistantClient.SCALE.get());
        executor = RandoAssistant.lootTableMap.getGraph().getExecutor();

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

        redraw();
    }

    public static LootTableScreen getInstance() {
        return instance;
    }

    public void redrawWithSelectedNode() {
        NodeWidget selected = NodeWidget.getSelectedNode();
        if(selected == null) return;

        executor.draw(selected.getVertex(), () -> {
            RandoAssistant.LOGGER.info("Successfully drew graph");
            drawGraph(executor.getDrawing());
        }, () -> {
            clearPanel();
            CenteredLabelWidget failedLabel = new CenteredLabelWidget("Failed to draw graph");
            CenteredLabelWidget reasonLabel = new CenteredLabelWidget("Reason: " + executor.getErrorMessage(), 20);
            panel.add(failedLabel, failedLabel.x(), failedLabel.y());
            panel.add(reasonLabel, reasonLabel.x(), reasonLabel.y());
        });
        clearPanel();
        CenteredLabelWidget drawingLabel = new CenteredLabelWidget("Drawing graph...");
        panel.add(drawingLabel, drawingLabel.x(), drawingLabel.y());
    }

    public void redraw() {
        executor.draw(() -> {
            RandoAssistant.LOGGER.info("Successfully drew graph");
            drawGraph(executor.getDrawing());
        }, () -> {
            clearPanel();
            CenteredLabelWidget failedLabel = new CenteredLabelWidget("Failed to draw graph");
            CenteredLabelWidget reasonLabel = new CenteredLabelWidget("Reason: " + executor.getErrorMessage(), 20);
            panel.add(failedLabel, failedLabel.x(), failedLabel.y());
            panel.add(reasonLabel, reasonLabel.x(), reasonLabel.y());
        });
        clearPanel();
        CenteredLabelWidget drawingLabel = new CenteredLabelWidget("Drawing graph...");
        panel.add(drawingLabel, drawingLabel.x(), drawingLabel.y());
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

    private void clearPanel() {
        for (WWidget widget : panel.streamChildren().toList()) {
            panel.remove(widget);
        }
    }

    @Override
    public void addPainters() {
    }
}
