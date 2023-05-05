package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.screen.LootBookWidget;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
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

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {

    private static final Identifier LOOT_BUTTON_TEXTURE = new Identifier("randoassistant", "textures/gui/loot_button.png");
    @Shadow
    @Final
    private RecipeBookWidget recipeBook;
    @Shadow
    private boolean narrow;
    private TexturedButtonWidget lootButton;
    private TexturedButtonWidget recipeButton;

    protected InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Shadow
    protected abstract void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY);

    @Inject(method = "handledScreenTick", at = @At("TAIL"))
    private void onHandledScreenTick(CallbackInfo ci) {
        LootBookWidget.getInstance().tick();
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", shift = At.Shift.AFTER))
    private void onInit(CallbackInfo ci) {
        LootBookWidget lootBook = LootBookWidget.getInstance();
        lootBook.initialise(client, width, height, narrow);
        lootButton = new TexturedButtonWidget(this.x + 126, this.height / 2 - 22, 20, 18, 0, 0, 19, LOOT_BUTTON_TEXTURE, (button) -> {
            lootBook.toggleOpen();
            if (lootBook.isOpen() && recipeBook.isOpen()) {
                recipeBook.toggleOpen();
            }
            this.x = lootBook.findLeftEdge(this.width, this.backgroundWidth);
            recipeButton.setX(this.x + 104);
            button.setX(recipeButton.getX() + 22);
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
            if (recipeBook.isOpen() && LootBookWidget.getInstance().isOpen()) {
                LootBookWidget.getInstance().toggleOpen();
            }
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            button.setPosition(this.x + 104, this.height / 2 - 22);
            lootButton.setX(button.getX() + 22);
        };
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;renderBackground(Lnet/minecraft/client/util/math/MatrixStack;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        LootBookWidget lootBook = LootBookWidget.getInstance();
        if (lootBook.isOpen()) {
            drawBackground(matrices, delta, mouseX, mouseY);
            lootBook.render(matrices, mouseX, mouseY, delta);
        }
        lootBook.drawTooltip(matrices, mouseX, mouseY);
    }

    @ModifyExpressionValue(method = "isPointWithinBounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeBookWidget;isOpen()Z"))
    private boolean checkWithinLootBookBounds(boolean original) {
        return original || !LootBookWidget.getInstance().isOpen();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        LootBookWidget lootBook = LootBookWidget.getInstance();
        if (lootBook.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(lootBook);
            cir.setReturnValue(true);
        }
        if (narrow && lootBook.isOpen()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isClickOutsideBounds", at = @At("RETURN"), cancellable = true)
    private void checkOutsideLootBookBounds(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && LootBookWidget.getInstance().isClickOutsideBounds(mouseX, mouseY, left, top, backgroundWidth, backgroundHeight, button));
    }

    @Inject(method = "onMouseClick", at = @At("TAIL"))
    private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        LootBookWidget lootBook = LootBookWidget.getInstance();
        lootBook.slotClicked(slot);
    }
}
