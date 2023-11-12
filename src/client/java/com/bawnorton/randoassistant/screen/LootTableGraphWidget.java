package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.extend.HoveredTooltipPositionerExtender;
import com.bawnorton.randoassistant.extend.InventoryScreenExtender;
import com.bawnorton.randoassistant.render.RenderingHelper;
import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.bawnorton.randoassistant.util.IdentifierType;
import com.mojang.blaze3d.systems.RenderSystem;
import grapher.graph.drawing.Drawing;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LootTableGraphWidget {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(RandoAssistant.MOD_ID, "graph");
    private static final Identifier UNOBTAINED_GOAL_TEXTURE = AdvancementObtainedStatus.UNOBTAINED.getFrameTexture(AdvancementFrame.GOAL);
    private static final Identifier OBTAINED_GOAL_TEXTURE = AdvancementObtainedStatus.OBTAINED.getFrameTexture(AdvancementFrame.GOAL);
    private static final Identifier UNOBTAINED_TASK_TEXTURE = AdvancementObtainedStatus.UNOBTAINED.getFrameTexture(AdvancementFrame.TASK);
    private static final Identifier OBTAINED_CHALLENGE_TEXTURE = AdvancementObtainedStatus.OBTAINED.getFrameTexture(AdvancementFrame.CHALLENGE);
    
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);
    private static final int ABSOLUTE_SCALE = 2;

    public static final int WIDTH = 324;
    public static final int HEIGHT = 167;

    private TrackingGraph graph;
    private Drawing<TrackingGraph.Vertex, TrackingGraph.Edge> drawing;
    private Map<Identifier, Point2D> vertexLocations;

    private final Identifier target;
    private final Set<Identifier> selected;

    private double xOffset = 0;
    private double yOffset = 0;
    private double scale = 1;
    private int x;
    private int y;

    public LootTableGraphWidget(TrackingGraph graph, Identifier target) {
        this.target = target;
        this.graph = graph;
        this.selected = Collections.synchronizedSet(new HashSet<>());
        refresh();
    }

    private void centreOnPoint(Point2D point) {
        if(point == null) return;
        scale = 1;
        xOffset = -point.getX() / ABSOLUTE_SCALE + (double) WIDTH / 2 - 8;
        yOffset = -point.getY() / ABSOLUTE_SCALE + (double) HEIGHT / 2 - 8;
    }

    public void centreOnTarget() {
        if(drawing == null) return;
        centreOnPoint(vertexLocations.get(target));
    }

    public void move(double x, double y) {
        xOffset += x;
        yOffset += y;
    }

    public boolean scale(double scale) {
        this.scale *= scale;
        if(this.scale < 0.2 || this.scale > 2) {
            this.scale = Math.min(Math.max(this.scale, 0.2), 2);
            return false;
        }
        return true;
    }

    public void setGraph(TrackingGraph graph) {
        this.graph = graph;
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

    public void render(DrawContext context, int x, int y, double mouseX, double mouseY) {
        this.x = x;
        this.y = y;
        Set<Tooltip> sideBarTooltips = renderSideBars(context, x - 30, y, mouseX, mouseY);
        renderBackground(context, x, y);
        if(drawing == null) {
            renderPlaceholder(context, x + WIDTH / 2, y + HEIGHT / 2);
            return;
        }
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.scale((float) scale, (float) scale, 1);
        context.enableScissor(x + 8, y + 8, x + WIDTH - 8, y + HEIGHT - 8);
        renderArrows(matrices, x + xOffset, y + yOffset);
        Set<Tooltip> nodeTooltips = renderNodes(context, x + xOffset, y + yOffset, mouseX / scale, mouseY / scale);
        context.disableScissor();
        renderTooltips(nodeTooltips, context, mouseX / scale, mouseY / scale);
        matrices.pop();
        renderTooltips(sideBarTooltips, context, mouseX, mouseY);
    }

    private void renderTooltips(Iterable<Tooltip> tooltips, DrawContext context, double mouseX, double mouseY) {
        tooltips.forEach(tooltip -> {
            Screen screen = MinecraftClient.getInstance().currentScreen;
            assert screen != null;
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(0, 0, -200);
            ((HoveredTooltipPositionerExtender) HoveredTooltipPositioner.INSTANCE).setIgnorePreventOverflow(true);
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip.getLines(MinecraftClient.getInstance()), HoveredTooltipPositioner.INSTANCE, (int) mouseX, (int) mouseY);
            ((HoveredTooltipPositionerExtender) HoveredTooltipPositioner.INSTANCE).setIgnorePreventOverflow(false);
            matrices.pop();
        });
    }

    private Set<Tooltip> renderSideBars(DrawContext context, int x, int y, double mouseX, double mouseY) {
        boolean hoverTarget = mouseX >= x && mouseX <= x + 26 && mouseY >= y && mouseY <= y + 26;
        context.drawGuiTexture(UNOBTAINED_GOAL_TEXTURE, x, y, 26, 26);
        RenderingHelper.renderIdentifier(target, context, 1, x + 5, y + 5, false);
        y += 30;
        boolean hoverCompass = mouseX >= x && mouseX <= x + 26 && mouseY >= y && mouseY <= y + 26;
        context.drawGuiTexture(hoverCompass ? OBTAINED_GOAL_TEXTURE : UNOBTAINED_GOAL_TEXTURE, x, y, 26, 26);
        RenderingHelper.renderIdentifier(Registries.ITEM.getId(Items.COMPASS), context, 1, x + 5, y + 5, false);
        x += WIDTH + 34;
        y -= 26;
        boolean hoverClose = mouseX >= x && mouseX <= x + 18 && mouseY >= y && mouseY <= y + 18;
        context.drawGuiTexture(LootBookSettingsWidget.TOGGLE_TEXTURES.get(false, hoverClose), x, y, 18, 18);

        Set<Tooltip> tooltips = new HashSet<>();
        if(hoverTarget) {
            if(graph == null) {
                tooltips.add(Tooltip.of(Text.of("Loading...")));
            } else {
                int numSources = graph.getRoots().size();
                tooltips.add(Tooltip.of(Text.of(IdentifierType.getName(target, false) + "\n\n" +
                        "There " + (numSources == 1 ? "is" : "are") + " §b" + numSources +
                        " §rknown way" + (numSources == 1 ? "" : "s") +  " to get this item.")));
            }
        }
        if(hoverCompass) tooltips.add(Tooltip.of(Text.of("Center on " + IdentifierType.getName(target, false))));
        if(hoverClose) tooltips.add(Tooltip.of(Text.of("Close")));
        return tooltips;
    }

    private void renderBackground(DrawContext context, int x, int y) {
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, WIDTH, HEIGHT);
    }

    private void renderPlaceholder(DrawContext context, int x, int y) {
        Text text = Text.of("Loading...");
        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(text);
        int textHeight = MinecraftClient.getInstance().textRenderer.fontHeight;
        int textX = x - textWidth / 2;
        int textY = y - textHeight / 2;
        context.drawText(MinecraftClient.getInstance().textRenderer, text, textX, textY, 0xFFFFFFFF, false);
    }

    private Set<Tooltip> renderNodes(DrawContext context, double x, double y, double mouseX, double mouseY) {
        Set<Tooltip> tooltips = new HashSet<>();
        vertexLocations.forEach((identifier, location) -> {
            int posX = (int) (location.getX() / ABSOLUTE_SCALE + x) - 5;
            int posY = (int) (location.getY() / ABSOLUTE_SCALE + y) - 5;
            boolean hovered = mouseX >= posX  && mouseX <= posX + 26 && mouseY >= posY && mouseY <= posY + 26;
            boolean doesRender = posX * scale >= this.x - 8 && posX * scale <= this.x + WIDTH - 18 && posY * scale >= this.y - 8 && posY * scale <= this.y + HEIGHT - 18;
            if(hovered && doesRender) {
                tooltips.add(Tooltip.of(Text.of(IdentifierType.getName(identifier, !identifier.equals(target)))));
            }
            Identifier nodeTexture = UNOBTAINED_TASK_TEXTURE;
            if (identifier.equals(target)) {
                nodeTexture = OBTAINED_CHALLENGE_TEXTURE;
            } else if (selected != null && selected.contains(identifier)) {
                RenderSystem.setShaderColor(1, 0.1f, 0.1f, 1);
            }
            context.drawGuiTexture(nodeTexture, posX, posY, 26, 26);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderingHelper.renderIdentifier(identifier, context, scale, posX + 5, posY + 5, !identifier.equals(target));
        });
        return tooltips;
    }

    private void renderArrows(MatrixStack matrices, double x, double y) {
        if(drawing == null) return;
        Map<TrackingGraph.Edge, List<Point2D>> mappings = drawing.getEdgeMappings();
        for(TrackingGraph.Edge edge: mappings.keySet()) {
            List<Point2D> points = mappings.get(edge);
            Point2D source = points.get(0);
            Point2D target = points.get(1);

            int x1 = (int) (source.getX() / ABSOLUTE_SCALE + x + 8);
            int y1 = (int) (source.getY() / ABSOLUTE_SCALE + y + 8);
            int x2 = (int) (target.getX() / ABSOLUTE_SCALE + x + 8);
            int y2 = (int) (target.getY() / ABSOLUTE_SCALE + y + 8);

            int colour = 0xFFFFFFFF;
            if(selected.contains(edge.getOrigin().getIdentifier()) && selected.contains(edge.getDestination().getIdentifier())) {
                colour = 0xFFFF2626;
            }

            RenderingHelper.renderGuiArrow(matrices, x1, y1, x2, y2, colour);
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY) {
        if(mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT) {
            move(deltaX / scale, deltaY / scale);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if(mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT) {
            double scale = Math.pow(1.1, verticalAmount);
            if(scale(scale)) {
                mouseX = mouseX - (double) WIDTH / 2;
                mouseY = mouseY - (double) HEIGHT / 2;
                xOffset -= mouseX * (scale - 1);
                yOffset -= mouseY * (scale - 1);
            }
            return true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT) {
            if (button == 1) {
                final double mouseXf = mouseX / scale;
                final double mouseYf = mouseY / scale;
                selected.clear();
                vertexLocations.forEach((identifier, point) -> {
                    if (selected.size() > 0) return;
                    int posX = (int) (point.getX() / ABSOLUTE_SCALE + x + xOffset - 5);
                    int posY = (int) (point.getY() / ABSOLUTE_SCALE + y + yOffset - 5);
                    if (mouseXf >= posX && mouseXf <= posX + 26 && mouseYf >= posY && mouseYf <= posY + 26) {
                        selected.add(identifier);
                        selected.addAll(graph.getChildren(identifier).stream().map(TrackingGraph.Vertex::getIdentifier).collect(Collectors.toSet()));
                    }
                });
                return true;
            }
        } else if(mouseX >= x - 30 && mouseX <= x - 4 && mouseY >= y + 30 && mouseY <= y + 56) {
            centreOnTarget();
            return true;
        } else if(mouseX >= x + WIDTH && mouseX <= x + WIDTH + 26 && mouseY >= y && mouseY <= y + 26) {
            close();
            return true;
        }
        return false;
    }

    public void close() {
        LootTableResultButton.getLastClicked().closeGraph();
        LootBookWidget lootBook = LootBookWidget.getInstance();
        lootBook.moveWidgets(true);
        lootBook.getScreen().y -= HEIGHT / 2;
        TexturedButtonWidget lootButton = ((InventoryScreenExtender) lootBook.getScreen()).getLootBookButton();
        TexturedButtonWidget recipeButton = ((InventoryScreenExtender) lootBook.getScreen()).getRecipeBookButton();
        lootButton.setY(lootButton.getY() - HEIGHT / 2);
        recipeButton.setY(recipeButton.getY() - HEIGHT / 2);
    }
}
