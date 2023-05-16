package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.RandoAssistant;
import com.bawnorton.randoassistant.stat.RandoAssistantStats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeUnlocker;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeUnlocker.class)
public interface RecipeUnlockerMixin {
    @Shadow @Nullable Recipe<?> getLastRecipe();

    @Inject(method = "unlockLastRecipe", at = @At("HEAD"), cancellable = true)
    default void onUnlockLastRecipe(PlayerEntity player, CallbackInfo ci) {
        if(getLastRecipe() != null) {
            if(getLastRecipe().getId().getNamespace().equals(RandoAssistant.MOD_ID)) {
                ci.cancel();
            }
            player.incrementStat(RandoAssistantStats.CRAFTED.getOrCreateStat(getLastRecipe().getId()));
        }
    }
}
