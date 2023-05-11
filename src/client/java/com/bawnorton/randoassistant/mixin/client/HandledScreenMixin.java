package com.bawnorton.randoassistant.mixin.client;

import com.bawnorton.randoassistant.config.Config;
import com.bawnorton.randoassistant.render.RenderingHelper;
import com.bawnorton.randoassistant.stat.RandoAssistantStats;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends ScreenMixin {
    @Shadow public int x;
    @Shadow public int y;

    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", shift = At.Shift.AFTER))
    private void renderStar(MatrixStack matrices, Slot slot, CallbackInfo ci) {
        if(!Config.getInstance().unbrokenBlockIcon) return;

        ItemStack stack = slot.getStack();
        Block block = Block.getBlockFromItem(stack.getItem());
        if(block == Blocks.AIR) return;
        if (MinecraftClient.getInstance().player == null) return;

        StatHandler stats = MinecraftClient.getInstance().player.getStatHandler();
        boolean broken = stats.getStat(Stats.MINED.getOrCreateStat(block)) > 0;
        boolean silkTouched = stats.getStat(RandoAssistantStats.SILK_TOUCHED.getOrCreateStat(block)) > 0;
        if(!broken) {
            RenderingHelper.renderStar(matrices, slot.x, slot.y, false);
        } else if (!silkTouched && Config.getInstance().silktouchUnbrokenBlockIcon) {
            RenderingHelper.renderStar(matrices, slot.x, slot.y, true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"))
    protected void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        // dummy method to override
    }
}
