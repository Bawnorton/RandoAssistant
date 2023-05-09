package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.render.RenderingHelper;
import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.mojang.blaze3d.systems.RenderSystem;
import grapher.graph.drawing.Drawing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.minecraft.client.gui.DrawableHelper.drawTexture;

public class LootTableGraphWidget {
    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(RandoAssistant.MOD_ID, "textures/gui/graph.png");
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    private Map<Identifier, Point2D> vertexLocations;
    private Drawing<TrackingGraph.Vertex, TrackingGraph.Edge> drawing;

    private double xOffset = 0;
    private double yOffset = 0;

    private int x;
    private int y;
    
    public static int WIDTH = 324;
    public static int HEIGHT = 167;

    public LootTableGraphWidget(TrackingGraph graph) {
        EXECUTOR_SERVICE.submit(() -> {
            drawing = graph.draw();
            this.vertexLocations = new HashMap<>();
            Map<TrackingGraph.Edge, List<Point2D>> edgeLocations = drawing.getEdgeMappings();

            for (TrackingGraph.Edge edge : edgeLocations.keySet()) {
                TrackingGraph.Vertex source = edge.getOrigin();
                TrackingGraph.Vertex target = edge.getDestination();
                Point2D sourceLocation = edgeLocations.get(edge).get(0);
                Point2D targetLocation = edgeLocations.get(edge).get(1);
                vertexLocations.put(source.getContent(), sourceLocation);
                vertexLocations.put(target.getContent(), targetLocation);
            }

            Point2D centreOnPoint = vertexLocations.get(graph.getLeaves().iterator().next().getIdentifier());
            if(centreOnPoint == null) {
                centreOnPoint = new Point2D.Double(0, 0);
            }
            xOffset = -centreOnPoint.getX() + (double) WIDTH / 2;
            yOffset = -centreOnPoint.getY() + (double) HEIGHT / 2;
        });
    }

    public void move(double x, double y) {
        xOffset += x;
        yOffset += y;
    }

    public void render(int x, int y, MatrixStack matrices) {
        this.x = x;
        this.y = y;
        renderBackground(matrices, x, y);
        if(drawing == null) {
            renderPlaceholder(matrices, x + WIDTH / 2, y + HEIGHT / 2);
            return;
        }
        DrawableHelper.enableScissor(x + 8, y + 8, x + WIDTH - 8, y + HEIGHT - 8);
        renderArrows(matrices, x + xOffset, y + yOffset);
        renderGraph(matrices, x + xOffset, y + yOffset);
        DrawableHelper.disableScissor();
    }

    private void renderBackground(MatrixStack matrices, int x, int y) {
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        drawTexture(matrices, x, y, 1, 1, WIDTH, HEIGHT, 512, 512);
    }

    private void renderPlaceholder(MatrixStack matrices, int x, int y) {
        Text text = Text.of("Loading...");
        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(text);
        int textHeight = MinecraftClient.getInstance().textRenderer.fontHeight;
        int textX = x - textWidth / 2;
        int textY = y - textHeight / 2;
        MinecraftClient.getInstance().textRenderer.draw(matrices, text, textX, textY, 0xFFFFFFFF);
    }

    private void renderGraph(MatrixStack matrices, double x, double y) {
        vertexLocations.forEach((identifier, location) -> {
            int posX = (int) (location.getX() + x);
            int posY = (int) (location.getY() + y);

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
            drawTexture(matrices, posX - 5, posY - 5, 0, 154, 26, 26);
            RenderingHelper.renderIdentifier(identifier, matrices, posX, posY);
        });
    }

    private void renderArrows(MatrixStack matrices, double x, double y) {
        for(TrackingGraph.Edge edge: drawing.getEdgeMappings().keySet()) {
            List<Point2D> points = drawing.getEdgeMappings().get(edge);
            Point2D source = points.get(0);
            Point2D target = points.get(1);

            int x1 = (int) (source.getX() + x + 8);
            int y1 = (int) (source.getY() + y + 8);
            int x2 = (int) (target.getX() + x + 8);
            int y2 = (int) (target.getY() + y + 8);

            int colour = 0xFFFFFFFF;

            Matrix4f matrix = matrices.peek().getPositionMatrix();

            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            double angle = Math.atan2(y2 - y1, x2 - x1);
            double thickness = 1.2;
            
            double xOffset = Math.sin(angle) * thickness;
            double yOffset = Math.cos(angle) * thickness;
            
            bufferBuilder.vertex(matrix, (float) (x1 + xOffset), (float) (y1 - yOffset), 0).color(colour).next();
            bufferBuilder.vertex(matrix, (float) (x1 - xOffset), (float) (y1 + yOffset), 0).color(colour).next();
            bufferBuilder.vertex(matrix, (float) (x2 - xOffset), (float) (y2 + yOffset), 0).color(colour).next();
            bufferBuilder.vertex(matrix, (float) (x2 + xOffset), (float) (y2 - yOffset), 0).color(colour).next();

            Tessellator.getInstance().draw();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            BufferBuilder arrowBufferBuilder = Tessellator.getInstance().getBuffer();
            arrowBufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

            double arrowHeadWidth = 6;
            double arrowHeadDepth = 10;
            double offset = 10;

            x2 -= offset * Math.cos(angle);
            y2 -= offset * Math.sin(angle);

            double arrowX1 = x2 - arrowHeadDepth * Math.cos(angle) + arrowHeadWidth * Math.sin(angle);
            double arrowY1 = y2 - arrowHeadDepth * Math.sin(angle) - arrowHeadWidth * Math.cos(angle);
            double arrowX2 = x2 - arrowHeadDepth * Math.cos(angle) - arrowHeadWidth * Math.sin(angle);
            double arrowY2 = y2 - arrowHeadDepth * Math.sin(angle) + arrowHeadWidth * Math.cos(angle);

            bufferBuilder.vertex(matrix, (float) arrowX1, (float) arrowY1, 0).color(colour).next();
            bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0).color(colour).next();
            bufferBuilder.vertex(matrix, (float) arrowX2, (float) arrowY2, 0).color(colour).next();

            Tessellator.getInstance().draw();
            RenderSystem.enableCull();
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY) {
        if(mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT) {
            move(deltaX, deltaY);
            return true;
        }
        return false;
    }
}
