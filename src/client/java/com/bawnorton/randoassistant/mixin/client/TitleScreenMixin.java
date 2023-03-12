package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.util.Status;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @Inject(method = "init()V", at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        if(RandoAssistantClient.saveStatus == Status.FAILURE) {
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                    SystemToast.Type.UNSECURE_SERVER_WARNING,
                    Text.of("§cFailed to save RandoAssistant data"),
                    Text.of("Attempting to dump data to most recent save")
            ));
        } else if (RandoAssistantClient.saveStatus == Status.SUCCESS && Config.getInstance().toasts) {
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                    SystemToast.Type.UNSECURE_SERVER_WARNING,
                    Text.of("§b[RandoAssistant]"),
                    Text.of("Successfully saved RandoAssistant data")
            ));
        }
        if(RandoAssistantClient.dumpStatus == Status.FAILURE) {
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                    SystemToast.Type.UNSECURE_SERVER_WARNING,
                    Text.of("§cFailed to dump RandoAssistant data"),
                    Text.of("§cPlease send the report generated in the .minecraft folder to the mod author"
            )));
        } else if (RandoAssistantClient.dumpStatus == Status.SUCCESS) {
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                    SystemToast.Type.UNSECURE_SERVER_WARNING,
                    Text.of("§b[RandoAssistant]"),
                    Text.of("Successfully dumped RandoAssistant data")
            ));
        }
        RandoAssistantClient.saveStatus = Status.NONE;
        RandoAssistantClient.dumpStatus = Status.NONE;
    }
}
