package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.tracking.graph.TrackingGraph;
import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;

// list of loot table buttons
public class LootTableListWidget {
    private final List<LootTableResultButton> buttons = Lists.newArrayListWithCapacity(4);
    private int pageCount;
    private int currentPage;

    private final MinecraftClient client;
    private final int x;
    private final int y;

    private final ToggleButtonWidget nextPageButton;
    private final ToggleButtonWidget previousPageButton;

    private Item lastTargetClicked;

    public LootTableListWidget(MinecraftClient client, int x, int y) {
        this.client = client;
        this.x = x;
        this.y = y;

        this.nextPageButton = new ToggleButtonWidget(x + 95, y + 104, 12, 17, false);
        this.previousPageButton = new ToggleButtonWidget(x + 20, y + 104, 12, 17, true);
        this.nextPageButton.setTextureUV(1, 208, 12, 18, LootBookWidget.TEXTURE);
        this.previousPageButton.setTextureUV(2, 208, 12, 18, LootBookWidget.TEXTURE);

        resetResults(false);
    }

    public void resetResults(boolean resetPage) {
        TrackingGraph graph = TrackingGraph.getInstance();
        graph.getVertices().forEach(vertex -> {
            if(!graph.isRoot(vertex)) {
                Set<TrackingGraph.Vertex> parents = graph.getRootsOf(vertex);
                List<Identifier> parentIds = parents.stream().map(TrackingGraph.Vertex::getIdentifier).toList();
                LootTableResultButton button = new LootTableResultButton(parentIds, vertex.getItem());
                button.setX(this.x);
                buttons.add(button);
            }
        });
        this.pageCount = (int) Math.ceil(buttons.size() / 4.0);
        if (this.pageCount <= currentPage || resetPage) {
            this.currentPage = 0;
        }
        hideShowPageButtons();
    }

    private void hideShowPageButtons() {
        this.nextPageButton.visible = this.pageCount > 1 && this.currentPage < this.pageCount - 1;
        this.previousPageButton.visible = this.pageCount > 1 && this.currentPage > 0;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(this.pageCount > 1) {
            String pageText = String.format("%d/%d", this.currentPage + 1, this.pageCount);
            int pageTextWidth = this.client.textRenderer.getWidth(pageText);
            this.client.textRenderer.draw(matrices, pageText, this.x - pageTextWidth / 2f + 66, this.y + 109f, -1);
        }
        for(int i = 0; i < 4; i++) {
            if (i + 4 * currentPage >= buttons.size()) break;
            LootTableResultButton button = buttons.get(i + 4 * currentPage);
            button.setY(this.y + i * 25);
            button.render(matrices, mouseX, mouseY, delta);
        }
        this.nextPageButton.render(matrices, mouseX, mouseY, delta);
        this.previousPageButton.render(matrices, mouseX, mouseY, delta);
    }

    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        for(int i = 0; i < 4; i++) {
            LootTableResultButton button = buttons.get(i + 4 * currentPage);
            if(button.renderTooltip(matrices, mouseX, mouseY)) {
                return;
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.nextPageButton.mouseClicked(mouseX, mouseY, button)) {
            this.currentPage++;
            hideShowPageButtons();
            return true;
        }
        if(this.previousPageButton.mouseClicked(mouseX, mouseY, button)) {
            this.currentPage--;
            hideShowPageButtons();
            return true;
        }
        for(int i = 0; i < 4; i++) {
            LootTableResultButton lootTableResultButton = buttons.get(i + 4 * currentPage);
            if(lootTableResultButton.mouseClicked(mouseX, mouseY, button)) {
                this.lastTargetClicked = lootTableResultButton.getTarget();
                return true;
            }
        }
        return false;
    }

    public Item getLastTargetClicked() {
        return this.lastTargetClicked;
    }
}
