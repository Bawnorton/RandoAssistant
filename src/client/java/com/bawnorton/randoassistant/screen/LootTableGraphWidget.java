package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.render.RenderingHelper;
import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.bawnorton.randoassistant.util.IdentifierType;
import com.mojang.blaze3d.systems.RenderSystem;
import grapher.graph.drawing.Drawing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.minecraft.client.gui.DrawableHelper.drawTexture;

public class LootTableGraphWidget {
    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(RandoAssistant.MOD_ID, "textures/gui/graph.png");
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);
    private Map<Identifier, Point2D> vertexLocations;
    private Drawing<TrackingGraph.Vertex, TrackingGraph.Edge> drawing;

    private final TrackingGraph graph;
    private final Identifier target;
    private double xOffset = 0;
    private double yOffset = 0;
    private double scale = 1;

    private int x;
    private int y;

    public static int WIDTH = 324;
    public static int HEIGHT = 167;

    public LootTableGraphWidget(TrackingGraph graph, Identifier target) {
        this.target = target;
        this.graph = graph;
        refresh();
    }

    private void centreOnPoint(Point2D point) {
        xOffset = -point.getX() + (double) WIDTH / 2 - 8;
        yOffset = -point.getY() + (double) HEIGHT / 2 - 8;
    }

    public void centreOnTarget() {
        if(drawing == null) return;
        centreOnPoint(vertexLocations.get(target));
    }

    public void move(double x, double y) {
        xOffset += x;
        yOffset += y;
    }

    public void scale(double scale) {
        this.scale *= scale;
        this.scale = Math.min(Math.max(this.scale, 0.43), 2);
    }

    public void refresh() {
        drawing = null;
        EXECUTOR_SERVICE.submit(() -> {
            drawing = graph.draw();
            this.vertexLocations = Collections.synchronizedMap(new HashMap<>());
            Map<TrackingGraph.Edge, List<Point2D>> edgeLocations = drawing.getEdgeMappings();

            for (TrackingGraph.Edge edge : edgeLocations.keySet()) {
                TrackingGraph.Vertex origin = edge.getOrigin();
                TrackingGraph.Vertex destination = edge.getDestination();
                Point2D originLocation = edgeLocations.get(edge).get(0);
                Point2D destinationLocation = edgeLocations.get(edge).get(1);
                vertexLocations.put(origin.getContent(), originLocation);
                vertexLocations.put(destination.getContent(), destinationLocation);
            }

            centreOnPoint(vertexLocations.get(target));
        });
    }

    public void render(MatrixStack matrices, int x, int y, double mouseX, double mouseY) {
        this.x = x;
        this.y = y;
        renderBackground(matrices, x, y);
        if(drawing == null) {
            renderPlaceholder(matrices, x + WIDTH / 2, y + HEIGHT / 2);
            return;
        }
        matrices.push();
        matrices.scale((float) scale, (float) scale, 1);
        DrawableHelper.enableScissor(x + 8, y + 8, x + WIDTH - 8, y + HEIGHT - 8);
        renderArrows(matrices, x + xOffset, y + yOffset);
        Set<Tooltip> toolips = renderNodes(matrices, x + xOffset, y + yOffset, mouseX, mouseY);
        DrawableHelper.disableScissor();
        toolips.forEach(tooltip -> {
            Screen screen = MinecraftClient.getInstance().currentScreen;
            assert screen != null;
            matrices.push();
            matrices.translate(0, 0, -200);
            screen.renderOrderedTooltip(matrices, tooltip.getLines(MinecraftClient.getInstance()), (int) (mouseX / scale), (int) (mouseY / scale));
            matrices.pop();
        });
        matrices.pop();
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

    private Set<Tooltip> renderNodes(MatrixStack matrices, double x, double y, double mouseX, double mouseY) {
        Set<Tooltip> tooltips = new HashSet<>();
        final double mouseXf = mouseX / scale;
        final double mouseYf = mouseY / scale;

        vertexLocations.forEach((identifier, location) -> {
            int posX = (int) (location.getX() + x) - 5;
            int posY = (int) (location.getY() + y) - 5;
            boolean hovered = mouseXf >= posX  && mouseXf <= posX + 26 && mouseYf >= posY && mouseYf <= posY + 26;
            boolean doesRender = posX * scale >= this.x - 8 && posX * scale <= this.x + WIDTH - 18 && posY * scale >= this.y - 8 && posY * scale <= this.y + HEIGHT - 18;
            if(hovered && doesRender) {
                tooltips.add(Tooltip.of(Text.of(IdentifierType.getName(identifier, !identifier.equals(target)))));
            }

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
            int u = 0;
            int v = 154;
            if (identifier.equals(target)) {
                u += 26;
                v -= 26;
            }
            drawTexture(matrices, posX, posY, u, v, 26, 26);
            RenderingHelper.renderIdentifier(identifier, matrices, scale, posX + 5, posY + 5, !identifier.equals(target));
        });
        return tooltips;
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
            move(deltaX / scale, deltaY / scale);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if(mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT) {
            double scale = Math.pow(1.1, amount);
            scale(scale);
            mouseX = mouseX - (double) WIDTH / 2;
            mouseY = mouseY - (double) HEIGHT / 2;
            xOffset -= mouseX * (scale - 1);
            yOffset -= mouseY * (scale - 1);
            return true;
        }
        return false;
    }
}
