package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.render.RenderingHelper;
import com.bawnorton.randoassistant.tracking.graph.GraphHelper;
import com.bawnorton.randoassistant.tracking.graph.TrackableCrawler;
import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.concurrent.*;

public class LootTableResultButton extends ClickableWidget {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(RandoAssistant.MOD_ID, "textures/gui/loot_book.png");
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(40, 40, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    private static LootTableResultButton lastClicked;
    private static final int width = 125;
    private static final int height = 25;

    private final MinecraftClient client;
    private final Identifier target;
    private TrackingGraph graph;
    private Identifier source;

    public LootTableGraphWidget graphWidget;
    public boolean graphOpen;

    public LootTableResultButton(Identifier target) {
        super(0, 0, width, height, ScreenTexts.EMPTY);
        client = MinecraftClient.getInstance();
        this.target = target;
        EXECUTOR_SERVICE.submit(() -> {
            this.graph = TrackableCrawler.crawl(target);
            this.source = GraphHelper.getBestSource(graph, graph.getVertex(target));
        });
    }

    public static LootTableResultButton getLastClicked() {
        return lastClicked;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if(lastClicked != null) {
            lastClicked.closeGraph();
        }
        if(lastClicked == this) {
            lastClicked = null;
            return;
        }
        lastClicked = this;
        lastClicked.openGraph();
    }

    private void closeGraph() {
        graphOpen = false;
    }

    private void openGraph() {
        if(graphWidget == null) {
            graphWidget = new LootTableGraphWidget(graph);
        }
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
        RenderingHelper.renderIdentifier(Objects.requireNonNullElse(source, Registries.ITEM.getId(Items.STRUCTURE_VOID)), matrices, getX() + 4, getY() + 4);
        renderTarget(matrices, getX() + 104, getY() + 4);
    }

    private void renderTarget(MatrixStack matrices, int x, int y) {
        ItemStack icon = new ItemStack(Registries.ITEM.get(target));
        client.getItemRenderer().renderGuiItemIcon(matrices, icon, x, y);
    }

    public boolean renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        if (isHovered() && client.currentScreen != null) {
            Text text = Text.of(Objects.requireNonNullElse(source, "Loading...") + " -> " + target);
            client.currentScreen.renderTooltip(matrices, text, mouseX, mouseY);
            return true;
        }
        return false;
    }

    public static boolean isGraphOpen() {
        if(!LootBookWidget.getInstance().isOpen()) {
            return false;
        }
        return lastClicked != null && lastClicked.graphOpen;
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

    @Override
    public int hashCode() {
        return Objects.hash(target);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof LootTableResultButton other) {
            return other.target.equals(target);
        }
        return false;
    }

    public Identifier getTarget() {
        return target;
    }
}
