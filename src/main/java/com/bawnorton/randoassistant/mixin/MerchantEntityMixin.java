package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.util.LootAdvancement;
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
            if(customer instanceof ServerPlayerEntity serverPlayer) {
                LootAdvancement.SHAME.grant(serverPlayer);
            }
        }
    }
}
