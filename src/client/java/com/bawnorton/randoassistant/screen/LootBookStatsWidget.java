package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.tracking.Tracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import static com.bawnorton.randoassistant.screen.LootTableGraphWidget.HEIGHT;

public class LootBookStatsWidget {
    private final MinecraftClient client;
    private final int x;
    private int y;

    private final ToggleButtonWidget backButton;


    private final int totalBlocks = Tracker.getInstance().getTotalBlocksCount();
    private final int totalEntities = Tracker.getInstance().getTotalEntitiesCount();
    private final int totalOther = Tracker.getInstance().getTotalOtherCount();
    private final int totalTotal = Tracker.getInstance().getTotalCount();

    private final int discoveredBlocks = Math.min(Tracker.getInstance().getDiscoveredBlocksCount(), totalBlocks);
    private final int discoveredEntities = Math.min(Tracker.getInstance().getDiscoveredEntitiesCount(), totalEntities);
    private final int discoveredOther = Math.min(Tracker.getInstance().getDiscoveredOtherCount(), totalOther);
    private final int discoveredTotal = Math.min(Tracker.getInstance().getDiscoveredCount(), totalTotal);

    private final String blockCount = discoveredBlocks + "/" + totalBlocks;
    private final String entityCount = discoveredEntities + "/" + totalEntities;
    private final String otherCount = discoveredOther + "/" + totalOther;
    private final String totalCount = discoveredTotal + "/" + totalTotal;


    public LootBookStatsWidget(MinecraftClient client, int x, int y) {
        this.client = client;
        this.x = x;
        this.y = y;

        backButton = new ToggleButtonWidget(x + 10, y + 10, 16, 16, false);
        backButton.setTextureUV(206, 41, 0, 18, LootBookWidget.TEXTURE);
        backButton.setTooltip(Tooltip.of(Text.of("Exit")));
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        client.textRenderer.draw(matrices, Text.of("Stats"), x + 60, y + 13, 0xFFFFFF);
        this.backButton.render(matrices, mouseX, mouseY, delta);
        client.textRenderer.draw(matrices, Text.of("Loot Tables Discovered:"), x + 12, y + 40, 0xFFFFFF);
        client.textRenderer.draw(matrices, Text.of("Blocks:"), x + 20, y + 55, 0xDDDDDD);
        client.textRenderer.draw(matrices, Text.of(blockCount), x + 135 - MinecraftClient.getInstance().textRenderer.getWidth(blockCount), y + 55, 0xFFFFFF);
        client.textRenderer.draw(matrices, Text.of("Entities:"), x + 20, y + 70, 0xDDDDDD);
        client.textRenderer.draw(matrices, Text.of(entityCount), x + 135 - MinecraftClient.getInstance().textRenderer.getWidth(entityCount), y + 70, 0xFFFFFF);
        client.textRenderer.draw(matrices, Text.of("Other:"), x + 20, y + 85, 0xDDDDDD);
        client.textRenderer.draw(matrices, Text.of(otherCount), x + 135 - MinecraftClient.getInstance().textRenderer.getWidth(otherCount), y + 85, 0xFFFFFF);
        client.textRenderer.draw(matrices, Text.of("Total:"), x + 20, y + 100, 0xDDDDDD);
        client.textRenderer.draw(matrices, Text.of(totalCount), x + 135 - MinecraftClient.getInstance().textRenderer.getWidth(totalCount), y + 100, 0xFFFFFF);
        Text tooltip = null;
        if (mouseX >= x + 20 && mouseY >= y + 55 && mouseX <= x + 135 && mouseY <= y + 55 + 9) {
            tooltip = Text.of(String.format("Blocks: %.1f", (float) discoveredBlocks / totalBlocks * 100) + "%");
        } else if (mouseX >= x + 20 && mouseY >= y + 70 && mouseX <= x + 135 && mouseY <= y + 70 + 9) {
            tooltip = Text.of(String.format("Entities: %.1f", (float) discoveredEntities / totalEntities * 100) + "%");
        } else if (mouseX >= x + 20 && mouseY >= y + 85 && mouseX <= x + 135 && mouseY <= y + 85 + 9) {
            tooltip = Text.of(String.format("Other: %.1f", (float) discoveredOther / totalOther * 100) + "%");
        } else if (mouseX >= x + 20 && mouseY >= y + 100 && mouseX <= x + 135 && mouseY <= y + 100 + 9) {
            tooltip = Text.of(String.format("Total: %.1f", (float) discoveredTotal / totalTotal * 100) + "%");
        }
        if (tooltip != null && client.currentScreen != null) {
            client.currentScreen.renderTooltip(matrices, tooltip, mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (backButton.mouseClicked(mouseX, mouseY, button)) {
            LootBookWidget.getInstance().closeStats();
            return true;
        }
        return false;
    }

    public void moveWidgets(boolean up) {
        if(up) {
            this.y -= HEIGHT / 2;
            this.backButton.setY(this.backButton.getY() - HEIGHT / 2);
        } else {
            this.y += HEIGHT / 2;
            this.backButton.setY(this.backButton.getY() + HEIGHT / 2);
        }
    }
}
