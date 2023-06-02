package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.stat.StatsManager;
import com.bawnorton.randoassistant.util.LootAdvancement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract Identifier getLootTable();

    @SuppressWarnings("ConstantValue")
    @Inject(method = "dropLoot", at = @At("HEAD"))
    private void dropLoot(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if(source.getAttacker() instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.incrementStat(StatsManager.LOOTED.getOrCreateStat(getLootTable()));
            Object thiz = this;
            if(thiz instanceof TurtleEntity || thiz instanceof DolphinEntity || thiz instanceof AllayEntity) {
                LootAdvancement.MONSTER.grant(serverPlayer);
            } else if (thiz instanceof PolarBearEntity polarBear) {
                List<PolarBearEntity> list = polarBear.getWorld().getNonSpectatingEntities(PolarBearEntity.class, polarBear.getBoundingBox().expand(8.0, 4.0, 8.0));
                for (PolarBearEntity polarBearEntity : list) {
                    if (polarBearEntity.isBaby()) {
                        LootAdvancement.ORPHANED_POLAR_BEAR.grant(serverPlayer);
                        return;
                    }
                }
            }
        }
    }
}
