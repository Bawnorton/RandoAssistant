package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.config.ConfigManager;
import com.bawnorton.randoassistant.event.client.EventManager;
import com.bawnorton.randoassistant.tracking.Tracker;
import com.bawnorton.randoassistant.tracking.trackable.TrackableCrawler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bawnorton.randoassistant.screen.LootTableGraphWidget.HEIGHT;

public class LootBookSettingsWidget {
    public static final ButtonTextures BACK_TEXTURES = new ButtonTextures(
            new Identifier(RandoAssistant.MOD_ID, "loot_book/back"),
            new Identifier(RandoAssistant.MOD_ID, "loot_book/back_focused")
    );
    public static final ButtonTextures NEXT_TEXTURES = new ButtonTextures(
            new Identifier(RandoAssistant.MOD_ID, "loot_book/next"),
            new Identifier(RandoAssistant.MOD_ID, "loot_book/next_focused")
    );
    public static final ButtonTextures PREV_TEXTURES = new ButtonTextures(
            new Identifier(RandoAssistant.MOD_ID, "loot_book/prev"),
            new Identifier(RandoAssistant.MOD_ID, "loot_book/prev_focused")
    );
    public static final ButtonTextures TOGGLE_TEXTURES = new ButtonTextures(
            new Identifier(RandoAssistant.MOD_ID, "loot_book/toggle_enabled"),
            new Identifier(RandoAssistant.MOD_ID, "loot_book/toggle_disabled"),
            new Identifier(RandoAssistant.MOD_ID, "loot_book/toggle_enabled_focused"),
            new Identifier(RandoAssistant.MOD_ID, "loot_book/toggle_disabled_focused")
    );
    
    private final MinecraftClient client;
    private final int x;
    private int y;

    private final TexturedButtonWidget backButton;
    private final TexturedButtonWidget nextButton;
    private final TexturedButtonWidget prevButton;

    private final ToggleButtonWidget starIcons;
    private final ToggleButtonWidget silkTouchStarIcons;
    private final ToggleButtonWidget autoToggle;
    private final ToggleButtonWidget enableOverride;
    private final ToggleButtonWidget invertSearch;
    private final ToggleButtonWidget enableCrafting;
    private final ToggleButtonWidget enableStripping;
    private final ToggleButtonWidget randomizeColours;

    private final TextFieldWidget searchDepth;
    private final TextFieldWidget highlightRadius;

    private final List<Map<String, ClickableWidget>> pages;
    private int pageNum = 0;

    public LootBookSettingsWidget(MinecraftClient client, int x, int y) {
        this.client = client;
        this.x = x;
        this.y = y;

        pages = new ArrayList<>();

        backButton = new TexturedButtonWidget(x + 10, y + 10, 16, 16, BACK_TEXTURES, (button) -> LootBookWidget.getInstance().closeSettings());
        backButton.setTooltip(Tooltip.of(Text.of("Save and Exit")));
        nextButton = new TexturedButtonWidget(x + 106, y + 136, 12, 17, NEXT_TEXTURES, (button) -> {
            pages.get(pageNum).forEach((name, widget) -> widget.active = false);
            pageNum++;
            if (pageNum >= pages.size()) pageNum = 0;
            pages.get(pageNum).forEach((name, widget) -> widget.active = true);
        });
        nextButton.setTooltip(Tooltip.of(Text.of("Next Page")));
        prevButton = new TexturedButtonWidget(x + 31, y + 136, 12, 17, PREV_TEXTURES, (button) -> {
            pages.get(pageNum).forEach((name, widget) -> widget.active = false);
            pageNum--;
            if (pageNum < 0) pageNum = pages.size() - 1;
            pages.get(pageNum).forEach((name, widget) -> widget.active = true);
        });
        prevButton.setTooltip(Tooltip.of(Text.of("Previous Page")));

        starIcons = createButton("Unbroken Stars", "Display star icons on unbroken blocks", Config.getInstance().unbrokenBlockIcon);
        silkTouchStarIcons = createButton( "Silk-Touch Stars", "Display star icons on broken but not silk-touched blocks\n\nRequires §bUnbroken Stars", Config.getInstance().silktouchUnbrokenBlockIcon);
        autoToggle = createButton( "Auto toggle", "Automatically hides the stars if no fasguy datapack is detected", Config.getInstance().silktouchUnbrokenBlockIcon);
        enableOverride = createButton("Enable Override", "Enable all undiscovered loot tables\n\n§7This is not permanent", Config.getInstance().enableOverride);
        searchDepth = createTextField("Search Depth", "The maximum number of steps to search for a path to the target item\n\n§6Warning: §rValues over §c15§r are not recommended!", String.valueOf(Config.getInstance().searchDepth));
        highlightRadius = createTextField("Highlight Radius", "The radius of the highlight effect when pressing \"" + EventManager.highlight.getBoundKeyLocalizedText().getString() + "\"\n\n§6Warning: §rValues over §c10§r are not recommended!", String.valueOf(Config.getInstance().highlightRadius));

        invertSearch = createButton("Invert Search", "Search for what drops from the target item instead of what drops the target item", Config.getInstance().invertSearch);
        enableCrafting = createButton("Enable Crafting", "Toggle whether crafting recipes are displayed in the graph.", Config.getInstance().enableCrafting);
        enableStripping = createButton("Enable Interactables", "Toggle whether interactions are displayed in the graph.", Config.getInstance().enableInteractions);
        randomizeColours = createButton("Randomize Colours", "Randomize world and entity colours (Cosmetic)", Config.getInstance().randomizeColours);
    }

