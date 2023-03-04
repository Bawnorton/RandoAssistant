package com.bawnorton.randoassistant.thread;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.graph.LootTableGraph;
import grapher.graph.drawing.Drawing;
import grapher.graph.layout.GraphLayoutProperties;
import grapher.graph.layout.LayoutAlgorithms;
import grapher.graph.layout.Layouter;
import grapher.graph.layout.PropertyEnums;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.util.*;

public class GraphTaskExecutor {
    private final LootTableGraph graph;
    private final SerialExecutor executor;
    private final FailableSerialExecutor failableExecutor;
    private final HighlightTask highlightTask;
    private final DrawTask drawTask;

    public GraphTaskExecutor(LootTableGraph graph) {
        this.graph = graph;
        this.executor = new SerialExecutor();
        this.failableExecutor = new FailableSerialExecutor();
        this.highlightTask = new HighlightTask();
        this.drawTask = new DrawTask();
    }

    public void highlight(LootTableGraph.Vertex vertex) {
        highlightTask.highlight(vertex);
    }

    public void unhighlightConnected() {
        highlightTask.unhighlightConnected();
    }

    public void highlightChildren(LootTableGraph.Vertex vertex) {
        highlightTask.start(vertex, false);
    }

    public void highlightParents(LootTableGraph.Vertex vertex) {
        highlightTask.start(vertex, true);
    }

    public void draw() {
        drawTask.start();
    }

    public void draw(Runnable onSuccess, Runnable onFailure) {
        drawTask.start(onSuccess, onFailure);
    }

    public void draw(LootTableGraph.Vertex vertex, Runnable onSuccess, Runnable onFailure) {
        drawTask.start(vertex, onSuccess, onFailure);
    }

    public void disableDrawTask() {
        drawTask.disable();
    }

    public void enableDrawTask() {
        drawTask.enable();
    }

    public void markDrawTaskDirty() {
        drawTask.markDirty();
    }

    public Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> getDrawing() {
        return drawTask.getDrawing();
    }

    public String getErrorMessage() {
        return drawTask.getError();
    }

    private class HighlightTask {
        private final Set<LootTableGraph.Vertex> highlighted;
        private final Runnable task;
        private Boolean isParent;
        private LootTableGraph.Vertex vertex;

        public HighlightTask() {
            this.highlighted = Collections.synchronizedSet(new HashSet<>());
            task = () -> {
                if (isParent) {
                    vertex.getParents().forEach(LootTableGraph.Vertex::highlightAsParent);
                } else {
                    vertex.getChildren().forEach(LootTableGraph.Vertex::highlightAsChild);
                }
                highlightInteractables();
            };
        }

        public void highlight(LootTableGraph.Vertex vertex) {
            executor.execute(() -> highlighted.add(vertex));
        }

        public void start(LootTableGraph.Vertex vertex, boolean isParent) {
            this.vertex = vertex;
            this.isParent = isParent;
            executor.execute(task);
        }

        public void unhighlightConnected() {
            executor.execute(() -> {
                highlighted.forEach(vertex -> {
                    vertex.unhighlightAsChild();
                    vertex.unhighlightAsParent();
                    vertex.unhighlightAsTarget();
                    vertex.unhighlightAsInteraction();
                });
                highlighted.clear();
            });
        }

        private void highlightInteractables() {
            highlighted.forEach(node -> RandoAssistant.interactionMap.getMap().forEach((entry) -> {
                entry.getKey().forEach(item -> {
                    LootTableGraph.Vertex interactionVertex = graph.getVertex(item);
                    if (interactionVertex == null) return;
                    boolean isHighlightedCorrectly = isParent ? interactionVertex.isHighlightedAsParent() : interactionVertex.isHighlightedAsChild();
                    if (isHighlightedCorrectly || interactionVertex.isHighlightedAsTarget()) {
                        interactionVertex.highlightAsInteraction();
                    }
                });
                entry.getValue().forEach(item -> {
                    LootTableGraph.Vertex interactionVertex = graph.getVertex(item);
                    if (interactionVertex == null) return;
                    boolean isHighlightedCorrectly = isParent ? interactionVertex.isHighlightedAsParent() : interactionVertex.isHighlightedAsChild();
                    if (isHighlightedCorrectly || interactionVertex.isHighlightedAsTarget()) {
                        interactionVertex.highlightAsInteraction();
                    }
                });
            }));
        }
    }

    private class DrawTask {
        private final Runnable task;

        private LootTableGraph.Vertex vertex;
        private Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing;
        private String error;
        private boolean enabled;
        private boolean dirty;

        public DrawTask() {
            enabled = true;
            task = () -> {
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

                if (vertex == null) {
                    vertexSet = graph.getVertices();
                    edgeSet = graph.getEdges();
                } else {
                    vertexSet = vertex.getVerticesAssociatedWith(true);
                    edgeSet = vertex.getEdgesAssociatedWithVertices(vertexSet);
                }

                List<LootTableGraph.Vertex> vertices = Collections.synchronizedList(new ArrayList<>(vertexSet));
                List<LootTableGraph.Edge> edges = Collections.synchronizedList(new ArrayList<>(edgeSet));

                Layouter<LootTableGraph.Vertex, LootTableGraph.Edge> layouter = new Layouter<>(vertices, edges, algorithm, layoutProperties);
                try {
                    drawing = layouter.layout();
                    dirty = false;
                } catch (Exception e) {
                    RandoAssistant.LOGGER.error("Failed to layout graph");
                    error = e.getMessage();
                    markDirty();
                    throw new RuntimeException(e);
                }
                // scale down the drawing width so it better fits in the screen
                for (LootTableGraph.Edge edge : drawing.getEdgeMappings().keySet()) {
                    List<Point2D> points = drawing.getEdgeMappings().get(edge);
                    Point2D source = points.get(0);
                    Point2D target = points.get(1);
                    source.setLocation(source.getX() / 10, source.getY());
                    target.setLocation(target.getX() / 10, target.getY());
                }
            };
        }

        public void enable() {
            enabled = true;
        }

        public void disable() {
            enabled = false;
        }

        public void markDirty() {
            dirty = true;
        }

        public boolean isDirty() {
            return dirty;
        }

        public void start(LootTableGraph.Vertex vertex, Runnable successTask, Runnable failTask) {
            if (!enabled) return;
            this.vertex = vertex;
            if (isDirty()) {
                failableExecutor.execute(task, successTask, failTask);
            } else {
                failableExecutor.execute(() -> {
                }, successTask, failTask);
            }
        }

        public void start(Runnable successTask, Runnable failTask) {
            start(null, successTask, failTask);
        }

        public void start(Runnable successTask) {
            start(successTask, () -> {
            });
        }

        public void start() {
            start(() -> {
            });
        }

        @Nullable
        public Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> getDrawing() {
            return drawing;
        }

        @Nullable
        public String getError() {
            return error;
        }
    }
}
