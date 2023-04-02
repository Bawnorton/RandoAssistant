package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BlockColors.class)
public abstract class BlockColorsMixin {
    @Inject(method = "getColor", at = @At("RETURN"), cancellable = true)
    private void onGetColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> cir) {
        try {
            if(!Config.getInstance().randomizeColours) return;
            Random random = new Random(state.getBlock().hashCode() + RandoAssistantClient.seed);
            Random posRandom = new Random(pos.hashCode());
            int r = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            int g = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            int b = (random.nextInt(256) + (posRandom.nextBoolean() ? 10 : -10)) & 0xFF;
            cir.setReturnValue((r << 16) + (g << 8) + b);
        } catch (Exception ignored) {}
    }
}