    private ToggleButtonWidget createButton(String name, String tooltip, boolean toggled) {
        ToggleButtonWidget button = new ToggleButtonWidget(x + 120, y + 33, 16, 16, toggled);
        button.setTextures(TOGGLE_TEXTURES);
        button.setTooltip(Tooltip.of(Text.of(tooltip)));
        button.setY(button.getY() + addToPages(name, button));
        return button;
    }

    private TextFieldWidget createTextField(String name, String tooltip, String text) {
        TextFieldWidget textField = new TextFieldWidget(client.textRenderer, x + 115, y + 34, 20, client.textRenderer.fontHeight + 3, Text.of(""));
        textField.setMaxLength(2);
        textField.setVisible(true);
        textField.setEditableColor(0xFFFFFF);
        textField.setText(text);
        textField.setTooltip(Tooltip.of(Text.of(tooltip)));
        textField.setChangedListener(content -> {
            String filtered = content.replaceAll("[^0-9]", "");
            while (filtered.startsWith("0")) filtered = filtered.substring(1);
            if (filtered.isEmpty()) {
                filtered = "0";
                textField.setEditableColor(0xAAAAAA);
            } else textField.setEditableColor(0xFFFFFF);
            if (!filtered.equals(content)) textField.setText(filtered);
        });
        textField.setY(textField.getY() + addToPages(name, textField));
        return textField;
    }

