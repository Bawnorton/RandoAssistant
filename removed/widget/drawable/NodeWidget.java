package com.bawnorton.randoassistant.screen.widget.drawable;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.graph.LootTableGraph;
import com.bawnorton.randoassistant.mixin.AbstractPlantPartBlockInvoker;
import com.bawnorton.randoassistant.mixin.AttachedStemBlockAccessor;
import com.bawnorton.randoassistant.screen.widget.GraphDisplayWidget;
import com.bawnorton.randoassistant.screen.widget.ShowOneLineWidget;
import com.bawnorton.randoassistant.search.Searchable;
import com.bawnorton.randoassistant.util.Line;
import com.bawnorton.randoassistant.util.tuples.Wrapper;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import java.util.Set;

public class NodeWidget extends DrawableHelper implements Searchable {
    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");
    private static NodeWidget selectedNode;
    private static int SIZE;

    private final LootTableGraph.Vertex vertex;
    private final int x;
    private final int y;
    private Rectangle2D.Float bounds;

    public NodeWidget(LootTableGraph.Vertex vertex, Point2D location) {
        this.vertex = vertex;
        this.x = (int) (location.getX());
        this.y = (int) (location.getY());
        SIZE = 26;
    }

    public static void deselect() {
        if (selectedNode == null) return;
        selectedNode.vertex.unhighlightConnected();
    }

    public static void refreshSelectedNode() {
        if (selectedNode == null) return;
        selectedNode.vertex.unhighlightConnected();
        selectedNode.vertex.highlightAsTarget();
        if (!RandoAssistantClient.hideChildren) selectedNode.vertex.highlightChildren();
        selectedNode.vertex.highlightParents();
    }

    @Nullable
    public static NodeWidget getSelectedNode() {
        return selectedNode;
    }

