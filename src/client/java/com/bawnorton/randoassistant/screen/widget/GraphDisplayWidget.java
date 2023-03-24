package com.bawnorton.randoassistant.screen.widget;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.graph.LootTableGraph;
import com.bawnorton.randoassistant.screen.widget.drawable.NodeWidget;
import com.bawnorton.randoassistant.screen.widget.drawable.ResetPositionWidget;
import com.bawnorton.randoassistant.util.Line;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import grapher.graph.drawing.Drawing;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL33C;

import java.awt.geom.Point2D;
import java.util.*;

import static com.bawnorton.randoassistant.RandoAssistantClient.SCALE;

public class GraphDisplayWidget extends WWidget {
    private static GraphDisplayWidget instance;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Map<LootTableGraph.Edge, List<Point2D>> edgeLocations;
    private final List<NodeWidget> nodeWidgets = new ArrayList<>();
    private final ResetPositionWidget resetPositionWidget;
    private final int initialOffsetX = 35;
    private final int initialOffsetY = 90;
    public float xOffset = 0;
    public float yOffset = 0;
    private List<Line> currentLines = new ArrayList<>();

    public GraphDisplayWidget(Drawing<LootTableGraph.Vertex, LootTableGraph.Edge> drawing) {
        instance = this;
        edgeLocations = drawing.getEdgeMappings();

        this.width = client.getWindow().getScaledWidth();
        this.height = client.getWindow().getScaledHeight();

        for (LootTableGraph.Edge edge : edgeLocations.keySet()) {
            LootTableGraph.Vertex source = edge.getOrigin();
            LootTableGraph.Vertex target = edge.getDestination();
            Point2D sourceLocation = edgeLocations.get(edge).get(0);
            Point2D targetLocation = edgeLocations.get(edge).get(1);
            nodeWidgets.add(new NodeWidget(source, sourceLocation));
            nodeWidgets.add(new NodeWidget(target, targetLocation));
        }

        resetPositionWidget = new ResetPositionWidget(40, client.getWindow().getScaledHeight() - 26, this);

        if (NodeWidget.getSelectedNode() != null) {
            centerOnNode(NodeWidget.getSelectedNode());
            Set<Line> lines = Line.builder().addLines(NodeWidget.getSelectedNode().getVertex()).build();
            setCurrentLines(lines);
        }
    }

    public static GraphDisplayWidget getInstance() {
        return instance;
    }

    @Override
    public InputResult onMouseDown(int x, int y, int button) {
        InputResult result = InputResult.IGNORED;
        for (NodeWidget value : nodeWidgets) {
            if (button == 1) result = value.handleMouseDown(x, y);
        }
        if (result == InputResult.IGNORED) result = resetPositionWidget.handleMouseDown(x, y);
        return result;
    }

    @Override
    public InputResult onMouseScroll(int x, int y, double amount) {
        SCALE.set(SCALE.get() + (amount > 0 ? 0.5d : -0.5d));
        if (SCALE.get() > 8) SCALE.set(8d);
        if (SCALE.get() < 1) SCALE.set(1d);
        client.getWindow().setScaleFactor(SCALE.get());
        width = client.getWindow().getScaledWidth();
        height = client.getWindow().getScaledHeight();
        return super.onMouseScroll(x, y, amount);
    }

    @Override
    public InputResult onMouseDrag(int x, int y, int button, double deltaX, double deltaY) {
        xOffset += deltaX;
        yOffset += deltaY;
        return InputResult.PROCESSED;
    }

    public void resetOffset() {
        xOffset = 0;
        yOffset = 0;
    }

    public void resetScale() {
        SCALE.set(RandoAssistantClient.ACTUAL_SCALE.get());
        client.getWindow().setScaleFactor(SCALE.get());
        width = client.getWindow().getScaledWidth();
        height = client.getWindow().getScaledHeight();
    }

    public void centerOnNode(NodeWidget nodeWidget) {
        if (nodeWidget == null) return;
        xOffset = client.getWindow().getScaledWidth() / 2f - initialOffsetX - nodeWidget.getX();
        yOffset = client.getWindow().getScaledHeight() / 2f - initialOffsetY - nodeWidget.getY();
    }

    public List<NodeWidget> getNodes() {
        return nodeWidgets;
    }

    public Line getLine(int index) {
        try {
            return currentLines.get(index);
        } catch (IndexOutOfBoundsException e) {
            return Line.EMPTY;
        }
    }

