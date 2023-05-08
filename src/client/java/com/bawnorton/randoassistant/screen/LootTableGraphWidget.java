package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.render.RenderingHelper;
import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.bawnorton.randoassistant.tracking.trackable.Trackable;
import com.bawnorton.randoassistant.util.tuples.Pair;
import com.mojang.blaze3d.systems.RenderSystem;
import grapher.graph.drawing.Drawing;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LootTableGraphWidget {
    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");
    private final Drawing<TrackingGraph.Vertex, TrackingGraph.Edge> drawing;
    private final Set<Pair<Trackable<?>, Point2D>> vertexLocations;

    public LootTableGraphWidget(TrackingGraph graph) {
        this.drawing = graph.draw();
        this.vertexLocations = new HashSet<>();
        Map<TrackingGraph.Edge, List<Point2D>> edgeLocations = drawing.getEdgeMappings();

        for (TrackingGraph.Edge edge : edgeLocations.keySet()) {
            TrackingGraph.Vertex source = edge.getOrigin();
            TrackingGraph.Vertex target = edge.getDestination();
            Point2D sourceLocation = edgeLocations.get(edge).get(0);
            Point2D targetLocation = edgeLocations.get(edge).get(1);
            vertexLocations.add(new Pair<>(source.getContent(), sourceLocation));
            vertexLocations.add(new Pair<>(target.getContent(), targetLocation));
        }
    }

    public void render(MatrixStack matrices, int x, int y) {
        renderBackground(x, y);
        renderArrows(matrices, x, y);
        renderGraph(matrices, x, y);
    }

    private void renderBackground(int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, new Identifier("textures/block/stone.png"));
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(x, y + 128, 0.0D).texture(0.0F, 1.0F).next();
        bufferBuilder.vertex(x + 128, y + 128, 0.0D).texture(1.0F, 1.0F).next();
        bufferBuilder.vertex(x + 128, y, 0.0D).texture(1.0F, 0.0F).next();
        bufferBuilder.vertex(x, y, 0.0D).texture(0.0F, 0.0F).next();
        tessellator.draw();
    }

    private void renderGraph(MatrixStack matrices, int x, int y) {
        vertexLocations.forEach(pair -> {
            Trackable<?> trackable = pair.a();
            Point2D location = pair.b();
            int posX = (int) location.getX() + x;
            int posY = (int) location.getY() + y;

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
            DrawableHelper.drawTexture(matrices, posX - 5, posY - 5, 0, 154, 26, 26);
            RenderingHelper.renderTrackable(trackable, matrices, posX, posY);
        });
    }

    private void renderArrows(MatrixStack matrices, int x, int y) {
        for(TrackingGraph.Edge edge: drawing.getEdgeMappings().keySet()) {
            List<Point2D> points = drawing.getEdgeMappings().get(edge);
            Point2D source = points.get(0);
            Point2D target = points.get(1);

            int x1 = (int) source.getX() + x + 8;
            int y1 = (int) source.getY() + y + 8;
            int x2 = (int) target.getX() + x + 8;
            int y2 = (int) target.getY() + y + 8;

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
}
