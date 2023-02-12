package com.bawnorton.randoassistant.mixin;

import net.minecraft.block.AttachedStemBlock;
import net.minecraft.block.GourdBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AttachedStemBlock.class)
public interface AttachedStemBlockAccessor {
    @Accessor
    GourdBlock getGourdBlock();
}
