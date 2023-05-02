package com.bawnorton.randoassistant.thread;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.file.FileManager;
import com.bawnorton.randoassistant.graph.LootTableGraph;
import grapher.graph.drawing.Drawing;
import grapher.graph.layout.GraphLayoutProperties;
import grapher.graph.layout.LayoutAlgorithms;
import grapher.graph.layout.Layouter;
import grapher.graph.layout.PropertyEnums;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

public class GraphTaskExecutor {
    private final SerialExecutor executor;
    private final FailableSerialExecutor failableExecutor;
    private final HighlightTask highlightTask;
    private final DrawTask drawTask;

    public GraphTaskExecutor() {
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
        draw(() -> {});
    }

    public void draw(Runnable onSuccess) {
        draw(onSuccess, () -> {});
    }

    public void draw(Runnable onSuccess, Runnable onFailure) {
        draw(null, onSuccess, onFailure);
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
            try {
                executor.execute(() -> highlighted.add(vertex));
            } catch (RejectedExecutionException e) {
                RandoAssistant.LOGGER.error("Too many nodes to highlight", e);
            }
        }

        public void start(LootTableGraph.Vertex vertex, boolean isParent) {
            this.vertex = vertex;
            this.isParent = isParent;
            try {
                executor.execute(task);
            } catch (RejectedExecutionException e) {
                RandoAssistant.LOGGER.error("Failed to start highligher on vertex: " + vertex, e);
                FileManager.createFailureZip();
                MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                        SystemToast.Type.UNSECURE_SERVER_WARNING,
                        Text.of("[RandoAssistant]§c failed to highlight vertex"),
                        Text.of("§cPlease send the zip file in your .minecraft folder to the developer")
                ));
            }
        }

        public void unhighlightConnected() {
            try {
                executor.execute(() -> {
                    highlighted.forEach(vertex -> {
                        vertex.unhighlightAsChild();
                        vertex.unhighlightAsParent();
                        vertex.unhighlightAsTarget();
                        vertex.unhighlightAsInteraction();
                    });
                    highlighted.clear();
                });
            } catch (RejectedExecutionException e) {
                RandoAssistant.LOGGER.error("Failed to unhighlight vertices", e);
                FileManager.createFailureZip();
                MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                        SystemToast.Type.UNSECURE_SERVER_WARNING,
                        Text.of("[RandoAssistant]§c failed to unhighlight vertex"),
                        Text.of("§cPlease send the zip file in your .minecraft folder to the developer")
                ));
            }
        }

        private void highlightInteractables() {
            highlighted.forEach(node -> RandoAssistantClient.interactionMap.getMap().forEach((entry) -> {
                entry.getKey().forEach(item -> {
                    LootTableGraph.Vertex interactionVertex = graph.getVertex(item);
                    if (interactionVertex == null) return;
                    boolean isHighlightedCorrectly = isParent ? interactionVertex.isHighlightedAsParent() : interactionVertex.isHighlightedAsChild();
                    if ((isHighlightedCorrectly || interactionVertex.isHighlightedAsTarget()) && !interactionVertex.isHighlightedAsInteraction()) {
                        interactionVertex.highlightAsInteraction();
                    }
                });
                entry.getValue().forEach(item -> {
                    LootTableGraph.Vertex interactionVertex = graph.getVertex(item);
                    if (interactionVertex == null) return;
                    boolean isHighlightedCorrectly = isParent ? interactionVertex.isHighlightedAsParent() : interactionVertex.isHighlightedAsChild();
                    if ((isHighlightedCorrectly || interactionVertex.isHighlightedAsTarget()) && !interactionVertex.isHighlightedAsInteraction()) {
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
                try {
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
                        vertexSet = graph.getEnabledVertices();
                        edgeSet = graph.getEnabledEdges();
                    } else {
                        vertexSet = vertex.getVerticesAssociatedWith(true);
                        edgeSet = vertex.getEdgesAssociatedWithVertices(vertexSet);
                    }
                    System.out.println("Drawing graph with " + vertexSet.size() + " vertices and " + edgeSet.size() + " edges");

                    List<LootTableGraph.Vertex> vertices = Collections.synchronizedList(new ArrayList<>(vertexSet));
                    List<LootTableGraph.Edge> edges = Collections.synchronizedList(new ArrayList<>(edgeSet));

                    Layouter<LootTableGraph.Vertex, LootTableGraph.Edge> layouter = new Layouter<>(vertices, edges, algorithm, layoutProperties);

                    System.out.println("Started Layout at " + System.currentTimeMillis());
                    drawing = layouter.layout();
                    System.out.println("Finished Layout at " + System.currentTimeMillis());
                    dirty = false;
                    // scale down the drawing width so it better fits in the screen
                    for (LootTableGraph.Edge edge : drawing.getEdgeMappings().keySet()) {
                        List<Point2D> points = drawing.getEdgeMappings().get(edge);
                        Point2D source = points.get(0);
                        Point2D target = points.get(1);
                        source.setLocation(source.getX() / 10, source.getY());
                        target.setLocation(target.getX() / 10, target.getY());
                    }
                } catch (Exception e) {
                    RandoAssistant.LOGGER.error("Failed to draw graph");
                    error = e.getMessage();
                    markDirty();
                    throw new RuntimeException(e);
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
            try {
                if (isDirty()) {
                    failableExecutor.execute(task, successTask, failTask);
                } else {
                    failableExecutor.execute(() -> {
                    }, successTask, failTask);
                }
            } catch (RejectedExecutionException e) {
                RandoAssistant.LOGGER.error("Failed to start drawer on vertex: " + vertex, e);
                FileManager.createFailureZip();
                MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                        SystemToast.Type.UNSECURE_SERVER_WARNING,
                        Text.of("[RandoAssistant]§c failed to draw graph"),
                        Text.of("§cPlease send the zip file in your .minecraft folder to the developer")
                ));
            }
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
