package com.bawnorton.randoassistant.util;

import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;

public enum RecipeType {
    CRAFTING,
    SMELTING;

    public static RecipeType fromRecipe(RecipeEntry<?> recipe) {
        if(recipe.value() instanceof AbstractCookingRecipe) {
            return SMELTING;
        }
        return CRAFTING;
    }

    public static RecipeType fromName(String name) {
        if(name.startsWith("recipe/crafting/")) {
            return CRAFTING;
        } else if(name.startsWith("recipe/smelting/")) {
            return SMELTING;
        }
        return null;
    }

    public String getName() {
        return switch(this) {
            case CRAFTING -> "recipe/crafting/";
            case SMELTING -> "recipe/smelting/";
        };
    }
}
