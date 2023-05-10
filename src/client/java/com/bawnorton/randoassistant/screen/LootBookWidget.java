package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Locale;

import static com.bawnorton.randoassistant.screen.LootTableGraphWidget.HEIGHT;

/*
To be implemented:
- Loot table preview from source
- Settings button
- Settings screen
 */
@SuppressWarnings("DataFlowIssue")
public class LootBookWidget extends DrawableHelper implements Drawable, Element, Selectable {
    public static final Identifier TEXTURE = new Identifier(RandoAssistant.MOD_ID, "textures/gui/loot_book.png");
    private static LootBookWidget INSTANCE;

    private MinecraftClient client;
    private InventoryScreen screen;

    private ToggleButtonWidget toggleInteractablesButton;
    private TextFieldWidget searchField;
    private LootTableListWidget lootTableArea;

    private String searchText = "";
    private int rightOffset;
    private int parentWidth;
    private int parentHeight;
    private boolean open;
    private boolean showInteractables = false;
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
        this.searchField = new TextFieldWidget(client.textRenderer, x + 26, y + 14, 79, client.textRenderer.fontHeight + 3, Text.translatable("itemGroup.search"));
        this.searchField.setMaxLength(50);
        this.searchField.setVisible(true);
        this.searchField.setEditableColor(0xFFFFFF);
        this.searchField.setText(search);
        this.searchField.setPlaceholder(Text.translatable("gui.recipebook.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
        this.lootTableArea = new LootTableListWidget(client, x + 11, y + 32);
        this.toggleInteractablesButton = new ToggleButtonWidget(x + 110, y + 12, 26, 16, showInteractables);
        this.updateTooltip();
        this.setInteractableButtonTexture();
        // init settings button
        // update tooltip
    }

    private void updateTooltip() {
        this.toggleInteractablesButton.setTooltip(!showInteractables ? Tooltip.of(Text.translatable("randoassistant.tooltip.show_all")) : Tooltip.of(Text.translatable("randoassistant.tooltip.hide_interactables")));
    }

    private void setInteractableButtonTexture() {
        this.toggleInteractablesButton.setTextureUV(152, 41, 28, 18, TEXTURE);
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

    public void tick() {
        if(!this.isOpen()) return;
        this.searchField.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(!open) return;
        matrices.push();
        matrices.translate(0, 0, 100);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.parentWidth - 147) / 2 + this.rightOffset;
        int y = (this.parentHeight - 166) / 2;
        if(LootTableResultButton.isGraphOpen()) y += HEIGHT / 2;
        drawTexture(matrices, x, y, 1, 1, 147, 166);
        this.searchField.render(matrices, mouseX, mouseY, delta);
        this.lootTableArea.render(matrices, mouseX, mouseY, delta);
        this.toggleInteractablesButton.render(matrices, mouseX, mouseY, delta);
        // draw settings button
        matrices.pop();
    }

    public int findLeftEdge(int parentWidth, int backgroundWidth) {
        return this.isOpen() ? (parentWidth - backgroundWidth - 200) / 2 + 23: (parentWidth - backgroundWidth) / 2;
    }

    public void drawTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        if(!this.isOpen()) return;
        this.lootTableArea.renderTooltip(matrices, mouseX, mouseY);
    }

    public boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int backgroundWidth, int backgroundHeight) {
        if(!this.isOpen()) return true;
        boolean outside = mouseX < left || mouseY < top || mouseX >= left + backgroundWidth || mouseY >= top + backgroundHeight;
        boolean inside = left - 147 < mouseX && mouseX < left && top < mouseY && mouseY < top + backgroundHeight;
        return outside && !inside;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(!this.isOpen()) return false;
        LootTableResultButton lastClicked = LootTableResultButton.getLastClicked();
        if(lastClicked == null) return false;
        LootTableGraphWidget graphWidget = lastClicked.graphWidget;
        return graphWidget != null && graphWidget.mouseDragged(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if(!this.isOpen()) return false;
        LootTableResultButton lastClicked = LootTableResultButton.getLastClicked();
        if(lastClicked == null) return false;
        LootTableGraphWidget graphWidget = lastClicked.graphWidget;
        return graphWidget != null && graphWidget.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!this.isOpen() || client.player.isSpectator()) return false;
        if (this.searchField.mouseClicked(mouseX, mouseY, button) || this.lootTableArea.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if(this.toggleInteractablesButton.mouseClicked(mouseX, mouseY, button)) {
            this.toggleInteractablesButton.setToggled(toggleShowInteractables());
            this.updateTooltip();
            this.refreshSearchResults();
            return true;
        }
        // check settings button
        return false;
    }

    private boolean toggleShowInteractables() {
        this.showInteractables = !this.showInteractables;
        return this.showInteractables;
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
            this.refreshSearchResults();
            return true;
        }
        if(this.searchField.isFocused() && this.searchField.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return true;
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
            this.refreshSearchResults();
            return true;
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

    private void refreshSearchResults() {
        String text = this.searchField.getText().toLowerCase(Locale.ROOT);
        if(!text.equals(this.searchText)) {
            this.searchText = text;
            lootTableArea.resetResults(true);
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
        if(up) {
            this.searchField.setY(this.searchField.getY() - HEIGHT / 2);
            this.toggleInteractablesButton.setY(this.toggleInteractablesButton.getY() - HEIGHT / 2);
        } else {
            this.searchField.setY(this.searchField.getY() + HEIGHT / 2);
            this.toggleInteractablesButton.setY(this.toggleInteractablesButton.getY() + HEIGHT / 2);
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
        list.add(this.toggleInteractablesButton);
        list.addAll(this.lootTableArea.getButtons());
        // add settings button
        Screen.SelectedElementNarrationData selectedElementNarrationData = Screen.findSelectedElementData(list, null);
        if (selectedElementNarrationData != null) {
            selectedElementNarrationData.selectable.appendNarrations(builder.nextMessage());
        }
    }
}
