package com.bawnorton.randoassistant.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.Map;

public interface WallBlockLookup {
    Map<Block, Block> lookup = Map.<Block, Block>ofEntries(
            Map.entry(Blocks.WALL_TORCH, Blocks.TORCH),
            Map.entry(Blocks.OAK_WALL_SIGN, Blocks.OAK_SIGN),
            Map.entry(Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_SIGN),
            Map.entry(Blocks.BIRCH_WALL_SIGN, Blocks.BIRCH_SIGN),
            Map.entry(Blocks.JUNGLE_WALL_SIGN, Blocks.JUNGLE_SIGN),
            Map.entry(Blocks.ACACIA_WALL_SIGN, Blocks.ACACIA_SIGN),
            Map.entry(Blocks.DARK_OAK_WALL_SIGN, Blocks.DARK_OAK_SIGN),
            Map.entry(Blocks.CRIMSON_WALL_SIGN, Blocks.CRIMSON_SIGN),
            Map.entry(Blocks.WARPED_WALL_SIGN, Blocks.WARPED_SIGN),
            Map.entry(Blocks.BAMBOO_WALL_SIGN, Blocks.BAMBOO_SIGN),
            Map.entry(Blocks.OAK_WALL_HANGING_SIGN, Blocks.OAK_HANGING_SIGN),
            Map.entry(Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN),
            Map.entry(Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN),
            Map.entry(Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN),
            Map.entry(Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN),
            Map.entry(Blocks.DARK_OAK_WALL_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN),
            Map.entry(Blocks.CRIMSON_WALL_HANGING_SIGN, Blocks.CRIMSON_HANGING_SIGN),
            Map.entry(Blocks.WARPED_WALL_HANGING_SIGN, Blocks.WARPED_HANGING_SIGN),
            Map.entry(Blocks.BAMBOO_WALL_HANGING_SIGN, Blocks.BAMBOO_HANGING_SIGN),
            Map.entry(Blocks.REDSTONE_WALL_TORCH, Blocks.REDSTONE_TORCH),
            Map.entry(Blocks.SOUL_WALL_TORCH, Blocks.SOUL_TORCH),
            Map.entry(Blocks.SKELETON_WALL_SKULL, Blocks.SKELETON_SKULL),
            Map.entry(Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL),
            Map.entry(Blocks.ZOMBIE_WALL_HEAD, Blocks.ZOMBIE_HEAD),
            Map.entry(Blocks.PLAYER_WALL_HEAD, Blocks.PLAYER_HEAD),
            Map.entry(Blocks.CREEPER_WALL_HEAD, Blocks.CREEPER_HEAD),
            Map.entry(Blocks.DRAGON_WALL_HEAD, Blocks.DRAGON_HEAD),
            Map.entry(Blocks.PIGLIN_WALL_HEAD, Blocks.PIGLIN_HEAD),
            Map.entry(Blocks.WHITE_WALL_BANNER, Blocks.WHITE_BANNER),
            Map.entry(Blocks.ORANGE_WALL_BANNER, Blocks.ORANGE_BANNER),
            Map.entry(Blocks.MAGENTA_WALL_BANNER, Blocks.MAGENTA_BANNER),
            Map.entry(Blocks.LIGHT_BLUE_WALL_BANNER, Blocks.LIGHT_BLUE_BANNER),
            Map.entry(Blocks.YELLOW_WALL_BANNER, Blocks.YELLOW_BANNER),
            Map.entry(Blocks.LIME_WALL_BANNER, Blocks.LIME_BANNER),
            Map.entry(Blocks.PINK_WALL_BANNER, Blocks.PINK_BANNER),
            Map.entry(Blocks.GRAY_WALL_BANNER, Blocks.GRAY_BANNER),
            Map.entry(Blocks.LIGHT_GRAY_WALL_BANNER, Blocks.LIGHT_GRAY_BANNER),
            Map.entry(Blocks.CYAN_WALL_BANNER, Blocks.CYAN_BANNER),
            Map.entry(Blocks.PURPLE_WALL_BANNER, Blocks.PURPLE_BANNER),
            Map.entry(Blocks.BLUE_WALL_BANNER, Blocks.BLUE_BANNER),
            Map.entry(Blocks.BROWN_WALL_BANNER, Blocks.BROWN_BANNER),
            Map.entry(Blocks.GREEN_WALL_BANNER, Blocks.GREEN_BANNER),
            Map.entry(Blocks.RED_WALL_BANNER, Blocks.RED_BANNER),
            Map.entry(Blocks.BLACK_WALL_BANNER, Blocks.BLACK_BANNER),
            Map.entry(Blocks.DEAD_TUBE_CORAL_WALL_FAN, Blocks.DEAD_TUBE_CORAL_FAN),
            Map.entry(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, Blocks.DEAD_BRAIN_CORAL_FAN),
            Map.entry(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, Blocks.DEAD_BUBBLE_CORAL_FAN),
            Map.entry(Blocks.DEAD_FIRE_CORAL_WALL_FAN, Blocks.DEAD_FIRE_CORAL_FAN),
            Map.entry(Blocks.DEAD_HORN_CORAL_WALL_FAN, Blocks.DEAD_HORN_CORAL_FAN),
            Map.entry(Blocks.TUBE_CORAL_WALL_FAN, Blocks.TUBE_CORAL_FAN),
            Map.entry(Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_FAN),
            Map.entry(Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_FAN),
            Map.entry(Blocks.FIRE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_FAN),
            Map.entry(Blocks.HORN_CORAL_WALL_FAN, Blocks.HORN_CORAL_FAN)
    );

    static Block getBlock(Block block) {
        return lookup.getOrDefault(block, block);
    }
}
