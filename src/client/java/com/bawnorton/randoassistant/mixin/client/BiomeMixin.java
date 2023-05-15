package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(Biome.class)
public abstract class BiomeMixin {
    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
    private void onGetFogColor(CallbackInfoReturnable<Integer> cir) {
        try {
            if(!Config.getInstance().randomizeColours) return;
            Random random = new Random(cir.getReturnValue() + RandoAssistantClient.seed);
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            cir.setReturnValue((r << 16) + (g << 8) + b);
        } catch (Exception ignored) {}
    }

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void onGetSkyColor(CallbackInfoReturnable<Integer> cir) {
        try {
            if(!Config.getInstance().randomizeColours || !RandoAssistantClient.isInstalledOnServer) return;
            Random random = new Random(cir.getReturnValue() + RandoAssistantClient.seed);
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            cir.setReturnValue((r << 16) + (g << 8) + b);
        } catch (Exception ignored) {}
    }

    @Inject(method = "getWaterColor", at = @At("RETURN"), cancellable = true)
    private void onGetWaterColor(CallbackInfoReturnable<Integer> cir) {
        try {
            if(!Config.getInstance().randomizeColours || !RandoAssistantClient.isInstalledOnServer) return;
            Random random = new Random(RandoAssistantClient.seed);
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            cir.setReturnValue((r << 16) + (g << 8) + b);
        } catch (Exception ignored) {}
    }

    @Inject(method = "getWaterFogColor", at = @At("RETURN"), cancellable = true)
    private void onGetWaterFogColor(CallbackInfoReturnable<Integer> cir) {
        try {
            if(!Config.getInstance().randomizeColours || !RandoAssistantClient.isInstalledOnServer) return;
            Random random = new Random(RandoAssistantClient.seed);
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            cir.setReturnValue((r << 16) + (g << 8) + b);
        } catch (Exception ignored) {}
    }
}
