package com.bawnorton.randoassistant.tracking.trackable;

import net.minecraft.block.Block;
import net.minecraft.stat.Stat;

public class MinedTrackable extends Trackable<Block> {
    public MinedTrackable(Stat<Block> minedStat) {
        super(minedStat);
    }
}
