package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "afterBreak", at = @At("HEAD"))
    private void onAfterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        if(player instanceof ServerPlayerEntity serverPlayer) {
            ItemStack handStack = player.getMainHandStack();
            if(EnchantmentHelper.hasSilkTouch(handStack)) {
                serverPlayer.incrementStat(RandoAssistantStats.SILK_TOUCHED.getOrCreateStat(state.getBlock()));
            }
        }
    }
}
