package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.file.FileManager;
import com.bawnorton.randoassistant.graph.LootTableGraph;
import com.bawnorton.randoassistant.screen.widget.*;
import com.bawnorton.randoassistant.screen.widget.drawable.NodeWidget;
import com.bawnorton.randoassistant.thread.GraphTaskExecutor;
import grapher.graph.drawing.Drawing;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WLabel;
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
        executor = RandoAssistantClient.lootTableMap.getGraph().getExecutor();

        panel = new WPlainPanel() {
            @Override
            public InputResult onMouseScroll(int x, int y, double amount) {
                width = MinecraftClient.getInstance().getWindow().getScaledWidth();
                height = MinecraftClient.getInstance().getWindow().getScaledHeight();
                return super.onMouseScroll(x, y, amount);
            }

            @Override
            public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
                if (Config.getInstance().debug) {
                    MinecraftClient.getInstance().currentScreen.renderOrderedTooltip(matrices, MinecraftClient.getInstance().textRenderer.wrapLines(Text.of("Mouse: " + mouseX + ", " + mouseY), 200), mouseX, mouseY);
                }
                super.paint(matrices, x, y, mouseX, mouseY);
            }
        };

        setRootPanel(panel);
        setFullscreen(true);
        panel.setInsets(Insets.NONE);
        panel.setBackgroundPainter(((matrices, left, top, panel1) -> {
        }));

        redraw();
    }

    public static LootTableScreen getInstance() {
        return instance;
    }

    public void redrawWithSelectedNode() {
        NodeWidget selected = NodeWidget.getSelectedNode();
        if (selected == null) return;

        executor.draw(selected.getVertex(), () -> drawGraph(executor.getDrawing()), () -> {
            clearPanel();
            FileManager.createFailureZip();
            CenteredLabelWidget failedLabel = new CenteredLabelWidget("Failed to draw graph :(");
            CenteredLabelWidget reasonLabel = new CenteredLabelWidget("Reason: " + (executor.getErrorMessage() == null ? "Unknown" : executor.getErrorMessage()), 20);
            CenteredLabelWidget logLabel = new CenteredLabelWidget("A report has been created in your .minecraft folder", 40);
            CenteredLabelWidget logLabel2 = new CenteredLabelWidget("Please send it to the mod author", 60);
            panel.add(failedLabel, failedLabel.x(), failedLabel.y());
            panel.add(reasonLabel, reasonLabel.x(), reasonLabel.y());
            panel.add(logLabel, logLabel.x(), logLabel.y());
            panel.add(logLabel2, logLabel2.x(), logLabel2.y());
        });
        clearPanel();
        CenteredLabelWidget drawingLabel = new CenteredLabelWidget("Drawing graph...");
        panel.add(drawingLabel, drawingLabel.x(), drawingLabel.y());
    }

    public void redraw() {
        if(RandoAssistantClient.lootTableMap.getGraph().getVertices().isEmpty()) {
            clearPanel();
            CenteredLabelWidget failedLabel = new CenteredLabelWidget("There is nothing to draw");
            panel.add(failedLabel, failedLabel.x(), failedLabel.y());
        } else {
            executor.draw(() -> drawGraph(executor.getDrawing()), () -> {
                clearPanel();
                FileManager.createFailureZip();
                CenteredLabelWidget failedLabel = new CenteredLabelWidget("Failed to draw graph :(");
                CenteredLabelWidget reasonLabel = new CenteredLabelWidget("Reason: " + (executor.getErrorMessage() == null ? "Unknown" : executor.getErrorMessage()), 20);
                CenteredLabelWidget logLabel = new CenteredLabelWidget("A report has been created in your .minecraft folder", 40);
                CenteredLabelWidget logLabel2 = new CenteredLabelWidget("Please send it to the mod author", 60);
                panel.add(failedLabel, failedLabel.x(), failedLabel.y());
                panel.add(reasonLabel, reasonLabel.x(), reasonLabel.y());
                panel.add(logLabel, logLabel.x(), logLabel.y());
                panel.add(logLabel2, logLabel2.x(), logLabel2.y());
            });
            clearPanel();
            CenteredLabelWidget drawingLabel = new CenteredLabelWidget("Drawing graph...");
            panel.add(drawingLabel, drawingLabel.x(), drawingLabel.y());
        }
    }

    private void drawGraph(Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing) {
        GraphDisplayWidget graphDisplayWidget = new GraphDisplayWidget(drawing);
        SearchBarWidget searchBar = new SearchBarWidget();
        HideOtherNodesWidget hideOtherNodesWidget = new HideOtherNodesWidget();
        HideChildrenWidget hideChildrenWidget = new HideChildrenWidget();
        SearchTypeWidget searchTypeWidget = new SearchTypeWidget();
        ShowOneLineWidget showOneLineWidget = new ShowOneLineWidget();
        showOneLineWidget.setMaxValue(graphDisplayWidget.getLineCount() - 1);
        showOneLineWidget.setValue(RandoAssistantClient.showLine, true);

        Window window = MinecraftClient.getInstance().getWindow();

        if (!RandoAssistantClient.hideOtherNodes) {
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
        panel.add(new WLabel(Text.of("Random Assistant v0.3.2"), 0xAAAAAA){
            @Override
            public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
                this.x = window.getScaledWidth() - 160;
                this.y = 20;

                matrices.push();
                matrices.translate(0, 0, 100);
                super.paint(matrices, x, y, mouseX, mouseY);
                matrices.pop();
            }
        }, window.getScaledWidth() - 160, 20);

        if (searchBar.getHost() == null) {
            searchBar.setHost(this);
        }
        if (showOneLineWidget.getHost() == null) {
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
