package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.screen.LootBookWidget;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractInventoryScreen.class)
public abstract class AbstractInventoryScreenMixin extends HandledScreenMixin {
    @ModifyVariable(method = "drawStatusEffects", at = @At("STORE"), ordinal = 2)
    private int modifyStatusEffectX(int x) {
        return x + (LootBookWidget.getInstance().isOpen() ? 147 : 0);
    }
}
