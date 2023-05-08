package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.render.RenderingHelper;
import com.bawnorton.randoassistant.tracking.graph.GraphHelper;
import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.bawnorton.randoassistant.tracking.trackable.Trackable;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LootTableResultButton extends ClickableWidget {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(RandoAssistant.MOD_ID, "textures/gui/loot_book.png");
    private static LootTableResultButton lastClicked;
    private static final int width = 125;
    private static final int height = 25;

    private final MinecraftClient client;
    private final LootTableGraphWidget graphWidget;
    private final Trackable<Item> target;
    private final Trackable<?> source;

    private boolean graphOpen = false;

    public LootTableResultButton(TrackingGraph associatedGraph, Trackable<Item> target) {
        super(0, 0, width, height, ScreenTexts.EMPTY);
        client = MinecraftClient.getInstance();
        this.graphWidget = new LootTableGraphWidget(associatedGraph);
        this.target = target;
        this.source = GraphHelper.getBestSource(associatedGraph, associatedGraph.getVertex(target));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if(lastClicked != null) {
            lastClicked.closeGraph();
        }
        lastClicked = this;
        lastClicked.openGraph();
    }

    private void closeGraph() {
        graphOpen = false;
    }

    private void openGraph() {
        graphOpen = true;
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
        RenderingHelper.renderTrackable(source, matrices, getX() + 4, getY() + 4);
        renderTarget(matrices, getX() + 104, getY() + 4);
        if(graphOpen) {
            graphWidget.render(matrices, 200, 200);
        }
    }
    
    private void renderTarget(MatrixStack matrices, int x, int y) {
        ItemStack icon = new ItemStack(target.getContent());
        client.getItemRenderer().renderGuiItemIcon(matrices, icon, x, y);
    }

    public boolean renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        if (isHovered() && client.currentScreen != null) {
            Text text = Text.of(source.getIdentifier().getPath() + " -> " + target.getContent().getName().getString());
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
