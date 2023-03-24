package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.RandoAssistantClient;
import com.bawnorton.randoassistant.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", shift = At.Shift.AFTER))
    private void renderStar(MatrixStack matrices, Slot slot, CallbackInfo ci) {
        if(!Config.getInstance().unbrokenBlockIcon) return;

        ItemStack stack = slot.getStack();
        Block block = Block.getBlockFromItem(stack.getItem());
        if(block == Blocks.AIR) return;

        StatHandler stats = MinecraftClient.getInstance().player.getStatHandler();
        if(stats.getStat(Stats.MINED.getOrCreateStat(block)) != 0) return;

        RandoAssistantClient.renderStar(matrices, slot.x, slot.y);
    }
}
