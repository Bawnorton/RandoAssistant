package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.render.RenderingHelper;
import com.bawnorton.randoassistant.tracking.graph.GraphHelper;
import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.bawnorton.randoassistant.tracking.trackable.TrackableCrawler;
import com.bawnorton.randoassistant.util.IdentifierType;
import com.bawnorton.randoassistant.util.tuples.Pair;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LootTableResultButton extends ClickableWidget {
    private static final Identifier ARROW_TEXTURE = new Identifier(RandoAssistant.MOD_ID, "textures/gui/arrow.png");
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(RandoAssistant.MOD_ID, "textures/gui/loot_book.png");
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(40, 40, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    private static LootTableResultButton lastClicked;
    private static final int width = 125;
    private static final int height = 25;

    private final MinecraftClient client;
    private final Identifier target;
    private TrackingGraph graph;
    private Identifier source;
    private int distance;

    public LootTableGraphWidget graphWidget;
    public boolean graphOpen;

    public LootTableResultButton(Identifier target) {
        super(0, 0, width, height, ScreenTexts.EMPTY);
        client = MinecraftClient.getInstance();
        this.target = target;
        EXECUTOR_SERVICE.submit(() -> {
            this.graph = TrackableCrawler.crawl(target);
            Pair<Identifier, Integer> bestSource = GraphHelper.getBestSource(graph, graph.getVertex(target));
            this.source = bestSource.a();
            this.distance = bestSource.b();
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

    public void closeGraph() {
        graphOpen = false;
        lastClicked = null;
    }

    private void openGraph() {
        if(graphWidget == null) {
            this.graphWidget = new LootTableGraphWidget(graph, target);
        } else {
            graphWidget.centreOnTarget();
        }
        graphOpen = true;
    }

    public void markDirty() {
        if(graph != null) {
            graph.markDirty();
        }
    }

    public boolean isDirty() {
        return graph != null && graph.isDirty();
    }

    public void refresh() {
        EXECUTOR_SERVICE.submit(() -> {
            this.graph = TrackableCrawler.crawl(target);
            graphWidget.setGraph(graph);
            this.graphWidget.refresh();
        });
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
        RenderingHelper.renderIdentifier(Objects.requireNonNullElse(source, Registries.ITEM.getId(Items.STRUCTURE_VOID)), matrices, 1, getX() + 4, getY() + 4, true);
        renderArrow(matrices, getX() + 24, getY() + 3);
        renderTarget(matrices, getX() + 104, getY() + 4);
    }

    private void renderTarget(MatrixStack matrices, int x, int y) {
        ItemStack icon = new ItemStack(Registries.ITEM.get(target));
        client.getItemRenderer().renderGuiItemIcon(matrices, icon, x, y);
    }

    private void renderArrow(MatrixStack matices, int x, int y) {
        RenderSystem.setShaderTexture(0, ARROW_TEXTURE);
        drawTexture(matices, x + 1, y, 0, 0, 76, 17, 76, 17);
        Text text = Text.of(String.valueOf(distance));
        int textWidth = client.textRenderer.getWidth(text);
        int textHeight = client.textRenderer.fontHeight;
        int textX = x + 39 - textWidth / 2;
        int textY = y + 9 - textHeight / 2;
        client.textRenderer.draw(matices, text, textX, textY, 0x000000);
    }

    public boolean renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        if (isHovered() && client.currentScreen != null) {
            Text text = Text.of((source == null ? "Loading..." : IdentifierType.getName(source, true)) + " -> " + IdentifierType.getName(target, false));
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
