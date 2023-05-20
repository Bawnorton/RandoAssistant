package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.util.LootAdvancement;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.TradeOffer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantEntity.class)
public abstract class MerchantEntityMixin {
    @Shadow private @Nullable PlayerEntity customer;

    @Inject(method = "trade", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/VillagerTradeCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/passive/MerchantEntity;Lnet/minecraft/item/ItemStack;)V"))
    private void onTrade(TradeOffer offer, CallbackInfo ci) {
        if(offer.getSellItem().getItem().equals(Items.ENDER_PEARL)) {
            Advancement advancement = Networking.server.getAdvancementLoader().get(LootAdvancement.SHAME.id());
            if(advancement == null) return;
            if(customer instanceof ServerPlayerEntity serverPlayer) {
                AdvancementProgress progress = serverPlayer.getAdvancementTracker().getProgress(advancement);
                if(progress.isDone()) return;
                for(String criterion : progress.getUnobtainedCriteria()) {
                    serverPlayer.getAdvancementTracker().grantCriterion(advancement, criterion);
                }
            }
        }
    }
}
