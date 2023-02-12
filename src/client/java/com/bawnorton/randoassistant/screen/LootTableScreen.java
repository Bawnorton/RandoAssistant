package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.screen.widget.GraphDisplay;
import com.bawnorton.randoassistant.screen.widget.SearchBarWidget;
import com.bawnorton.randoassistant.util.LootTableGraph;
import grapher.graph.drawing.Drawing;
import grapher.graph.exception.CannotBeAppliedException;
import grapher.graph.layout.GraphLayoutProperties;
import grapher.graph.layout.LayoutAlgorithms;
import grapher.graph.layout.Layouter;
import grapher.graph.layout.PropertyEnums;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.MinecraftClient;

import java.util.List;

public class LootTableScreen extends LightweightGuiDescription {
    private static GraphDisplay cacheDisplay;

    public LootTableScreen() {
        GraphDisplay graphDisplay = cacheDisplay;
        if (RandoAssistant.graphChanged) {
            Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing = getDrawing();
            if (drawing == null) return;
            graphDisplay = new GraphDisplay(drawing);
            cacheDisplay = graphDisplay;
            RandoAssistant.graphChanged = false;
        }

        SearchBarWidget searchBar = new SearchBarWidget(graphDisplay);

        WPlainPanel panel = new WPlainPanel();
        setRootPanel(panel);
        setFullscreen(true);
        panel.setInsets(Insets.NONE);
        panel.add(graphDisplay, 0, 0);
        panel.add(searchBar, 40, 40, MinecraftClient.getInstance().getWindow().getScaledWidth() - 80, 20);
        panel.setBackgroundPainter(((matrices, left, top, panel1) -> {
        }));
    }

    private Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> getDrawing() {
        double levelGap = 40;
        double nodeGap = 40;
        double heirarchyGap = 0;

        LootTableGraph graph = RandoAssistant.getCurrentLootTables().toGraph();

        LayoutAlgorithms algorithm = LayoutAlgorithms.HIERARCHICAL;

        GraphLayoutProperties layoutProperties = new GraphLayoutProperties();
        layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTER_HIERARCHY_SPACING, heirarchyGap);
        layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTER_RANK_CELL_SPACING, levelGap);
        layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTRA_CELL_SPACING, nodeGap);

        List<LootTableGraph.Vertex> vertices = graph.getVertices();
        List<LootTableGraph.Edge> edges = graph.getEdges();
        Layouter<LootTableGraph.Vertex, LootTableGraph.Edge> layouter = new Layouter<>(vertices, edges, algorithm, layoutProperties);
        try {
            return layouter.layout();
        } catch (CannotBeAppliedException e) {
            RandoAssistant.LOGGER.error("Could not layout graph", e);
            return null;
        }
    }

    @Override
    public void addPainters() {
    }
}
