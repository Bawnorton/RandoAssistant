package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Locale;

import static com.bawnorton.randoassistant.screen.LootTableGraphWidget.HEIGHT;

@SuppressWarnings("DataFlowIssue")
public class LootBookWidget implements Drawable, Element, Selectable {
    public static final ButtonTextures SETTINGS_TEXTURES = new ButtonTextures(
            new Identifier(RandoAssistant.MOD_ID, "loot_book/settings"),
            new Identifier(RandoAssistant.MOD_ID, "loot_book/settings_focused")
    );
    public static final ButtonTextures STATS_TEXTURES = new ButtonTextures(
            new Identifier(RandoAssistant.MOD_ID, "loot_book/stats"),
            new Identifier(RandoAssistant.MOD_ID, "loot_book/stats_focused")
    );
    
    public static final Identifier BACKGROUND_TEXTURE = new Identifier(RandoAssistant.MOD_ID, "loot_book/background");
    
    private static LootBookWidget INSTANCE;

    private MinecraftClient client;
    private InventoryScreen screen;

    private ToggleButtonWidget settingsButton;
    private ToggleButtonWidget statsButton;
    private TextFieldWidget searchField;
    private LootTableListWidget lootTableArea;

    private LootBookSettingsWidget settingsWidget;
    private LootBookStatsWidget statsWidget;

    private String searchText = "";
    private int rightOffset;
    private int parentWidth;
    private int parentHeight;
    private boolean open;
    private boolean settingsOpen = false;
    private boolean statsOpen = false;
    private boolean searching;

