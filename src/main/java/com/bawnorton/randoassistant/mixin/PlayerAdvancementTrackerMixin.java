package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {
    @Shadow private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    private void onBroadcast(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if(advancement.getId().equals(new Identifier(RandoAssistant.MOD_ID, "all_loottables"))) {
            ItemStack goldCrown = Items.GOLDEN_HELMET.getDefaultStack();
            NbtCompound tag = new NbtCompound();
            NbtList lore = new NbtList();
            lore.add(NbtString.of("{\"text\": \"\"}"));
            lore.add(NbtString.of("{\"text\": \"§d§oRewarded to those who have\"}"));
            lore.add(NbtString.of("{\"text\": \"§d§odiscovered all loot tables\"}"));
            tag.put("Lore", lore);
            goldCrown.getOrCreateNbt().put("display", tag);
            goldCrown.getOrCreateNbt().putInt("Unbreakable", 1);
            goldCrown.setCustomName(Text.of("§5§lThe Loot King's Crown"));
            goldCrown.addEnchantment(Enchantments.PROTECTION, 10);
            goldCrown.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
            goldCrown.addEnchantment(Enchantments.RESPIRATION, 10);
            goldCrown.addEnchantment(Enchantments.THORNS, 10);
            owner.giveItemStack(goldCrown);
        }
    }
}
