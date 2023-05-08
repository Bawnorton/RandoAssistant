package com.bawnorton.randoassistant.render;

import com.bawnorton.randoassistant.mixin.client.AbstractPlantPartBlockInvoker;
import com.bawnorton.randoassistant.tracking.trackable.Trackable;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RenderingHelper {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void renderTrackable(Trackable<?> trackable, MatrixStack matrices, int x, int y) {
        Identifier source = trackable.getIdentifier();
        if (Registries.ENTITY_TYPE.containsId(source)) {
            Entity entity = Registries.ENTITY_TYPE.get(source).create(client.world);
            if (entity instanceof LivingEntity livingEntity) {
                drawEntity(x + 11, y + 12, livingEntity);
                return;
            }
        }
        if (Registries.BLOCK.containsId(source)) {
            Block block = Registries.BLOCK.get(source);
            if (block instanceof FlowerPotBlock ||
                    block instanceof CandleCakeBlock ||
                    block instanceof AttachedStemBlock ||
                    block instanceof FrostedIceBlock ||
                    block instanceof AbstractFireBlock) {
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
            return;
        }
        if(Registries.ITEM.containsId(source)) {
            Item item = Registries.ITEM.get(source);
            ItemStack icon = new ItemStack(item);
            client.getItemRenderer().renderGuiItemIcon(matrices, icon, x, y);
            return;
        }
        ItemStack icon = new ItemStack(Items.CHEST);
        client.getItemRenderer().renderGuiItemIcon(matrices, icon, x, y);
    }

    private static void drawBlock(MatrixStack matrixStack, Block block, int x, int y) {
        BlockState state = block.getDefaultState();
        DiffuseLighting.disableGuiDepthLighting();

        matrixStack.push();
        matrixStack.translate(x + 10, y + 6, 100);
        if(block instanceof FlowerPotBlock) {
            matrixStack.translate(-2, 0, 0);
        }
        matrixStack.scale(15, -15, 40);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(225));
        matrixStack.translate(-0.5, -0.5, -0.5);

        VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
        client.getBlockRenderManager().renderBlockAsEntity(state, matrixStack, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);

        RenderSystem.setShaderLights(new Vector3f(-1.5f, -0.5f, 0), new Vector3f(0, -1, 0));
        vertexConsumers.draw();
        DiffuseLighting.enableGuiDepthLighting();

        matrixStack.pop();
    }

    private static void drawEntity(int x, int y, LivingEntity entity) {
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(x, y, 1050);
        matrixStack.scale(1, 1, -1);
        RenderSystem.applyModelViewMatrix();
        MatrixStack matrixStack2 = new MatrixStack();
        matrixStack2.translate(-3, 0, 100);

        Box box = entity.getBoundingBox();
        float scale = 1.0f / (float) (Math.max(box.getXLength(), Math.max(box.getYLength(), box.getZLength())));
        matrixStack2.scale(scale, scale, scale);

        if(entity instanceof SquidEntity) {
            matrixStack2.translate(5, -3, 0);
            matrixStack2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            matrixStack2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-123));
            matrixStack2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(22.5F));
        } else if (entity instanceof SlimeEntity) {
            matrixStack2.scale(4, 4, 4);
        } else if (entity instanceof EnderDragonEntity) {
            matrixStack2.scale(-4, 4, 4);
            matrixStack2.translate(-20, 0, 0);
            matrixStack2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45));
            matrixStack2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
        } else if (entity instanceof BatEntity) {
            matrixStack2.scale(2, 2, 2);
            matrixStack2.translate(0, 2, 0);
        } else if (entity instanceof FoxEntity) {
            matrixStack2.translate(2, 2, 0);
        } else if (entity instanceof AbstractHorseEntity) {
            matrixStack2.translate(-2, 0, 0);
        }

        matrixStack2.scale(10, 10, 10);
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
        EntityRenderDispatcher entityRenderDispatcher = client.getEntityRenderDispatcher();
        quaternionf2.conjugate();
        entityRenderDispatcher.setRotation(quaternionf2);
        entityRenderDispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
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
}
