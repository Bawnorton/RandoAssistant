package com.bawnorton.randoassistant.graph;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.util.Wrapper;
import grapher.graph.drawing.Drawing;
import grapher.graph.exception.CannotBeAppliedException;
import grapher.graph.layout.GraphLayoutProperties;
import grapher.graph.layout.LayoutAlgorithms;
import grapher.graph.layout.Layouter;
import grapher.graph.layout.PropertyEnums;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class GraphDrawer {
    LootTableGraph graph;

    private Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing;

    private Runnable onFinishedDrawing = () -> {};
    private Runnable onFailedDrawing = () -> {};

    private boolean isDrawing = false;
    private boolean disabled = false;
    private String errorMessage = "";

    public GraphDrawer(LootTableGraph graph) {
        this.graph = graph;
    }

    public void updateDrawing() {
        if(!disabled) {
            updateDrawing(0, null);
        }
    }

    public void updateDrawing(LootTableGraph.Vertex vertex) {
        if(!disabled) {
            updateDrawing(0, vertex);
        }
    }

    private void updateDrawing(int retries, LootTableGraph.Vertex vertex) {
        if (isDrawing && retries == 0) {
            return;
        }
        if (retries >= 3) {
            throw new RuntimeException("Failed to draw graph after 3 retries");
        }
        isDrawing = true;

        CompletableFuture.runAsync(() -> {
            double levelGap = 40;
            double nodeGap = 40;
            double heirarchyGap = 0;

            LayoutAlgorithms algorithm = LayoutAlgorithms.HIERARCHICAL;

            GraphLayoutProperties layoutProperties = new GraphLayoutProperties();
            layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTER_HIERARCHY_SPACING, heirarchyGap);
            layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTER_RANK_CELL_SPACING, levelGap);
            layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTRA_CELL_SPACING, nodeGap);

            Set<LootTableGraph.Vertex> vertexSet;
            Set<LootTableGraph.Edge> edgeSet;

            if(vertex == null) {
                vertexSet = graph.getVertices();
                edgeSet = graph.getEdges();
            } else {
                vertexSet = vertex.getVerticesAssociatedWith();
                edgeSet = vertex.getEdgesAssociatedWithVertices(vertexSet);
            }

            List<LootTableGraph.Vertex> vertices = Collections.synchronizedList(new ArrayList<>(vertexSet));
            List<LootTableGraph.Edge> edges = Collections.synchronizedList(new ArrayList<>(edgeSet));

            Layouter<LootTableGraph.Vertex, LootTableGraph.Edge> layouter = new Layouter<>(vertices, edges, algorithm, layoutProperties);
            try {
                drawing = layouter.layout();
            } catch (CannotBeAppliedException e) {
                throw new RuntimeException(e);
            } catch (NullPointerException e) {
                RandoAssistant.LOGGER.error("NullPointerException while trying to layout graph. Not fatal, trying again", e);
                errorMessage = "NullPointerException while trying to layout graph. Not fatal, trying again";
                updateDrawing(retries + 1, vertex);
            }
            // scale down the drawing width so it better fits in the screen
            for (LootTableGraph.Edge edge : drawing.getEdgeMappings().keySet()) {
                List<Point2D> points = drawing.getEdgeMappings().get(edge);
                Point2D source = points.get(0);
                Point2D target = points.get(1);
                source.setLocation(source.getX() / 10, source.getY());
                target.setLocation(target.getX() / 10, target.getY());
            }
        }).thenRun(() -> {
            isDrawing = false;
            onFinishedDrawing.run();
            onFinishedDrawing = () -> {};
        }).exceptionally(throwable -> {
            drawing = null;
            isDrawing = false;
            onFinishedDrawing = () -> {};
            onFailedDrawing.run();
            onFailedDrawing = () -> {};
            errorMessage = throwable.getMessage();
            RandoAssistant.LOGGER.error("Failed to draw graph", throwable);
            return null;
        });
    }

    public Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> getDrawing() {
        return isDrawing ? null : drawing;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void afterDrawing(Runnable onFinishedDrawing, Runnable onFailedDrawing) {
        this.onFinishedDrawing = onFinishedDrawing;
        this.onFailedDrawing = onFailedDrawing;
    }

    public void disable() {
        disabled = true;
    }

    public void enable() {
        disabled = false;
    }
}
