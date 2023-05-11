package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract Identifier getLootTable();

    @Inject(method = "dropLoot", at = @At("HEAD"))
    private void dropLoot(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if(source.getAttacker() instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.incrementStat(RandoAssistantStats.LOOTED.getOrCreateStat(getLootTable()));
        }
    }
}
