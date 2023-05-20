package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.GiveGiftsToHeroTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(GiveGiftsToHeroTask.class)
public abstract class GiveGiftsToHeroTaskMixin {
    @Shadow @Final private static Map<VillagerProfession, Identifier> GIFTS;

    @Inject(method = "giveGifts", at = @At("HEAD"))
    private void onGetGifts(VillagerEntity villager, LivingEntity recipient, CallbackInfo ci) {
        Identifier id = GIFTS.get(villager.getVillagerData().getProfession());
        if(id == null) return;
        if(recipient instanceof PlayerEntity player) {
            player.incrementStat(RandoAssistantStats.LOOTED.getOrCreateStat(id));
        }
    }
}
