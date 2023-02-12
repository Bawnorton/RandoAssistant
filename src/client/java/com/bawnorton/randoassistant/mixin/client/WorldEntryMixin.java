package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.RandoAssistantClient;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(WorldListWidget.WorldEntry.class)
public abstract class WorldEntryMixin {
    @Shadow
    public abstract String getLevelDisplayName();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Inject(method = "delete", at = @At("HEAD"))
    private void delete(CallbackInfo ci) {
        File dir = RandoAssistantClient.ASSISTANT_DIRECTORY;
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith(getLevelDisplayName())) {
                        file.delete();
                    }
                }
            }
        }
    }
}
