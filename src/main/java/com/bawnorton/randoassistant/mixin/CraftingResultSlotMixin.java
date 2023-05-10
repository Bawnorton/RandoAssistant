package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin {
    @Shadow @Final private CraftingInventory input;
    @Shadow @Final private PlayerEntity player;

    @Inject(method = "onCrafted(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    public void onCraft(ItemStack stack, CallbackInfo ci) {
        Set<Item> items = new HashSet<>();
        for(int i = 0; i < input.size(); i++) {
            ItemStack itemStack = input.getStack(i);
            if(itemStack != ItemStack.EMPTY) {
                items.add(itemStack.getItem());
            }
        }
        items.forEach(item -> player.incrementStat(RandoAssistantStats.INTERACTED.getOrCreateStat(Registries.ITEM.getId(item))));
    }
}
