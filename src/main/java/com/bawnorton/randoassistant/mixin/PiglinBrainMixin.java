package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {
    @SuppressWarnings("OptionalAssignedToNull")
    @Inject(method = "getBarteredItem", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void onGetBarteredItem(PiglinEntity piglin, CallbackInfoReturnable<List<ItemStack>> cir, LootTable lootTable, List<ItemStack> list) {
        Optional<PlayerEntity> player = piglin.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if(player == null) return; // who tf makes a nullable optional, just return a default value :mojank:
        player.ifPresent(playerEntity -> playerEntity.incrementStat(RandoAssistantStats.LOOTED.getOrCreateStat(LootTables.PIGLIN_BARTERING_GAMEPLAY)));
    }
}