    public static LootBookWidget getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new LootBookWidget();
        }
        return INSTANCE;
    }

    public void initialise(InventoryScreen screen) {
        this.client =  MinecraftClient.getInstance();
        this.screen = screen;
        this.parentWidth = screen.width;
        this.parentHeight = screen.height;
        if (this.isOpen()) {
            this.reset();
        }
    }

    public void reset() {
        this.rightOffset = 86;
        int x = (this.parentWidth - 147) / 2 + this.rightOffset;
        int y = (this.parentHeight - 166) / 2;
        String search = this.searchField != null ? this.searchField.getText() : "";
        this.searchField = new TextFieldWidget(client.textRenderer, x + 26, y + 14, 69, client.textRenderer.fontHeight + 3, Text.of(""));
        this.searchField.setMaxLength(50);
        this.searchField.setVisible(true);
        this.searchField.setEditableColor(0xFFFFFF);
        this.searchField.setText(search);
        this.searchField.setPlaceholder(Text.translatable("gui.recipebook.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
        this.lootTableArea = new LootTableListWidget(client, x + 11, y + 32);
        this.settingsWidget = new LootBookSettingsWidget(client, x, y);
        this.settingsButton = new ToggleButtonWidget(x + 120, y + 12, 16, 16, false);
        this.settingsButton.setTextures(SETTINGS_TEXTURES);
        this.settingsButton.setTooltip(Tooltip.of(Text.of("Settings")));
        this.statsWidget = new LootBookStatsWidget(client, x, y);
        this.statsButton = new ToggleButtonWidget(x + 100, y + 12, 16, 16, false);
        this.statsButton.setTextures(STATS_TEXTURES);
        this.statsButton.setTooltip(Tooltip.of(Text.of("Stats")));
    }
    public void toggleOpen() {
        this.setOpen(!this.isOpen());
    }

    public boolean isOpen() {
        return open;
    }

    private void setOpen(boolean open) {
        if(open) {
            this.reset();
        }
        this.open = open;
    }

    public void clearCache() {
        if(this.searchField != null) {
            this.searchField.setText("");
            this.searchText = "";
        }
        if(this.lootTableArea != null) {
            this.lootTableArea.clearCache();
        }
    }

    public void closeSettings() {
        this.settingsOpen = false;
        if(settingsWidget != null) {
            settingsWidget.onClose();
        }
    }

    public void closeStats() {
        this.statsOpen = false;
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if(!open) return;
        if(!RandoAssistantClient.isInstalledOnServer) {
            if(isOpen()) this.toggleOpen();
        }
        int x = (this.parentWidth - 147) / 2 + this.rightOffset;
        int y = (this.parentHeight - 166) / 2;
        if(LootTableResultButton.isGraphOpen()) {
            y += HEIGHT / 2;
        }
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, 147, 166);
        if(settingsOpen) {
            this.settingsWidget.render(context, mouseX, mouseY, delta);
            this.lootTableArea.renderLastClickedGraph(context, mouseX, mouseY);
        } else if (statsOpen) {
            this.statsWidget.render(context, mouseX, mouseY, delta);
            this.lootTableArea.renderLastClickedGraph(context, mouseX, mouseY);
        } else {
            this.searchField.render(context, mouseX, mouseY, delta);
            this.lootTableArea.render(context, mouseX, mouseY, delta);
            this.settingsButton.render(context, mouseX, mouseY, delta);
            this.statsButton.render(context, mouseX, mouseY, delta);
        }
    }

    public int findLeftEdge(int parentWidth, int backgroundWidth) {
        return this.isOpen() ? (parentWidth - backgroundWidth - 200) / 2 + 23: (parentWidth - backgroundWidth) / 2;
    }

    public void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        if(!this.isOpen()) return;
        if(!settingsOpen && !statsOpen) {
            this.lootTableArea.renderTooltip(context, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(!this.isOpen() || button == 1) return false;
        LootTableResultButton lastClicked = LootTableResultButton.getLastClicked();
        if(lastClicked == null) return false;
        LootTableGraphWidget graphWidget = lastClicked.graphWidget;
        return graphWidget != null && graphWidget.mouseDragged(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if(!this.isOpen()) return false;
        LootTableResultButton lastClicked = LootTableResultButton.getLastClicked();
        if(lastClicked == null) return false;
        LootTableGraphWidget graphWidget = lastClicked.graphWidget;
        return graphWidget != null && graphWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!RandoAssistantClient.isInstalledOnServer) return false;
        if(!this.isOpen() || client.player.isSpectator()) return false;
        searchField.setFocused(false);
        if(this.settingsOpen) {
            return this.settingsWidget.mouseClicked(mouseX, mouseY, button);
        }
        if(this.statsOpen) {
            return this.statsWidget.mouseClicked(mouseX, mouseY, button);
        }
        if (this.searchField.mouseClicked(mouseX, mouseY, button)) {
            searchField.setFocused(true);
            return true;
        }
        if(this.lootTableArea.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if(this.settingsButton.mouseClicked(mouseX, mouseY, button)) {
            this.settingsOpen = true;
            return true;
        }
        if(this.statsButton.mouseClicked(mouseX, mouseY, button)) {
            this.statsOpen = true;
            statsWidget.refresh();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.searching = false;
        if(!this.isOpen() || client.player.isSpectator()) return false;
        if(keyCode == GLFW.GLFW_KEY_ESCAPE && !this.isWide()) {
            this.setOpen(false);
            return true;
        }
        if(this.searchField.keyPressed(keyCode, scanCode, modifiers)) {
            this.refreshSearchResults(true);
            return true;
        }
        if(this.searchField.isFocused() && this.searchField.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }
        if(this.settingsOpen) {
            return this.settingsWidget.keyPressed(keyCode, scanCode, modifiers);
        }
        if(client.options.chatKey.matchesKey(keyCode, scanCode) && !this.searchField.isFocused()) {
            this.searching = true;
            this.searchField.setFocused(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.searching = false;
        return Element.super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if(this.searching) {
            return false;
        }
        if(!this.isOpen() || client.player.isSpectator()) return false;
        if(this.searchField.charTyped(chr, modifiers)) {
            this.refreshSearchResults(true);
            return true;
        }
        if(this.settingsOpen) {
            return this.settingsWidget.charTyped(chr, modifiers);
        }
        return Element.super.charTyped(chr, modifiers);
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    public void refreshSearchResults(boolean resetPage) {
        String text = this.searchField.getText().toLowerCase(Locale.ROOT);
        if(!text.equals(this.searchText)) {
            this.searchText = text;
            lootTableArea.resetResults(resetPage);
        }
    }

    public String getSearchText() {
        return this.searchText;
    }

    private boolean isWide() {
        return this.rightOffset == HEIGHT / 2;
    }

    public int getInvX() {
        return screen.x;
    }

    public int getInvY() {
        return screen.y;
    }

    public InventoryScreen getScreen() {
        return screen;
    }

    public void moveWidgets(boolean up) {
        this.lootTableArea.movePageButtons(up);
        this.settingsWidget.moveWidgets(up);
        this.statsWidget.moveWidgets(up);
        if(up) {
            this.searchField.setY(this.searchField.getY() - HEIGHT / 2);
            this.settingsButton.setY(this.settingsButton.getY() - HEIGHT / 2);
            this.statsButton.setY(this.statsButton.getY() - HEIGHT / 2);
        } else {
            this.searchField.setY(this.searchField.getY() + HEIGHT / 2);
            this.settingsButton.setY(this.settingsButton.getY() + HEIGHT / 2);
            this.statsButton.setY(this.statsButton.getY() + HEIGHT / 2);
        }
    }

    @Override
    public SelectionType getType() {
        return this.open ? SelectionType.HOVERED : SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        ArrayList<ClickableWidget> list = Lists.newArrayList();
        list.add(this.searchField);
        list.add(this.settingsButton);
        list.addAll(this.lootTableArea.getButtons());
        Screen.SelectedElementNarrationData selectedElementNarrationData = Screen.findSelectedElementData(list, null);
        if (selectedElementNarrationData != null) {
            selectedElementNarrationData.selectable.appendNarrations(builder.nextMessage());
        }
    }
}
