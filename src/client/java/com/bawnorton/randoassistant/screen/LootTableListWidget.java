package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.tracking.Tracker;
import com.bawnorton.randoassistant.util.IdentifierType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.bawnorton.randoassistant.screen.LootTableGraphWidget.HEIGHT;

// list of loot table buttons
public class LootTableListWidget {
    private final List<LootTableResultButton> buttons = Collections.synchronizedList(new ArrayList<>());
    private int pageCount;
    private int currentPage;

    private final MinecraftClient client;
    private final int x;
    private final int y;

    private final ToggleButtonWidget nextPageButton;
    private final ToggleButtonWidget previousPageButton;

    public LootTableListWidget(MinecraftClient client, int x, int y) {
        this.client = client;
        this.x = x;
        this.y = y;

        this.nextPageButton = new ToggleButtonWidget(x + 95, y + 104, 12, 17, false);
        this.previousPageButton = new ToggleButtonWidget(x + 20, y + 104, 12, 17, true);
        this.nextPageButton.setTextures(LootBookSettingsWidget.NEXT_TEXTURES);
        this.previousPageButton.setTextures(LootBookSettingsWidget.PREV_TEXTURES);

        resetResults(false);
    }

    public void clearCache() {
        this.buttons.forEach(LootTableResultButton::markDirty);
        resetResults(false);
    }

    public void resetResults(boolean resetPage) {
        this.buttons.clear();
        String searchText = LootBookWidget.getInstance().getSearchText().toLowerCase().replace("^[a-z]", "");
        Tracker.getInstance().getEnabled().forEach(identifier -> {
            String name = (switch (IdentifierType.fromId(identifier)) {
                case ITEM -> Registries.ITEM.get(identifier).getName().getString();
                case BLOCK -> Registries.BLOCK.get(identifier).getName().getString();
                case ENTITY -> Registries.ENTITY_TYPE.get(identifier).getName().getString();
                case OTHER -> identifier.toString();
            }).toLowerCase().replace("^[a-z]", "");
            if(name.contains(searchText) || identifier.getPath().contains(searchText)) {
                LootTableResultButton button = new LootTableResultButton(identifier, (resultButton, throwable) -> {
                    if(resultButton.hasNoConnections()) {
                        buttons.remove(resultButton);
                        updatePageCount(false);
                    }
                });
                button.setX(this.x);
                buttons.add(button);
            }
        });
        try {
            buttons.sort(Comparator.comparing(LootTableResultButton::getTarget));
        } catch (NullPointerException ignored) {}
        updatePageCount(resetPage);
    }

    public void updatePageCount(boolean resetPage) {
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

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int y = this.y + (LootTableResultButton.isGraphOpen() ? HEIGHT / 2 : 0);
        if(this.pageCount > 1) {
            String pageText = String.format("%d/%d", this.currentPage + 1, this.pageCount);
            int pageTextWidth = this.client.textRenderer.getWidth(pageText);
            context.drawText(client.textRenderer, pageText, (int) (this.x - pageTextWidth / 2f + 66), (int) (y + 109f), -1, false);
        }

        for(int i = 0; i < 4; i++) {
            if (i + 4 * currentPage >= buttons.size()) break;
            LootTableResultButton button = buttons.get(i + 4 * currentPage);
            button.setY(y + i * 25);
            button.render(context, mouseX, mouseY, delta);
        }
        this.nextPageButton.render(context, mouseX, mouseY, delta);
        this.previousPageButton.render(context, mouseX, mouseY, delta);
        this.renderLastClickedGraph(context, mouseX, mouseY);
    }

    public void renderLastClickedGraph(DrawContext context, int mouseX, int mouseY) {
        LootTableResultButton lastClicked = LootTableResultButton.getLastClicked();
        if(lastClicked != null && lastClicked.graphOpen) {
            if(lastClicked.isDirty()) {
                lastClicked.refresh();
            }
            int invX = LootBookWidget.getInstance().getInvX();
            int invY = LootBookWidget.getInstance().getInvY() - HEIGHT - 2;
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(0, 0, 600);
            lastClicked.graphWidget.render(context, invX, invY, mouseX, mouseY);
            matrices.pop();
        }
    }

    public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        for(int i = 0; i < 4; i++) {
            if (i + 4 * currentPage >= buttons.size()) break;
            LootTableResultButton button = buttons.get(i + 4 * currentPage);
            if(button.renderTooltip(context, mouseX, mouseY)) {
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
        LootTableResultButton lastClicked = LootTableResultButton.getLastClicked();
        if(lastClicked != null) {
            LootTableGraphWidget graphWidget = lastClicked.graphWidget;
            if(graphWidget != null) {
                return graphWidget.mouseClicked(mouseX, mouseY, button);
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
