package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.mixin.AbstractPlantPartBlockInvoker;
import com.bawnorton.randoassistant.mixin.AttachedStemBlockAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LootTableResultButton extends ClickableWidget {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(RandoAssistant.MOD_ID, "textures/gui/loot_book.png");
    private static final int width = 125;
    private static final int height = 25;

    private final MinecraftClient client;

    private final List<Identifier> sources;
    private final Item target;

    public LootTableResultButton(List<Identifier> sources, Item target) {
        super(0, 0, width, height, ScreenTexts.EMPTY);
        this.sources = sources;
        this.target = target;
        client = MinecraftClient.getInstance();
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int u = 29;
        int v = 206;
        drawTexture(matrices, getX(), getY(), u, v, width, height);
        renderSources(matrices, getX(), getY());
        renderTarget(matrices, getX() + 100, getY());
    }
    
    private void renderSources(MatrixStack matrices, int x, int y) {
        List<Identifier> topSources = new ArrayList<>();
        if(sources.size() >= 2) topSources.add(sources.get(1));
        if(sources.size() >= 1) topSources.add(sources.get(0));
        x -= 2; y -= 2;
        for(Identifier source: topSources) {
            x += 2; y += 2;
            if(Registries.ITEM.containsId(source)) {
                Item item = Registries.ITEM.get(source);
                ItemStack icon = new ItemStack(item);
                client.getItemRenderer().renderGuiItemIcon(matrices, icon, x - height / 2, y - height / 2);
            } else if (Registries.BLOCK.containsId(source)) {
                Block block = Registries.BLOCK.get(source);
                if (block instanceof FlowerPotBlock flowerPotBlock) {
                    ItemStack icon = new ItemStack(flowerPotBlock.getContent().asItem());
                    ItemStack pot = new ItemStack(Items.FLOWER_POT);
                    client.getItemRenderer().renderGuiItemIcon(matrices, icon, x - height / 2, y - height / 2);
                    client.getItemRenderer().renderGuiItemIcon(matrices, pot, x - height / 2, y + height / 2);
                } else if (block instanceof CandleCakeBlock candleCakeBlock) {
                    ItemStack icon = new ItemStack(Items.CAKE);
                    ItemStack candle = new ItemStack(RandoAssistant.CANDLE_CAKE_MAP.get(candleCakeBlock));
                    client.getItemRenderer().renderGuiItemIcon(matrices, icon, x - height / 2, y - height / 2);
                    client.getItemRenderer().renderGuiItemIcon(matrices, candle, x - height / 2, y + height / 2);
                } else if (block instanceof AbstractPlantPartBlock abstractPlantBlock) {
                    AbstractPlantStemBlock stemBlock = ((AbstractPlantPartBlockInvoker) abstractPlantBlock).getStem();
                    ItemStack icon = new ItemStack(stemBlock.getDefaultState().getBlock().asItem());
                    client.getItemRenderer().renderGuiItemIcon(matrices, icon, x - height / 2, y - height / 2);
                } else if (block instanceof AttachedStemBlock attachedStemBlock) {
                    GourdBlock gourdBlock = ((AttachedStemBlockAccessor) attachedStemBlock).getGourdBlock();
                    ItemStack icon = new ItemStack(gourdBlock.getStem().asItem());
                    client.getItemRenderer().renderGuiItemIcon(matrices, icon, x - height / 2, y - height / 2);
                } else if (block instanceof TallSeagrassBlock) {
                    ItemStack icon = new ItemStack(Items.SEAGRASS);
                    client.getItemRenderer().renderGuiItemIcon(matrices, icon, x - height / 2, y - height / 2);
                } else {
                    ItemStack icon = new ItemStack(block.asItem());
                    if (icon.getItem() == Items.AIR) icon = new ItemStack(Items.BARRIER);
                    client.getItemRenderer().renderGuiItemIcon(matrices, icon, x - height / 2, y - height / 2);
                }
            } else if (Registries.ENTITY_TYPE.containsId(source)) {
                EntityType<?> entityType = Registries.ENTITY_TYPE.get(source);
                drawEntity(x - 5, y + 2, (LivingEntity) Objects.requireNonNull(entityType.create(client.world)));
            } else {
                ItemStack icon = new ItemStack(Items.CHEST);
                client.getItemRenderer().renderGuiItemIcon(matrices, icon, x - height / 2, y - height / 2);
            }
        }
    }
    
    private void renderTarget(MatrixStack matrices, int x, int y) {
        ItemStack icon = new ItemStack(target);
        client.getItemRenderer().renderGuiItemIcon(matrices, icon, x + 4, y + 4);
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
            RenderSystem.disableDepthTest();
            client.currentScreen.renderTooltip(matrices, target.getName(), mouseX, mouseY);
            RenderSystem.enableDepthTest();
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

    public Item getTarget() {
        return target;
    }
}
