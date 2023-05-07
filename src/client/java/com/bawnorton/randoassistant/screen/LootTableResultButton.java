package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.mixin.client.AbstractPlantPartBlockInvoker;
import com.bawnorton.randoassistant.mixin.client.AttachedStemBlockAccessor;
import com.bawnorton.randoassistant.tracking.graph.GraphHelper;
import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.bawnorton.randoassistant.tracking.trackable.Trackable;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
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
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LootTableResultButton extends ClickableWidget {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(RandoAssistant.MOD_ID, "textures/gui/loot_book.png");
    private static final int width = 125;
    private static final int height = 25;

    private final MinecraftClient client;
    private final TrackingGraph associatedGraph;
    private final Trackable<Item> target;
    private final Identifier source;

    public LootTableResultButton(TrackingGraph associatedGraph, Trackable<Item> target) {
        super(0, 0, width, height, ScreenTexts.EMPTY);
        client = MinecraftClient.getInstance();
        this.associatedGraph = associatedGraph;
        this.target = target;
        this.source = GraphHelper.getBestSource(associatedGraph, associatedGraph.getVertex(target));
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int u = 29;
        int v = 206;
        if(this.isHovered()) {
            v += height;
        }
        drawTexture(matrices, getX(), getY(), u, v, width, height);
        renderSource(matrices, getX(), getY());
        renderTarget(matrices, getX() + 100, getY());
    }

    private void renderSource(MatrixStack matrices, int x, int y) {
        y += 4; x += 4;
        if (Registries.ENTITY_TYPE.containsId(source)) {
            Entity entity = Registries.ENTITY_TYPE.get(source).create(client.world);
            if (entity instanceof LivingEntity livingEntity) {
                drawEntity(x + 11, y + 12, livingEntity);
                return;
            }
        }
        if (Registries.BLOCK.containsId(source)) {
            Block block = Registries.BLOCK.get(source);
            if (block instanceof FlowerPotBlock || block instanceof CandleCakeBlock || block instanceof AttachedStemBlock) {
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
    
    private void renderTarget(MatrixStack matrices, int x, int y) {
        ItemStack icon = new ItemStack(target.getContent());
        client.getItemRenderer().renderGuiItemIcon(matrices, icon, x + 4, y + 4);
    }

    private void drawBlock(MatrixStack matrixStack, Block block, int x, int y) {
        BlockState state = block.getDefaultState();

        matrixStack.push();

        matrixStack.translate(x + 16 / 2f, y + 12 / 2f, 100);
        matrixStack.scale(40 * 24 / 64f, -40 * 24 / 64f, 40);

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45 + 180));

        matrixStack.translate(-.5, -.5, -.5);

        RenderSystem.runAsFancy(() -> {
            final var vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
            if (state.getRenderType() != BlockRenderType.ENTITYBLOCK_ANIMATED) {
                this.client.getBlockRenderManager().renderBlockAsEntity(
                    state, matrixStack, vertexConsumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV
                );
            }

            RenderSystem.setShaderLights(new Vector3f(-1.5f, -.5f, 0), new Vector3f(0, -1, 0));
            vertexConsumers.draw();
            DiffuseLighting.enableGuiDepthLighting();
        });

        matrixStack.pop();
    }

    private void drawEntity(int x, int y, LivingEntity entity) {
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate((float) x, (float) y, 1050F);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        MatrixStack matrixStack2 = new MatrixStack();
        matrixStack2.translate(0.0F, 0.0F, 100.0F);

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
            matrixStack2.translate(2, 5, 0);
        } else if (entity instanceof FoxEntity) {
            matrixStack2.translate(2, 2, 0);
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

    public boolean renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        if (isHovered() && client.currentScreen != null) {
            Text text = Text.of(source.getPath() + " -> " + target.getContent().getName().getString());
            client.currentScreen.renderTooltip(matrices, text, mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == 0 || button == 1;
    }

    public Trackable<Item> getTarget() {
        return target;
    }
}
