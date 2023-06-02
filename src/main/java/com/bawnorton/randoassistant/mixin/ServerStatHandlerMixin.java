package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.stat.StatsManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ServerStatHandler.class)
public abstract class ServerStatHandlerMixin {
    @Shadow public abstract void sendStats(ServerPlayerEntity player);

    @Inject(method = "setStat", at = @At("TAIL"))
    private void onSetStat(PlayerEntity player, Stat<?> stat, int value, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if(StatsManager.isCustom(stat)) {
                sendStats(serverPlayer);
                Networking.sendClearCachePacket(serverPlayer);
            }
        }
    }

    @Inject(method = "getStatId", at = @At("RETURN"), cancellable = true)
    private static <T> void onGetStatId(Stat<T> stat, CallbackInfoReturnable<Identifier> cir) {
        if(StatsManager.usesIdentifier(stat)) {
            cir.setReturnValue((Identifier) stat.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "createStat", at = @At("RETURN"), cancellable = true)
    private <T> void onCreateStat(StatType<T> type, String id, CallbackInfoReturnable<Optional<Stat<T>>> cir) {
        if(type.equals(StatsManager.LOOTED)) {
            cir.setReturnValue(Optional.of((Stat<T>) StatsManager.LOOTED.getOrCreateStat(Identifier.tryParse(id))));
        } else if (type.equals(StatsManager.INTERACTED)) {
            cir.setReturnValue(Optional.of((Stat<T>) StatsManager.INTERACTED.getOrCreateStat(Identifier.tryParse(id))));
        } else if (type.equals(StatsManager.CRAFTED)) {
            cir.setReturnValue(Optional.of((Stat<T>) StatsManager.CRAFTED.getOrCreateStat(Identifier.tryParse(id))));
        }
    }
}
