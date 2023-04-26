package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import me.jellysquid.mods.sodium.client.model.quad.blender.ColorSampler;
import me.jellysquid.mods.sodium.client.model.quad.blender.LinearColorBlender;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(LinearColorBlender.class)
public abstract class LinearColorBlenderMixin {
    @Inject(method = "getBlockColor", at = @At("RETURN"), cancellable = true)
    private <T> void onGetColor(BlockRenderView world, T state, ColorSampler<T> sampler, int x, int y, int z, int colorIdx, CallbackInfoReturnable<Integer> cir) {
        try {
            if(!Config.getInstance().randomizeColours) return;
            Random random = new Random(((BlockState) state).getBlock().hashCode() + RandoAssistantClient.seed);
            Random posRandom = new Random(x * random.nextLong() + y * random.nextLong() + z * random.nextLong());
            int r = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            int g = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            int b = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            cir.setReturnValue((r << 16) + (g << 8) + b);
        } catch (Exception ignored) {}
    }
}
