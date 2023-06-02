package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.stat.StatsManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Debug(export = true)
@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {

    @Shadow @Nullable public abstract PlayerEntity getPlayerOwner();

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContextParameterSet;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onUse(ItemStack usedItem, CallbackInfoReturnable<Integer> cir, PlayerEntity playerEntity, int i, LootContextParameterSet lootContextParameterSet, LootTable lootTable) {
        if(getPlayerOwner() instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.incrementStat(StatsManager.LOOTED.getOrCreateStat(LootTables.FISHING_GAMEPLAY));
        }
    }
}
