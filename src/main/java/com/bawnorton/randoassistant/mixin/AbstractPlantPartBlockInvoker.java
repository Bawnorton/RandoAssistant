package com.bawnorton.randoassistant.mixin;

import net.minecraft.block.AbstractPlantPartBlock;
import net.minecraft.block.AbstractPlantStemBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractPlantPartBlock.class)
public interface AbstractPlantPartBlockInvoker {
    @Invoker("getStem")
    AbstractPlantStemBlock getStem();
}
