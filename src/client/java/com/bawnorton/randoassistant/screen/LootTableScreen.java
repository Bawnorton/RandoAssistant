package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.screen.widget.GraphDisplay;
import com.bawnorton.randoassistant.screen.widget.SearchBarWidget;
import com.bawnorton.randoassistant.util.LootTableGraph;
import graph.drawing.Drawing;
import graph.exception.CannotBeAppliedException;
import graph.layout.GraphLayoutProperties;
import graph.layout.LayoutAlgorithms;
import graph.layout.Layouter;
import graph.layout.PropertyEnums;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.MinecraftClient;

import java.util.List;

public class LootTableScreen extends LightweightGuiDescription {
    private final LootTableGraph graph;

    public LootTableScreen() {
        double levelGap = 40;
        double nodeGap = 40;
        double heirarchyGap = 0;

        graph = RandoAssistant.getCurrentLootTables().toGraph();

        LayoutAlgorithms algorithm = LayoutAlgorithms.HIERARCHICAL;

        GraphLayoutProperties layoutProperties = new GraphLayoutProperties();
        layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTER_HIERARCHY_SPACING, heirarchyGap);
        layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTER_RANK_CELL_SPACING, levelGap);
        layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTRA_CELL_SPACING, nodeGap);

        List<LootTableGraph.Vertex> vertices = graph.getVertices();
        List<LootTableGraph.Edge> edges = graph.getEdges();
        Layouter<LootTableGraph.Vertex, LootTableGraph.Edge> layouter = new Layouter<>(vertices, edges, algorithm, layoutProperties);
        Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing;
        try {
            drawing = layouter.layout();
        } catch (CannotBeAppliedException e) {
            RandoAssistant.LOGGER.error("Could not layout graph", e);
            return;
        }

        GraphDisplay graphDisplay = new GraphDisplay(drawing);
        SearchBarWidget searchBar = new SearchBarWidget(graphDisplay);

        WPlainPanel panel = new WPlainPanel();
        setRootPanel(panel);
        setFullscreen(true);
        panel.setInsets(Insets.NONE);
        panel.add(graphDisplay, 0, 0);
        panel.add(searchBar, 40, 40, MinecraftClient.getInstance().getWindow().getScaledWidth() - 80, 20);
        panel.setBackgroundPainter(((matrices, left, top, panel1) -> {}));
    }

    @Override
    public void addPainters() {
    }
}
