package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.util.LootTableGraph;
import graph.drawing.Drawing;
import graph.elements.Edge;
import graph.elements.Vertex;
import graph.exception.CannotBeAppliedException;
import graph.layout.GraphLayoutProperties;
import graph.layout.LayoutAlgorithms;
import graph.layout.Layouter;
import graph.layout.PropertyEnums;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LootTableScreen extends LightweightGuiDescription {
    private final LootTableGraph graph;

    public LootTableScreen() {
        double levelGap = 5;
        double nodeGap = 10;

        graph = RandoAssistant.getCurrentLootTables().toGraph();

        LayoutAlgorithms algorithm = LayoutAlgorithms.HIERARCHICAL;

        GraphLayoutProperties layoutProperties = new GraphLayoutProperties();
        layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTER_RANK_CELL_SPACING, levelGap);
        layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTRA_CELL_SPACING, nodeGap);

        List<LootTableGraph.Vertex> vertices = new ArrayList<>(graph.vertexSet());
        List<LootTableGraph.Edge> edges = new ArrayList<>(graph.edgeSet());
        Layouter<LootTableGraph.Vertex, LootTableGraph.Edge> layouter = new Layouter<>(vertices, edges, algorithm, layoutProperties);
        try {
            Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing = layouter.layout();
            Map<LootTableGraph.Vertex, Point2D> vertexLocations = drawing.getVertexMappings();
            Map<LootTableGraph.Edge, List<Point2D>> edgeLocations = drawing.getEdgeMappings();
            RandoAssistant.LOGGER.info("Layouted graph:\nVertices: {}\nEdges: {}", vertexLocations, edgeLocations);

        } catch (CannotBeAppliedException e) {
            RandoAssistant.LOGGER.error("Could not layout graph", e);
        }
    }
}
