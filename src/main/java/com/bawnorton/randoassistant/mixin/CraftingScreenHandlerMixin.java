package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.util.LootAdvancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin {
    @Shadow @Final private PlayerEntity player;

    private final Item[] wobRecipe = new Item[] {
        Items.STICK, Items.STRING, Items.AIR,
        Items.STICK, Items.AIR, Items.STRING,
        Items.STICK, Items.STRING, Items.AIR
    };

    @Inject(method = "onContentChanged", at = @At("HEAD"))
    public void onContentChanged(Inventory inventory, CallbackInfo ci) {
        int size = inventory.size();
        boolean isWobRecipe = true;
        if (size == 9) {
            for (int i = 0; i < size; i++) {
                ItemStack stack = inventory.getStack(i);
                if(!stack.isOf(wobRecipe[i])) {
                    isWobRecipe = false;
                    break;
                }
            }
        }
        if (isWobRecipe && player instanceof ServerPlayerEntity serverPlayer) {
            LootAdvancement.WOB.grant(serverPlayer);
        }
        checkForDeb(inventory);
    }

    private void checkForDeb(Inventory inventory) {
        int airType = -1;
        for (int i = 0; i < 3; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isOf(Items.AIR)) break;
            if (i == 2) airType = 0;
        }
        for (int i = 6; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isOf(Items.AIR)) break;
            if (i == 8) airType = 1;
        }
        if (airType == -1) return;
        Item plankType;
        Item woolType;
        if(airType == 0) {
            plankType = inventory.getStack(3).getItem();
            if(notPlank(plankType)) return;
            for(int i = 4; i < 6; i++) {
                ItemStack stack = inventory.getStack(i);
                if (!stack.isOf(plankType)) return;
            }
            woolType = inventory.getStack(6).getItem();
            if(notWool(woolType)) return;
            for(int i = 7; i < 9; i++) {
                ItemStack stack = inventory.getStack(i);
                if (!stack.isOf(woolType)) return;
            }
        } else {
            woolType = inventory.getStack(3).getItem();
            if(notWool(woolType)) return;
            for(int i = 4; i < 6; i++) {
                ItemStack stack = inventory.getStack(i);
                if (!stack.isOf(woolType)) return;
            }
            plankType = inventory.getStack(0).getItem();
            if(notPlank(plankType)) return;
            for(int i = 1; i < 2; i++) {
                ItemStack stack = inventory.getStack(i);
                if (!stack.isOf(plankType)) return;
            }
        }
        if (notPlank(plankType) || notWool(woolType)) return;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            LootAdvancement.DEB.grant(serverPlayer);
        }
    }

    private boolean notPlank(Item item) {
        return !item.getRegistryEntry().isIn(ItemTags.PLANKS);
    }

    private boolean notWool(Item item) {
        return !item.getRegistryEntry().isIn(ItemTags.WOOL);
    }
}