    private int addToPages(String name, ClickableWidget widget) {
        for(Map<String, ClickableWidget> page: pages) {
            if(page.size() < 5) {
                page.put(name, widget);
                return (page.size() - 1) * 20;
            }
        }
        Map<String, ClickableWidget> page = new HashMap<>();
        page.put(name, widget);
        pages.add(page);
        return 0;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawText(client.textRenderer, Text.of("Settings"), x + 52, y + 13, 0xFFFFFF, false);
        this.backButton.render(context, mouseX, mouseY, delta);

        Map<String, ClickableWidget> page = pages.get(this.pageNum);
        for (Map.Entry<String, ClickableWidget> entry : page.entrySet()) {
            int x = entry.getValue().getX() - 110;
            int y = entry.getValue().getY() + 4;
            if(entry.getValue() instanceof TextFieldWidget) x += 5;
            context.drawText(client.textRenderer, Text.of(entry.getKey()), x, y, 0xFFFFFF, false);
            entry.getValue().render(context, mouseX, mouseY, delta);
        }

        if(pages.size() > 1) {
            this.nextButton.render(context, mouseX, mouseY, delta);
            this.prevButton.render(context, mouseX, mouseY, delta);
            String pageText = String.format("%d/%d", this.pageNum + 1, this.pages.size());
            int width = this.client.textRenderer.getWidth(pageText);
            context.drawText(client.textRenderer, pageText, x - width / 2 + 77, y + 141, -1, false);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        searchDepth.setFocused(false);
        highlightRadius.setFocused(false);

        if(starIcons.mouseClicked(mouseX, mouseY, button)) {
            starIcons.setToggled(!starIcons.isToggled());
            if(silkTouchStarIcons.isToggled()) silkTouchStarIcons.setToggled(false);
            return true;
        }
        if(silkTouchStarIcons.mouseClicked(mouseX, mouseY, button)) {
            silkTouchStarIcons.setToggled(!silkTouchStarIcons.isToggled());
            if(!starIcons.isToggled()) starIcons.setToggled(true);
            return true;
        }
        if (backButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (nextButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (prevButton.mouseClicked(mouseX, mouseY, button)) return true;

        for (Map.Entry<String, ClickableWidget> entry : pages.get(pageNum).entrySet()) {
            ClickableWidget widget = entry.getValue();
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                if(widget instanceof ToggleButtonWidget toggleButtonWidget) {
                    toggleButtonWidget.setToggled(!toggleButtonWidget.isToggled());
                } else if (widget instanceof TextFieldWidget) {
                    widget.setFocused(true);
                }
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Map.Entry<String, ClickableWidget> entry : pages.get(pageNum).entrySet()) {
            ClickableWidget widget = entry.getValue();
            if (!(widget instanceof TextFieldWidget)) continue;
            if(widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        for (Map.Entry<String, ClickableWidget> entry : pages.get(pageNum).entrySet()) {
            ClickableWidget widget = entry.getValue();
            if (!(widget instanceof TextFieldWidget)) continue;
            if(widget.charTyped(chr, modifiers)) return true;
        }
        return false;
    }

    public void onClose() {
        Config.getInstance().unbrokenBlockIcon = starIcons.isToggled();
        Config.getInstance().silktouchUnbrokenBlockIcon = silkTouchStarIcons.isToggled();
        Config.getInstance().autoToggle = autoToggle.isToggled();
        Config.getInstance().enableOverride = enableOverride.isToggled();
        if(randomizeColours.isToggled() != Config.getInstance().randomizeColours) {
            client.worldRenderer.reload();
            RandoAssistantClient.seed++;
        }
        Config.getInstance().randomizeColours = randomizeColours.isToggled();
        Config.getInstance().searchDepth = Integer.parseInt(searchDepth.getText());
        Config.getInstance().highlightRadius = Integer.parseInt(highlightRadius.getText());
        Config.getInstance().invertSearch = invertSearch.isToggled();
        Config.getInstance().enableCrafting = enableCrafting.isToggled();
        Config.getInstance().enableInteractions = enableStripping.isToggled();

        TrackableCrawler.clearCache();
        Tracker.getInstance().clearCache();
        ConfigManager.saveConfig();
    }

    public void moveWidgets(boolean up) {
        if(up) {
            this.y -= HEIGHT / 2;
            this.backButton.setY(this.backButton.getY() - HEIGHT / 2);
            this.nextButton.setY(this.nextButton.getY() - HEIGHT / 2);
            this.prevButton.setY(this.prevButton.getY() - HEIGHT / 2);
            for(Map<String, ClickableWidget> page: pages) {
                for (Map.Entry<String, ClickableWidget> entry : page.entrySet()) {
                    entry.getValue().setY(entry.getValue().getY() - HEIGHT / 2);
                }
            }
        } else {
            this.y += HEIGHT / 2;
            this.backButton.setY(this.backButton.getY() + HEIGHT / 2);
            this.nextButton.setY(this.nextButton.getY() + HEIGHT / 2);
            this.prevButton.setY(this.prevButton.getY() + HEIGHT / 2);
            for(Map<String, ClickableWidget> page: pages) {
                for (Map.Entry<String, ClickableWidget> entry : page.entrySet()) {
                    entry.getValue().setY(entry.getValue().getY() + HEIGHT / 2);
                }
            }
        }
    }
}
