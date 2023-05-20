package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.util.LootAdvancement;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
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
            Advancement advancement = Networking.server.getAdvancementLoader().get(LootAdvancement.WOB.id());
            if(advancement == null) return;
            AdvancementProgress progress = field_29183.getAdvancementTracker().getProgress(advancement);
            if(progress.isDone()) return;
            for(String criterion : progress.getUnobtainedCriteria()) {
                field_29183.getAdvancementTracker().grantCriterion(advancement, criterion);
            }
        }
    }
}
