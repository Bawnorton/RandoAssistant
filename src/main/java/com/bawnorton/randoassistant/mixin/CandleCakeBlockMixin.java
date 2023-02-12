package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CandleCakeBlock.class)
public abstract class CandleCakeBlockMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Block candle, AbstractBlock.Settings settings, CallbackInfo ci) {
        RandoAssistant.CANDLE_CAKE_MAP.put((CandleCakeBlock) (Object) this, (CandleBlock) candle);
    }
}
