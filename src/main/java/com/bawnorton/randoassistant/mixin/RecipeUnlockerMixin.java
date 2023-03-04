package com.bawnorton.randoassistant.mixin;

import com.bawnorton.randoassistant.RandoAssistant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeUnlocker;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipeUnlocker.class)
public interface RecipeUnlockerMixin {
    @Shadow
    @Nullable Recipe<?> getLastRecipe();

    @Inject(method = "unlockLastRecipe", at = @At("HEAD"))
    default void unlockLastRecipe(PlayerEntity player, CallbackInfo ci) {
        Recipe<?> recipe = getLastRecipe();
        if (recipe != null) {
            Item output = recipe.getOutput().getItem();
            List<Ingredient> ingredients = recipe.getIngredients();
            List<Item> input = new ArrayList<>();
            ingredients.forEach(ingredient -> {
                for (ItemStack stack : ingredient.getMatchingStacks()) {
                    input.add(stack.getItem());
                }
            });
            RandoAssistant.interactionMap.addCraftingInteraction(input, output);
        }
    }
}
