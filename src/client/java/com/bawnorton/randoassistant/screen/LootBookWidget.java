package com.bawnorton.randoassistant.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

/*
To be implemented:
- Search bar
- List of loot tables
- Interaction toggle button
- Loot table preview from source
- Settings button
- Settings screen
 */
public class LootBookWidget extends DrawableHelper implements Drawable, Element, Selectable {
    protected static final Identifier TEXTURE = new Identifier("textures/gui/recipe_book.png");
    private static LootBookWidget INSTANCE;

    private int rightOffset;
    private int parentWidth;
    private int parentHeight;
    private boolean narrow;
    private boolean open;

    public static LootBookWidget getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new LootBookWidget();
        }
        return INSTANCE;
    }

    public void initialise(int parentWidth, int parentHeight, boolean narrow) {
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
        this.narrow = narrow;
    }

    public void toggleOpen() {
        open = !open;
    }

    public boolean isOpen() {
        return open;
    }


    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(!open) return;
        matrices.push();
        matrices.translate(0, 0, -100f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.parentWidth + 147) / 2 + this.rightOffset;
        int j = (this.parentHeight + 166) / 2;
        drawTexture(matrices, i, j, 1, 1, 147, 166);
        // draw search bar
        // draw interaction toggle button
        // draw list of loot tables
        matrices.pop();
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public SelectionType getType() {
        return this.open ? SelectionType.HOVERED : SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        ArrayList<ClickableWidget> list = Lists.newArrayList();
        // add search bar
        // add interaction toggle button
        // add list of loot tables
        Screen.SelectedElementNarrationData selectedElementNarrationData = Screen.findSelectedElementData(list, null);
        if (selectedElementNarrationData != null) {
            selectedElementNarrationData.selectable.appendNarrations(builder.nextMessage());
        }
    }
}
