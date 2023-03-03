package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.screen.widget.drawable.NodeWidget;
import com.bawnorton.randoassistant.util.Wrapper;
import com.bawnorton.randoassistant.screen.LootTableScreen;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.bawnorton.randoassistant.RandoAssistantClient.ACTUAL_SCALE;
import static com.bawnorton.randoassistant.RandoAssistantClient.SCALE;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {

    private static final Identifier LOOT_BUTTON_TEXTURE = new Identifier("randoassistant", "textures/gui/loot_button.png");
    @Shadow
    @Final
    private RecipeBookWidget recipeBook;
    private TexturedButtonWidget lootButton;

    protected InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", shift = At.Shift.AFTER))
    private void onInit(CallbackInfo ci) {
        ACTUAL_SCALE = Wrapper.of(MinecraftClient.getInstance().getWindow().getScaleFactor());
        if(SCALE.get() == null) SCALE.set(ACTUAL_SCALE.get());
        lootButton = new TexturedButtonWidget(this.x + 126, this.height / 2 - 22, 20, 18, 0, 0, 19, LOOT_BUTTON_TEXTURE, (button) -> MinecraftClient.getInstance().setScreen(new CottonClientScreen(new LootTableScreen()) {
            @Override
            public void close() {
                super.close();
                if (client != null && client.player != null) {
                    MinecraftClient.getInstance().getWindow().setScaleFactor(ACTUAL_SCALE.get());
                    MinecraftClient.getInstance().setScreen(new InventoryScreen(client.player));
                }
            }

            @Override
            public void resize(MinecraftClient client, int width, int height) {
                super.resize(client, width, height);
                if(RandoAssistantClient.hideOtherNodes) {
                    LootTableScreen.getInstance().redrawWithSelectedNode();
                } else {
                    LootTableScreen.getInstance().redraw();
                }
            }

            @Override
            public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
                width = MinecraftClient.getInstance().getWindow().getScaledWidth();
                height = MinecraftClient.getInstance().getWindow().getScaledHeight();
                super.render(matrices, mouseX, mouseY, partialTicks);
            }
        }));
        addDrawableChild(lootButton);
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TexturedButtonWidget;<init>(IIIIIIILnet/minecraft/util/Identifier;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)V"), index = 8)
    private ButtonWidget.PressAction onAddDrawableChild(ButtonWidget.PressAction pressAction) {
        return (button) -> {
            recipeBook.toggleOpen();
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            button.setPos(this.x + 104, this.height / 2 - 22);
            lootButton.setX(button.getX() + 22);
        };
    }
}
