package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.extend.HoveredTooltipPositionerExtender;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HoveredTooltipPositioner.class)
public abstract class HoveredTooltipPositionerMixin implements HoveredTooltipPositionerExtender {
    private boolean ignorePreventOverflow = false;

    @Inject(method = "preventOverflow", at = @At(value = "HEAD"), cancellable = true)
    private void ignorePreventOverflow(int screenWidth, int screenHeight, Vector2i pos, int width, int height, CallbackInfo ci) {
        if(ignorePreventOverflow) ci.cancel();
    }

    @Override
    public void setIgnorePreventOverflow(boolean ignorePreventOverflow) {
        this.ignorePreventOverflow = ignorePreventOverflow;
    }
}
