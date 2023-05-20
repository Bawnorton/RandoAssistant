package com.bawnorton.randoassistant.mixin.client;

import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("ALL")
@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractParentElement {
    @Shadow protected abstract <T extends Element & Selectable> T addSelectableChild(T child);
    @Shadow protected abstract <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement);
    @Shadow public int height;
    @Shadow public int width;
}
