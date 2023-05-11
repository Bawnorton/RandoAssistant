package com.bawnorton.randoassistant.render;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.mixin.client.AbstractPlantPartBlockInvoker;
import com.bawnorton.randoassistant.util.Easing;
import com.bawnorton.randoassistant.util.IdentifierType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.geom.Point2D;

public class RenderingHelper {
    private static final Identifier STAR = new Identifier(RandoAssistant.MOD_ID, "textures/gui/item_stars.png");
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void renderIdentifier(Identifier id, MatrixStack matrices, double scale, int x, int y, boolean preferEntity) {
        IdentifierType type = IdentifierType.fromId(id);
        if(IdentifierType.isItemAndEntity(id)) {
            type = preferEntity ? IdentifierType.ENTITY : IdentifierType.ITEM;
        }
        switch (type) {
            case ENTITY -> {
                Entity entity = Registries.ENTITY_TYPE.get(id).create(client.world);
                if (entity instanceof LivingEntity livingEntity) {
                    drawEntity(scale, x + 11, y + 12, livingEntity);
                    return;
                }
                try {
                    client.getItemRenderer().renderGuiItemIcon(matrices, new ItemStack(Registries.ITEM.get(id)), x, y);
                } catch (IllegalStateException e) {
                    client.getItemRenderer().renderGuiItemIcon(matrices, new ItemStack(Items.BARRIER), x, y);
                }
            }
            case BLOCK -> {
                Block block = Registries.BLOCK.get(id);
                if (block instanceof FlowerPotBlock ||
                        block instanceof CandleCakeBlock ||
                        block instanceof AttachedStemBlock ||
                        block instanceof FrostedIceBlock ||
                        block instanceof AbstractFireBlock ||
                        block instanceof BambooSaplingBlock) {
                    drawBlock(matrices, block, x, y);
                    return;
                }
                if (block instanceof AbstractPlantPartBlock abstractPlantBlock) {
                    AbstractPlantStemBlock stemBlock = ((AbstractPlantPartBlockInvoker) abstractPlantBlock).getStem();
                    ItemStack icon = new ItemStack(stemBlock.getDefaultState().getBlock().asItem());
                    client.getItemRenderer().renderGuiItemIcon(matrices, icon, x, y);
                    return;
                }
                if (block instanceof TallSeagrassBlock) {
                    ItemStack icon = new ItemStack(Items.SEAGRASS);
                    client.getItemRenderer().renderGuiItemIcon(matrices, icon, x, y);
                    return;
                }
                ItemStack icon = new ItemStack(block.asItem());
                if (icon.getItem() == Items.AIR) icon = new ItemStack(Items.BARRIER);
                client.getItemRenderer().renderGuiItemIcon(matrices, icon, x, y);
            }
            case ITEM -> client.getItemRenderer().renderGuiItemIcon(matrices, new ItemStack(Registries.ITEM.get(id)), x, y);
            case OTHER -> {
                String path = id.getPath();
                if (path.contains("sheep")) {
                    String[] parts = path.split("/");
                    String colour = parts[parts.length - 1];
                    SheepEntity sheep = EntityType.SHEEP.create(client.world);
                    if(sheep != null) {
                        sheep.setColor(DyeColor.byName(colour.toLowerCase(), DyeColor.WHITE));
                        drawEntity(scale, x + 11, y + 12, sheep);
                    }
                } else if (path.contains("fishing")) {
                    client.getItemRenderer().renderGuiItemIcon(matrices, new ItemStack(Items.FISHING_ROD), x, y);
                } else {
                    client.getItemRenderer().renderGuiItemIcon(matrices, new ItemStack(Items.CHEST), x, y);
                }
            }
        }
    }

    private static void drawBlock(MatrixStack matrices, Block block, int x, int y) {
        BlockState state = block.getDefaultState();
        DiffuseLighting.disableGuiDepthLighting();

        matrices.push();
        matrices.translate(x + 10, y + 6, 100);
        if(block instanceof FlowerPotBlock || block instanceof CandleCakeBlock || block instanceof BambooSaplingBlock) {
            matrices.translate(-2, 0, 0);
        }
        matrices.scale(15, -15, 40);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(225));
        matrices.translate(-0.5, -0.5, -0.5);

        VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
        client.getBlockRenderManager().renderBlockAsEntity(state, matrices, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);

        RenderSystem.setShaderLights(new Vector3f(-1.5f, -0.5f, 0), new Vector3f(0, -1, 0));
        vertexConsumers.draw();
        DiffuseLighting.enableGuiDepthLighting();

