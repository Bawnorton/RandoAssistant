package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import com.bawnorton.randoassistant.tracking.Tracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onStatistics", at = @At(value = "INVOKE", target = "Lnet/minecraft/stat/StatHandler;setStat(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/stat/Stat;I)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onStatistics(StatisticsS2CPacket packet, CallbackInfo ci, Iterator var2, Map.Entry<Stat<?>, Integer> entry, Stat<?> stat, int i) {
        if(RandoAssistantStats.isCustom(stat) && i > 0) {
            Tracker.getInstance().testAll();
        }
    }
}
