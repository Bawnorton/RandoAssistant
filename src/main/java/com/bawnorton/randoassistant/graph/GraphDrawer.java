package com.bawnorton.randoassistant.graph;

import com.bawnorton.randoassistant.RandoAssistant;
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
import java.util.concurrent.CompletableFuture;

public class GraphDrawer {
    LootTableGraph graph;

    private Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing;
    private Runnable onFinishedDrawing = () -> {
    };

    private boolean isDrawing = false;
    private boolean disabled = false;
    private boolean failedToDraw = false;
    private String errorMessage = "";

    public GraphDrawer(LootTableGraph graph) {
        this.graph = graph;
    }

    public void updateDrawing() {
        if(!disabled) updateDrawing(0);
    }

    private void updateDrawing(int retries) {
        if (isDrawing && retries == 0) {
            return;
        }
        if (retries > 3) {
            RandoAssistant.LOGGER.error("Failed to draw graph after 3 retries");
            failedToDraw = true;
            errorMessage = "Failed to draw graph after 3 retries";
            return;
        }
        isDrawing = true;
        failedToDraw = false;
        CompletableFuture.runAsync(() -> {
            double levelGap = 40;
            double nodeGap = 40;
            double heirarchyGap = 0;

            LayoutAlgorithms algorithm = LayoutAlgorithms.HIERARCHICAL;

            GraphLayoutProperties layoutProperties = new GraphLayoutProperties();
            layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTER_HIERARCHY_SPACING, heirarchyGap);
            layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTER_RANK_CELL_SPACING, levelGap);
            layoutProperties.setProperty(PropertyEnums.HierarchicalProperties.INTRA_CELL_SPACING, nodeGap);

            List<LootTableGraph.Vertex> vertices = Collections.synchronizedList(new ArrayList<>(graph.getVertices()));
            List<LootTableGraph.Edge> edges = Collections.synchronizedList(new ArrayList<>(graph.getEdges()));

            Layouter<LootTableGraph.Vertex, LootTableGraph.Edge> layouter = new Layouter<>(vertices, edges, algorithm, layoutProperties);
            try {
                drawing = layouter.layout();
            } catch (CannotBeAppliedException e) {
                RandoAssistant.LOGGER.error("Could not layout graph", e);
                errorMessage = e.getMessage();
                failedToDraw = true;
            } catch (NullPointerException e) {
                RandoAssistant.LOGGER.error("NullPointerException while trying to layout graph. Not fatal, trying again", e);
                errorMessage = "NullPointerException while trying to layout graph. Not fatal, trying again";
                updateDrawing(retries + 1);
            }
            // scale down the drawing width so it better fits in the screen
            for (LootTableGraph.Edge edge : drawing.getEdgeMappings().keySet()) {
                List<Point2D> points = drawing.getEdgeMappings().get(edge);
                Point2D source = points.get(0);
                Point2D target = points.get(1);
                source.setLocation(source.getX() / 10, source.getY());
                target.setLocation(target.getX() / 10, target.getY());
            }
            isDrawing = false;
        }).thenRun(() -> {
            if(!failedToDraw) {
                onFinishedDrawing.run();
                onFinishedDrawing = () -> {};
            }
        }).exceptionally(throwable -> {
            RandoAssistant.LOGGER.error("Failed to draw graph", throwable);
            failedToDraw = true;
            errorMessage = throwable.getMessage();
            isDrawing = false;
            return null;
        });
    }

    public Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> getDrawing() {
        return isDrawing ? null : drawing;
    }

    public boolean didFailToDraw() {
        return failedToDraw;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void afterDrawing(Runnable runnable) {
        onFinishedDrawing = runnable;
    }

    public void disable() {
        disabled = true;
    }

    public void enable() {
        disabled = false;
    }
}
