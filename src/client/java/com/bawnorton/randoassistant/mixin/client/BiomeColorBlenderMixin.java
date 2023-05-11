package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import me.jellysquid.mods.sodium.client.model.quad.blender.ColorSampler;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@SuppressWarnings("DuplicatedCode")
@Pseudo
@Mixin(targets = {"me.jellysquid.mods.sodium.client.model.quad.blender.BiomeColorBlender"})
public abstract class BiomeColorBlenderMixin {
    @Inject(method = "getBlockColor", at = @At("RETURN"), cancellable = true)
    private <T> void onGetColor(BlockRenderView world, T state, ColorSampler<T> sampler, int x, int y, int z, int colorIdx, CallbackInfoReturnable<Integer> cir) {
        try {
            if(!Config.getInstance().randomizeColours) return;
            Random random = new Random(state.hashCode() + RandoAssistantClient.seed);
            Random posRandom = new Random(x * random.nextLong() + y * random.nextLong() + z * random.nextLong());
            int r = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            int g = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            int b = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            cir.setReturnValue((r << 16) + (g << 8) + b);
        } catch (Exception ignored) {}
    }
}

@SuppressWarnings("DuplicatedCode")
@Pseudo
@Mixin(targets = {"me.jellysquid.mods.sodium.client.model.quad.blender.LinearColorBlender"})
abstract class LinearColorBlenderMixin {
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "getBlockColor", at = @At("RETURN"), cancellable = true)
    private <T> void onGetColor(BlockRenderView world, T state, ColorSampler<T> sampler, int x, int y, int z, int colorIdx, CallbackInfoReturnable<Integer> cir) {
        try {
            if(!Config.getInstance().randomizeColours) return;
            Random random = new Random(state.hashCode() + RandoAssistantClient.seed);
            Random posRandom = new Random(x * random.nextLong() + y * random.nextLong() + z * random.nextLong());
            int r = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            int g = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            int b = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            cir.setReturnValue((r << 16) + (g << 8) + b);
        } catch (Exception ignored) {}
    }
}
