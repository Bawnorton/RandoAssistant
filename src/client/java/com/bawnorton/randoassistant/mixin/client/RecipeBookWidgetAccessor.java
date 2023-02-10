package com.bawnorton.randoassistant.mixin.client;

import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookWidget.class)
public interface RecipeBookWidgetAccessor {
    @Accessor
    int getLeftOffset();

    @Accessor
    int getParentWidth();

    @Accessor
    int getParentHeight();

    @Accessor
    TextFieldWidget getSearchField();
}
