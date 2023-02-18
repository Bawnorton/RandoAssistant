package com.bawnorton.randoassistant.screen.widget.drawable;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.mixin.AbstractPlantPartBlockInvoker;
import com.bawnorton.randoassistant.mixin.AttachedStemBlockAccessor;
import com.bawnorton.randoassistant.search.Searchable;
import com.bawnorton.randoassistant.graph.LootTableGraph;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

public class NodeWidget extends DrawableHelper implements Searchable {
    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");
    private static NodeWidget selectedNode;
    private static int SIZE;
    private final int x;
    private final int y;
    private final LootTableGraph.Vertex node;
    private Rectangle2D.Float bounds;

    public NodeWidget(LootTableGraph.Vertex node, Point2D location) {
        this.node = node;
        this.x = (int) (location.getX());
        this.y = (int) (location.getY());
        SIZE = 26;
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
        entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, matrixStack2, immediate, 16777215);
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

        bounds = new Rectangle2D.Float(x - SIZE / 2f - 5, y - SIZE / 2f - 5, SIZE, SIZE);
        boolean hovered = bounds.contains(mouseX, mouseY);
        if (hovered) {
            RenderSystem.setShaderColor(0.75F, 0.75F, 0.75F, 1F);
            tooltip = Tooltip.of(node.getTooltip());
        } else if (node.isHighlightedAsTarget()) {
            RenderSystem.setShaderColor(0.1F, 1F, 0.1F, 1F);
        } else if (node.isHighlightedAsParent()) {
            RenderSystem.setShaderColor(1F, 0.1F, 0.1F, 1F);
        } else if (node.isHighlightedAsChild()) {
            RenderSystem.setShaderColor(0.1F, 0.1F, 1F, 1F);
        } else {
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }

        this.drawTexture(matrices, x - SIZE / 2 - 5, y - SIZE / 2 - 5, 0, 128 + 26, SIZE, SIZE);

        if (node.isItem()) {
            ItemStack icon = new ItemStack(node.getItem());
            MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(icon, x - SIZE / 2, y - SIZE / 2);
        } else if (node.isBlock()) {
            Block block = node.getBlock();
            if (block instanceof FlowerPotBlock flowerPotBlock) {
                ItemStack icon = new ItemStack(flowerPotBlock.getContent().asItem());
                ItemStack pot = new ItemStack(Items.FLOWER_POT);
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(icon, x - SIZE / 2, y - SIZE / 2);
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(pot, x - SIZE / 2, y + SIZE / 2);
            } else if (block instanceof CandleCakeBlock candleCakeBlock) {
                ItemStack icon = new ItemStack(Items.CAKE);
                ItemStack candle = new ItemStack(RandoAssistant.CANDLE_CAKE_MAP.get(candleCakeBlock));
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(icon, x - SIZE / 2, y - SIZE / 2);
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(candle, x - SIZE / 2, y + SIZE / 2);
            } else if (block instanceof AbstractPlantPartBlock abstractPlantBlock) {
                AbstractPlantStemBlock stemBlock = ((AbstractPlantPartBlockInvoker) abstractPlantBlock).getStem();
                ItemStack icon = new ItemStack(stemBlock.getDefaultState().getBlock().asItem());
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(icon, x - SIZE / 2, y - SIZE / 2);
            } else if (block instanceof AttachedStemBlock attachedStemBlock) {
                GourdBlock gourdBlock = ((AttachedStemBlockAccessor) attachedStemBlock).getGourdBlock();
                ItemStack icon = new ItemStack(gourdBlock.getStem().asItem());
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(icon, x - SIZE / 2, y - SIZE / 2);
            } else if (block instanceof TallSeagrassBlock) {
                ItemStack icon = new ItemStack(Items.SEAGRASS);
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(icon, x - SIZE / 2, y - SIZE / 2);
            } else {
                ItemStack icon = new ItemStack(block.asItem());
                if (icon.getItem() == Items.AIR) icon = new ItemStack(Items.BARRIER);
                MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(icon, x - SIZE / 2, y - SIZE / 2);
            }
        } else if (node.isEntity()) {
            EntityType<?> entityType = node.getEntityType();
            MinecraftClient client = MinecraftClient.getInstance();
            drawEntity(x - 5, y + 2, (LivingEntity) Objects.requireNonNull(entityType.create(client.world)));
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
        return node.getTooltip().getString();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void highlightParents() {
        node.highlightAsTarget();
        node.highlightAsParent();
        node.getParents().forEach(LootTableGraph.Vertex::highlightAsParent);
    }

    public void unhighlightParents() {
        node.unhighlightAsTarget();
        node.unhighlightAsParent();
        node.getParents().forEach(LootTableGraph.Vertex::unhighlightAsParent);
    }

    public void highlightChildren() {
        node.highlightAsTarget();
        node.highlightAsChild();
        node.getChildren().forEach(LootTableGraph.Vertex::highlightAsChild);
    }

    public void unhighlightChildren() {
        node.unhighlightAsTarget();
        node.unhighlightAsChild();
        node.getChildren().forEach(LootTableGraph.Vertex::unhighlightAsChild);
    }

    public static void deselect() {
        if(selectedNode == null) return;
        selectedNode.unhighlightChildren();
        selectedNode.unhighlightParents();
    }

    public void select() {
        if(this == selectedNode) return;
        if (selectedNode != null) {
            deselect();
        }
        selectedNode = this;
        selectedNode.highlightChildren();
        selectedNode.highlightParents();
    }
}
