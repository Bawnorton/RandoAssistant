package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.networking.SerializeableLootTable;
import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
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
    private Identifier id;

    @Inject(method = "checkLootInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;supplyInventory(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/loot/context/LootContext;)V", shift = At.Shift.AFTER))
    private void onCheckLootInteraction(PlayerEntity player, CallbackInfo ci) {
        if(player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.incrementStat(RandoAssistantStats.LOOTED.getOrCreateStat(id));
        }
    }

    @Inject(method = "checkLootInteraction", at = @At("HEAD"))
    private void onCheckLootInteractionHead(PlayerEntity player, CallbackInfo ci) {
        if (lootTableId != null) {
            id = new Identifier(lootTableId.toString());
        }
    }
}
