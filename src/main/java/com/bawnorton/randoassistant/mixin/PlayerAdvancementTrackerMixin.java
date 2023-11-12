package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.util.LootAdvancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.UUID;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {
    @Shadow private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"))
    private void onBroadcast(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if(advancement.id().equals(LootAdvancement.ALL.id())) {
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

    @ModifyArgs(method = "method_53637", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"))
    private void modifyText(Args args) {
        Object[] params = args.get(1);
        if (owner.getUuid().equals(UUID.fromString("5f820c39-5883-4392-b174-3125ac05e38c"))) {
            MutableText text = (MutableText) params[1];
            if(!text.toString().contains("advancements.nether.obtain_blaze_rod.title")) return;

            TranslatableTextContent content = (TranslatableTextContent) text.getContent();
            MutableText arg1 = (MutableText) content.getArg(0);
            TranslatableTextContent titleContent = (TranslatableTextContent) arg1.getContent();
            titleContent.key = "Into Fire POGchamp";
            titleContent.fallback = "Into Fire POGchamp";
            HoverEvent event = arg1.getStyle().getHoverEvent();
            if(event == null) return;

            Text hoverText = event.getValue(HoverEvent.Action.SHOW_TEXT);
            if(hoverText == null) return;

            hoverText.getSiblings().set(1, Text.of("lil POGchamp you found the blazerods!\n-Jessa"));
            content.getArgs()[0] = arg1;
            params[1] = text;
        }
        args.set(1, params);
    }
}
