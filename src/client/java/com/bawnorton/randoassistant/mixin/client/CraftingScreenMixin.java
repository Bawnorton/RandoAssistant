package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.screen.LootBookWidget;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin extends HandledScreenMixin {
    @Shadow @Final private RecipeBookWidget recipeBook;

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TexturedButtonWidget;<init>(IIIIIIILnet/minecraft/util/Identifier;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)V"), index = 8)
    private ButtonWidget.PressAction onAddDrawableChild(ButtonWidget.PressAction pressAction) {
        return (button) -> {
            recipeBook.toggleOpen();
            LootBookWidget lootBook = LootBookWidget.getInstance();
            if (recipeBook.isOpen() && lootBook.isOpen()) {
                lootBook.toggleOpen();
            }
            x = this.recipeBook.findLeftEdge(width, backgroundWidth);
            button.setPosition(x + 5, height / 2 - 49);
        };
    }
}
