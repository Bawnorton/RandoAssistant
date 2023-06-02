package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.stat.StatsManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.minecraft.loot.context.LootContextParameters.THIS_ENTITY;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
    @Shadow public abstract Identifier getLootTableId();

    @Inject(method = "getDroppedStacks", at = @At("HEAD"))
    private void getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        Entity source = builder.getOptional(THIS_ENTITY);
        if(source instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.incrementStat(StatsManager.LOOTED.getOrCreateStat(getLootTableId()));
        }
    }
}
