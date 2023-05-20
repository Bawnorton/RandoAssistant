package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.util.LootAdvancement;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/network/ServerPlayerEntity$2")
public abstract class ServerPlayerEntityMixin {

    @Shadow @Final
    ServerPlayerEntity field_29183;

    @Inject(method = "onSlotUpdate(Lnet/minecraft/screen/ScreenHandler;ILnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/InventoryChangedCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/item/ItemStack;)V"))
    private void onTrigger(ScreenHandler handler, int slotId, ItemStack stack, CallbackInfo ci) {
        if(stack.getItem().equals(RandoAssistant.WOB)) {
            LootAdvancement.WOB.grant(field_29183);
        }
    }
}
