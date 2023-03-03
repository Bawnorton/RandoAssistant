package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.RandoAssistant;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> onUse(LootTable instance, LootContext context, Operation<ObjectArrayList<ItemStack>> original) {
        ObjectArrayList<ItemStack> result = original.call(instance, context);
        RandoAssistant.interactionMap.addInteraction(Items.FISHING_ROD, result);
        return result;
    }
}