    private void drawEntity(int x, int y, LivingEntity entity) {
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate((float) x, (float) y, 1050F);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        MatrixStack matrixStack2 = new MatrixStack();
        matrixStack2.translate(0.0F, 0.0F, 1000.0F);
        matrixStack2.scale((float) 10, (float) 10, (float) 10);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX((float) (-30 * Math.PI / 180));
        quaternionf.mul(quaternionf2);
        matrixStack2.multiply(quaternionf);
        float h = entity.bodyYaw;
        float i = entity.getYaw();
        float j = entity.getPitch();
        float k = entity.prevHeadYaw;
        float l = entity.headYaw;
        entity.bodyYaw = 135F;
        entity.setYaw(135F);
        entity.setPitch(0F);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();
        RenderSystem.setShaderLights(new Vector3f(-0.2F, 1.0F, -1.0F), new Vector3f(0.2F, -1.0F, 0.0F));
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        quaternionf2.conjugate();
        entityRenderDispatcher.setRotation(quaternionf2);
        entityRenderDispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, matrixStack2, immediate, 15728880);
        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);
        entity.bodyYaw = h;
        entity.setYaw(i);
        entity.setPitch(j);
        entity.prevHeadYaw = k;
        entity.headYaw = l;
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();
    }

    public Tooltip render(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        Tooltip tooltip = null;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);

        boolean isInteraction = vertex.isHighlightedAsInteraction();
        if (vertex.isHighlightedAsTarget()) {
            RenderSystem.setShaderColor(0.1F, 1F, 0.1F, 1F);
        } else if (vertex.isHighlightedAsParent()) {
            if (isInteraction) {
                Wrapper<Boolean> shouldHighlightAsInteraction = Wrapper.of(false);
                vertex.getImmediateVerticesAssociatedWith(false).forEach(parent -> {
                    if (parent.isHighlightedAsInteraction()) shouldHighlightAsInteraction.set(true);
                });
                if (shouldHighlightAsInteraction.get()) {
                    RenderSystem.setShaderColor(1F, 1F, 0.1F, 1F);
                } else {
                    RenderSystem.setShaderColor(1F, 0.1F, 0.1F, 1F);
                }
            } else {
                RenderSystem.setShaderColor(1F, 0.1F, 0.1F, 1F);
            }
            if (RandoAssistantClient.showLine != -1) {
                Line currentLine = GraphDisplayWidget.getInstance().getLine(RandoAssistantClient.showLine);
                if (!currentLine.contains(getVertex())) {
                    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                }
            }
        } else if (vertex.isHighlightedAsChild()) {
            if (isInteraction) {
                Wrapper<Boolean> shouldHighlightAsInteraction = Wrapper.of(false);
                vertex.getImmediateVerticesAssociatedWith(false).forEach(child -> {
                    if (child.isHighlightedAsInteraction()) shouldHighlightAsInteraction.set(true);
                });
                if (shouldHighlightAsInteraction.get()) {
                    RenderSystem.setShaderColor(1F, 1F, 0.1F, 1F);
                } else {
                    RenderSystem.setShaderColor(0.1F, 0.1F, 1F, 1F);
                }
            } else {
                RenderSystem.setShaderColor(0.1F, 0.1F, 1F, 1F);
            }
        } else {
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }

        bounds = new Rectangle2D.Float(x - SIZE / 2f - 5, y - SIZE / 2f - 5, SIZE, SIZE);
        boolean hovered = bounds.contains(mouseX, mouseY);

        if (hovered) {
            float[] color = RenderSystem.getShaderColor();
            RenderSystem.setShaderColor(color[0] * 0.7F, color[1] * 0.7F, color[2] * 0.7F, 1F);
            if (Config.getInstance().debug) {
                tooltip = Tooltip.of(Text.of(
                        "Content: " + vertex.getContent().toString() + "\n"
                                + "Target: " + vertex.isHighlightedAsTarget() + "\n"
                                + "Interaction: " + vertex.isHighlightedAsInteraction() + "\n"
                                + "Parent: " + vertex.isHighlightedAsParent() + "\n"
                                + "Child: " + vertex.isHighlightedAsChild()
                ));
            } else {
                tooltip = Tooltip.of(vertex.getTooltip());
            }
        }

        DrawableHelper.drawTexture(matrices, x - SIZE / 2 - 5, y - SIZE / 2 - 5, 0, 128 + 26, SIZE, SIZE);

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        Object content = vertex.getContent();

        if (content instanceof Item item) {
            ItemStack icon = new ItemStack(item);
            MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(matrices, icon, x - SIZE / 2, y - SIZE / 2);
        } else if (content instanceof Block block) {
            if (block instanceof FlowerPotBlock flowerPotBlock) {
                ItemStack icon = new ItemStack(flowerPotBlock.getContent().asItem());
                ItemStack pot = new ItemStack(Items.FLOWER_POT);
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(matrices, icon, x - SIZE / 2, y - SIZE / 2);
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(matrices, pot, x - SIZE / 2, y + SIZE / 2);
            } else if (block instanceof CandleCakeBlock candleCakeBlock) {
                ItemStack icon = new ItemStack(Items.CAKE);
                ItemStack candle = new ItemStack(RandoAssistant.CANDLE_CAKE_MAP.get(candleCakeBlock));
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(matrices, icon, x - SIZE / 2, y - SIZE / 2);
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(matrices, candle, x - SIZE / 2, y + SIZE / 2);
            } else if (block instanceof AbstractPlantPartBlock abstractPlantBlock) {
                AbstractPlantStemBlock stemBlock = ((AbstractPlantPartBlockInvoker) abstractPlantBlock).getStem();
                ItemStack icon = new ItemStack(stemBlock.getDefaultState().getBlock().asItem());
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(matrices, icon, x - SIZE / 2, y - SIZE / 2);
            } else if (block instanceof AttachedStemBlock attachedStemBlock) {
                GourdBlock gourdBlock = ((AttachedStemBlockAccessor) attachedStemBlock).getGourdBlock();
                ItemStack icon = new ItemStack(gourdBlock.getStem().asItem());
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(matrices, icon, x - SIZE / 2, y - SIZE / 2);
            } else if (block instanceof TallSeagrassBlock) {
                ItemStack icon = new ItemStack(Items.SEAGRASS);
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(matrices, icon, x - SIZE / 2, y - SIZE / 2);
            } else {
                ItemStack icon = new ItemStack(block.asItem());
                if (icon.getItem() == Items.AIR) icon = new ItemStack(Items.BARRIER);
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(matrices, icon, x - SIZE / 2, y - SIZE / 2);
            }
        } else if (content instanceof EntityType<?> entityType) {
            MinecraftClient client = MinecraftClient.getInstance();
            drawEntity(x - 5, y + 2, (LivingEntity) Objects.requireNonNull(entityType.create(client.world)));
        } else if (content instanceof Identifier) {
            ItemStack icon = new ItemStack(Items.CHEST);
            MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(matrices, icon, x - SIZE / 2, y - SIZE / 2);
        }
        return tooltip;
    }

    public InputResult handleMouseDown(int x, int y) {
        if (bounds == null) return InputResult.IGNORED;
        if (bounds.contains(x, y)) {
            this.select();
            return InputResult.PROCESSED;
        }
        return InputResult.IGNORED;
    }

    @Override
    public String getSearchableString() {
        return vertex.getTooltip().getString();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public LootTableGraph.Vertex getVertex() {
        return vertex;
    }

    public void select() {
        if (selectedNode != null) {
            deselect();
        }
        selectedNode = this;
        selectedNode.vertex.highlightAsTarget();
        if (!RandoAssistantClient.hideChildren) selectedNode.vertex.highlightChildren();
        selectedNode.vertex.highlightParents();

        Set<Line> lines = Line.builder().addLines(this.getVertex()).build();
        GraphDisplayWidget.getInstance().setCurrentLines(lines);
        ShowOneLineWidget.getInstance().setMaxValue(lines.size() - 1);
        ShowOneLineWidget.getInstance().setValue(-1, true);
    }
}
