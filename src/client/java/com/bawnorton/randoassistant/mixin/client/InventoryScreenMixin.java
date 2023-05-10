package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.screen.LootBookWidget;
import com.bawnorton.randoassistant.screen.LootTableResultButton;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.bawnorton.randoassistant.screen.LootTableGraphWidget.HEIGHT;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreenMixin {

    private static final Identifier LOOT_BUTTON_TEXTURE = new Identifier("randoassistant", "textures/gui/loot_button.png");
    @Shadow
    @Final
    private RecipeBookWidget recipeBook;
    @Shadow
    private boolean narrow;
    private TexturedButtonWidget lootButton;
    private TexturedButtonWidget recipeButton;

    @Shadow
    protected abstract void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY);

    @Inject(method = "handledScreenTick", at = @At("TAIL"))
    private void onHandledScreenTick(CallbackInfo ci) {
        LootBookWidget.getInstance().tick();
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", shift = At.Shift.AFTER))
    private void onInit(CallbackInfo ci) {
        LootBookWidget lootBook = LootBookWidget.getInstance();
        lootBook.initialise((InventoryScreen) (Object) this);
        lootButton = new TexturedButtonWidget(this.x + 126, this.height / 2 - 22, 20, 18, 0, 0, 19, LOOT_BUTTON_TEXTURE, (button) -> {
            lootBook.toggleOpen();
            if (lootBook.isOpen() && recipeBook.isOpen()) {
                recipeBook.toggleOpen();
            }
            this.x = lootBook.findLeftEdge(this.width, this.backgroundWidth);
            recipeButton.setX(this.x + 104);
            button.setX(recipeButton.getX() + 22);

            if(LootTableResultButton.isGraphOpen() && lootBook.getScreen().y == (this.height - this.backgroundHeight) / 2) {
                lootBook.getScreen().y += HEIGHT / 2;
                button.setY(button.getY() + HEIGHT / 2);
                recipeButton.setY(recipeButton.getY() + HEIGHT / 2);
                lootBook.moveWidgets(false);
            } else if (!LootTableResultButton.isGraphOpen() && lootBook.getScreen().y != (this.height - this.backgroundHeight) / 2) {
                lootBook.getScreen().y -= HEIGHT / 2;
                button.setY(button.getY() - HEIGHT / 2);
                recipeButton.setY(recipeButton.getY() - HEIGHT / 2);
                lootBook.moveWidgets(true);
            }
        });
        if (recipeBook.isOpen()) {
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
        } else if (lootBook.isOpen()) {
            this.x = lootBook.findLeftEdge(this.width, this.backgroundWidth);
            lootButton.setX(this.x + 126);
            recipeButton.setX(this.x + 104);
        } else {
            this.x = (this.width - this.backgroundWidth) / 2;
        }
        addDrawableChild(lootButton);
        addSelectableChild(lootButton);

        if(LootTableResultButton.isGraphOpen() && y == (this.height - this.backgroundHeight) / 2) {
            y += HEIGHT / 2;
            lootButton.setY(lootButton.getY() + HEIGHT / 2);
            recipeButton.setY(recipeButton.getY() + HEIGHT / 2);
            lootBook.moveWidgets(false);
        }
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    private Element onAddRecipeButton(Element button) {
        recipeButton = (TexturedButtonWidget) button;
        return button;
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TexturedButtonWidget;<init>(IIIIIIILnet/minecraft/util/Identifier;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)V"), index = 8)
    private ButtonWidget.PressAction onAddDrawableChild(ButtonWidget.PressAction pressAction) {
        return (button) -> {
            recipeBook.toggleOpen();
            LootBookWidget lootBook = LootBookWidget.getInstance();
            if (recipeBook.isOpen() && lootBook.isOpen()) {
                lootBook.toggleOpen();
            }
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            recipeButton.setPosition(this.x + 104, this.height / 2 - 22);
            lootButton.setX(recipeButton.getX() + 22);

            if (lootBook.getScreen().y != (this.height - this.backgroundHeight) / 2) {
                lootBook.getScreen().y -= HEIGHT / 2;
                lootButton.setY(lootButton.getY() - HEIGHT / 2);
                lootBook.moveWidgets(true);
            }
        };
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;renderBackground(Lnet/minecraft/client/util/math/MatrixStack;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        LootBookWidget lootBook = LootBookWidget.getInstance();
        if (lootBook.isOpen()) {
            drawBackground(matrices, delta, mouseX, mouseY);
            lootBook.render(matrices, mouseX, mouseY, delta);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeBookWidget;drawTooltip(Lnet/minecraft/client/util/math/MatrixStack;IIII)V", shift = At.Shift.AFTER))
    private void onTooltipRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        LootBookWidget.getInstance().drawTooltip(matrices, mouseX, mouseY);
    }

    @SuppressWarnings("unused")
    @ModifyExpressionValue(method = "isPointWithinBounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeBookWidget;isOpen()Z"))
    private boolean checkWithinLootBookBounds(boolean original) {
        return original || !LootBookWidget.getInstance().isOpen();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        LootBookWidget lootBook = LootBookWidget.getInstance();
        if (lootBook.mouseClicked(mouseX, mouseY, button)) {
            ((InventoryScreen) (Object) this).setFocused(lootBook);

            if(LootTableResultButton.isGraphOpen() && y == (this.height - this.backgroundHeight) / 2) {
                y += HEIGHT / 2;
                lootButton.setY(lootButton.getY() + HEIGHT / 2);
                recipeButton.setY(recipeButton.getY() + HEIGHT / 2);
                lootBook.moveWidgets(false);
            } else if (!LootTableResultButton.isGraphOpen() && y != (this.height - this.backgroundHeight) / 2) {
                y = (this.height - this.backgroundHeight) / 2;
                lootButton.setY(lootButton.getY() - HEIGHT / 2);
                recipeButton.setY(recipeButton.getY() - HEIGHT / 2);
                lootBook.moveWidgets(true);
            }
            cir.setReturnValue(true);
        }
        if (narrow && lootBook.isOpen()) {
            cir.setReturnValue(false);
        }
    }

    @Override
    protected void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        LootBookWidget.getInstance().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return LootBookWidget.getInstance().mouseScrolled(mouseX, mouseY, amount);
    }
}