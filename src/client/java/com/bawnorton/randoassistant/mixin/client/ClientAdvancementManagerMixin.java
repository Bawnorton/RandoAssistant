package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ClientAdvancementManager.class)
public class ClientAdvancementManagerMixin {

    @Inject(method = "onAdvancements", at = @At("TAIL"))
    private void checkForFasguyAdvancement(AdvancementUpdateS2CPacket packet, CallbackInfo ci) {
        if (Config.getInstance().autoToggle) {
            RandoAssistantClient.datapackDetected = false;
            for (Map.Entry<Identifier, AdvancementProgress> entry : packet.getAdvancementsToProgress().entrySet()) {
                if (entry.getKey().getNamespace().equals("fasguys_toolbox") && entry.getKey().getPath().startsWith("loot_table_randomizer")) {
                    RandoAssistantClient.datapackDetected = true;
                    break;
                }
            }
        }
    }
}
