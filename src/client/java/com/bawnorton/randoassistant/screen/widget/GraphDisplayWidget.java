package com.bawnorton.randoassistant.screen.widget;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.util.LootTableGraph;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lambdaurora.spruceui.util.ScissorManager;
import grapher.graph.drawing.Drawing;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class GraphDisplayWidget extends WWidget {
    private final Map<LootTableGraph.Edge, List<Point2D>> edgeLocations;
    private final List<NodeWidget> nodeWidgets = new ArrayList<>();
    private final Map<NodeWidget, String> queryNodeMap = new HashMap<>();
    private final Rectangle2D.Float bounds;
    private final ResetPositionWidget resetPositionWidget;
    private final int initialOffsetX = 35;
    private final int initialOffsetY = 90;
    public float xOffset = 0;
    public float yOffset = 0;
    private float scale = 1;

    public GraphDisplayWidget(Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing) {
        edgeLocations = drawing.getEdgeMappings();

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        this.width = screenWidth;
        this.height = screenHeight;
        bounds = new Rectangle2D.Float(0, 0, width, height);

        for (LootTableGraph.Edge edge : edgeLocations.keySet()) {
            LootTableGraph.Vertex source = edge.getOrigin();
            LootTableGraph.Vertex target = edge.getDestination();
            Point2D sourceLocation = edgeLocations.get(edge).get(0);
            Point2D targetLocation = edgeLocations.get(edge).get(1);
            nodeWidgets.add(new NodeWidget(source, sourceLocation));
            nodeWidgets.add(new NodeWidget(target, targetLocation));
        }
        nodeWidgets.sort((o1, o2) -> {
            String o1Tooltip = o1.getNode().getTooltip().getString().toLowerCase().replaceAll("\\s+", "");
            String o2Tooltip = o2.getNode().getTooltip().getString().toLowerCase().replaceAll("\\s+", "");
            queryNodeMap.put(o1, o1Tooltip);
            queryNodeMap.put(o2, o2Tooltip);
            return o1Tooltip.compareTo(o2Tooltip);
        });

        resetPositionWidget = new ResetPositionWidget(this);
    }

    // binary search for the closest node to the query
    private NodeWidget getClosestNode(String query) {
        if(query.isEmpty()) return null;
        query = query.toLowerCase().replaceAll("\\s+", "");
        int low = 0;
        int high = nodeWidgets.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            String midVal = queryNodeMap.get(nodeWidgets.get(mid));
            if (midVal.startsWith(query)) {
                return nodeWidgets.get(mid);
            } else if (midVal.compareTo(query) < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return null;
    }

    public void inputChanged(String query) {
        NodeWidget bestMatch = null;
        for (NodeWidget nodeWidget : nodeWidgets) {
            nodeWidget.unhighlightParents();
            nodeWidget.unhighlightChildren();
            bestMatch = getClosestNode(query);
        }

        if (query.isEmpty()) {
            resetOffset();
            return;
        }
        if (bestMatch != null) {
            xOffset = -bestMatch.getX() - initialOffsetX + MinecraftClient.getInstance().getWindow().getWidth() / 4f;
            yOffset = -bestMatch.getY() - initialOffsetY + MinecraftClient.getInstance().getWindow().getHeight() / 4f;
            bestMatch.highlightParents();
            bestMatch.highlightChildren();
            NodeWidget.selectedNode = bestMatch;
        }
    }

    @Override
    public InputResult onMouseDown(int x, int y, int button) {
        InputResult result = InputResult.IGNORED;
        for (NodeWidget value : nodeWidgets) {
            if (button == 1) result = value.handleMouseDown(x, y);
        }
        if (result == InputResult.IGNORED) {
            result = resetPositionWidget.handleMouseDown(x, y);
        }
        return result;
    }

    @Override
    public InputResult onMouseDrag(int x, int y, int button, double deltaX, double deltaY) {
        xOffset += deltaX;
        yOffset += deltaY;
        return InputResult.PROCESSED;
    }

    @Override
    public InputResult onMouseScroll(int x, int y, double amount) {
        scale += amount / 10;
        if (scale < 0.5) scale = 0.5f;
        if (scale > 2) scale = 2;
        return InputResult.PROCESSED;
    }

    public void resetOffset() {
        xOffset = 0;
        yOffset = 0;
    }


    private void drawLine(MatrixStack matrices, int x1, int y1, int x2, int y2, int colour) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        int width = 1;

        if (x1 > x2) {
            bufferBuilder.vertex(matrix, x2 + width, y2 + width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x1 + width, y1 + width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x1 - width, y1 - width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x2 - width, y2 - width, 0).color(colour).next();
        } else if (x1 < x2 && y2 > y1) {
            bufferBuilder.vertex(matrix, x1 + width, y1 - width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x1 - width, y1 + width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x2 - width, y2 + width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x2 + width, y2 - width, 0).color(colour).next();
        } else if (x1 < x2) {
            bufferBuilder.vertex(matrix, x1 - width, y1 + width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x2 - width, y2 + width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x2 + width, y2 - width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x1 + width, y1 - width, 0).color(colour).next();
        } else {
            if (y1 > y2) {
                bufferBuilder.vertex(matrix, x2 + width, y2, 0).color(colour).next();
                bufferBuilder.vertex(matrix, x2 - width, y2, 0).color(colour).next();
                bufferBuilder.vertex(matrix, x1 - width, y1, 0).color(colour).next();
                bufferBuilder.vertex(matrix, x1 + width, y1, 0).color(colour).next();
            } else {
                bufferBuilder.vertex(matrix, x1 + width, y1, 0).color(colour).next();
                bufferBuilder.vertex(matrix, x1 - width, y1, 0).color(colour).next();
                bufferBuilder.vertex(matrix, x2 - width, y2, 0).color(colour).next();
                bufferBuilder.vertex(matrix, x2 + width, y2, 0).color(colour).next();
            }
        }

        Tessellator.getInstance().draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder arrowBufferBuilder = Tessellator.getInstance().getBuffer();
        arrowBufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        Vector2f arrowVector = new Vector2f(x2 - x1, y2 - y1).normalize();
        float offset = 15;

        arrowBufferBuilder.vertex(matrix, x2 - arrowVector.x * (offset + 10) + arrowVector.y * 5, y2 - arrowVector.y * (offset + 10) - arrowVector.x * 5, 0).color(colour).next();
        arrowBufferBuilder.vertex(matrix, x2 - arrowVector.x * (offset + 10) - arrowVector.y * 5, y2 - arrowVector.y * (offset + 10) + arrowVector.x * 5, 0).color(colour).next();
        arrowBufferBuilder.vertex(matrix, x2 - arrowVector.x * offset, y2 - arrowVector.y * offset, 0).color(colour).next();

        Tessellator.getInstance().draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private void renderLines(MatrixStack matrices, int x, int y) {
        for (LootTableGraph.Edge edge : edgeLocations.keySet()) {
            LootTableGraph.Vertex dest = edge.getDestination();
            LootTableGraph.Vertex origin = edge.getOrigin();
            if (dest.isHighlightedAsParent() && origin.isHighlightedAsParent()) {
                drawLine(matrices, x, y, edge, -65536);
            } else if (dest.isHighlightedAsChild() && origin.isHighlightedAsChild()) {
                drawLine(matrices, x, y, edge, -16776961);
            }
        }
    }

    private void drawLine(MatrixStack matrices, int x, int y, LootTableGraph.Edge edge, int colour) {
        List<Point2D> points = edgeLocations.get(edge);
        Point2D point1 = points.get(0);
        Point2D point2 = points.get(1);
        int x1 = (int) ((point1.getX() + x + xOffset));
        int y1 = (int) ((point1.getY() + y + yOffset));
        int x2 = (int) ((point2.getX() + x + xOffset));
        int y2 = (int) ((point2.getY() + y + yOffset));
        drawLine(matrices, x1, y1, x2, y2, colour);
    }

    private ArrayList<Tooltip> renderGraphNodes(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        ArrayList<Tooltip> tooltips = new ArrayList<>();

        for (NodeWidget widget : nodeWidgets) {
            int posX = (int) (x + widget.getX() + xOffset);
            int posY = (int) (y + widget.getY() + yOffset);

            if (bounds.contains(posX, posY)) {
                tooltips.add(widget.render(matrices, posX, posY, mouseX, mouseY));
            }
        }
        return tooltips;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        super.paint(matrices, x, y, mouseX, mouseY);

        ScissorManager.pushScaleFactor(MinecraftClient.getInstance().getWindow().getScaleFactor());

        bounds.setRect(x, y, getWidth(), getHeight());

        renderLines(matrices, x + initialOffsetX - 6, y + initialOffsetY);
        ArrayList<Tooltip> tooltips = renderGraphNodes(matrices, x + initialOffsetX, y + initialOffsetY, mouseX, mouseY);
        tooltips.add(resetPositionWidget.render(matrices, 40, MinecraftClient.getInstance().getWindow().getScaledHeight() - 40, mouseX, mouseY));

        for (Tooltip tooltip : tooltips) {
            if (tooltip != null) {
                Objects.requireNonNull(MinecraftClient.getInstance().currentScreen).renderOrderedTooltip(matrices, tooltip.getLines(MinecraftClient.getInstance()), mouseX, mouseY);
            }
        }
        ScissorManager.popScaleFactor();
    }
}