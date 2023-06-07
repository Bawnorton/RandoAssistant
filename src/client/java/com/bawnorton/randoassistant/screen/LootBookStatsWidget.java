package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.tracking.Tracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.text.Text;

import static com.bawnorton.randoassistant.screen.LootTableGraphWidget.HEIGHT;

public class LootBookStatsWidget {
    private final MinecraftClient client;
    private final int x;
    private int y;

    private final ToggleButtonWidget backButton;

    private int totalBlocks;
    private int totalEntities;
    private int totalChests;
    private int totalVillagerGifts;
    private int totalOther;
    private int totalTotal;

    private int discoveredBlocks;
    private int discoveredEntities;
    private int discoveredChests;
    private int discoveredVillagerGifts;
    private int discoveredOther;
    private int discoveredTotal;

    private String blockCount;
    private String entityCount;
    private String chestCount;
    private String villagerGiftCount;
    private String otherCount;
    private String totalCount;


    public LootBookStatsWidget(MinecraftClient client, int x, int y) {
        this.client = client;
        this.x = x;
        this.y = y;

        backButton = new ToggleButtonWidget(x + 10, y + 10, 16, 16, false);
        backButton.setTextureUV(206, 41, 0, 18, LootBookWidget.TEXTURE);
        backButton.setTooltip(Tooltip.of(Text.of("Exit")));

        refresh();
    }

    public void refresh() {
        totalBlocks = Tracker.getInstance().getTotalBlocksCount();
        totalEntities = Tracker.getInstance().getTotalEntitiesCount();
        totalChests = Tracker.getInstance().getTotalChestsCount();
        totalVillagerGifts = Tracker.getInstance().getTotalVillagerGiftsCount();
        totalOther = Tracker.getInstance().getTotalOtherCount();
        totalTotal = Tracker.getInstance().getTotalCount();

        discoveredBlocks = Math.min(Tracker.getInstance().getDiscoveredBlocksCount(), totalBlocks);
        discoveredEntities = Math.min(Tracker.getInstance().getDiscoveredEntitiesCount(), totalEntities);
        discoveredChests = Math.min(Tracker.getInstance().getDiscoveredChestsCount(), totalChests);
        discoveredVillagerGifts = Math.min(Tracker.getInstance().getDiscoveredVillagerGiftsCount(), totalVillagerGifts);
        discoveredOther = Math.min(Tracker.getInstance().getDiscoveredOtherCount(), totalOther);
        discoveredTotal = Math.min(Tracker.getInstance().getDiscoveredCount(), totalTotal);

        blockCount = discoveredBlocks + "/" + totalBlocks;
        entityCount = discoveredEntities + "/" + totalEntities;
        chestCount = discoveredChests + "/" + totalChests;
        villagerGiftCount = discoveredVillagerGifts + "/" + totalVillagerGifts;
        otherCount = discoveredOther + "/" + totalOther;
        totalCount = discoveredTotal + "/" + totalTotal;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawText(client.textRenderer, Text.of("Stats"), x + 60, y + 13, 0xFFFFFF, false);
        this.backButton.render(context, mouseX, mouseY, delta);
        context.drawText(client.textRenderer, Text.of("Loot Tables Discovered:"), x + 12, y + 40, 0xFFFFFF, false);
        context.drawText(client.textRenderer, Text.of("Blocks:"), x + 20, y + 55, 0xDDDDDD, false);
        context.drawText(client.textRenderer, Text.of(blockCount), x + 135 - MinecraftClient.getInstance().textRenderer.getWidth(blockCount), y + 55, 0xFFFFFF, false);
        context.drawText(client.textRenderer, Text.of("Entities:"), x + 20, y + 70, 0xDDDDDD, false);
        context.drawText(client.textRenderer, Text.of(entityCount), x + 135 - MinecraftClient.getInstance().textRenderer.getWidth(entityCount), y + 70, 0xFFFFFF, false);
        context.drawText(client.textRenderer, Text.of("Chests:"), x + 20, y + 85, 0xDDDDDD, false);
        context.drawText(client.textRenderer, Text.of(chestCount), x + 135 - MinecraftClient.getInstance().textRenderer.getWidth(chestCount), y + 85, 0xFFFFFF, false);
        context.drawText(client.textRenderer, Text.of("Villager Gifts:"), x + 20, y + 100, 0xDDDDDD, false);
        context.drawText(client.textRenderer, Text.of(villagerGiftCount), x + 135 - MinecraftClient.getInstance().textRenderer.getWidth(villagerGiftCount), y + 100, 0xFFFFFF, false);
        context.drawText(client.textRenderer, Text.of("Other:"), x + 20, y + 115, 0xDDDDDD, false);
        context.drawText(client.textRenderer, Text.of(otherCount), x + 135 - MinecraftClient.getInstance().textRenderer.getWidth(otherCount), y + 115, 0xFFFFFF, false);
        context.drawText(client.textRenderer, Text.of("Total:"), x + 20, y + 130, 0xDDDDDD, false);
        context.drawText(client.textRenderer, Text.of(totalCount), x + 135 - MinecraftClient.getInstance().textRenderer.getWidth(totalCount), y + 130, 0xFFFFFF, false);
        Text tooltip = null;
        if (mouseX >= x + 20 && mouseY >= y + 55 && mouseX <= x + 135 && mouseY <= y + 55 + 9) {
            tooltip = Text.of(String.format("Blocks: %.1f", (float) discoveredBlocks / totalBlocks * 100) + "%");
        } else if (mouseX >= x + 20 && mouseY >= y + 70 && mouseX <= x + 135 && mouseY <= y + 70 + 9) {
            tooltip = Text.of(String.format("Entities: %.1f", (float) discoveredEntities / totalEntities * 100) + "%");
        } else if (mouseX >= x + 20 && mouseY >= y + 85 && mouseX <= x + 135 && mouseY <= y + 85 + 9) {
            tooltip = Text.of(String.format("Chests: %.1f", (float) discoveredChests / totalChests * 100) + "%");
        } else if (mouseX >= x + 20 && mouseY >= y + 100 && mouseX <= x + 135 && mouseY <= y + 100 + 9) {
            tooltip = Text.of(String.format("Villager Gifts: %.1f", (float) discoveredVillagerGifts / totalVillagerGifts * 100) + "%");
        } else if (mouseX >= x + 20 && mouseY >= y + 115 && mouseX <= x + 135 && mouseY <= y + 115 + 9) {
            tooltip = Text.of(String.format("Other: %.1f", (float) discoveredOther / totalOther * 100) + "%");
        } else if (mouseX >= x + 20 && mouseY >= y + 130 && mouseX <= x + 135 && mouseY <= y + 130 + 9) {
            tooltip = Text.of(String.format("Total: %.1f", (float) discoveredTotal / totalTotal * 100) + "%");
        }
        if (tooltip != null) {
            context.drawTooltip(client.textRenderer, tooltip, mouseX, mouseY);
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
