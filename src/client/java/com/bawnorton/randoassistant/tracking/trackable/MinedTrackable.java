package com.bawnorton.randoassistant.tracking.trackable;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;

public class MinedTrackable extends Trackable<Block> {
    public MinedTrackable(Stat<Block> minedStat) {
        super(minedStat);
    }

    @Override
    public Identifier getIdentifier() {
        return Registries.BLOCK.getId(getContent());
    }
}
