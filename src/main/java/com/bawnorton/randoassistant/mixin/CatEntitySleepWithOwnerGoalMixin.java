package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootTables;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CatEntity.SleepWithOwnerGoal.class)
public abstract class CatEntitySleepWithOwnerGoalMixin {
    @Shadow private @Nullable PlayerEntity owner;

    @Inject(method = "dropMorningGifts", at = @At("HEAD"))
    private void onDropMorningGifts(CallbackInfo ci) {
        if(owner != null) {
            owner.incrementStat(RandoAssistantStats.LOOTED.getOrCreateStat(LootTables.CAT_MORNING_GIFT_GAMEPLAY));
        }
    }
}
