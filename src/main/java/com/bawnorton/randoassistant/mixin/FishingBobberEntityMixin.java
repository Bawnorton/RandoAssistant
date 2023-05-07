package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.networking.Networking;
import com.bawnorton.randoassistant.networking.SerializeableInteraction;
import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {

    @Shadow @Nullable public abstract PlayerEntity getPlayerOwner();

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> onUse(LootTable instance, LootContext context, Operation<ObjectArrayList<ItemStack>> original) {
        ObjectArrayList<ItemStack> result = original.call(instance, context);
        if(getPlayerOwner() instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.incrementStat(RandoAssistantStats.LOOTED.getOrCreateStat(LootTables.FISHING_GAMEPLAY));
            Networking.sendInteractionPacket(serverPlayer, SerializeableInteraction.ofItemToItemStacks(Items.FISHING_ROD, result));
        }
        return result;
    }
}
