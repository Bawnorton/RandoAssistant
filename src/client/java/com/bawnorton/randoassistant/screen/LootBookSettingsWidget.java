package com.bawnorton.randoassistant.screen;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.config.ConfigManager;
import com.bawnorton.randoassistant.event.client.EventManager;
import com.bawnorton.randoassistant.tracking.Tracker;
import com.bawnorton.randoassistant.tracking.trackable.TrackableCrawler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.text.Text;

import static com.bawnorton.randoassistant.screen.LootTableGraphWidget.HEIGHT;

public class LootBookSettingsWidget {
    private final MinecraftClient client;
    private final int x;
    private int y;

    private final ToggleButtonWidget backButton;
    private final ToggleButtonWidget starIcons;
    private final ToggleButtonWidget silkTouchStarIcons;
    private final ToggleButtonWidget autoToggle;
    private final ToggleButtonWidget enableOverride;
    private final ToggleButtonWidget randomizeColours;
    private final TextFieldWidget searchDepth;
    private final TextFieldWidget highlightRadius;

    public LootBookSettingsWidget(MinecraftClient client, int x, int y) {
        this.client = client;
        this.x = x;
        this.y = y;

        backButton = createButton(x + 10, y + 10, "Save and Exit", false);
        backButton.setTextureUV(206, 41, 0, 18, LootBookWidget.TEXTURE);
        x += 120;
        y += 40;
        starIcons = createButton(x, y, "Display star icons on unbroken blocks", Config.getInstance().unbrokenBlockIcon);
        silkTouchStarIcons = createButton(x, y + 20, "Display star icons on broken but not silk-touched blocks\n\nRequires §bUnbroken Stars", Config.getInstance().silktouchUnbrokenBlockIcon);
        autoToggle = createButton(x, y + 40, "Automatically enable the mod if a fasguy datapack is detected", Config.getInstance().autoToggle);
        enableOverride = createButton(x, y + 60, "Enable all undiscovered loot tables\n\n§7This is not permanent", Config.getInstance().enableOverride);
        randomizeColours = createButton(x, y + 80, "Randomize world and entity colours (Cosmetic)", Config.getInstance().randomizeColours);

        searchDepth = new TextFieldWidget(client.textRenderer, x - 5, y + 100, 20, client.textRenderer.fontHeight + 3, Text.of(""));
        searchDepth.setChangedListener((text) -> {
            String filtered = text.replaceAll("[^0-9]", "");
            while (filtered.startsWith("0")) filtered = filtered.substring(1);
            if (filtered.isEmpty()) {
                filtered = "0";
                searchDepth.setEditableColor(0xAAAAAA);
            } else {
                searchDepth.setEditableColor(0xFFFFFF);
            }
            if (!filtered.equals(text)) {
                searchDepth.setText(filtered);
            }
        });
        searchDepth.setMaxLength(2);
        searchDepth.setVisible(true);
        searchDepth.setEditableColor(0xFFFFFF);
        searchDepth.setText(String.valueOf(Config.getInstance().searchDepth));
        searchDepth.setTooltip(Tooltip.of(Text.of("The maximum number of steps to search for a path to the target item\n\n§6Warning: §rValues over §c15§r are not recommended!")));

        highlightRadius = new TextFieldWidget(client.textRenderer, x - 5, y + 120, 20, client.textRenderer.fontHeight + 3, Text.of(""));
        highlightRadius.setChangedListener((text) -> {
            String filtered = text.replaceAll("[^0-9]", "");
            while (filtered.startsWith("0")) filtered = filtered.substring(1);
            if (filtered.isEmpty()) {
                filtered = "0";
                highlightRadius.setEditableColor(0xAAAAAA);
            } else {
                highlightRadius.setEditableColor(0xFFFFFF);
            }
            if (!filtered.equals(text)) {
                highlightRadius.setText(filtered);
            }
        });
        highlightRadius.setMaxLength(2);
        highlightRadius.setVisible(true);
        highlightRadius.setEditableColor(0xFFFFFF);
        highlightRadius.setText(String.valueOf(Config.getInstance().highlightRadius));
        highlightRadius.setTooltip(Tooltip.of(Text.of("The radius of the highlight effect when pressing \"" + EventManager.highlight.getBoundKeyLocalizedText().getString() + "\"\n\n§6Warning: §rValues over §c10§r are not recommended!")));
    }

