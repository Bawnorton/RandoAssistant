package com.bawnorton.randoassistant.tracking.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import grapher.graph.drawing.Drawing;
import grapher.graph.layout.GraphLayoutProperties;
import grapher.graph.layout.LayoutAlgorithms;
import grapher.graph.layout.Layouter;
import grapher.graph.layout.PropertyEnums;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GraphDrawer {
    private final TrackingGraph graph;

    private Drawing<TrackingGraph.Vertex, TrackingGraph.Edge> drawing;
    private boolean dirty = true;

    public GraphDrawer(TrackingGraph graph) {
        this.graph = graph;
    }

    public Drawing<TrackingGraph.Vertex, TrackingGraph.Edge> draw() {
        if(!dirty) return drawing;
        try {
            double levelGap = 40;
            double nodeGap = 40;
            double heirarchyGap = 0;

            LayoutAlgorithms algorithm = LayoutAlgorithms.HIERARCHICAL;

            GraphLayoutProperties layoutProperties = new GraphLayoutProperties();
            layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTER_HIERARCHY_SPACING, heirarchyGap);
            layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTER_RANK_CELL_SPACING, levelGap);
            layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTRA_CELL_SPACING, nodeGap);
            layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.ORIENTATION, SwingConstants.WEST);

            Set<TrackingGraph.Vertex> vertices = Collections.synchronizedSet(graph.vertexSet());
            Set<TrackingGraph.Edge> edges = Collections.synchronizedSet(graph.edgeSet());
            edges.forEach(edge -> {
                if(edge.getOrigin() == null || edge.getDestination() == null) {
                    throw new RuntimeException("Edge: " + edge + " has null origin or destination");
                }}
            );
            vertices.forEach(vertex -> {
                if(vertex.getContent() == null) {
                    throw new RuntimeException("Vertex: " + vertex + " has null content");
                }
            });

            Layouter<TrackingGraph.Vertex, TrackingGraph.Edge> layouter = new Layouter<>(vertices, edges, algorithm, layoutProperties);

            drawing = layouter.layout();
            dirty = false;
            // scale down the drawing width so it better fits in the screen
            for (TrackingGraph.Edge edge : drawing.getEdgeMappings().keySet()) {
                List<Point2D> points = drawing.getEdgeMappings().get(edge);
                Point2D source = points.get(0);
                Point2D target = points.get(1);
                source.setLocation(source.getX() / 2, source.getY() / 2);
                target.setLocation(target.getX() / 2, target.getY() / 2);
            }
            return drawing;
        } catch (Exception e) {
            RandoAssistant.LOGGER.error("Failed to draw graph");
            markDirty();
            throw new RuntimeException(e);
        }
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }
}