        matrices.pop();
    }

    private static void drawEntity(double scale, int x, int y, LivingEntity entity) {
        MatrixStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.push();
        modelViewStack.translate(x * scale, y * scale, 1050);
        modelViewStack.scale((float) scale, (float) scale, -1);
        RenderSystem.applyModelViewMatrix();
        MatrixStack matrices = new MatrixStack();
        matrices.translate(-3, 0, 100);

        Box box = entity.getBoundingBox();
        float entityScale = 1.0f / (float) (Math.max(box.getXLength(), Math.max(box.getYLength(), box.getZLength())));
        matrices.scale(entityScale, entityScale, entityScale);

        if(entity instanceof SquidEntity) {
            matrices.translate(5, -3, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-123));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(22.5F));
        } else if (entity instanceof SlimeEntity) {
            matrices.scale(4, 4, 4);
        } else if (entity instanceof EnderDragonEntity) {
            matrices.scale(-4, 4, 4);
            matrices.translate(-20, 0, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
        } else if (entity instanceof BatEntity) {
            matrices.scale(2, 2, 2);
            matrices.translate(0, 2, 0);
        } else if (entity instanceof FoxEntity) {
            matrices.translate(2, 2, 0);
        } else if (entity instanceof AbstractHorseEntity) {
            matrices.translate(-2, 0, 0);
        }

        matrices.scale(10, 10, 10);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX((float) (-30 * Math.PI / 180));
        quaternionf.mul(quaternionf2);
        matrices.multiply(quaternionf);

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

        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.setShaderLights(new Vector3f(-0.2F, 1.0F, -1.0F), new Vector3f(0.2F, -1.0F, 0.0F));
        quaternionf2.conjugate();

        EntityRenderDispatcher entityRenderDispatcher = client.getEntityRenderDispatcher();
        entityRenderDispatcher.setRotation(quaternionf2);
        entityRenderDispatcher.setRenderShadows(false);
        entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, matrices, immediate, 15728880);
        entityRenderDispatcher.setRenderShadows(true);

        immediate.draw();

        entity.bodyYaw = h;
        entity.setYaw(i);
        entity.setPitch(j);
        entity.prevHeadYaw = k;
        entity.headYaw = l;

        matrices.pop();
        modelViewStack.pop();
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();
    }

    public static void renderStar(MatrixStack matrices, int x, int y, boolean silkTouch) {
        float timeOffset = Math.abs(((System.currentTimeMillis() % 2000) / 1000.0f) - 1.0f);
        matrices.push();
        matrices.translate(0, -Easing.ease(0, 1, timeOffset), 300);
        RenderSystem.setShaderTexture(0, STAR);
        DrawableHelper.drawTexture(matrices, x, y, silkTouch ? 8 : 0, 0, 8, 8, 16, 16);
        matrices.pop();
    }

    public static void renderGuiArrow(MatrixStack matrices, int x1, int y1, int x2, int y2, int colour) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float angle = (float) Math.atan2(y2 - y1, x2 - x1);
        float thickness = 1.2f;

        float xOffset = (float) (Math.sin(angle) * thickness);
        float yOffset = (float) (Math.cos(angle) * thickness);

        Point2D.Float corner1 = new Point2D.Float(x1 + xOffset, y1 - yOffset);
        Point2D.Float corner2 = new Point2D.Float(x1 - xOffset, y1 + yOffset);
        Point2D.Float corner3 = new Point2D.Float(x2 - xOffset, y2 + yOffset);
        Point2D.Float corner4 = new Point2D.Float(x2 + xOffset, y2 - yOffset);

        double arrowHeadWidth = 6;
        double arrowHeadDepth = 10;
        double offset = 10;

        x2 -= offset * Math.cos(angle);
        y2 -= offset * Math.sin(angle);

        double arrowX1 = x2 - arrowHeadDepth * Math.cos(angle) + arrowHeadWidth * Math.sin(angle);
        double arrowY1 = y2 - arrowHeadDepth * Math.sin(angle) - arrowHeadWidth * Math.cos(angle);
        double arrowX2 = x2 - arrowHeadDepth * Math.cos(angle) - arrowHeadWidth * Math.sin(angle);
        double arrowY2 = y2 - arrowHeadDepth * Math.sin(angle) + arrowHeadWidth * Math.cos(angle);

        Point2D.Float arrowCorner1 = new Point2D.Float((float) arrowX1, (float) arrowY1);
        Point2D.Float arrowCorner2 = new Point2D.Float(x2, y2);
        Point2D.Float arrowCorner3 = new Point2D.Float((float) arrowX2, (float) arrowY2);

        bufferBuilder.vertex(matrix, corner1.x, corner1.y, 0).color(colour).next();
        bufferBuilder.vertex(matrix, corner2.x, corner2.y, 0).color(colour).next();
        bufferBuilder.vertex(matrix, corner3.x, corner3.y, 0).color(colour).next();
        bufferBuilder.vertex(matrix, corner4.x, corner4.y, 0).color(colour).next();

        Tessellator.getInstance().draw();

        BufferBuilder arrowBufferBuilder = Tessellator.getInstance().getBuffer();
        arrowBufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, arrowCorner1.x, arrowCorner1.y, 0).color(colour).next();
        bufferBuilder.vertex(matrix, arrowCorner2.x, arrowCorner2.y, 0).color(colour).next();
        bufferBuilder.vertex(matrix, arrowCorner3.x, arrowCorner3.y, 0).color(colour).next();

        Tessellator.getInstance().draw();
        RenderSystem.enableCull();
    }
}
