package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.extend.InventoryScreenExtender;
import com.bawnorton.randoassistant.networking.client.Networking;
import com.bawnorton.randoassistant.screen.LootBookWidget;
import com.bawnorton.randoassistant.screen.LootTableResultButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.bawnorton.randoassistant.screen.LootTableGraphWidget.HEIGHT;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreenMixin implements InventoryScreenExtender {

    @Unique
    private static final ButtonTextures LOOT_BUTTON_TEXTURES = new ButtonTextures(
            new Identifier(RandoAssistant.MOD_ID, "loot_button"),
            new Identifier(RandoAssistant.MOD_ID, "loot_button_focused")
    );
    
    @Shadow
    @Final
    private RecipeBookWidget recipeBook;
    @Shadow
    private boolean narrow;

    private TexturedButtonWidget lootButton;
    private TexturedButtonWidget recipeButton;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", shift = At.Shift.AFTER))
    private void onInit(CallbackInfo ci) {
        LootBookWidget lootBook = LootBookWidget.getInstance();
        lootBook.initialise((InventoryScreen) (Object) this);
        Networking.requestStatsPacket();
        lootButton = new TexturedButtonWidget(this.x + 126, this.height / 2 - 22, 20, 18, LOOT_BUTTON_TEXTURES, (button) -> {
            if(!RandoAssistantClient.isInstalledOnServer) return;
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
        }) {
            @Override
            public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
                if(!RandoAssistantClient.isInstalledOnServer) RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1);
                super.renderButton(context, mouseX, mouseY, delta);
                if(!RandoAssistantClient.isInstalledOnServer) RenderSystem.setShaderColor(1, 1, 1, 1);
            }

            @Override
            public boolean isSelected() {
                return super.isSelected() && RandoAssistantClient.isInstalledOnServer;
            }
        };
        if(!RandoAssistantClient.isInstalledOnServer) {
            lootButton.setTooltip(Tooltip.of(Text.of("§cMod not installed on server§r\n\nThis mod requires the server to have it installed to work properly")));
        }
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

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TexturedButtonWidget;<init>(IIIILnet/minecraft/client/gui/screen/ButtonTextures;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)V"), index = 5)
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

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AbstractInventoryScreen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        LootBookWidget lootBook = LootBookWidget.getInstance();
        if (lootBook.isOpen()) {
            lootBook.render(context, mouseX, mouseY, delta);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V", shift = At.Shift.AFTER))
    private void onTooltipRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        LootBookWidget.getInstance().drawTooltip(context, mouseX, mouseY);
    }

    @Redirect(method = "isPointWithinBounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeBookWidget;isOpen()Z"))
    private boolean checkWithinLootBookBounds(RecipeBookWidget instance) {
        return !instance.isOpen() || !LootBookWidget.getInstance().isOpen();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        LootBookWidget lootBook = LootBookWidget.getInstance();
        if (lootBook.mouseClicked(mouseX, mouseY, button)) {
            setFocused(lootBook);

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

    @SuppressWarnings("unused")
    @Override
    protected void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        LootBookWidget.getInstance().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    protected void onClose(CallbackInfo ci) {
        LootBookWidget.getInstance().closeSettings();
        LootBookWidget.getInstance().closeStats();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return LootBookWidget.getInstance().mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public TexturedButtonWidget getLootBookButton() {
        return lootButton;
    }

    @Override
    public TexturedButtonWidget getRecipeBookButton() {
        return recipeButton;
    }
}