    public void setCurrentLines(Set<Line> lines) {
        currentLines = new ArrayList<>(lines);
        currentLines.sort(Comparator.comparingInt(Line::size));
    }

    public int getLineCount() {
        return currentLines.size();
    }

    private void drawLine(MatrixStack matrices, int x1, int y1, int x2, int y2, int colour) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        ShaderProgram currentShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        int width = y1 > y2 ? -1 : 1;

        if (x1 >= x2) {
            bufferBuilder.vertex(matrix, x2 + width, y2 + width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x1 + width, y1 + width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x1 - width, y1 - width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x2 - width, y2 - width, 0).color(colour).next();
        } else {
            bufferBuilder.vertex(matrix, x1 - width, y1 + width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x2 - width, y2 + width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x2 + width, y2 - width, 0).color(colour).next();
            bufferBuilder.vertex(matrix, x1 + width, y1 - width, 0).color(colour).next();
        }

        Tessellator.getInstance().draw();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder arrowBufferBuilder = Tessellator.getInstance().getBuffer();
        arrowBufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        Vector2f arrowVector = new Vector2f(x2 - x1, y2 - y1).normalize();
        float offset = 15;

        arrowBufferBuilder.vertex(matrix, x2 - arrowVector.x * (offset + 10) + arrowVector.y * 5, y2 - arrowVector.y * (offset + 10) - arrowVector.x * 5, 0).color(colour).next();
        arrowBufferBuilder.vertex(matrix, x2 - arrowVector.x * (offset + 10) - arrowVector.y * 5, y2 - arrowVector.y * (offset + 10) + arrowVector.x * 5, 0).color(colour).next();
        arrowBufferBuilder.vertex(matrix, x2 - arrowVector.x * offset, y2 - arrowVector.y * offset, 0).color(colour).next();

        Tessellator.getInstance().draw();
        RenderSystem.enableCull();
        RenderSystem.setShader(() -> currentShader);
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

    private void renderLines(MatrixStack matrices, int x, int y) {
        for (LootTableGraph.Edge edge : edgeLocations.keySet()) {
            LootTableGraph.Vertex dest = edge.getDestination();
            LootTableGraph.Vertex origin = edge.getOrigin();
            if (dest.isHighlightedAsParent() || origin.isHighlightedAsParent()) {
                if (RandoAssistantClient.showLine != -1) {
                    Line currentLine = getLine(RandoAssistantClient.showLine);
                    if (!currentLine.contains(dest) || !currentLine.contains(origin)) {
                        continue;
                    }
                }
            }
            boolean isInteraction = dest.isHighlightedAsInteraction() && origin.isHighlightedAsInteraction();

            if (isInteraction) {
                isInteraction = RandoAssistantClient.interactionMap.checkInteraction(origin.getItem(), dest.getItem());
            }

            if ((dest.isHighlightedAsParent() || dest.isHighlightedAsTarget()) && origin.isHighlightedAsParent()) {
                drawLine(matrices, x, y, edge, isInteraction ? -256 : -65536);
            } else if (dest.isHighlightedAsChild() && (origin.isHighlightedAsChild() || origin.isHighlightedAsTarget())) {
                drawLine(matrices, x, y, edge, isInteraction ? -256 : -16776961);
            }
        }
    }

    private ArrayList<Tooltip> renderGraphNodes(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        ArrayList<Tooltip> tooltips = new ArrayList<>();

        for (NodeWidget widget : nodeWidgets) {
            int posX = (int) (x + widget.getX() + xOffset);
            int posY = (int) (y + widget.getY() + yOffset);

            if (isWithinBounds(posX, posY)) {
                tooltips.add(widget.render(matrices, posX, posY, mouseX, mouseY));
            }
        }
        return tooltips;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        super.paint(matrices, x, y, mouseX, mouseY);

        renderLines(matrices, x + initialOffsetX - 6, y + initialOffsetY);
        ArrayList<Tooltip> tooltips = renderGraphNodes(matrices, x + initialOffsetX, y + initialOffsetY, mouseX, mouseY);
        tooltips.add(resetPositionWidget.render(matrices, mouseX, mouseY));

        for (Tooltip tooltip : tooltips) {
            if (tooltip != null) {
                assert client.currentScreen != null;
                client.currentScreen.renderOrderedTooltip(matrices, tooltip.getLines(client), mouseX, mouseY);
                break;
            }
        }
    }
}
