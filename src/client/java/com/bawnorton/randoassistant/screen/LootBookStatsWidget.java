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
    private final String blockCount;
    private final String entityCount;
    private final String otherCount;
    private final String totalCount;

    public LootBookStatsWidget(MinecraftClient client, int x, int y) {
        this.client = client;
        this.x = x;
        this.y = y;

        backButton = new ToggleButtonWidget(x + 10, y + 10, 16, 16, false);
        backButton.setTextureUV(206, 41, 0, 18, LootBookWidget.TEXTURE);
        backButton.setTooltip(Tooltip.of(Text.of("Exit")));

        blockCount = Tracker.getInstance().getDiscoveredBlocksCount() + "/" + Tracker.getInstance().getTotalBlocksCount();
        entityCount = Tracker.getInstance().getDiscoveredEntitiesCount() + "/" + Tracker.getInstance().getTotalEntitiesCount();
        otherCount = Tracker.getInstance().getDiscoveredOtherCount() + "/" + Tracker.getInstance().getTotalOtherCount();
        totalCount = Tracker.getInstance().getDiscoveredCount() + "/" + Tracker.getInstance().getTotalCount();
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
