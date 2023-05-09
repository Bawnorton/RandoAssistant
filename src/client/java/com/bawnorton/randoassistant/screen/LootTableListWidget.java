package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.search.SearchManager;
import com.bawnorton.randoassistant.tracking.Tracker;
import com.bawnorton.randoassistant.tracking.trackable.Trackable;
import com.google.common.collect.Sets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.bawnorton.randoassistant.screen.LootTableGraphWidget.HEIGHT;

// list of loot table buttons
public class LootTableListWidget {
    private final List<LootTableResultButton> buttons = new ArrayList<>();
    private int pageCount;
    private int currentPage;

    private final MinecraftClient client;
    private final int x;
    private final int y;

    private final ToggleButtonWidget nextPageButton;
    private final ToggleButtonWidget previousPageButton;

    private final SearchManager<Trackable<?>> searchManager;

    public LootTableListWidget(MinecraftClient client, int x, int y) {
        this.client = client;
        this.x = x;
        this.y = y;

        this.nextPageButton = new ToggleButtonWidget(x + 95, y + 104, 12, 17, false);
        this.previousPageButton = new ToggleButtonWidget(x + 20, y + 104, 12, 17, true);
        this.nextPageButton.setTextureUV(1, 208, 12, 18, LootBookWidget.TEXTURE);
        this.previousPageButton.setTextureUV(2, 208, 12, 18, LootBookWidget.TEXTURE);

        this.searchManager = new SearchManager<>(Tracker.getInstance().getEnabled());

        resetResults(false);
    }

    public void resetResults(boolean resetPage) {
        this.buttons.clear();
        Set<Identifier> identifiers = Sets.newHashSet();
        Tracker.getInstance().getEnabled(trackable -> LootBookWidget.getInstance().getSearchText().isEmpty() || searchManager.getMatches(LootBookWidget.getInstance().getSearchText()).contains(trackable)).forEach(trackable -> identifiers.addAll(trackable.getOutput()));
        identifiers.forEach(identifer -> {
            LootTableResultButton button = new LootTableResultButton(identifer);
            button.setX(this.x);
            buttons.add(button);
        });
        buttons.sort(Comparator.comparing(LootTableResultButton::getTarget));
        this.pageCount = (int) Math.ceil(identifiers.size() / 4.0);
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
        int y = this.y + (LootTableResultButton.isGraphOpen() ? HEIGHT / 2 : 0);
        if(this.pageCount > 1) {
            String pageText = String.format("%d/%d", this.currentPage + 1, this.pageCount);
            int pageTextWidth = this.client.textRenderer.getWidth(pageText);
            this.client.textRenderer.draw(matrices, pageText, this.x - pageTextWidth / 2f + 66, y + 109f, -1);
        }

        for(int i = 0; i < 4; i++) {
            if (i + 4 * currentPage >= buttons.size()) break;
            LootTableResultButton button = buttons.get(i + 4 * currentPage);
            button.setY(y + i * 25);
            button.render(matrices, mouseX, mouseY, delta);
        }
        this.nextPageButton.render(matrices, mouseX, mouseY, delta);
        this.previousPageButton.render(matrices, mouseX, mouseY, delta);

        LootTableResultButton lastClicked = LootTableResultButton.getLastClicked();
        if(lastClicked != null && lastClicked.graphOpen) {
            int invX = LootBookWidget.getInstance().getInvX();
            int invY = LootBookWidget.getInstance().getInvY();
            invY = Math.max(2, invY - 168);
            matrices.push();
            matrices.translate(0, 0, 600);
            lastClicked.graphWidget.render(invX, invY, matrices);
            matrices.pop();
        }
    }

    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        for(int i = 0; i < 4; i++) {
            if (i + 4 * currentPage >= buttons.size()) break;
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
            if (i + 4 * currentPage >= buttons.size()) break;
            LootTableResultButton lootTableResultButton = buttons.get(i + 4 * currentPage);
            if(lootTableResultButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    public void movePageButtons(boolean up) {
        if(up) {
            this.nextPageButton.setY(this.nextPageButton.getY() - HEIGHT / 2);
            this.previousPageButton.setY(this.previousPageButton.getY() - HEIGHT / 2);
        } else {
            this.nextPageButton.setY(this.nextPageButton.getY() + HEIGHT / 2);
            this.previousPageButton.setY(this.previousPageButton.getY() + HEIGHT / 2);
        }
    }

    public List<LootTableResultButton> getButtons() {
        return this.buttons;
    }
}
