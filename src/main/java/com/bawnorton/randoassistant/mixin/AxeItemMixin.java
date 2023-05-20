package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir, World world, BlockPos blockPos, PlayerEntity playerEntity, BlockState originalState, Optional<BlockState> optional, Optional<BlockState> optional2, Optional<BlockState> optional3, ItemStack itemStack, Optional<Object> optional4) {
        if (optional4.isPresent() && playerEntity instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.incrementStat(RandoAssistantStats.INTERACTED.getOrCreateStat(Registries.BLOCK.getId(originalState.getBlock())));
        }
    }
}