    private ToggleButtonWidget createButton(int x, int y, String tooltip, boolean toggled) {
        ToggleButtonWidget button = new ToggleButtonWidget(x, y, 16, 16, toggled);
        button.setTextureUV(170, 41, 18, 18, LootBookWidget.TEXTURE);
        button.setTooltip(Tooltip.of(Text.of(tooltip)));
        return button;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawText(client.textRenderer, Text.of("Settings"), x + 52, y + 13, 0xFFFFFF, false);
        this.backButton.render(context, mouseX, mouseY, delta);
        this.starIcons.render(context, mouseX, mouseY, delta);
        context.drawText(client.textRenderer, Text.of("Unbroken Stars"), starIcons.getX() - 110, starIcons.getY() + 4, 0xFFFFFF, false);
        this.silkTouchStarIcons.render(context, mouseX, mouseY, delta);
        context.drawText(client.textRenderer, Text.of("Silk-Touch Stars"), silkTouchStarIcons.getX() - 110, silkTouchStarIcons.getY() + 4, 0xFFFFFF, false);
        this.autoToggle.render(context, mouseX, mouseY, delta);
        context.drawText(client.textRenderer, Text.of("Auto Toggle"), autoToggle.getX() - 110, autoToggle.getY() + 4, 0xFFFFFF, false);
        this.enableOverride.render(context, mouseX, mouseY, delta);
        context.drawText(client.textRenderer, Text.of("Enable Override"), enableOverride.getX() - 110, enableOverride.getY() + 4, 0xFFFFFF, false);
        this.randomizeColours.render(context, mouseX, mouseY, delta);
        context.drawText(client.textRenderer, Text.of("Randomize Colours"), randomizeColours.getX() - 110, randomizeColours.getY() + 4, 0xFFFFFF, false);
        this.searchDepth.render(context, mouseX, mouseY, delta);
        context.drawText(client.textRenderer, Text.of("Search Depth"), searchDepth.getX() - 105, searchDepth.getY() + 4, 0xFFFFFF, false);
        this.highlightRadius.render(context, mouseX, mouseY, delta);
        context.drawText(client.textRenderer, Text.of("Highlight Radius"), highlightRadius.getX() - 105, highlightRadius.getY() + 4, 0xFFFFFF, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        searchDepth.setFocused(false);
        highlightRadius.setFocused(false);
        if (backButton.mouseClicked(mouseX, mouseY, button)) {
            LootBookWidget.getInstance().closeSettings();
            return true;
        }
        if(starIcons.mouseClicked(mouseX, mouseY, button)) {
            starIcons.setToggled(!starIcons.isToggled());
            if(silkTouchStarIcons.isToggled()) {
                silkTouchStarIcons.setToggled(false);
            }
            return true;
        }
        if(silkTouchStarIcons.mouseClicked(mouseX, mouseY, button)) {
            silkTouchStarIcons.setToggled(!silkTouchStarIcons.isToggled());
            if(!starIcons.isToggled()) {
                starIcons.setToggled(true);
            }
            return true;
        }
        if(autoToggle.mouseClicked(mouseX, mouseY, button)) {
            autoToggle.setToggled(!autoToggle.isToggled());
            return true;
        }
        if(enableOverride.mouseClicked(mouseX, mouseY, button)) {
            enableOverride.setToggled(!enableOverride.isToggled());
            return true;
        }
        if(randomizeColours.mouseClicked(mouseX, mouseY, button)) {
            randomizeColours.setToggled(!randomizeColours.isToggled());
            return true;
        }
        if(searchDepth.mouseClicked(mouseX, mouseY, button)) {
            searchDepth.setFocused(true);
            return true;
        }
        if(highlightRadius.mouseClicked(mouseX, mouseY, button)) {
            highlightRadius.setFocused(true);
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(searchDepth.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return highlightRadius.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char chr, int modifiers) {
        if(searchDepth.charTyped(chr, modifiers)) {
            return true;
        }
        return highlightRadius.charTyped(chr, modifiers);
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
        TrackableCrawler.clearCache();
        Tracker.getInstance().clearCache();
        ConfigManager.saveConfig();
    }

    public void moveWidgets(boolean up) {
        if(up) {
            this.y -= HEIGHT / 2;
            this.backButton.setY(this.backButton.getY() - HEIGHT / 2);
            this.starIcons.setY(this.starIcons.getY() - HEIGHT / 2);
            this.silkTouchStarIcons.setY(this.silkTouchStarIcons.getY() - HEIGHT / 2);
            this.enableOverride.setY(this.enableOverride.getY() - HEIGHT / 2);
            this.randomizeColours.setY(this.randomizeColours.getY() - HEIGHT / 2);
            this.searchDepth.setY(this.searchDepth.getY() - HEIGHT / 2);
            this.highlightRadius.setY(this.highlightRadius.getY() - HEIGHT / 2);
        } else {
            this.y += HEIGHT / 2;
            this.backButton.setY(this.backButton.getY() + HEIGHT / 2);
            this.starIcons.setY(this.starIcons.getY() + HEIGHT / 2);
            this.silkTouchStarIcons.setY(this.silkTouchStarIcons.getY() + HEIGHT / 2);
            this.enableOverride.setY(this.enableOverride.getY() + HEIGHT / 2);
            this.randomizeColours.setY(this.randomizeColours.getY() + HEIGHT / 2);
            this.searchDepth.setY(this.searchDepth.getY() + HEIGHT / 2);
            this.highlightRadius.setY(this.highlightRadius.getY() + HEIGHT / 2);
        }
    }
}
