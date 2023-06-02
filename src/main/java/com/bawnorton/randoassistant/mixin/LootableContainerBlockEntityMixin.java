package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.stat.StatsManager;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin {
    @Shadow
    @Nullable
    protected Identifier lootTableId;

    @Inject(method = "checkLootInteraction", at = @At("HEAD"))
    private void onCheckLootInteractionHead(PlayerEntity player, CallbackInfo ci) {
        if (lootTableId != null && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.incrementStat(StatsManager.LOOTED.getOrCreateStat(lootTableId));
        }
    }
}
